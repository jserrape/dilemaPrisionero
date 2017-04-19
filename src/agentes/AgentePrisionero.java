/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentes;

import dilemaPrisionero.OntologiaDilemaPrisionero;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
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
    private AID[] agentesConsola;
    private ArrayList<String> mensajesPendientes;

    private ContentManager manager = (ContentManager) getContentManager();

    // El lenguaje utilizado por el agente para la comunicación es SL 
    private Codec codec = new SLCodec();

    // La ontología que utilizará el agente
    private Ontology ontology;

    @Override
    protected void setup() {
        //Inicialización de las variables del agente
        mensajesPendientes = new ArrayList();

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

        addBehaviour(new InformarPartida(this, mensaje));
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
        }

    }
}
