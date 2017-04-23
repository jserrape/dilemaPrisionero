/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentes;

import dilemaPrisionero.OntologiaDilemaPrisionero;
import dilemaPrisionero.elementos.Condenas;
import dilemaPrisionero.elementos.DilemaPrisionero;
import dilemaPrisionero.elementos.ProponerPartida;
import juegos.elementos.Partida;
import juegos.elementos.Jugador;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ProposeInitiator;
import jade.proto.SubscriptionResponder;
import jade.proto.SubscriptionResponder.Subscription;
import jade.proto.SubscriptionResponder.SubscriptionManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import juegos.elementos.GanadorPartida;
import juegos.elementos.InformarPartida;
import juegos.elementos.PartidaAceptada;
import util.ResultadoJugador;

/**
 *
 * @author pedroj
 */
public class AgentePolicia extends Agent {
    
    private ContentManager manager = (ContentManager) getContentManager();
	
    // El lenguaje utilizado por el agente para la comunicación es SL 
    private Codec codec = new SLCodec();

    // La ontología que utilizará el agente
    private Ontology ontology;
    
    // Para enviar los mensajes a la consola
    private ArrayList<String> mensajesPendientes;
    private AID[] agentesConsola;
    
    // Variables
    private AID[] agentesLadron = null;
    private Map<String, List> clasificacion;
    private int partidasIniciadas = 0;
    private String idPartida;
    private Set<Subscription> suscripcionesJugadores;
    private Set<String> partidasActivas;
    
    // Valores por defecto
    private final long TIME_OUT = 20000; // 2seg
    private final int   MINIMO_LADRONES = 4; // mínimo número de jugadores 
    public static final int NUM_RONDAS = 10;
    public static final int PROB_FINAL = 25; // 25% una vez alcanzadas las rondas
    public static final int TENTACION = 1; 
    public static final int RECOMPENSA = 2; // por colaboración
    public static final int CASTIGO = 5; // mutua traición
    public static final int PRIMO = 10; // pena del pardillo

    @Override
    protected void setup() {
        
        //Incialización de variables
        mensajesPendientes = new ArrayList();
        clasificacion = new HashMap();
        suscripcionesJugadores = new HashSet();
        partidasActivas = new HashSet();
        
        // Regisro de la Ontología
        try {
            ontology = OntologiaDilemaPrisionero.getInstance();
        } catch (BeanOntologyException ex) {
            Logger.getLogger(AgentePrisionero.class.getName()).log(Level.SEVERE, null, ex);
        }
        manager.registerLanguage(codec);
	manager.registerOntology(ontology);
        
        //Añadir tareas principales
        addBehaviour(new TareaBuscarAgentes(this, 5000));
        addBehaviour(new TareaEnvioConsola(this,500));
        addBehaviour(new TareaNuevaPartida(this,20000));
        
        // Anadimos la tarea para las suscripciones
        // Primero creamos el gestor de las suscripciones
        SubscriptionManager gestorSuscripciones = new SubscriptionManager() {
            @Override
            public boolean register(Subscription s) throws RefuseException, NotUnderstoodException {
                suscripcionesJugadores.add(s);
                return true;
            }

            @Override
            public boolean deregister(Subscription s) throws FailureException {
                suscripcionesJugadores.remove(s);
                return true;
            }
        
        };
        // Plantilla del mensaje de suscripción
        MessageTemplate plantilla = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
        addBehaviour(new TareaInformarPartida(this, plantilla, gestorSuscripciones));
        addBehaviour(new TareaFinJuego(this,30000));
        
        mensajesPendientes.add("Se ha completado la inicialización del Policía");
        
    }

    @Override
    protected void takeDown() {
        //Se liberan los recuros y se despide
        //myGui.dispose();
        System.out.println("Finaliza la ejecución de " + this.getName());
    }
    
    private void presentarClasificacionProvisional( String idPartida, Map clasificacion) {
        String resultado;
        Iterator it;
        
        List listaJugadores = (List) clasificacion.get(idPartida);
        it = listaJugadores.iterator();
        
        resultado = "Partida: " + idPartida + "\n";
        while (it.hasNext()) {
            ResultadoJugador resultadoJugador = (ResultadoJugador) it.next();
            resultado = resultado + resultadoJugador;
        }
        
        System.out.println(resultado);
        mensajesPendientes.add(resultado);
        
    }
    
    /**
     * Tarea para buscas jugadores que quieren jugar una partida
     */
    public class TareaProponerPartida extends ProposeInitiator {
        
        public TareaProponerPartida(Agent agente, ACLMessage msg) {
            super(agente, msg);
        }

        @Override
        protected void handleAllResponses(Vector responses) {
        
            String rechazos = "Agentes que han rechazado\n";
            int numRechazos = 0;
            ACLMessage msg = null;
            PartidaAceptada partida = null;
            ArrayList<ResultadoJugador> jugadoresPartida = new ArrayList();
            Iterator it = responses.iterator();
            
            // Recorremos todas las respuestas recibidas
            while (it.hasNext()) {
                msg = (ACLMessage) it.next();
                if ( msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL ) {
                    // Obtenemos el jugador para la partida
                    Jugador jugador;
           
                    try {
                        partida = (PartidaAceptada) manager.extractContent(msg);
                    } catch (Codec.CodecException | OntologyException ex) {
                        Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                    }
            
                    jugador = partida.getJugador();
                    
                    jugadoresPartida.add(new ResultadoJugador(jugador, 0));
                    
                } else {
                    // El resto de contestaciones se tratan como rechazo
                    numRechazos++;
                    rechazos = rechazos + "El agente: " + msg.getSender().getLocalName()
                        + " ha rechazado el juego\n";
                }
            }
            
            // Guardamos la clasificación inicial de la nueva partida
            if (partida != null) {
                String idPartida = partida.getPartida().getIdPartida();
                clasificacion.put(idPartida, jugadoresPartida);
                presentarClasificacionProvisional(idPartida, clasificacion);
            }
            if (numRechazos > 0)
                mensajesPendientes.add(rechazos);
        }  
    }
    
    /**
     * Tarea para iniciar una nueva partida
     */
    public class TareaNuevaPartida extends TickerBehaviour {

        public TareaNuevaPartida(Agent agente, long period) {
            super(agente, period);
        }

        @Override
        protected void onTick() {
            
            if (agentesLadron != null) {
                //Creamos el mensaje para lanzar el protocolo Propose
                ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
                msg.setSender(myAgent.getAID());
                msg.setLanguage(codec.getName());
                msg.setOntology(ontology.getName());
                for (int i = 0; i < agentesLadron.length; i++ ) {
                    msg.addReceiver(agentesLadron[i]);
                }
                msg.setReplyByDate(new Date(System.currentTimeMillis() + TIME_OUT));
            
                // Creamos el elemento de ontología a enviar
                partidasIniciadas++;
                idPartida = myAgent.getName() + "-" + partidasIniciadas;
                Partida partida = new Partida(idPartida, OntologiaDilemaPrisionero.TIPO_JUEGO);
                Condenas condenasPartida = new Condenas(TENTACION, RECOMPENSA,
                            CASTIGO, PRIMO);
                DilemaPrisionero configuracion = new DilemaPrisionero( condenasPartida,
                        NUM_RONDAS, PROB_FINAL);
                ProponerPartida nuevoJuego = new ProponerPartida( partida, 
                        configuracion);
            
                // Añadimos el contenido del mensaje
                try {
                    Action action = new Action(myAgent.getAID(), nuevoJuego);
                    manager.fillContent(msg, action );
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
            
                System.out.println(msg);
            
                // Creamos la tarea de ProponerPartida
                addBehaviour(new TareaProponerPartida(myAgent, msg));
            
                mensajesPendientes.add("Nueva Partida: " + nuevoJuego);    
            }
        }
    }
    
    /**
     * Tarea que gestiona la suscripción para informar a los jugadores cuando
     * una partida ha terminado y el ganador de esa partida
     */
    public class TareaInformarPartida extends SubscriptionResponder {
        private Subscription suscripcionJugador;
        
        public TareaInformarPartida(Agent a, MessageTemplate mt, SubscriptionManager sm) {
            super(a, mt, sm);
        }

        @Override
        protected ACLMessage handleSubscription(ACLMessage subscription) throws NotUnderstoodException, RefuseException {
            InformarPartida partida = null;
            
            try {
                Action ac = (Action) manager.extractContent(subscription);
                partida = (InformarPartida) ac.getAction();
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            // Registra la suscripción del Jugador
            suscripcionJugador = createSubscription(subscription);
            mySubscriptionManager.register(suscripcionJugador);
            
            // Responde afirmativamente con la operación
            ACLMessage agree = subscription.createReply();
            agree.setPerformative(ACLMessage.AGREE);
            
            //provisional para las pruebas
            partidasActivas.add(partida.getPartida().getIdPartida());
            
            mensajesPendientes.add("Suscripción registrada al agente: " +
                    subscription.getSender().getLocalName() + " a la partida: " +
                    partida.getPartida().getIdPartida());
            return agree;
        }
        
        @Override
        protected ACLMessage handleCancel(ACLMessage cancel) throws FailureException {
            // Eliminamos la suscripción
            mySubscriptionManager.deregister(suscripcionJugador);
            
            // Informe de la cancelación
            ACLMessage cancelado = cancel.createReply();
            cancelado.setPerformative(ACLMessage.INFORM);
            
            mensajesPendientes.add("Suscripción cancelada del agente: " + 
                    cancel.getSender().getLocalName());
            return cancelado;
        }
    }
    
    /**
     * Tarea para probar la cancelación de suscripciones
     */
    public class TareaFinJuego extends TickerBehaviour {

        public TareaFinJuego(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            String idPartida;
            Iterator it;
            Subscription suscripcion;
            ACLMessage msg;
            InformarPartida partidaFinalizada = null;
            GanadorPartida ganador;
            
            if (!partidasActivas.isEmpty()) {
                // Conseguimos el identificador de la partida
                it = partidasActivas.iterator();
                idPartida = (String) it.next();
                it.remove();
                
                mensajesPendientes.add("FINALIZACION DE LA PARTIDA\n" + idPartida);
                // Recorremos las suscriciones buscando la que corresponde a la partida
                it = suscripcionesJugadores.iterator();
                while( it.hasNext()) {
                    suscripcion = (Subscription) it.next();
                    msg = suscripcion.getMessage();
                    
                    try {
                        // Recogemos la información de la partida
                        Action ac = (Action) manager.extractContent(msg);
                        partidaFinalizada = (InformarPartida) ac.getAction();
                    } catch (Codec.CodecException | OntologyException ex) {
                        Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                    }
                                        
                    if (partidaFinalizada.getPartida().getIdPartida().compareTo(idPartida) == 0) {
                        Jugador jugador = new Jugador("JugadorPrueba", myAgent.getAID());
                        ganador = new GanadorPartida(partidaFinalizada.getPartida(), jugador);
                        ACLMessage msgGanador = new ACLMessage(ACLMessage.INFORM);
                        //msg.setSender(myAgent.getAID());
                        msgGanador.setLanguage(codec.getName());
                        msgGanador.setOntology(ontology.getName());
                        
                        try {
                            manager.fillContent(msgGanador, ganador);
                        } catch (Codec.CodecException | OntologyException ex) {
                            Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        suscripcion.notify(msgGanador);
                        
                        mensajesPendientes.add("Envio INFORM al agente: \n" +
                                msg.getSender().getLocalName());
                    }
                }
                
            }
        }
    }
    
    /**
     * Tarea que localizará los agentes consola presentes en la plataforma y
     * los agentes ladrón para el juego.
     */
    public class TareaBuscarAgentes extends TickerBehaviour {
        //Se buscarán agentes consola y operación
        public TareaBuscarAgentes(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            DFAgentDescription template;
            ServiceDescription sd;
            DFAgentDescription[] result;
            
            //Busca agentes consola
            template = new DFAgentDescription();
            sd = new ServiceDescription();
            sd.setName(OntologiaDilemaPrisionero.REGISTRO_CONSOLA);
            template.addServices(sd);
            
            try {
                result = DFService.search(myAgent, template); 
                if (result.length > 0) {
                    System.out.println("Se han encontrado las siguientes consolas:");
                    agentesConsola = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        agentesConsola[i] = result[i].getName();
                        System.out.println(agentesConsola[i].getName());
                    }
                }
                else {
                    System.out.println("No se han encontrado consolas:");
                    agentesConsola = null;
                }
            }
            catch (FIPAException fe) {
		fe.printStackTrace();
            }
            
            //Busca agentes operación
            template = new DFAgentDescription();
            sd = new ServiceDescription();
            sd.setName(OntologiaDilemaPrisionero.REGISTRO_PRISIONERO);
            template.addServices(sd);
            
            try {
                result = DFService.search(myAgent, template); 
                if (result.length >= MINIMO_LADRONES) {
                    System.out.println("Se han encontrado las siguientes agentes ladrón:");
                    agentesLadron = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        agentesLadron[i] = result[i].getName();
                        System.out.println(agentesLadron[i].getName());
                    }
                    //myGui.activarEnviar();
                }
                else {
                    System.out.println("No se han encontrado suficientes agentes ladrón:");
                    agentesLadron = null;
                    //myGui.anularEnviar();
                } 
            }
            catch (FIPAException fe) {
		fe.printStackTrace();
            }
        }
    }
    
    public class TareaEnvioConsola extends TickerBehaviour {

        public TareaEnvioConsola(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            ACLMessage mensaje;
            if (agentesConsola != null) {
                if (!mensajesPendientes.isEmpty()) {
                    System.out.println("Empieza el envío");
                    mensaje = new ACLMessage(ACLMessage.INFORM);
                    mensaje.setSender(myAgent.getAID());
                    mensaje.addReceiver(agentesConsola[0]);
                    mensaje.setContent(mensajesPendientes.remove(0));
         
                    // 
            
                    System.out.println("Enviado a: " + agentesConsola[0].getName());
                    System.out.println("Contenido: " + mensaje.getContent());
            
                    myAgent.send(mensaje);
                }
                else {
                    mensaje = new ACLMessage(ACLMessage.INFORM);
                    mensaje.setSender(myAgent.getAID());
                    mensaje.addReceiver(agentesConsola[0]);
                    mensaje.setContent("No hay mensajes pendientes");
                    // myAgent.send(mensaje);
                    
                    //System.out.println(mensaje);
                }
            }
        }
    }
    
}
