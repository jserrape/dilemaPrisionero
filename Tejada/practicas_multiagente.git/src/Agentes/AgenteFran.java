/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Agentes;

import juegoQuoridor.OntologiaQuoridor;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import GUI.*;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.proto.ProposeResponder;
import java.util.logging.Level;
import java.util.logging.Logger;
import juegoQuoridor.elementos.FichaEntregada;
import juegosTablero.elementos.Jugador;
import juegosTablero.elementos.Partida;
import juegosTablero.elementos.ProponerPartida;

/**
 *
 *
 */
public class AgenteFran extends Agent {

    private Consola console;
    private int nPartidas;
    private String[] color = new String[5];
    protected Codec codec;
    protected ContentManager cm;

    /**
     * Se ejecuta cuando se inicia el agente
     */
    @Override
    protected void setup() {
        //Configuración del GUI y presentación
        console = new Consola(this);
        console.setVisible(true);
        console.presentarSalida("Se inicia la ejecución de " + this.getName() + "\n");

        //Incialización de variables
        nPartidas = 0;

        codec = new SLCodec();
        cm = new ContentManager();
        cm.registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL);
        try {
            cm.registerOntology(OntologiaQuoridor.getInstance(), OntologiaQuoridor.ONTOLOGY_NAME);
        } catch (BeanOntologyException ex) {
            Logger.getLogger(AgenteFran.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Registro de la Ontología
        //Registro en Página Amarillas
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(OntologiaQuoridor.REGISTRO_JUGADOR);
        sd.setName("Jugador de Fran");
        sd.addOntologies(OntologiaQuoridor.ONTOLOGY_NAME);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        //Creamos una plantilla
        MessageTemplate template = ContractNetResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);

        // Se añaden las tareas principales
        console.presentarSalida("Empieza lo chungo\n");
        addBehaviour(new Registrar(this, template));
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
        console.dispose();
        System.out.println("Finaliza la ejecución de " + this.getName());
    }

    //Métodos del agente
    public class Registrar extends ContractNetResponder {

        Agent padre;

        /**
         * Método-Clase para registrar el jugador con ContractNet
         *
         * @param a: es el agente que lo invoca.
         * @param plantilla: Es la plantilla del mensaje.
         */
        public Registrar(Agent a, MessageTemplate plantilla) {
            super(a, plantilla);
            padre = a;
        }

        @Override
        protected ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
            console.presentarSalida("Propuesta obtenida de " + cfp.getSender().getLocalName() + "\n");
            if (nPartidas < 3) {
                console.presentarSalida("Aceptada la proposición, preparando respuesta.\n");
                try {
                    
                    Action act = (Action) cm.extractContent(cfp);
                    ProponerPartida PP = (ProponerPartida) act.getAction();
                    Partida ESTAESLAPUTAPARTIDA = PP.getPartida();
                    console.presentarSalidaln(ESTAESLAPUTAPARTIDA.getIdPartida());
                    
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgenteFran.class.getName()).log(Level.SEVERE, null, ex);
                }
              
                ACLMessage respuesta = cfp.createReply();
                respuesta.setPerformative(ACLMessage.PROPOSE);
                respuesta.setContent(String.valueOf("patata"));
                return respuesta;
            } else {
                console.presentarSalida("Denegada la proposición.\n");
                throw new RefuseException("Que te den");
            }
        }

        @Override
        protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            console.presentarSalida("Preparando el resultado\n");

            try {
                Codec codec = new SLCodec();
                ContentManager cm = new ContentManager();
                cm.registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL);
                cm.registerOntology(OntologiaQuoridor.getInstance(), OntologiaQuoridor.ONTOLOGY_NAME);
                FichaEntregada inside = (FichaEntregada) cm.extractContent(accept);
                color[nPartidas] = inside.getFicha().getColor();
                console.presentarSalidaln("Partida con color: " + color[nPartidas]);
                nPartidas++;
            } catch (Exception exc) {
                exc.printStackTrace();
                System.err.println("Excepción no controlada");
            }
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);

            MessageTemplate MesT = ProposeResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_PROPOSE);
            try {
                padre.addBehaviour(new JugarPartida(padre, MesT));
            } catch (BeanOntologyException ex) {
                Logger.getLogger(AgenteFran.class.getName()).log(Level.SEVERE, null, ex);
            }

            return inform;
        }
    }

    public class JugarPartida extends ProposeResponder {

        public JugarPartida(Agent father, MessageTemplate MT) throws BeanOntologyException {
            super(father, MT);
            console.presentarSalidaln("Llego al JugarPartida");

        }

        @Override
        protected ACLMessage prepareResponse(ACLMessage propose) throws NotUnderstoodException, RefuseException {
            console.presentarSalidaln("Llego aquí");
            ACLMessage mes = propose.createReply();
            try {
                Action Activo = (Action) cm.extractContent(propose);
                juegoQuoridor.elementos.JugarPartida JP = (juegoQuoridor.elementos.JugarPartida) Activo.getAction();
                if (JP.getJugadorActivo().getAgenteJugador().equals(getAID())) {
                    console.presentarSalidaln("Es mi turno");
                    mes.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                } else {
                    console.presentarSalidaln("Es el turno de: " + JP.getJugadorActivo().getAgenteJugador().getLocalName());
                    mes.setPerformative(ACLMessage.REJECT_PROPOSAL);
                }
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgenteFran.class.getName()).log(Level.SEVERE, null, ex);
            }

            return mes;
        }

    }
}
