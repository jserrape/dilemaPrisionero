/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//-container -host 192.168.38.100 -agent serrano:agentes.AgenteConsola
package agentes;

import dilemaPrisionero.OntologiaDilemaPrisionero;
import dilemaPrisionero.elementos.ProponerPartida;
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
import jade.proto.ProposeResponder;
import jade.proto.SubscriptionInitiator;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author jcsp0003
 */
public class AgentePrisionero extends Agent {

    //Variables del agente
    private AID consola; //Agente consola
    private ArrayList<String> mensajesParaConsola;

    private ContentManager manager = (ContentManager) getContentManager();

    // El lenguaje utilizado por el agente para la comunicación es SL 
    private Codec codec = new SLCodec();

    // La ontología que utilizará el agente
    private Ontology ontology;

    @Override
    protected void setup() {
        System.out.println("Inicia el setup");
        //Inicialización de las variables del agente
        mensajesParaConsola = new ArrayList();

        //Obtenemos la instancia de la ontología y registramos el lenguaje
        //y la ontología para poder completar el contenido de los mensajes
        try {
            ontology = OntologiaDilemaPrisionero.getInstance();
        } catch (BeanOntologyException ex) {
            Logger.getLogger(AgentePrisionero.class.getName()).log(Level.SEVERE, null, ex);
        }

        manager.registerLanguage(codec);
        manager.registerOntology(ontology);

        System.out.println("El agente " + getName() + " esperando...");

        //Registro del agente en las páginas amarillas
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType(OntologiaDilemaPrisionero.REGISTRO_PRISIONERO);
        // Agents that want to use this service need to "know" the weather-forecast-ontology
        sd.addOntologies(OntologiaDilemaPrisionero.ONTOLOGY_NAME);
        // Agents that want to use this service need to "speak" the FIPA-SL language
        sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException ex) {
            Logger.getLogger(AgentePrisionero.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Mensaje para el protocolo Subscribe
        ACLMessage mensaje = new ACLMessage(ACLMessage.SUBSCRIBE);
        mensaje.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
        mensaje.setContent("Apuntame a jugar.");

        //Se añade el destinatario del mensaje
        AID id = new AID();
        id.setLocalName(OntologiaDilemaPrisionero.REGISTRO_POLICIA);
        mensaje.addReceiver(id);

        
        addBehaviour(new TareaBuscarConsola(this, 5000));
        addBehaviour(new TareaEnvioConsola());
        
        //addBehaviour(new InformarPartida(this, mensaje));
        
        mensajesParaConsola.add("Conectado el agente "+this.getLocalName());
    }

    /**
     * Se ejecuta al finalizar el agente
     */
    @Override
    protected void takeDown() {
        //Desregistro de las Páginas Amarillas
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        //Se liberan los recuros y se despide
        System.out.println("Finaliza la ejecución de " + this.getName());
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
                Logger.getLogger(AgentePrisionero.class.getName()).log(Level.SEVERE, null, ex);
            }
            

            return null;
        }
    }

    private class InformarPartida extends SubscriptionInitiator {

        public InformarPartida(Agent agente, ACLMessage mensaje) {
            super(agente, mensaje);
        }

        @Override
        protected void handleAgree(ACLMessage inform) {
            System.out.println("Solicitud aceptada");
        }

        @Override
        protected void handleRefuse(ACLMessage inform) {
            System.out.println("Solicitud rechazada");
        }

        //Maneja la informacion enviada: INFORM
        @Override
        protected void handleInform(ACLMessage inform) {

            System.out.println("Ha llegado un mensaje INFORM");

            try {
                juegos.elementos.GanadorPartida gp = (juegos.elementos.GanadorPartida) manager.extractContent(inform);
                System.out.println("Ha ganado el jugador " + gp.getJugador().getAgenteJugador().getLocalName() + " en la partida " + gp.getPartida().getIdPartida());
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgentePrisionero.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
    
    /**
     * Tarea para buscar un agente consola
     */
    public class TareaBuscarConsola extends TickerBehaviour {

        /**
         * Constructor parametrizado de la clase
         *
         * @param a Agente que llama a la tarea
         * @param period Periodo cada el que se realiza la tarea
         */
        public TareaBuscarConsola(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            DFAgentDescription template;
            ServiceDescription sd;
            DFAgentDescription[] result;

            //Busca agente consola
            template = new DFAgentDescription();
            sd = new ServiceDescription();
            sd.setName("Consola");
            template.addServices(sd);

            try {
                result = DFService.search(myAgent, template);
                if (result.length > 0) {
                    consola = result[0].getName();
                    System.out.println("Encontrada cosola");
                } else {
                    //No se ha encontrado agente consola
                    consola = null;
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }

    /**
     * Tarea para enviar los mensajes pendientes a la consola
     */
    public class TareaEnvioConsola extends CyclicBehaviour {

        @Override
        public void action() {
            if (consola != null && !mensajesParaConsola.isEmpty()) {
                ACLMessage mensaje = new ACLMessage(ACLMessage.INFORM);
                mensaje.setSender(myAgent.getAID());
                mensaje.addReceiver(consola);
                mensaje.setContent(mensajesParaConsola.remove(0));

                myAgent.send(mensaje);
            } else {
                block();
            }
        }
    }
}
