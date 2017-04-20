package Agentes;

import Backend.TableroBack;
import Auxiliar.*;
import jade.core.*;
import jade.domain.*;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.*;
import jade.proto.ContractNetResponder;
import juegoQuoridor.OntologiaQuoridor;
import jade.lang.acl.*;
import jade.content.*;
import jade.content.onto.basic.Action;
import jade.content.ContentElement;

import java.util.*;

import GUI.Consola;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.Predicate;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.OntologyException;
import jade.proto.ProposeResponder;
import jade.proto.SubscriptionInitiator;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import juegoQuoridor.elementos.Movimiento;
import juegoQuoridor.elementos.MovimientoRealizado;
import juegosTablero.elementos.Ficha;
import juegosTablero.elementos.Partida;
import juegosTablero.elementos.Tablero;
import juegosTablero.elementos.Jugador;
import juegosTablero.elementos.Posicion;

import juegoQuoridor.elementos.Movimiento;
import juegoQuoridor.elementos.MovimientoRealizado;
import juegoQuoridor.elementos.Muro;

/**
 * Agente to play differents instances of Quoridor
 *
 * @author cyane
 * @see field games ArrayList with the games playing at, it has a max number of
 * 5, in order of not make the Agent to slow.
 * @author Erik Tordera Bermejo
 */
public class AgenteErik extends Agent {

    protected ArrayList<Game> games;
    protected Consola gui;
    protected ContentManager cm;

    /**
     * Constructor del agente.
     *
     * @author Erik Tordera
     */
    protected void setup() {

        games = new ArrayList<>();
        gui = new Consola(this);
        gui.setVisible(true);

        //Init contentManager
        cm = new ContentManager();
        SLCodec codec = new SLCodec();
        cm.registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL);
        try {
            cm.registerOntology(OntologiaQuoridor.getInstance(), OntologiaQuoridor.ONTOLOGY_NAME);

        } catch (Exception e) {
            gui.presentarSalidaln("Error in the ContentManager registering process: " + e.getMessage());
        }

        this.addBehaviour(new register(this));
        this.addBehaviour(new apuntaPartidas(this, 0));

    }

    /**
     * Método para apagar el agente.
     *
     * @author Erik Tordera Bermejo
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
        gui.dispose();
        System.out.println("Finaliza la ejecución de " + this.getName());
    }

    /**
     * Registra el agente en las páginas amarillas
     *
     * @author Erik Tordera Bermejo
     */
    protected class register extends OneShotBehaviour {

        Agent father;

        public register(Agent nthis) {
            father = nthis;
        }

        @Override
        public void action() {
            gui.presentarSalida("Agent: " + father.getName() + " starting\n");
            gui.presentarSalida("Trying to register agent: " + getLocalName() + "\n");
            try {

                //Se inicializa el DFAgentDescription
                DFAgentDescription dfd = new DFAgentDescription();

                ServiceDescription sd = new ServiceDescription();
                sd.setType(OntologiaQuoridor.REGISTRO_JUGADOR);

                //Nombre del jugador
                sd.setName("Jugador de Erik");
                sd.addOntologies(OntologiaQuoridor.ONTOLOGY_NAME);
                dfd.addServices(sd);
                DFService.register(father, dfd);

                //Notificamos el éxito.
                gui.presentarSalidaln("Succesfully registered: " + getName());
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }

    /**
     * Clase que añade la tarea para añadirse a juegos.
     *
     * @author Erik Tordera Bermejo.
     */
    protected class apuntaPartidas extends WakerBehaviour {

        public apuntaPartidas(Agent nfather, long timeout) {
            super(nfather, timeout);
        }

        @Override
        protected void onWake() {
            MessageTemplate template = ContractNetResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            this.myAgent.addBehaviour(new getGames(this.myAgent, template));

            //Buscamos juegos cada 3 segundos.
            reset(3000);
        }
    }

    /**
     * Clase para apuntarse a las partidas
     *
     * @author Erik Tordera Bermejo
     */
    protected class getGames extends ContractNetResponder {

        protected Partida game;

        public getGames(Agent father, MessageTemplate template) {
            super(father, template);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
            gui.presentarSalidaln(this.myAgent.getAID().getName() + " recieved a game request by: " + cfp.getSender().getName());

            if (games.size() < 5) {
                gui.presentarSalidaln(this.myAgent.getName() + " Accepted propose");
                ACLMessage accept = cfp.createReply();
                accept.setPerformative(ACLMessage.PROPOSE);
                accept.setContent(this.myAgent.getAID() + ": Accepted propose");

                try {
                    Action Activo = (Action) cm.extractContent(cfp);
                    juegosTablero.elementos.ProponerPartida PP = (juegosTablero.elementos.ProponerPartida) Activo.getAction();

                    game = PP.getPartida();
                    gui.presentarSalidaln(game.getIdPartida());
                } catch (Exception e) {
                    gui.presentarSalidaln("Jugar partida no entendido " + e.toString());
                    throw new NotUnderstoodException("no entendido");
                }

                return accept;
            } else {
                gui.presentarSalidaln("Refussed game, playing to much at the moment");
                throw new RefuseException("to_many_games");
            }
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            Codec codec = new SLCodec();
            juegoQuoridor.elementos.FichaEntregada inside = null;

            try {

                ContentManager cm = new ContentManager();
                cm.registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL);
                try {
                    cm.registerOntology(OntologiaQuoridor.getInstance(), juegoQuoridor.OntologiaQuoridor.ONTOLOGY_NAME);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                inside = (juegoQuoridor.elementos.FichaEntregada) cm.extractContent(accept);

            } catch (Exception exc) {
                exc.printStackTrace();
                System.err.println("Excepción no controlada");
            }
            AID board = accept.getSender();

            games.add(new Game(inside.getFicha(), game, board));

            MessageTemplate template = ProposeResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_PROPOSE);
            this.myAgent.addBehaviour(new JugarPartida(this.myAgent, template, game, (games.size() - 1)));

            ACLMessage subscribe = new ACLMessage(ACLMessage.SUBSCRIBE);
            subscribe.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
            subscribe.addReceiver(cfp.getSender());

            this.myAgent.addBehaviour(new Subscribe(this.myAgent, subscribe));

            asignarPosicionInicial(inside.getFicha().getColor().toString(), games.get(games.size() - 1));
            return inform;
        }
    }

    /**
     * Clase que juega la partida, es reiniciada cada vez que se termina hasta
     * que se termina el juego.
     *
     * @author Erik Tordera Bermejo
     */
    public class JugarPartida extends ProposeResponder {

        protected Partida game;
        protected Integer indexGame;

        public JugarPartida(Agent father, MessageTemplate template, Partida ngame, Integer nindexGame) {
            super(father, template);
            game = ngame;
            indexGame = nindexGame;
        }

        @Override
        protected ACLMessage prepareResponse(ACLMessage propose) throws NotUnderstoodException, RefuseException {
            ACLMessage mes = propose.createReply();

            try {
                Action Activo = (Action) cm.extractContent(propose);
                juegoQuoridor.elementos.JugarPartida JP = (juegoQuoridor.elementos.JugarPartida) Activo.getAction();

                if (JP.getJugadorActivo().getAgenteJugador().equals(getAID())) {
                    //Presentamos por salida que si que es nuestro turno
                    gui.presentarSalidaln(game.getIdPartida() + ": Es mi turno");

                    Game thisGamo = busca(game.getIdPartida());

                    //calculamos la jugada
                    MovimientoRealizado toDo = juega(games.get(indexGame), this.myAgent.getAID(), thisGamo.getToken());
                    cm.fillContent(mes, toDo);

                    //Pnemos el permormatice para que acepte el movimiento,
                    mes.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

                } else {

                    gui.presentarSalidaln("Es el turno de: " + JP.getJugadorActivo().getAgenteJugador().getLocalName());

                    TableroBack board = games.get(indexGame).getBoard();
                    Movimiento last = JP.getMovimientoAnterior();

                    try {
                        if (last.getElementoJuego() instanceof juegosTablero.elementos.Ficha) {
                            board.Turno(TableroBack.__MUEVE__, last.getPosicion(), true);
                        } else {

                            if (last.getElementoJuego() != null) {
                                Muro wall = (Muro) last.getElementoJuego();
                                if (wall.getAlineacion().equals(juegoQuoridor.OntologiaQuoridor.ALINEACION_HORIZONTAL)) {
                                    board.Turno(TableroBack.__MURO__, last.getPosicion(), TableroBack.__HORIZONTAL__);
                                } else {
                                    board.Turno(TableroBack.__MURO__, last.getPosicion(), TableroBack.__VERTICAL__);
                                }
                            }
                        }
                    } catch (Exception e) {
                        gui.presentarSalidaln((JP.getJugadorActivo()).getAgenteJugador() + "Erró en la generación del mensaje, no pude rellenar mi tablero. "+ e.toString());
                    }

                    mes.setPerformative(ACLMessage.REJECT_PROPOSAL);
                }

            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgenteErik.class.getName()).log(Level.SEVERE, null, ex);
            }

            //IF PARTIDA NO TERMINADA.
            MessageTemplate template = ProposeResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_PROPOSE);
            this.myAgent.addBehaviour(new JugarPartida(this.myAgent, template, game, indexGame));
            //ENDIF
            return mes;
        }
    }

    /**
     *
     */
    protected class Subscribe extends SubscriptionInitiator {

        public Subscribe(Agent agente, ACLMessage mensaje) {
            super(agente, mensaje);
            gui.presentarSalidaln("Trying to register.");
        }

        //Maneja la respuesta en caso que acepte: AGREE
        protected void handleAgree(ACLMessage inform) {
            gui.presentarSalidaln("Successfully Registered.");
        }

        // Maneja la respuesta en caso que rechace: REFUSE
        protected void handleRefuse(ACLMessage inform) {
            gui.presentarSalidaln("Reffused subscription.");
        }

        //Maneja la informacion enviada: INFORM
        protected void handleInform(ACLMessage inform) {
            try {
                juegosTablero.elementos.GanadorPartida gp = (juegosTablero.elementos.GanadorPartida) cm.extractContent(inform);
                gui.presentarSalidaln("The player: " + gp.getJugador().getAgenteJugador().getLocalName() + "Has won at: " + gp.getPartida().getIdPartida());
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(JugadorTejada.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    /**
     * Busca un juego en el vector
     *
     * @param id id de la partida a buscar
     * @return Game de la partida quee estamos jugando.
     */
    protected Game busca(String id) {
        for (int i = 0; i < games.size(); i++) {
            if (games.get(i).getIdPartida() == id) {
                return games.get(i);
            }
        }
        return null;
    }

    /**
     * Procesa una jugada valida para la partida
     *
     * @param game partida en la que estamos jugando
     * @param myAID AID del jugador que juega
     * @param myToken ficha del jugador que juega
     * @return movimiento realizado.
     */
    protected MovimientoRealizado juega(Game game, AID myAID, Ficha myToken) {

        TableroBack board = game.getBoard();

        Integer jugador = -1;
        if (myToken.getColor().equals(OntologiaQuoridor.COLOR_FICHA_1)) {
            jugador = 0;
        } else if (myToken.getColor().equals(OntologiaQuoridor.COLOR_FICHA_2)) {
            jugador = 1;
        } else if (myToken.getColor().equals(OntologiaQuoridor.COLOR_FICHA_3)) {
            jugador = 2;
        } else {
            jugador = 3;
        }

        Jugador me = new Jugador(myAID, myToken);
        Posicion toPlay = new Posicion(5, 5);

        //IF MURO
        MovimientoRealizado toRet = new MovimientoRealizado(me, new Movimiento(new Muro(OntologiaQuoridor.ALINEACION_HORIZONTAL), toPlay));
        game.putWall();
        //ELSE
        //MovimientoRealizado toRet = new MovimientoRealizado(me, myToken);
        return toRet;
    }

    public void asignarPosicionInicial(String color, Game game) {

        switch (color) {
            case OntologiaQuoridor.COLOR_FICHA_1:
                game.setStart(new Posicion(4, 0));
                game.setEnd(new Posicion(1, 8));

                break;

            case OntologiaQuoridor.COLOR_FICHA_2:
                if (game.getGame().getNumeroJugadores() == 4) {
                    game.setStart(new Posicion(8, 4));
                    game.setEnd(new Posicion(0, 1));
                } else {
                    game.setStart(new Posicion(4, 8));
                    game.setEnd(new Posicion(1, 0));
                }
                break;

            case OntologiaQuoridor.COLOR_FICHA_3:
                game.setStart(new Posicion(4, 8));
                game.setEnd(new Posicion(1, 0));

                break;

            case OntologiaQuoridor.COLOR_FICHA_4:
                game.setStart(new Posicion(0, 4));
                game.setEnd(new Posicion(8, 1));
                break;

        }
    }
}
