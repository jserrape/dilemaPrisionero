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

    private Codec codec = new SLCodec();
    private Ontology ontologia;
    private Partida partida;
    private Jugador jugador;

    private AID[] agentesConsola;
    private ArrayList<String> mensajesPendientes;

    private ContentManager manager = (ContentManager) getContentManager();

    @Override
    protected void setup() {
        //Inicialización de las variables del agente   
        mensajesPendientes = new ArrayList();

        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        try {
            manager.registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL);
            manager.registerOntology(OntologiaDilemaPrisionero.getInstance(), OntologiaDilemaPrisionero.ONTOLOGY_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //registro ontologia
        sd.addOntologies(OntologiaDilemaPrisionero.ONTOLOGY_NAME);

        //registro paginas amarillas
        try {
            sd.setName(this.getLocalName());
            sd.setType(OntologiaDilemaPrisionero.REGISTRO_PRISIONERO);
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        //Se crea un mensaje de tipo SUBSCRIBE y se asocia al protocolo FIPA-Subscribe.
        ACLMessage mensaje = new ACLMessage(ACLMessage.SUBSCRIBE);
        mensaje.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
        mensaje.setContent("Apuntame a jugar.");

        //Se añade el destinatario del mensaje
        AID id = new AID();
        id.setLocalName(OntologiaDilemaPrisionero.REGISTRO_POLICIA);
        mensaje.addReceiver(id);

        System.out.println(OntologiaDilemaPrisionero.REGISTRO_POLICIA);

        this.addBehaviour(new InformarPartida(this, mensaje));
        addBehaviour(new TareaBuscarConsola(this, 5000));
        addBehaviour(new TareaEnvioConsola(this, 1000));
        mensajesPendientes.add("-----------> ME HE CONECTADO A LA PLATAFORMA <------------");
    }

    @Override
    protected void takeDown() {
        //Desregristo del agente de las Páginas Amarillas

        //Liberación de recursos, incluido el GUI
        //Despedida
        System.out.println("Finaliza la ejecución del agente: " + this.getName());
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

    private class InformarPartida extends SubscriptionInitiator {


        public InformarPartida(Agent agente, ACLMessage mensaje) {
            super(agente, mensaje);
        }

        //Maneja la respuesta en caso que acepte: AGREE
        @Override
        protected void handleAgree(ACLMessage inform) {
            //System.out.println(SubscriptionIni.this.getLocalName() + ": Solicitud aceptada.");
        }

        // Maneja la respuesta en caso que rechace: REFUSE
        @Override
        protected void handleRefuse(ACLMessage inform) {
            //System.out.println(SubscriptionIni.this.getLocalName() + ": Solicitud rechazada.");
        }

        //Maneja la informacion enviada: INFORM
        @Override
        protected void handleInform(ACLMessage inform) {

        }

        //Maneja la respuesta en caso de fallo: FAILURE
        @Override
        protected void handleFailure(ACLMessage failure) {

        }

        @Override
        public void cancellationCompleted(AID agente) {
        }
    }

}
