/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Agentes;

import GUI.Consola;
import jade.content.Concept;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;

import jade.core.Agent;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import juegoQuoridor.OntologiaQuoridor;
import juegoQuoridor.elementos.FichaEntregada;
import juegoQuoridor.elementos.Muro;
import juegosTablero.elementos.Ficha;
import juegosTablero.elementos.Jugador;

/**
 *
 * @author Manuel Tejada
 * @param gui es la ventana donde se mostraran los mensajes del agente
 * @param numPartidas es el numero de partidas que tiene actualmente
 * @param color es el color de la ficha que juega en la partida n
 * @param numJugadores vector donde sabremos el numero de jugadores de la
 * partida x
 * @param posicionActual posicion actual del peon de la partida (debemos hacer
 * un vector del mismo tamaño que numPartidas)
 * @param coorDestino coordenada destino x(0) o y(1) a la que debemos llevar el
 * peon (debemos hacer un vector del mismo tamaño que numPartidas)
 * @param destino destino del jugador en la coordenada ya dicha (debemos hacer
 * un vector del mismo tamaño que numPartidas)
 * @param cm es nuestro contentManager que se usa para extraer los mensajes que
 * nos llegan, para meter contenido a los mensajes que enviamos el cual ya tiene
 * el lenguaje y la ontologia que utilizamos
 * @param codec es el codec que necesitamos para que nuestro contentManager(cm)
 * entienda los mensajes que enviamos y que recibimos
 * @param tamTablero es el tamaño del tablero para que nuestro jugador mueva o
 * ponga muros del cual necesitaremos un array del tamaño de numPartidas para
 * poder manejar varias partidas y poder mover de forma inteligente.
 */
public class JugadorTejada extends Agent {

    //<editor-fold defaultstate="collapsed" desc="Variables para la clase JugadorTejada">
    private Consola gui;
    private int numPartidas;
    private String[] color = new String[5];
    private int[] numjugadores = new int[5];
    private int[] posicionActual = new int[2]; // 0 coordenada x, 1 coordenada y
    private int coorDestino;
    private int destino;
    private ContentManager cm;
    private Codec codec;
    private int[][] tamTablero = new int[9][9];

    // </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Metodos de inicializacion del agente">
    @Override
    protected void setup() {
        //Configuracion de la GUI y presentacion
        gui = new Consola(this);
        gui.setVisible(true);

        String name = "Jugador Tejada";
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        //Iniciamos las variables
        numPartidas = 0;
        codec = new SLCodec();
        try {
            cm = new ContentManager();
            cm.registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL);
            cm.registerOntology(OntologiaQuoridor.getInstance(), OntologiaQuoridor.ONTOLOGY_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //registro ontologia
        sd.addOntologies(OntologiaQuoridor.ONTOLOGY_NAME);

        //registro paginas amarillas
        gui.presentarSalidaln("Intentando registrar el agente: " + name);
        try {
            sd.setName(name);
            sd.setType(OntologiaQuoridor.REGISTRO_JUGADOR);
            dfd.addServices(sd);
            DFService.register(this, dfd);
            gui.presentarSalidaln("Registro Completado: " + getName());
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        //Creamos una plantilla
        MessageTemplate plantilla = ContractNetResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);

        //Añadimos las tareas principales
        addBehaviour(new PeticionPartida(this, plantilla));

    }

    @Override
    protected void takeDown() {
        try {
            //Desregistro de las paginas amarillas
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        gui.dispose();
        gui.presentarSalidaln("Jugador: " + this.getName() + " finalizo con exito");
    }

    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Clase para recibir las partidas propuestas (ContractNet)">
    /**
     * En PeticionPartida se trata el aceptar nuevas partidas según el numero de
     * partida que ya lleve nuestro agente usa las variables del agente jugador:
     *
     * @param numpartidas = para saber cuantas partidas tenemos activas
     * @param gui = es la consola de nuestro agente jugador para enviarle
     * mensajes importantes.
     * @param numJugadores = para en caso de aceptar saber el numero de
     * jugadores de la partida
     * @param color = para saber el color de nuestra ficha en cada partida.
     * @param posicionActual = para colocar a nuestro peon donde corresponda ya
     * sea una partida de 2 jugadores o de 4.
     * @param destino = es a la posicion que tiene que llegar en una coordenada
     * concreta para ganar (coorDestino)
     * @param coorDestino = es la posicion x(0) o y(1) en la cual tiene su
     * destino.
     *
     * @Autor: Manuel Tejada García
     *
     */
    public class PeticionPartida extends ContractNetResponder {

        Agent father;

        public PeticionPartida(Agent father, MessageTemplate template) {
            super(father, template);
            this.father = father;
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
            gui.presentarSalidaln("Jugador: " + this.myAgent.getName() + " ha recibido un juego de: " + cfp.getSender().getName());

            if (numPartidas < 5) // denegamos si el numero de partidas supera el maximo de partidas que puede llevar.
            {
                Action a;
                juegosTablero.elementos.ProponerPartida partidaPropuesta;
                try {
                    a = (Action) cm.extractContent(cfp);
                    partidaPropuesta = (juegosTablero.elementos.ProponerPartida) a.getAction();
                    numjugadores[numPartidas + 1] = partidaPropuesta.getPartida().getNumeroJugadores();
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(JugadorTejada.class.getName()).log(Level.SEVERE, null, ex);
                }

                gui.presentarSalidaln(this.myAgent.getName() + " Acepta la propuesta");
                ACLMessage mensaje = cfp.createReply();
                mensaje.setPerformative(ACLMessage.PROPOSE);
                mensaje.setContent(this.myAgent.getAID() + " Partida Aceptada");
                return mensaje;
            } else {
                gui.presentarSalidaln("Jugador: " + this.myAgent.getName() + "Rechaza la partida");
                throw new RefuseException("rechazo");
            }
        }

        @Override
        protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose, ACLMessage acepta) {
            gui.presentarSalidaln("Preparando resultado de la notificacion");
            numPartidas++;
            FichaEntregada fe;
            try {
                fe = (FichaEntregada) cm.extractContent(acepta);
                gui.presentarSalidaln(fe.getFicha().getColor());
                color[numPartidas] = fe.getFicha().getColor();
                switch (color[numPartidas]) {
                    case OntologiaQuoridor.COLOR_FICHA_1:
                        posicionActual[0] = 4; //coordenada x donde empieza el jugador 1 (peon superior)
                        posicionActual[1] = 0; //coordenada y donde empieza el jugador 1 (peon superior)

                        destino = 8;
                        coorDestino = 1;
                        break;

                    case OntologiaQuoridor.COLOR_FICHA_2:
                        if (numjugadores[numPartidas] == 4) {
                            posicionActual[0] = 8; //coordenada x donde empieza el jugador 2 (peon derecho)
                            posicionActual[1] = 4; //coordenada y donde empieza el jugador 2 (peon derecho)

                            destino = 0;
                            coorDestino = 0;
                        } else {
                            posicionActual[0] = 4; //coordenada x donde empieza el jugador 2 (peon inferior)
                            posicionActual[1] = 8; //coordenada y donde empieza el jugador 2 (peon inferior)

                            destino = 0;
                            coorDestino = 1;
                        }
                        break;

                    case OntologiaQuoridor.COLOR_FICHA_3:
                        posicionActual[0] = 4; //coordenada x donde empieza el jugador 3 (peon inferior)
                        posicionActual[1] = 8; //coordenada y donde empieza el jugador 3 (peon inferior)

                        destino = 0;
                        coorDestino = 1;
                        break;

                    case OntologiaQuoridor.COLOR_FICHA_4:
                        posicionActual[0] = 0; //coordenada x donde empieza el jugador 4 (peon izquierdo)
                        posicionActual[1] = 4; //coordenada y donde empieza el jugador 4 (peon izquierdo)

                        destino = 8;
                        coorDestino = 0;
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ACLMessage inform = acepta.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            gui.presentarSalidaln(propose.getContent());

            //creamos una plantilla para el metodo propose
            MessageTemplate plantilla = ContractNetResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_PROPOSE);
            father.addBehaviour(new JugarPartida(father, plantilla));
            
            //Creamos un ACLMessage para el metodo subscribe
            ACLMessage subscribe = new ACLMessage(ACLMessage.SUBSCRIBE);
            subscribe.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
            subscribe.addReceiver(cfp.getSender());
            
            
            father.addBehaviour(new SubcripcionPartida(father, subscribe));

            return inform;
        }
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Clase Propose para mover el jugador">
    /**
     * En JugarPartida se trata los mensajes de partidas ya aceptadas en las
     * cuales aveces nos tocara y enviaremos un accept_proposal (aceptamos) y
     * otras veces no nos tocara por lo que enviaremos un reject_proposal
     * (rechazo). En caso de aceptar enviaremos el movimiento que realiza
     * nuestro jugador usa las variables del agente jugador:
     *
     * @param gui = es la consola de nuestro agente jugador para enviarle
     * mensajes importantes.
     * @param color = para enviarle el movimiento de nuestra ficha.
     * @param posicionActual = para saber en que posicion se encuentra nuestra
     * ficha.
     * @param destino = es a la posicion que tiene que llegar en una coordenada
     * concreta para ganar (coorDestino)
     * @param coorDestino = es la posicion x(0) o y(1) en la cual tiene su
     * destino.
     *
     * @Autor: Manuel Tejada García
     *
     */
    public class JugarPartida extends ProposeResponder {

        public JugarPartida(Agent father, MessageTemplate template) {
            super(father, template);
        }

        //Preparacion de la respuesta despues de recibir un mensaje para aceptarlo o no
        public ACLMessage prepareResponse(ACLMessage propuesta) throws NotUnderstoodException {
            gui.presentarSalidaln(this.myAgent.getLocalName() + ": Proposicion recibida de " + propuesta.getSender().getLocalName() + ".");
            ACLMessage respuesta = propuesta.createReply();

            try {
                Action activo = (Action) cm.extractContent(propuesta);
                juegoQuoridor.elementos.JugarPartida jp = (juegoQuoridor.elementos.JugarPartida) activo.getAction();
                if (jp.getJugadorActivo().getAgenteJugador().equals(getAID())) //si somos el jugador activo nos toca mover
                {
                    gui.presentarSalidaln("Es mi turno");
                    if (coorDestino == 0) {
                        if (destino > posicionActual[0]) {
                            posicionActual[0]++;

                        } else {
                            posicionActual[0]--;
                        }
                    } else if (destino > posicionActual[1]) {
                        posicionActual[1]++;
                    } else {
                        posicionActual[1]--;
                    }

                    /**
                     * creamos el elemento posicion (pos) donde moveremos
                     * nuestro peon o colocaremos un muro (por ahora solo
                     * movemos el peon) creamos el movimiento (mov) diciendo el
                     * elemnto de juego que usaremos (ficha o muro) y la
                     * posicion que hemos escogido creamos el elemnto jugador el
                     * cual tiene nuestro AID y que ficha somos en dicho juego
                     * una vez creado esto generamos el movimiento que vamos a
                     * realizar en el juego. (jugada)
                     */
                    juegosTablero.elementos.Posicion pos = new juegosTablero.elementos.Posicion(posicionActual[0], posicionActual[1]);
                    juegoQuoridor.elementos.Movimiento mov = new juegoQuoridor.elementos.Movimiento(new juegosTablero.elementos.Ficha(color[numPartidas]), pos);
                    juegosTablero.elementos.Jugador jugador = new juegosTablero.elementos.Jugador(getAID(), new juegosTablero.elementos.Ficha(color[numPartidas]));

                    juegoQuoridor.elementos.MovimientoRealizado jugada = new juegoQuoridor.elementos.MovimientoRealizado(jugador, mov);

                    cm.fillContent(respuesta, jugada);

                    //Se crea la respuesta al mensaje con la performativa ACCEPT_PROPOSAL.
                    respuesta.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

                } else //si es el turno de otro jugador solo almacenamos el movimiento anterior para tener informacion de la partida y decicir como mover
                {
                    gui.presentarSalidaln("turno de otro");
                    juegoQuoridor.elementos.Movimiento movAnterior = jp.getMovimientoAnterior();
                    if(movAnterior.getElementoJuego() instanceof Ficha)
                    {
                        gui.presentarSalidaln("Elemento de juego Ficha");
                    }
                    else if(movAnterior.getElementoJuego() instanceof Muro)
                    {
                        gui.presentarSalidaln("Elemento de juego Muro");
                    }
                    else
                    {
                        gui.presentarSalidaln("No se reconoce el elemento de juego");
                    }
                    //tamTablero[movAnterior.getPosicion().getCoorX()][movAnterior.getPosicion().getCoorY()] = 1;
                    /* for(int i = 0; i< 9; i++)
                    {
                        for(int j=0;j<9; j++)
                        {
                            System.out.print(tamTablero[i][j]);
                        }
                        System.out.println(" ");
                    }*/

                    //Se crea la respuesta al mensaje con la performativa REJECT_PROPOSAL.
                    respuesta.setPerformative(ACLMessage.REJECT_PROPOSAL);
                }
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(JugadorTejada.class.getName()).log(Level.SEVERE, null, ex);
            }

            return respuesta;
        }
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Clase Subscription para subscribirnos para saber el ganador de la partida">
    private class SubcripcionPartida extends SubscriptionInitiator {

        public SubcripcionPartida(Agent agente, ACLMessage mensaje) {
            super(agente, mensaje);
            gui.presentarSalidaln("Entro en subscripcion");
        }

        //Maneja la respuesta en caso que acepte: AGREE
        protected void handleAgree(ACLMessage inform) {
            gui.presentarSalidaln(this.myAgent.getLocalName() + ": Solicitud aceptada.");
        }

        // Maneja la respuesta en caso que rechace: REFUSE
        protected void handleRefuse(ACLMessage inform) {
            gui.presentarSalidaln(this.myAgent.getLocalName() + ": Solicitud rechazada.");
        }

        //Maneja la informacion enviada: INFORM
        protected void handleInform(ACLMessage inform) {
            try {
                juegosTablero.elementos.GanadorPartida gp = (juegosTablero.elementos.GanadorPartida) cm.extractContent(inform);
                gui.presentarSalidaln("Ha ganado el jugador " + gp.getJugador().getAgenteJugador().getLocalName() + " en la partida " + gp.getPartida().getIdPartida());
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(JugadorTejada.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
    
    // </editor-fold>

}
