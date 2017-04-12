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
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jcsp0003
 */
public class AgentePrisionero extends Agent {

    private ContentManager manager = (ContentManager) getContentManager();

    // El lenguaje utilizado por el agente para la comunicación es SL 
    private Codec codec = new SLCodec();

    // La ontología que utilizará el agente
    private Ontology ontology;

    @Override
    protected void setup() {

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
        
        //Plantilla la exploración del mensaje para el protocolo Subscribe
        MessageTemplate template = MessageTemplate.and(
			MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE),
			MessageTemplate.MatchPerformative(ACLMessage.CFP) );
        
        //addBehaviour(new InformarPartida(this, template));
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

}
