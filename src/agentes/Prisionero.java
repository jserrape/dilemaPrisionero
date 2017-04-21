/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//-container -host 192.168.38.100 -agents serrano:agentes.AgenteConsola
package agentes;

import dilemaPrisionero.OntologiaDilemaPrisionero;
import dilemaPrisionero.elementos.ProponerPartida;
import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;
import jade.proto.ProposeResponder;
import jade.proto.SubscriptionInitiator;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import juegos.elementos.InformarPartida;
import juegos.elementos.Jugador;
import juegos.elementos.Partida;
import juegos.elementos.PartidaAceptada;


/**
 *
 * @author jcsp0003
 */
public class Prisionero extends Agent {

    private final Codec codec;
    private Ontology ontologia;
    private Partida partida;
    private Jugador jugador;

    private AID[] agentesConsola;
    private ArrayList<String> mensajesPendientes;
    
    private ContentManager manager = (ContentManager) getContentManager();

    public Prisionero() {
        this.codec = new SLCodec();
    }

    @Override
    protected void setup() {
        //Inicialización de las variables del agente   
        mensajesPendientes = new ArrayList();
        try {
            ontologia = juegos.OntologiaJuegos.getInstance();
            InformarPartida ip = new juegos.elementos.InformarPartida();
            manager.registerLanguage(codec);
            manager.registerOntology(ontologia);

            MessageTemplate protocolo = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
            MessageTemplate performativa = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            MessageTemplate lenguajeContenido = MessageTemplate.MatchLanguage(codec.getName());
            MessageTemplate ontoTemplate = MessageTemplate.MatchOntology(ontologia.getName());
            MessageTemplate plantilla = MessageTemplate.and(MessageTemplate.and(protocolo, performativa), MessageTemplate.and(lenguajeContenido, ontoTemplate));

            System.out.println("Suscribiendose a la plataforma;");

            addBehaviour(new TareaBuscarConsola(this, 5000));
            addBehaviour(new TareaEnvioConsola(this, 1000));
            mensajesPendientes.add("-----------> ME HE CONECTADO A LA PLATAFORMA <------------");
            mensajesPendientes.add(this.getLocalName());

        } catch (OntologyException e) {
        }

        System.out.println("Se inicia la ejecución del agente: " + this.getName());
        //Añadir las tareas principales
    }

    @Override
    protected void takeDown() {
        //Desregristo del agente de las Páginas Amarillas

        //Liberación de recursos, incluido el GUI
        //Despedida
        System.out.println("Finaliza la ejecución del agente: " + this.getName());
    }

    //Métodos de trabajo del agente
    class ManejadorInitiator extends AchieveREInitiator {

        public ManejadorInitiator(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        protected void handleAgree(ACLMessage agree) {
            try {
                // Decodifica el mensaje ACL recibido mediante el lenguaje de contenido y la ontologia actuales
                ContentElement ce = getContentManager().extractContent(agree);
                System.out.println("Iniciando suscribe;");
                if (ce instanceof juegos.elementos.PartidaAceptada) {
                    // Recibido un AGREE con contenido correcto
                    MessageTemplate protocolo = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
                    MessageTemplate performativa = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
                    MessageTemplate lenguajeContenido = MessageTemplate.MatchLanguage(codec.getName());
                    MessageTemplate ontoTemplate = MessageTemplate.MatchOntology(ontologia.getName());
                    MessageTemplate plantilla = MessageTemplate.and(MessageTemplate.and(protocolo, performativa), MessageTemplate.and(lenguajeContenido, ontoTemplate));

                    getContentManager().registerLanguage(codec);
                    getContentManager().registerOntology(ontologia);
                    System.out.println("Suscrito a la plataforma;");
                    System.out.println("Registrados en :" + agree.getSender().getName());
                    
                    MessageTemplate plantilla2 = ContractNetResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_PROPOSE);
                    mensajesPendientes.add("illo que me han aceptado la suscripcion");
                    addBehaviour(new ProposicionPartida(this.myAgent, plantilla2));
                } else {
                    System.out.println("Recibido mensaje " + agree.getSender().getName() + " cuyo contenido no es el esperado.");
                }

            } catch (Codec.CodecException | OntologyException ce) {
                System.out.println(ce);
            }
        }
    }


    public class TareaBuscarConsola extends TickerBehaviour {

        //Se buscarán consolas 
        public TareaBuscarConsola(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            //Busca agentes consola
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setName("Consola");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result.length > 0) {
                    agentesConsola = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        agentesConsola[i] = result[i].getName();
                    }
                } else {
                    //No se han encontrado agentes consola
                    agentesConsola = null;
                }
            } catch (FIPAException fe) {
            }
        }
    }

    public class TareaEnvioConsola extends TickerBehaviour {

        //Tarea de ejemplo que se repite cada 10 segundos
        public TareaEnvioConsola(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            ACLMessage mensaje;
            if (agentesConsola != null) {
                if (!mensajesPendientes.isEmpty()) {
                    mensaje = new ACLMessage(ACLMessage.INFORM);
                    mensaje.setSender(myAgent.getAID());
                    mensaje.addReceiver(agentesConsola[0]);
                    mensaje.setContent(mensajesPendientes.remove(0));

                    myAgent.send(mensaje);
                } else {
                    //Acciones que queremos hacer si no tenemos
                    //mensajes pendientes
                }
            }
        }
    }
    
    public class ProposicionPartida extends ProposeResponder {

        public ProposicionPartida(Agent father, MessageTemplate template) {
            super(father, template);
        }

        @Override
        public ACLMessage prepareResponse(ACLMessage propuesta) {

            try {
                Action a = (Action) manager.extractContent(propuesta);
                ProponerPartida partidaPropuesta = (ProponerPartida) a.getAction();
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(Prisionero.class.getName()).log(Level.SEVERE, null, ex);
            }

            return null;
        }
    }

}