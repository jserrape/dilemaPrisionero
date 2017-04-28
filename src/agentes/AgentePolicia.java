/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentes;

import dilemaPrisionero.OntologiaDilemaPrisionero;
import dilemaPrisionero.elementos.DilemaPrisionero;
import dilemaPrisionero.elementos.ProponerPartida;
import dilemaPrisionero.elementos.EntregarJugada;
import dilemaPrisionero.elementos.Jugada;
import dilemaPrisionero.elementos.JugadaEntregada;
import dilemaPrisionero.elementos.ResultadoJugada;
import gui.DilemaPrisioneroJFrame;
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
import jade.core.behaviours.OneShotBehaviour;
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
import jade.proto.ContractNetInitiator;
import jade.proto.ProposeInitiator;
import jade.proto.SubscriptionResponder;
import jade.proto.SubscriptionResponder.Subscription;
import jade.proto.SubscriptionResponder.SubscriptionManager;
import java.util.ArrayList;
import java.util.Collections;
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
import util.ElmPresentacion;
import util.RegistroPartida;
import util.ResultadoJugador;

/**
 *
 * @author pedroj
 */
public class AgentePolicia extends Agent {
    
    private final ContentManager manager = (ContentManager) getContentManager();
	
    // El lenguaje utilizado por el agente para la comunicación es SL 
    private final Codec codec = new SLCodec();

    // La ontología que utilizará el agente
    private Ontology ontology;
    
    // Para visualización de las acciones del agente
    private ArrayList<String> mensajesPendientes;
    private AID[] agentesConsola;
    private List<ElmPresentacion> presentacionPartidas;
    
    // Variables
    private DilemaPrisioneroJFrame myGUI;
    private AID[] agentesLadron = null;
    private Map<String, RegistroPartida> infoPartidas;
    private int partidasIniciadas = 0;
    private String idPartida;
    private Set<Subscription> suscripcionesJugadores;
    private Set<String> partidasActivas;
    
    // Valores por defecto
    private final long TIME_OUT = 20000; // 2seg
    private final long RETARDO_PRESENTACION = 2000; 
    private final long BUSCAR_AGENTES = 5000; // 0.5seg
    private final int   MINIMO_LADRONES = 4; // mínimo número de jugadores 
    public static final int NUM_RONDAS = 10;
    public static final int PROB_FINAL = 25; // 25% una vez alcanzadas las rondas
    public static final int TENTACION = 1; 
    public static final int RECOMPENSA = 2; // por colaboración
    public static final int CASTIGO = 5; // mutua traición
    public static final int PRIMO = 10; // pena del pardillo
    public static final int PRIMERA_RONDA = 0; // identifica la primera ronda de la partida
    public static final int CONDENA_INICIAL = 0;
    public static final int UNO = 0; // índice para el primer jugador
    public static final int DOS = 1; // índice para el segundo jugador

    @Override
    protected void setup() {
        
        //Incialización de variables
        myGUI = new DilemaPrisioneroJFrame(this);
        myGUI.setVisible(true);
        mensajesPendientes = new ArrayList();
        presentacionPartidas = new ArrayList();
        infoPartidas = new HashMap();
        suscripcionesJugadores = new HashSet();
        partidasActivas = new HashSet();
        
        // Regisro de la Ontología
        try {
            ontology = OntologiaDilemaPrisionero.getInstance();
        } catch (BeanOntologyException ex) {
            Logger.getLogger(AgenteLadron.class.getName()).log(Level.SEVERE, null, ex);
        }
        manager.registerLanguage(codec);
	manager.registerOntology(ontology);
        
        //Añadir tareas principales
        addBehaviour(new TareaVisualizacionJuego(this, RETARDO_PRESENTACION));
        addBehaviour(new TareaBuscarAgentes(this, BUSCAR_AGENTES));
        addBehaviour(new TareaEnvioConsola(this,RETARDO_PRESENTACION));
        
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
        
        mensajesPendientes.add("Se ha completado la inicialización del Policía");
        
    }

    @Override
    protected void takeDown() {
        //Se liberan los recuros y se despide
        myGUI.dispose();
        Iterator it = infoPartidas.values().iterator();
        while (it.hasNext()) {
            RegistroPartida registroPartida = (RegistroPartida) it.next();
            registroPartida.getPartidaGUI().dispose();
        }
        System.out.println("Finaliza la ejecución de " + this.getName());
    }
    
    /**
     * Resolvemos el problema que se presenta cuando la lista de jugadores
     * para una partida no es par
     * @param partida 
     */
    private void resolverImpar( RegistroPartida partida ) {
        List<ResultadoJugador> jugadoresPartida;
        
        // Comprobamos que los jugadores de la partida sean pares
        // o conseguimos que sean pares
        jugadoresPartida = partida.getClasificacion();
        if ( jugadoresPartida.size() % 2 != 0) {
            // jugadores impares, eliminamos al primero
            jugadoresPartida.remove(UNO);
        }
    }
    
    /**
     * Presentamos la presentacionPartida de la partida en su GUI asociado
     * @param idPartida 
     */
    private void presentacionPartida ( String idPartida, boolean finPartida ) {
        ResultadoJugador resultadoJugador;
        List<String> agentesJugador = new ArrayList();
        List<String> nombresJugador = new ArrayList();
        List<String> condenas = new ArrayList();
        
        RegistroPartida partida = infoPartidas.get(idPartida);
        Collections.sort(partida.getClasificacion());
        Iterator it = partida.getClasificacion().iterator();
        while ( it.hasNext() ) {
            resultadoJugador = (ResultadoJugador) it.next();
            agentesJugador.add(resultadoJugador.getJugador().getAgenteJugador().getLocalName());
            nombresJugador.add(resultadoJugador.getJugador().getNombre());
            condenas.add(Integer.toString(resultadoJugador.getTiempoCondena()));
        } 
        
        ElmPresentacion resultado = new ElmPresentacion(idPartida, partida.getRonda(),
                    agentesJugador,nombresJugador, condenas);
        resultado.setFinPartida(finPartida);
        presentacionPartidas.add(resultado);
    }  
    
    /**
     * Se añade una nueva partida con los parámetros regogicos del GUI
     * del agente policía
     * @param configuracion 
     */
    public void nuevaPartida( DilemaPrisionero configuracion) {
        this.addBehaviour(new TareaNuevaPartida(configuracion));
    }
    
    /**
     * Añadirmos una tarea JugarPartida para recoger el resultado de una pareja
     * de jugadores en la partida
     * @param partida
     * @param jugador1
     * @param jugador2 
     */
    private void iniciaJugarPartida ( Partida partida, List jugadores ) {
        
        //Creamos el mensaje para lanzar el protocolo Contrac_Net
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setSender(getAID());
        msg.setLanguage(codec.getName());
        msg.setOntology(ontology.getName());
        Iterator it = jugadores.iterator();
        while ( it.hasNext() ) {
            Jugador jugador = (Jugador) it.next();
            msg.addReceiver(jugador.getAgenteJugador());
        }
        msg.setReplyByDate(new Date(System.currentTimeMillis() + TIME_OUT));
        
        // Contenido del mensaje
        jade.util.leap.ArrayList listaJugadores = new jade.util.leap.ArrayList( (ArrayList) jugadores);
        EntregarJugada pedirJugada = new EntregarJugada ( partida, listaJugadores);
        Action ac = new Action (this.getAID(), pedirJugada);
        
        try {
            manager.fillContent(msg, ac);
        } catch (Codec.CodecException | OntologyException ex) {
            Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println(msg);
        
        mensajesPendientes.add("Jugada pedida \njugador1: " + ((Jugador) jugadores.get(UNO)).getNombre()
                    + "\njugador2: " + ((Jugador) jugadores.get(DOS)).getNombre() );
        
        addBehaviour(new TareaJugarPartida(this, msg));
    }
    
    private void actualizaCondena( List<ResultadoJugador> jugadores, Jugador jugador, int condena) {
        for (ResultadoJugador resultadoJugador : jugadores) {
            if (resultadoJugador.getJugador().getNombre().compareTo(jugador.getNombre()) == 0) {
                resultadoJugador.setTiempoCondena(resultadoJugador.getTiempoCondena() + condena);
                resultadoJugador.setActivo(true);
                break;
            }
        } 
    }
    
    private void contabilizaAbandono ( String idPartida ) {
        List<ResultadoJugador> jugadores = infoPartidas.get(idPartida).getClasificacion();
        for (ResultadoJugador jugador : jugadores) {
            if ( !jugador.isActivo() ) {
                jugador.setTiempoCondena(jugador.getTiempoCondena() + PRIMO);
            }
        }
    } 
    
    private boolean calcularResultado( String idPartida, List<JugadaEntregada> jugadas, Vector respuestas) {
        ACLMessage msg;
        ResultadoJugada resultado;
        
        RegistroPartida partida = infoPartidas.get(idPartida);
        List<ResultadoJugador> listaJugadores = partida.getClasificacion();
        partida.aumentarResultados(); // tenemos un resultado nuevo que calcular
        if ( jugadas.size() == 1 ) { // un jugador no ha dado su movimiento
            Jugador jugador = jugadas.get(UNO).getJugador();
            actualizaCondena(listaJugadores, jugador, TENTACION);
            resultado = new ResultadoJugada(jugadas.get(UNO).getPartida(), TENTACION);
            msg = (ACLMessage) respuestas.get(UNO);
            
            try {
                manager.fillContent(msg, resultado);
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
            }
  
            respuestas.set(UNO, msg);
        } else if ( jugadas.size() == 2 ) { // los dos jugadores han jugado
            Jugador jugador1 = jugadas.get(UNO).getJugador();
            Jugada jugada1 = jugadas.get(UNO).getRespuesta();
            Jugador jugador2 = jugadas.get(DOS).getJugador();
            Jugada jugada2 = jugadas.get(DOS).getRespuesta();
            if ( (jugada1.getRespuesta().compareTo(OntologiaDilemaPrisionero.HABLAR) == 0) &&
                    (jugada2.getRespuesta().compareTo(OntologiaDilemaPrisionero.HABLAR) == 0) ) {
                // Ninguno de los jugadores colabora
                actualizaCondena( listaJugadores, jugador1, CASTIGO);
                resultado = new ResultadoJugada(jugadas.get(UNO).getPartida(), CASTIGO);
                msg = (ACLMessage) respuestas.get(UNO);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(UNO, msg);
                
                actualizaCondena( listaJugadores, jugador2, CASTIGO);
                resultado = new ResultadoJugada(jugadas.get(DOS).getPartida(), CASTIGO);
                msg = (ACLMessage) respuestas.get(DOS);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(DOS, msg);
            } else if ( (jugada1.getRespuesta().compareTo(OntologiaDilemaPrisionero.HABLAR) == 0) &&
                    (jugada2.getRespuesta().compareTo(OntologiaDilemaPrisionero.CALLAR) == 0) ) {
                // El jugador 1 traiciona al jugador 2
                actualizaCondena( listaJugadores, jugador1, TENTACION);
                resultado = new ResultadoJugada(jugadas.get(UNO).getPartida(), TENTACION);
                msg = (ACLMessage) respuestas.get(UNO);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(UNO, msg);
                
                actualizaCondena( listaJugadores, jugador2, PRIMO);
                resultado = new ResultadoJugada(jugadas.get(DOS).getPartida(), PRIMO);
                msg = (ACLMessage) respuestas.get(DOS);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(DOS, msg);
            } else if ( (jugada1.getRespuesta().compareTo(OntologiaDilemaPrisionero.CALLAR) == 0) &&
                    (jugada2.getRespuesta().compareTo(OntologiaDilemaPrisionero.HABLAR) == 0) ) {
                // Jugador 2 traiciona al jugador 1
                actualizaCondena( listaJugadores, jugador1, PRIMO);
                resultado = new ResultadoJugada(jugadas.get(UNO).getPartida(), PRIMO);
                msg = (ACLMessage) respuestas.get(UNO);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(UNO, msg);
                
                actualizaCondena( listaJugadores, jugador2, TENTACION);
                resultado = new ResultadoJugada(jugadas.get(DOS).getPartida(), TENTACION);
                msg = (ACLMessage) respuestas.get(DOS);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(DOS, msg);
            } else {
                // Ambos jugadores colaboran
                actualizaCondena( listaJugadores, jugador1, RECOMPENSA);
                resultado = new ResultadoJugada(jugadas.get(UNO).getPartida(), RECOMPENSA);
                msg = (ACLMessage) respuestas.get(UNO);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(UNO, msg);
                
                actualizaCondena( listaJugadores, jugador2, RECOMPENSA);
                resultado = new ResultadoJugada(jugadas.get(DOS).getPartida(), RECOMPENSA);
                msg = (ACLMessage) respuestas.get(DOS);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(DOS, msg);
            }
        }
        return partida.finRonda();
    }
    
    /**
     * Tarea para iniciar una nueva TareaProponerPartida
     */
    class TareaNuevaPartida extends OneShotBehaviour {
        private final DilemaPrisionero configuracion;

        public TareaNuevaPartida(DilemaPrisionero configuracion) {
            this.configuracion = configuracion;
        }
        
        @Override
        public void action() {
            
            if (agentesLadron != null) {
                //Creamos el mensaje para lanzar el protocolo Propose
                ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
                msg.setSender(myAgent.getAID());
                msg.setLanguage(codec.getName());
                msg.setOntology(ontology.getName());
                for (AID agentes : agentesLadron) {
                    msg.addReceiver(agentes);
                }
                msg.setReplyByDate(new Date(System.currentTimeMillis() + TIME_OUT));
            
                // Creamos el elemento de ontología a enviar
                partidasIniciadas++;
                idPartida = myAgent.getName() + "-" + partidasIniciadas;
                Partida partida = new Partida(idPartida, OntologiaDilemaPrisionero.TIPO_JUEGO);
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
                
                // Creamos el elemento de información para llevar la partida
                infoPartidas.put(idPartida, new RegistroPartida(configuracion, idPartida));
            
                mensajesPendientes.add("Nueva Partida: " + nuevoJuego);    
            }
        }
    }

    /**
     * Tarea para buscas jugadores que quieren jugar una partida
     */
    class TareaProponerPartida extends ProposeInitiator {
        
        public TareaProponerPartida(Agent agente, ACLMessage msg) {
            super(agente, msg);
        }

        @Override
        protected void handleAllResponses(Vector responses) {
        
            String rechazos = "Agentes que han rechazado\n";
            int numRechazos = 0;
            ACLMessage msg;
            PartidaAceptada partida = null;
            ArrayList<ResultadoJugador> jugadoresPartida = new ArrayList();
            Iterator it = responses.iterator();
            
            // Recorremos todas las respuestas recibidas
            while (it.hasNext()) {
                msg = (ACLMessage) it.next();
                if ( msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL ) {
                    // Obtenemos el jugador para la partida
                    try {
                        partida = (PartidaAceptada) manager.extractContent(msg);
                    } catch (Codec.CodecException | OntologyException ex) {
                        Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Jugador jugador = partida.getJugador();
                    jugadoresPartida.add(new ResultadoJugador(jugador, CONDENA_INICIAL));
                } else {
                    // El resto de contestaciones se tratan como rechazo
                    numRechazos++;
                    rechazos = rechazos + "El agente: " + msg.getSender().getLocalName()
                        + " ha rechazado el juego\n";
                }
            }
            
            if (numRechazos > 0)
                mensajesPendientes.add(rechazos);
            
            // Completamos los datos del registro de la partida e iniciamos
            // el juego
            if (partida != null) {
                String idPartida = partida.getPartida().getIdPartida();
                RegistroPartida registroPartida = infoPartidas.get(idPartida);
                registroPartida.setClasificacion(jugadoresPartida);
                infoPartidas.put(idPartida, registroPartida);
                myAgent.addBehaviour(new TareaInicioRonda(idPartida));
            }
        }  
    }
    
    /**
     * Tarea que inicia las rondas de juego donde se establecen las parejas que
     * se van a enfrentar. Y añadirá tantas TareasJugarPartida como sean necesarias.
     * 
     * Si es la primera ronda de la partida, resolverá el problema de no tener un
     * número de jugadores pares.
     */
    class TareaInicioRonda extends OneShotBehaviour {
        private final String idPartida;
        private final Partida partida;

        public TareaInicioRonda(String idPartida) {
            this.idPartida = idPartida;
            partidasActivas.add(idPartida);
            partida = new Partida(idPartida, OntologiaDilemaPrisionero.TIPO_JUEGO);
        }
        
        @Override
        public void action() {
            List<ResultadoJugador> jugadoresPartida;
            RegistroPartida partidaActiva;
        
            // Recuperamos los datos de la partida para la nueva ronda
            partidaActiva = infoPartidas.get(idPartida);
            if ( partidaActiva.getRonda() == PRIMERA_RONDA ) {
                resolverImpar(partidaActiva);
                presentacionPartida(idPartida, false);
            }
            
            if ( !partidaActiva.finPartida() ) {
                // Empieza una nueva ronda
                partidaActiva.aumentarRonda();
                mensajesPendientes.add("Jugando la Ronda: " + partidaActiva.getRonda());
            
                // Preparamos las parejas y se inicia la ronda de la partida
                jugadoresPartida = partidaActiva.getClasificacion();
                Collections.shuffle(jugadoresPartida);
                Iterator it = jugadoresPartida.iterator();
                List<Jugador> jugadores = new ArrayList();
                while ( it.hasNext() ) {
                    ResultadoJugador jugador = (ResultadoJugador) it.next();
                    jugadores.add(jugador.getJugador());
                    jugador = (ResultadoJugador) it.next();
                    jugadores.add(jugador.getJugador());
                    iniciaJugarPartida(partida, jugadores);
                    jugadores.clear();
                }
            } else {
                presentacionPartida(idPartida, true);
                myAgent.addBehaviour(new TareaFinJuego(idPartida));
            }
        }
    }
    
    class TareaJugarPartida extends ContractNetInitiator {
        private boolean finRonda;
        
        public TareaJugarPartida(Agent a, ACLMessage cfp) {
            super(a, cfp);
            finRonda = false;
        }

        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            String resultado = "";
            JugadaEntregada jugada = null;
            ACLMessage respuesta;
            List<JugadaEntregada> jugadas = new ArrayList();
            
            Iterator it = responses.iterator();
            while ( it.hasNext() ) {
                ACLMessage msg = (ACLMessage) it.next();
                
                try {
                    jugada = (JugadaEntregada) manager.extractContent(msg);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuesta = msg.createReply();
                respuesta.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                acceptances.add(respuesta);
                jugadas.add(jugada);
                resultado = resultado + "Movimiento de la partida: " + jugada.getPartida().getIdPartida()
                        + "\ndel jugador: " + jugada.getJugador().getNombre()
                        + "\ny la jugada: " + jugada.getRespuesta().getRespuesta() + "\n";
            }
            
            mensajesPendientes.add(resultado);
            finRonda = calcularResultado(jugada.getPartida().getIdPartida(), jugadas, acceptances);
            if ( finRonda ) {
                contabilizaAbandono(jugada.getPartida().getIdPartida());
                // Clasificación a la finalización de la ronda
                presentacionPartida(idPartida, false);
                myAgent.addBehaviour(new TareaInicioRonda(jugada.getPartida().getIdPartida()));
            }
        }
    }
        
    /**
     * Tarea que gestiona la suscripción para informar a los jugadores cuando
     * una partida ha terminado y el ganador de esa partida
     */
    class TareaInformarPartida extends SubscriptionResponder {
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
    class TareaFinJuego extends OneShotBehaviour {
        private String idPartida;

        public TareaFinJuego( String idPartida ) {
            this.idPartida = idPartida;
        }

        @Override
        public void action() {
            Iterator it;
            Subscription suscripcion;
            ACLMessage msg;
            InformarPartida partidaFinalizada = null;
            GanadorPartida ganador;
                
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
    
    class TareaVisualizacionJuego extends TickerBehaviour {

        public TareaVisualizacionJuego(Agent a, long period) {
            super(a, period);
        }
       
        @Override
        protected void onTick() {
            if ( !presentacionPartidas.isEmpty() ) {
                ElmPresentacion presentacion = presentacionPartidas.remove(UNO);
                RegistroPartida partida = infoPartidas.get(presentacion.getIdPartida());
                partida.getPartidaGUI().presentarResultados(presentacion, partida.getJuego().getRondas());
            }
        }
    }
    
    /**
     * Tarea que localizará los agentes consola presentes en la plataforma y
     * los agentes ladrón para el juego.
     */
    class TareaBuscarAgentes extends TickerBehaviour {
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
            
            //Busca agentes larón
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
                    myGUI.activarNuevaPartida(result.length);
                }
                else {
                    System.out.println("No se han encontrado suficientes agentes ladrón:");
                    myGUI.anularNuevaPartida(result.length);
                    agentesLadron = null;
                } 
            }
            catch (FIPAException fe) {
		fe.printStackTrace();
            }
        }
    }
    
    /**
     * Tarea para enviar los mensajes a un agente consola que esté dispnible,
     * si no hay ninguno no hace nada.
     */
    class TareaEnvioConsola extends TickerBehaviour {

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
//                    mensaje = new ACLMessage(ACLMessage.INFORM);
//                    mensaje.setSender(myAgent.getAID());
//                    mensaje.addReceiver(agentesConsola[0]);
//                    mensaje.setContent("No hay mensajes pendientes");
//                    // myAgent.send(mensaje);
//                    
//                    System.out.println(mensaje);
                }
            }
        }
    }
}
