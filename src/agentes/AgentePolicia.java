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
import dilemaPrisionero.elementos.EntregarJugada;
import dilemaPrisionero.elementos.Jugada;
import dilemaPrisionero.elementos.JugadaEntregada;
import dilemaPrisionero.elementos.ResultadoJugada;
import gui.ClasificacionJFrame;
import gui.DilemaPrisioneroJFrame;
import gui.ErrorJFrame;
import juegos.elementos.Partida;
import juegos.elementos.Jugador;
import juegos.elementos.Error;
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
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import juegos.OntologiaJuegos;
import juegos.elementos.DetalleInforme;
import juegos.elementos.GanadorPartida;
import juegos.elementos.PartidaAceptada;
import static util.Constantes.BUSCAR_AGENTES;
import static util.Constantes.CONDENA_INICIAL;
import static util.Constantes.JUGADOR_ABANDONO;
import static util.Constantes.MINIMO_LADRONES;
import static util.Constantes.NOMBRE_AGENTE_IMPAR;
import static util.Constantes.NOMBRE_JUGADOR_IMPAR;
import static util.Constantes.PRIMERA_RONDA;
import static util.Constantes.PRIMERO;
import static util.Constantes.PRIMO;
import static util.Constantes.RETARDO_PRESENTACION;
import static util.Constantes.SEGUNDO;
import static util.Constantes.TIME_OUT;
import util.ElmPresentacion;
import util.GestorSuscripciones;
import util.RegistroPartida;
import util.ResultadoJugador;

/**
 * Agente que representa el rol del policía que realiza las preguntas
 * a los dos ladrones en el Dilema del Prisionero
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
    private ErrorJFrame errorGUI;
    private ClasificacionJFrame clasificacionGUI;
    private HashSet<AID> agentesLadron;
    private Map<String, RegistroPartida> infoPartidas;
    private int partidasIniciadas = 0;
    private String idPartida;
    private TareaInformarPartida eventosPolicia;
    private GestorSuscripciones gestor;

    @Override
    protected void setup() {
        
        //Incialización de variables
        myGUI = new DilemaPrisioneroJFrame(this);
        myGUI.setVisible(true);
        errorGUI = new ErrorJFrame(this);
        errorGUI.setVisible(true);
        clasificacionGUI = new ClasificacionJFrame(this);
        mensajesPendientes = new ArrayList();
        presentacionPartidas = new ArrayList();
        infoPartidas = new HashMap();
        agentesLadron = new HashSet();
        gestor = new GestorSuscripciones();
        
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
        // Plantilla del mensaje de suscripción
        MessageTemplate plantilla = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
        eventosPolicia = new TareaInformarPartida(this, plantilla, gestor);
        addBehaviour(eventosPolicia);
        
        mensajesPendientes.add("Se ha completado la inicialización del Policía");
        errorGUI.presentarError("ERRORES detectados por el agente: " +
                this.getLocalName() + ", durante su ejecución");
    }

    @Override
    protected void takeDown() {
        //Se liberan los recuros y se despide
        myGUI.dispose();
        errorGUI.dispose();
        clasificacionGUI.dispose();
        for (RegistroPartida registroPartida : infoPartidas.values()) {
            registroPartida.getPartidaGUI().dispose();
        }
        System.out.println("Finaliza la ejecución de " + this.getName());
    }
    
    /**
     * Agentes que no han contestado a la propuesta de partida
     */
    private void noHanContestado() {
        if (!agentesLadron.isEmpty()) {
            for (AID agente : agentesLadron) {
                errorGUI.presentarError("El agente: " + agente.getLocalName() +
                        " no ha respondido\n");
            }  
        }
    }
    
    /**
     * Resolvemos el problema que se presenta cuando la lista de jugadores
     * para una partida no es par
     * @param partida datos completos de la partida
     */
    private void resolverImpar( RegistroPartida partida ) {
        List<ResultadoJugador> jugadoresPartida;
        
        // Comprobamos que los jugadores de la partida sean pares
        // o conseguimos que sean pares
        jugadoresPartida = partida.getClasificacion();
        if ( jugadoresPartida.size() % 2 != 0) {
            // jugadores impares, creamos un jugador adicional
            try {
            this.getContainerController().createNewAgent(NOMBRE_AGENTE_IMPAR,
                    "agentes.AgenteLadronImpar", null).start();
            } catch (StaleProxyException ex) {
                Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            AID agenteImpar = new AID(NOMBRE_AGENTE_IMPAR, AID.ISLOCALNAME);
            Jugador jugadorImpar = new Jugador(NOMBRE_JUGADOR_IMPAR, agenteImpar);
            jugadoresPartida.add(new ResultadoJugador(jugadorImpar,
                                        CONDENA_INICIAL));
            errorGUI.presentarError("Creado jugador Impar " + jugadorImpar);
        }
            
        partida.setCancelada(jugadoresPartida.size() < MINIMO_LADRONES);
    }
    
    /**
     * Presentamos la representación de la partida en su GUI asociado
     * @param idPartida identificador de la partida
     * @param finPartida true para mostrar el mensaje de finalización
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
     * Presentamos la clasificación de los agentes que han jugado partidas
     * con el policía
     * @param idPartida 
     */
    public void presentarClasificacion( String idPartida ) {
        clasificacionGUI.presentarClasificacion(
                        infoPartidas.get(idPartida).getClasificacion());
    }
    
    /**
     * Se añade una nueva partida con los parámetros regogicos del GUI
     * del agente policía
     * @param configuracion datos del juego del Dilema del Prisionero
     */
    public void nuevaPartida( DilemaPrisionero configuracion) {
        this.addBehaviour(new TareaNuevaPartida(configuracion));
    }
    
    public boolean finPartida( String idPartida ) {
        return infoPartidas.get(idPartida).finPartida();
    }
    
    public void cancelaPartida( String idPartida ) {
        infoPartidas.get(idPartida).setCancelada(true);
        presentacionPartidas.clear();
    }
    
    /**
     * Añadirmos una tarea JugarPartida para recoger el resultado de una pareja
     * de jugadores en la partida
     * @param partida la partida que se está jugando
     * @param jugadores pareja de jugadores para esta ronda de la partida
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
        
//        System.out.println(msg);
        
        mensajesPendientes.add("Jugada pedida \njugador1: " + ((Jugador) jugadores.get(PRIMERO)).getNombre()
                    + "\njugador2: " + ((Jugador) jugadores.get(SEGUNDO)).getNombre() );
        
        addBehaviour(new TareaJugarPartida(this, msg));
    }
    
    /**
     * Actualiza la condena de un jugador según el resultado del juego
     * @param jugadores lista de los jugadores de la partida
     * @param jugador jugador al que se le actualiza su condena
     * @param condena el número de años que se añadirán a su condena
     */
    private void actualizaCondena( List<ResultadoJugador> jugadores, Jugador jugador, int condena) {
        for (ResultadoJugador resultadoJugador : jugadores) {
            if (resultadoJugador.getJugador().getNombre().compareTo(jugador.getNombre()) == 0) {
                resultadoJugador.setTiempoCondena(resultadoJugador.getTiempoCondena() + condena);
                resultadoJugador.setActivo(true);
                // Ya se ha actualizado la condena del jugador
                break;
            }
        } 
    }
    
    /**
     * Los jugadores que no han respondido en este turno se considera el resultado
     * más desfavorable, máxima condena
     * @param idPartida identificador de la partida
     */
    private void contabilizaAbandono ( String idPartida ) {
        int abandonos = 0;
        
        List<ResultadoJugador> jugadores = infoPartidas.get(idPartida).getClasificacion();
        for (ResultadoJugador jugador : jugadores) {
            if ( !jugador.isActivo() ) {
                errorGUI.presentarError("El jugador: " + jugador.getJugador().getNombre() 
                        + JUGADOR_ABANDONO + idPartida);
                jugador.setTiempoCondena(jugador.getTiempoCondena() + PRIMO);
                abandonos++;
            } else {
                // Inicializamos el estado para la siguiente ronda de todos los
                // jugadores
                jugador.setActivo(false);
            }
        }
        
        // Si hay abandonos cancelamos la partida
        if (abandonos > 0) {
            errorGUI.presentarError("Han abandonado: " + abandonos + " jugadores");
            infoPartidas.get(idPartida).setCancelada(true);
        }
    } 
    
    /**
     * Se calcula la pena que obtendrá cada jugador en una ronda del turno de juego
     * según las jugadas entregadas por los dos ladrones
     * @param idPartida identificador de la partida
     * @param jugadas jugadas entregadas por los dos ladrones
     * @param respuestas mensaje en el que se comunica el ResultadoJugada para cada ladron
     * @return true si ya no hay más jugadas en el turno, false en otro caso
     */
    private boolean calcularResultado( String idPartida, List<JugadaEntregada> jugadas, Vector respuestas) {
        ACLMessage msg;
        ResultadoJugada resultado;
        Condenas condenas;
        
        RegistroPartida partida = infoPartidas.get(idPartida);
        condenas = partida.getJuego().getTiempoCondena();
        List<ResultadoJugador> listaJugadores = partida.getClasificacion();
        partida.aumentarResultados(); // tenemos un resultado nuevo que calcular
        if ( jugadas.size() == 1 ) { // un jugador no ha dado su movimiento
            Jugador jugador = jugadas.get(PRIMERO).getJugador();
            actualizaCondena(listaJugadores, jugador, condenas.getTentacion());
            resultado = new ResultadoJugada(jugadas.get(PRIMERO).getPartida(), 
                                            condenas.getTentacion());
            msg = (ACLMessage) respuestas.get(PRIMERO);
            
            try {
                manager.fillContent(msg, resultado);
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
            }
  
            respuestas.set(PRIMERO, msg);
        } else if ( jugadas.size() == 2 ) { // los dos jugadores han jugado
            Jugador jugador1 = jugadas.get(PRIMERO).getJugador();
            Jugada jugada1 = jugadas.get(PRIMERO).getRespuesta();
            Jugador jugador2 = jugadas.get(SEGUNDO).getJugador();
            Jugada jugada2 = jugadas.get(SEGUNDO).getRespuesta();
            if ( (jugada1.getRespuesta().compareTo(OntologiaDilemaPrisionero.HABLAR) == 0) &&
                    (jugada2.getRespuesta().compareTo(OntologiaDilemaPrisionero.HABLAR) == 0) ) {
                // Ninguno de los jugadores colabora
                actualizaCondena( listaJugadores, jugador1, condenas.getCastigo());
                resultado = new ResultadoJugada(jugadas.get(PRIMERO).getPartida(), 
                                                condenas.getCastigo());
                msg = (ACLMessage) respuestas.get(PRIMERO);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(PRIMERO, msg);
                
                actualizaCondena( listaJugadores, jugador2, condenas.getCastigo());
                resultado = new ResultadoJugada(jugadas.get(SEGUNDO).getPartida(), 
                                                    condenas.getCastigo());
                msg = (ACLMessage) respuestas.get(SEGUNDO);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(SEGUNDO, msg);
            } else if ( (jugada1.getRespuesta().compareTo(OntologiaDilemaPrisionero.HABLAR) == 0) &&
                    (jugada2.getRespuesta().compareTo(OntologiaDilemaPrisionero.CALLAR) == 0) ) {
                // El jugador 1 traiciona al jugador 2
                actualizaCondena( listaJugadores, jugador1, condenas.getTentacion());
                resultado = new ResultadoJugada(jugadas.get(PRIMERO).getPartida(), 
                                            condenas.getTentacion());
                msg = (ACLMessage) respuestas.get(PRIMERO);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(PRIMERO, msg);
                
                actualizaCondena( listaJugadores, jugador2, condenas.getPrimo());
                resultado = new ResultadoJugada(jugadas.get(SEGUNDO).getPartida(), 
                                                        condenas.getPrimo());
                msg = (ACLMessage) respuestas.get(SEGUNDO);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(SEGUNDO, msg);
            } else if ( (jugada1.getRespuesta().compareTo(OntologiaDilemaPrisionero.CALLAR) == 0) &&
                    (jugada2.getRespuesta().compareTo(OntologiaDilemaPrisionero.HABLAR) == 0) ) {
                // Jugador 2 traiciona al jugador 1
                actualizaCondena( listaJugadores, jugador1, condenas.getPrimo());
                resultado = new ResultadoJugada(jugadas.get(PRIMERO).getPartida(), 
                                                        condenas.getPrimo());
                msg = (ACLMessage) respuestas.get(PRIMERO);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(PRIMERO, msg);
                
                actualizaCondena( listaJugadores, jugador2, condenas.getTentacion());
                resultado = new ResultadoJugada(jugadas.get(SEGUNDO).getPartida(), 
                                                    condenas.getTentacion());
                msg = (ACLMessage) respuestas.get(SEGUNDO);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(SEGUNDO, msg);
            } else {
                // Ambos jugadores colaboran
                actualizaCondena( listaJugadores, jugador1, condenas.getRecompensa());
                resultado = new ResultadoJugada(jugadas.get(PRIMERO).getPartida(), 
                                            condenas.getRecompensa());
                msg = (ACLMessage) respuestas.get(PRIMERO);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(PRIMERO, msg);
                
                actualizaCondena( listaJugadores, jugador2, condenas.getRecompensa());
                resultado = new ResultadoJugada(jugadas.get(SEGUNDO).getPartida(), 
                                                condenas.getRecompensa());
                msg = (ACLMessage) respuestas.get(SEGUNDO);
                
                try {
                    manager.fillContent(msg, resultado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                respuestas.set(SEGUNDO, msg);
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
            
            if (!agentesLadron.isEmpty()) {
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
            
                // System.out.println(msg);
             
                // Creamos el elemento de información para llevar la partida
                infoPartidas.put(idPartida, new RegistroPartida(configuracion, idPartida, 
                                                        (AgentePolicia) myAgent));
            
                mensajesPendientes.add("Nueva Partida: " + nuevoJuego);  
                
                 // Creamos la tarea de ProponerPartida
                addBehaviour(new TareaProponerPartida(myAgent, msg, partida));
            }
        }
    }

    /**
     * Tarea para buscas jugadores que quieren jugar una partida
     */
    class TareaProponerPartida extends ProposeInitiator {
        private Partida partida;
        
        public TareaProponerPartida(Agent agente, ACLMessage msg, Partida partida) {
            super(agente, msg);
            this.partida = partida;
        }

        @Override
        protected void handleOutOfSequence(ACLMessage msg) {
             // Ha llegado un mensaje fuera de la secuencia del protocolo
            errorGUI.presentarError("El agente: " + msg.getSender().getName() +
                    "\nha enviado el siguiente mensaje: " + msg.getContent());
        }
        
        @Override
        protected void handleAllResponses(Vector responses) {
        
            String rechazos = "Agentes que han rechazado la partida: " + idPartida + "\n";
            int numRechazos = 0;
            ACLMessage msg;
            ArrayList<ResultadoJugador> jugadoresPartida = new ArrayList();
            Iterator it = responses.iterator();
            
            String idPartida = partida.getIdPartida();
            // Recorremos todas las respuestas recibidas
            while (it.hasNext()) {
                msg = (ACLMessage) it.next();
                if ( msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL ) {
                    // Obtenemos el jugador que acepta jugar la partida
                    try {
                        PartidaAceptada partidaAceptada = (PartidaAceptada) manager.extractContent(msg);
                        Jugador jugador = partidaAceptada.getJugador();
                        jugadoresPartida.add(new ResultadoJugador(jugador, CONDENA_INICIAL));
                    } catch (Codec.CodecException | OntologyException ex) {
                        Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                        errorGUI.presentarError("El agente: " + msg.getSender().getName() + 
                                "\nno ha enviado el contenido correcto: " + msg.getContent() +
                                "\npara la partida " + idPartida);
                    }
                } else {
                    // El resto de contestaciones se tratan como rechazo
                    numRechazos++;
                    rechazos = rechazos + "El agente: " + msg.getSender().getLocalName()
                        + " ha rechazado el juego\n";
                }
                
                // Contabilizamos los agentes que han contestado, lo que quedan
                // no han contestado
                agentesLadron.remove(msg.getSender());
            }
            
            
            noHanContestado();
            
            if (numRechazos > 0)
                mensajesPendientes.add(rechazos);
            
            // Completamos los datos del registro de la partida e iniciamos
            // el juego
            RegistroPartida registroPartida = infoPartidas.get(idPartida);
            registroPartida.setClasificacion(jugadoresPartida);
            infoPartidas.put(idPartida, registroPartida);
            myAgent.addBehaviour(new TareaInicioRonda(idPartida));
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
            }
            
            if ( !partidaActiva.finPartida() && !partidaActiva.isCancelada()) {
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
                myAgent.addBehaviour(new TareaFinJuego(idPartida, partidaActiva.isCancelada()));
            }
        }
    }
    
    /**
     * Tarea que representa una ronda entre dos jugadores dentro de un turno de juego
     * en el Dilema del Prisionero
     */
    class TareaJugarPartida extends ContractNetInitiator {
        private boolean finRonda;
        
        public TareaJugarPartida(Agent a, ACLMessage cfp) {
            super(a, cfp);
            finRonda = false;
        }

        @Override
        protected void handleOutOfSequence(ACLMessage msg) {
            // Ha llegado un mensaje fuera de la secuencia del protocolo
            errorGUI.presentarError("El agente: " + msg.getSender().getName() +
                    "\nha enviado el siguiente mensaje: " + msg.getContent());
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
                    jugadas.add(jugada);
                    resultado = resultado + "Movimiento de la partida: " + jugada.getPartida().getIdPartida()
                            + "\ndel jugador: " + jugada.getJugador().getNombre()
                            + "\ny la jugada: " + jugada.getRespuesta().getRespuesta() + "\n";
                    respuesta = msg.createReply();
                    respuesta.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    acceptances.add(respuesta);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                    errorGUI.presentarError("El agente: " + msg.getSender().getName() 
                            + "\nno ha enviado una Jugada correcta: " + msg.getContent());
                }
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
        
        public TareaInformarPartida(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        public TareaInformarPartida(Agent a, MessageTemplate mt, SubscriptionManager sm) {
            super(a, mt, sm);
        }
        
        @Override
        protected ACLMessage handleSubscription(ACLMessage subscription) throws NotUnderstoodException, RefuseException {
            
            String nombreAgente = subscription.getSender().getName();
            
            // Registra la suscripción del Jugador si no hay una previa
            suscripcionJugador = createSubscription(subscription);
            if (!gestor.haySuscripcion(nombreAgente)) {
                mySubscriptionManager.register(suscripcionJugador);            
                mensajesPendientes.add("Suscripción registrada al agente: " +
                        nombreAgente + "\nnúmero de suscripciones: " +
                        gestor.numSuscripciones());
            } else {
                // Ya tenemos una suscripción anterior del jugador y no 
                // volvemos a registrarlo.
                mensajesPendientes.add("Suscripción ya registrada al agente: " +
                        nombreAgente);
            }
            
            // Responde afirmativamente con la operación
            ACLMessage agree = subscription.createReply();
            agree.setPerformative(ACLMessage.AGREE);
            return agree;
        }
        
        @Override
        protected ACLMessage handleCancel(ACLMessage cancel) throws FailureException {
            
            // Eliminamos la suscripción del agente jugador
            String nombreAgente = cancel.getSender().getName();
            suscripcionJugador = gestor.getSuscripcion(nombreAgente);
            mySubscriptionManager.deregister(suscripcionJugador);
                        
            mensajesPendientes.add("Suscripción cancelada del agente: " + 
                    cancel.getSender().getLocalName()+ 
                    "\nsuscripciones restantes: " + gestor.numSuscripciones());
            return null; // no hay que enviar mensaje de confirmación
        }
    }
    
    /**
     * Tarea para informar de la finalización de una partida
     */
    class TareaFinJuego extends OneShotBehaviour {
        private String idPartida;
        private boolean partidaCancelada;

        public TareaFinJuego(String idPartida, boolean partidaCancelada) {
            this.idPartida = idPartida;
            this.partidaCancelada = partidaCancelada;
        }

        @Override
        public void action() {
            Iterator it;
            Subscription suscripcion;
            GanadorPartida ganador;
            Error error; 
            Jugador jugador;
            ResultadoJugador jugadorPartida;
            DetalleInforme partidaFinalizada = null;
                
            mensajesPendientes.add("FINALIZACION DE LA PARTIDA\n" + idPartida);
            
            // Comprobamos si la partida ha sido cancelada o no
            if (!partidaCancelada) {
                jugador = infoPartidas.get(idPartida).getClasificacion().get(PRIMERO).getJugador();
                ganador = new GanadorPartida(jugador);
                partidaFinalizada = new DetalleInforme(new Partida(idPartida, 
                        OntologiaDilemaPrisionero.TIPO_JUEGO), ganador);
            } else {
                error = new Error(OntologiaJuegos.CANCELACION_PARTIDA);
                partidaFinalizada = new DetalleInforme(new Partida(idPartida, 
                        OntologiaDilemaPrisionero.TIPO_JUEGO), error);
                errorGUI.presentarError("La partida: " + idPartida + 
                        "\nha sido CANCELADA" + 
                        "\nnúmero jugadores " + infoPartidas.get(idPartida).getClasificacion().size());
            }

            // Localizamos las suscripciones de los jugadores de la partida
            // que ha finalizado para enviarles el ganador
            it = infoPartidas.get(idPartida).getClasificacion().iterator();
            while( it.hasNext()) {
                jugadorPartida = (ResultadoJugador) it.next();
                suscripcion = gestor.getSuscripcion(
                            jugadorPartida.getJugador().getAgenteJugador().getName());
                                
                // Creamos el mensaje para enviar a los jugadores
                ACLMessage msgGanador = new ACLMessage(ACLMessage.INFORM);
                msgGanador.setLanguage(codec.getName());
                msgGanador.setOntology(ontology.getName());        
                try {
                    manager.fillContent(msgGanador, partidaFinalizada);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                }
                 
                if (suscripcion != null ) {
                    // Si hay un jugador impar no tine suscripción
                    suscripcion.notify(msgGanador);
                    mensajesPendientes.add("Envio INFORM al agente: \n" +
                        jugadorPartida.getJugador().getNombre() +
                        " FIN PARTIDA");
                } else if (jugadorPartida.getJugador().getNombre().compareTo(NOMBRE_JUGADOR_IMPAR) == 0) {
                    try {
                        // Finaliza el agente impar su funcion
                        myAgent.getContainerController().getAgent(NOMBRE_AGENTE_IMPAR).kill();
                    } catch (ControllerException ex) {
                        Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    
    /**
     * Tarea para dar representación visual al desarrollo del juego
     * Dilema del Prisionero
     */
    class TareaVisualizacionJuego extends TickerBehaviour {

        public TareaVisualizacionJuego(Agent a, long period) {
            super(a, period);
        }
       
        @Override
        protected void onTick() {
            if ( !presentacionPartidas.isEmpty() ) {
                ElmPresentacion presentacion = presentacionPartidas.remove(PRIMERO);
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
                    //System.out.println("Se han encontrado las siguientes consolas:");
                    agentesConsola = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        agentesConsola[i] = result[i].getName();
                        //System.out.println(agentesConsola[i].getName());
                    }
                }
                else {
                    //System.out.println("No se han encontrado consolas:");
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
            
            // Dejamos lista la estructura para los agentes ladrón que se 
            // localicen
            agentesLadron.clear();
            
            try {
                result = DFService.search(myAgent, template); 
                if (result.length >= MINIMO_LADRONES) {
                    //System.out.println("Se han encontrado las siguientes agentes ladrón:");
                    for (int i = 0; i < result.length; ++i) {
                        agentesLadron.add(result[i].getName());
//                        System.out.println(agentesLadron[i].getName());
                    }
                    myGUI.activarNuevaPartida(result.length);
                }
                else {
//                    System.out.println("No se han encontrado suficientes agentes ladrón:");
                    myGUI.anularNuevaPartida(result.length);
                    agentesLadron.clear();
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
                AID consola = agentesConsola[PRIMERO];
                Iterator it = mensajesPendientes.iterator();
                while ( it.hasNext() ) {
//                    System.out.println("Empieza el envío");
                    mensaje = new ACLMessage(ACLMessage.INFORM);
                    mensaje.setSender(myAgent.getAID());
                    mensaje.addReceiver(consola);
                    String contenido = (String) it.next();
           
                    mensaje.setContent(contenido);
            
//                    System.out.println("Enviado a: " + consola.getName());
//                    System.out.println("Contenido: " + mensaje.getContent());
            
                    myAgent.send(mensaje);
                    it.remove();
                }
            }
        }
    }
}
