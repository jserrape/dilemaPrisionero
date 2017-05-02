/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//-container -host 192.168.38.100 -agents MartinezLledo:agentes.AgenteLadron;MartinezLledoC:agentes.AgenteConsola
//Para hacer: cuando llega un mensaje hay que meter el nombre (AID o lo que sea) en una lista y comprobar si estaba antes,
//Si envía True (no estaba) hacer la subscripción, y si estaba (False) no hacer nada nuevo
//Se envía un Inform(DetalleInforme) con Partida y Detalle(Concepto) Que realizará Guardar(Jugador) o un Error(String)
package agentes;

import dilemaPrisionero.OntologiaDilemaPrisionero;
import dilemaPrisionero.elementos.EntregarJugada;
import dilemaPrisionero.elementos.Jugada;
import dilemaPrisionero.elementos.JugadaEntregada;
import dilemaPrisionero.elementos.ProponerPartida;
import dilemaPrisionero.elementos.ResultadoJugada;
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
public class AgenteLadron extends Agent {

    private Codec codec = new SLCodec();

    // La ontología que utilizará el agente
    private Ontology ontologia;

    private Partida partida;
    private Jugador jugador;

    private AID[] agentesConsola;
    private ArrayList<String> mensajesPendientes;

    private ContentManager manager = (ContentManager) getContentManager();

    private int condenaAcumulada = 0;

    @Override
    protected void setup() {
        //Inicialización de las variables del agente   
        mensajesPendientes = new ArrayList();

        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        // Regisro de la Ontología
        try {
            ontologia = OntologiaDilemaPrisionero.getInstance();
        } catch (BeanOntologyException ex) {
            Logger.getLogger(AgenteLadron.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            manager.registerLanguage(codec);
            manager.registerOntology(ontologia);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //registro ontologia
        sd.addOntologies(OntologiaDilemaPrisionero.ONTOLOGY_NAME);

        //registro paginas amarillas
        try {
            sd.setName(OntologiaDilemaPrisionero.REGISTRO_PRISIONERO);
            sd.setType(OntologiaDilemaPrisionero.REGISTRO_PRISIONERO);
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        mensajesPendientes.add("ME HE CONECTADO A LA PLATAFORMA");

        jugador = new Jugador(this.getLocalName(), this.getAID());

        //BUSCO LA CONSULA Y LE MANDO LOS MENSAJES
        addBehaviour(new TareaBuscarConsola(this, 5000));
        addBehaviour(new TareaEnvioConsola(this, 1000));

        //LEO LAS PROPOSICIONES DE PARTIDA
        MessageTemplate plantilla = ProposeResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_PROPOSE);
        addBehaviour(new ResponderProposicionPartida(this, plantilla));

        //Leo las rondas
        MessageTemplate template = ContractNetResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        addBehaviour(new TareaJugarPartida(this, template));
    }

    @Override
    protected void takeDown() {
        //Desregristo del agente de las Páginas Amarillas
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
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
            sd.setName(OntologiaDilemaPrisionero.REGISTRO_CONSOLA);
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

    private class InformarPartidaSubscribe extends SubscriptionInitiator {

        public InformarPartidaSubscribe(Agent agente, ACLMessage mensaje) {
            super(agente, mensaje);
        }

        //Maneja la respuesta en caso que acepte: AGREE
        @Override
        protected void handleAgree(ACLMessage inform) {
            mensajesPendientes.add("Mi subscripcion a la plataforma ha sido aceptada");
        }

        // Maneja la respuesta en caso que rechace: REFUSE
        @Override
        protected void handleRefuse(ACLMessage inform) {
            mensajesPendientes.add("Mi subscripcion a la plataforma ha sido rechazada");
        }

        //Maneja la informacion enviada: INFORM
        @Override
        protected void handleInform(ACLMessage ganador) {
            mensajesPendientes.add("Me ha llegado un ganador de partida");
        }

        //Maneja la respuesta en caso de fallo: FAILURE
        @Override
        protected void handleFailure(ACLMessage failure) {

        }

        @Override
        public void cancellationCompleted(AID agente) {
        }
    }

    private class ResponderProposicionPartida extends ProposeResponder {

        public ResponderProposicionPartida(Agent agente, MessageTemplate plantilla) {
            super(agente, plantilla);
        }

        @Override
        protected ACLMessage prepareResponse(ACLMessage propuesta) throws NotUnderstoodException {
            mensajesPendientes.add("Me ha llegado una proposicion de partida");

            ProponerPartida pp = null;
            Partida p = null;
            try {
                Action ac = (Action) manager.extractContent(propuesta);
                pp = (ProponerPartida) ac.getAction();
                p = pp.getPartida();
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgenteLadron.class.getName()).log(Level.SEVERE, null, ex);
            }

            Jugador j = new Jugador(this.myAgent.getLocalName(), this.myAgent.getAID());
            PartidaAceptada pa = new PartidaAceptada(p, j);

            ACLMessage agree = propuesta.createReply();
            agree.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            agree.setLanguage(codec.getName());
            agree.setOntology(ontologia.getName());
            try {
                manager.fillContent(agree, pa);
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgenteLadron.class.getName()).log(Level.SEVERE, null, ex);
            }

            ///////////////////////////////////////////////////
            InformarPartida inf = new InformarPartida(jugador);
            ACLMessage mensaje = new ACLMessage(ACLMessage.SUBSCRIBE);
            mensaje.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
            mensaje.setSender(this.myAgent.getAID());
            mensaje.setLanguage(codec.getName());
            mensaje.setOntology(ontologia.getName());
            try {
                Action action = new Action(getAID(), inf);
                manager.fillContent(mensaje, action);
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgentePolicia.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Se añade el destinatario del mensaje
            AID id = new AID();
            id.setLocalName(OntologiaDilemaPrisionero.REGISTRO_POLICIA);
            mensaje.addReceiver(id);

            //ME REGISTRO AL SUBSCRIBE
            addBehaviour(new InformarPartidaSubscribe(this.myAgent, mensaje));

            ////////////////////////////////////////////////
            return agree;
        }
    }

    private class TareaJugarPartida extends ContractNetResponder {

        public TareaJugarPartida(Agent agente, MessageTemplate plantilla) {
            super(agente, plantilla);
        }

        @Override
        protected ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
            Action ac;
            EntregarJugada entJug = null;

            try {
                ac = (Action) manager.extractContent(cfp);
                entJug = (EntregarJugada) ac.getAction();
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgenteLadron.class.getName()).log(Level.SEVERE, null, ex);
            }
            mensajesPendientes.add("Me ha llegado una peticion de ronda para la partida con id=" + entJug.getPartida().getIdPartida());
            //De lo anterior leo quien es mi oponente
            int numero = ((int) (Math.random() * 1000)) % 2;
            Partida part = entJug.getPartida();
            Jugada jugada;
            mensajesPendientes.add(numero + "");
            if (numero == 1) {
                jugada = new Jugada(OntologiaDilemaPrisionero.CALLAR);
            } else {
                jugada = new Jugada(OntologiaDilemaPrisionero.HABLAR);
            }
            JugadaEntregada jugEnt = new JugadaEntregada(part, jugador, jugada);

            ACLMessage respuesta = cfp.createReply();
            respuesta.setPerformative(ACLMessage.PROPOSE);
            respuesta.setSender(myAgent.getAID());
            respuesta.setLanguage(codec.getName());
            respuesta.setOntology(ontologia.getName());

            try {
                manager.fillContent(respuesta, jugEnt);
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgenteLadron.class.getName()).log(Level.SEVERE, null, ex);
            }

            return respuesta;
        }

        @Override
        protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            ResultadoJugada resultado = null;

            try {
                resultado = (ResultadoJugada) manager.extractContent(accept);
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgenteLadron.class.getName()).log(Level.SEVERE, null, ex);
            }

            condenaAcumulada += resultado.getCondenaRecibida();
            mensajesPendientes.add("Me ha llegado un ResultadoJugada, me han caido: " + resultado.getCondenaRecibida() + " años, llevo " + condenaAcumulada);

            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        }

        @Override
        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            mensajesPendientes.add("Me ha llegado algo al handleRejectProposal");
        }
    }

}
