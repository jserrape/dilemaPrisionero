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
    private final int   MINIMO_LADRONES = 2; // mínimo número de jugadores 
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
            Logger.getLogger(Prisionero.class.getName()).log(Level.SEVERE, null, ex);
        }
        manager.registerLanguage(codec);
	manager.registerOntology(ontology);
        
        //Añadir tareas principales
        addBehaviour(new TareaBuscarAgentes(this, 5000));
        addBehaviour(new TareaEnvioConsola(this,500));
        
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
        //myGui.dispose();
        System.out.println("Finaliza la ejecución de " + this.getName());
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
            
            System.out.println("-------------------------------------------------Quieren subscribirse--------------------------------------");
            
            mensajesPendientes.add("Soy el poli, me ha llegado una subscripcion");
            
            // Responde afirmativamente con la operación
            ACLMessage agree = subscription.createReply();
            agree.setPerformative(ACLMessage.AGREE);

            return agree;
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
            
                    //System.out.println("Enviado a: " + agentesConsola[0].getName());
                    //System.out.println("Contenido: " + mensaje.getContent());
            
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
