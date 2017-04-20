package Agentes;

import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.*;
import jade.content.*;
import jade.content.onto.*;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.domain.FIPANames.Ontology;
import java.util.logging.*;

import java.util.*;
import juegoQuoridor.OntologiaQuoridor;
import juegoQuoridor.elementos.*;
import Auxiliar.*;
import Backend.TableroBack;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder.Subscription;
import juegosTablero.elementos.Ficha;

public class BoardAgent extends Agent {

    protected TableroBack boardDriver;
    //Temporal
    protected int count;

    //
    protected int jugadorActual;
    protected ArrayList<PartidaQuoridor> games;
    protected DFAgentDescription[] results;
    protected ContentManager cm;
    protected Codec codec = new SLCodec();

    protected void setup() {

        games = new ArrayList<>();
        count = 0;
        cm = new ContentManager();
        cm.registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL);
        try {
            cm.registerOntology(OntologiaQuoridor.getInstance(), juegoQuoridor.OntologiaQuoridor.ONTOLOGY_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        jugadorActual = 0;

        boardDriver = new TableroBack(this);
        
    }

    public void addTareaBusca() {
        this.addBehaviour(new buscaJugadores(this, 0));
    }

// <editor-fold defaultstate="collapsed" desc="Clase de búsqueda de jugadores">
    /**
     *  Clase para mandar un mensaje a los jugadores de las paginas amarillas para la busqueda de participantes en un nuevo juego.
     *  @author Ratones y mazmorras
     */
    public class buscaJugadores extends WakerBehaviour {

        public buscaJugadores(Agent father, long ms) {
            super(father, ms);
        }

        /**
         * onWake method Overriden, it send the init ACLMessage of the
         * contract-net and initializates the behaviour to handle it responses
         */
        @Override
        public void onWake() {
            System.out.println("\"Ratones y Mazmorras\" Board agent searching for players");
            ServiceDescription serv = new ServiceDescription();
            serv.setType(OntologiaQuoridor.REGISTRO_JUGADOR);

            DFAgentDescription descr = new DFAgentDescription();
            serv.addOntologies(OntologiaQuoridor.ONTOLOGY_NAME);
            descr.addServices(serv);

            try {
                results = DFService.search(myAgent, descr);
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                msg.setOntology(juegoQuoridor.OntologiaQuoridor.ONTOLOGY_NAME);

                for (int i = 0; i < results.length; i++) {
                    msg.addReceiver(results[i].getName());
                }

                if (results.length >= 4) {

                    msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);

                    String id = new Long(System.currentTimeMillis()).toString();
                    juegosTablero.elementos.Partida nueva = new juegosTablero.elementos.Partida(id, juegoQuoridor.OntologiaQuoridor.TIPO_JUEGO, 4, new juegosTablero.elementos.Tablero(9, 9));
                    cm.fillContent(msg, new Action(this.myAgent.getAID(), (new juegosTablero.elementos.ProponerPartida(nueva))));

                    PartidaQuoridor gamo = new PartidaQuoridor(nueva.getNumeroJugadores(), nueva);

                    msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));

                    this.myAgent.addBehaviour(new choosePlayers(this.myAgent, msg, gamo));
                } else if (results.length >= 2) {
                    msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);

                    String id = new Long(System.currentTimeMillis()).toString();
                    juegosTablero.elementos.Partida nueva = new juegosTablero.elementos.Partida(id, juegoQuoridor.OntologiaQuoridor.TIPO_JUEGO, 2, new juegosTablero.elementos.Tablero(9, 9));
                    cm.fillContent(msg, new Action(this.myAgent.getAID(), (new juegosTablero.elementos.ProponerPartida(nueva))));

                    PartidaQuoridor gamo = new PartidaQuoridor(nueva.getNumeroJugadores(), nueva);

                    msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));

                    this.myAgent.addBehaviour(new choosePlayers(this.myAgent, msg, gamo));

                } else {
                    boardDriver.showStart();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
// </editor-fold>
    
// <editor-fold defaultstate="collapsed" desc="Clase ContractNetInitiator">   
    /**
     *  Clase que implementa el metodo ContractNet en nuestro tablero, se lanza para la buqueda de jugadores para iniciar un nuevo juego.
     *  @author Ratones y mazmorras
     *  @param salidaPartida variable tipo ACLMessage encargada de enviar el mensaje a los agente jugador para decidir que jugadores jugaran la partida.
     *  @param gamo variable tipo PartidaQuoridor que se usa para guardar los datos de la partida que se esta generando, luego dicha partida se guarda en el vector games.
     *  @param informCounter variable tipo Integer que se usa para asegurarnos que solo se genera una vez el mensaje de inicio de juego.
     */
    protected class choosePlayers extends ContractNetInitiator {

        Integer informCounter;
        ACLMessage salidaPartida;
        PartidaQuoridor gamo;

        private ArrayList<ACLMessage> responses;

        public choosePlayers(Agent father, ACLMessage pro, PartidaQuoridor ngamo) {
            super(father, pro);
            responses = new ArrayList<>();
            informCounter = 0;
            salidaPartida = new ACLMessage(ACLMessage.PROPOSE);
            salidaPartida.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
            salidaPartida.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
            salidaPartida.setOntology(juegoQuoridor.OntologiaQuoridor.ONTOLOGY_NAME);
            gamo = ngamo;
        }

        /**
         * Overrided method for catch al accepted propses
         *
         * @param pro Message received
         * @param accept Vector of ACLMessage to response-
         */
        @Override
        protected void handlePropose(ACLMessage pro, Vector accept) {
            responses.add(pro);
            System.out.println("Recieved a propose from: " + pro.getSender().getName());
        }

        /**
         * Overriden method, it has to handle all received responses.
         *
         * @param res vector with all the respnses received
         * @param acc vector with all the ACLMessages to send
         */
        @Override
        protected void handleAllResponses(Vector res, Vector acc) {
            if (responses.size() < 2) {
                System.out.println("Not enought players");
                for (int i = 0; i < responses.size(); i++) {
                    responses.set(i, responses.get(i).createReply());
                    responses.get(i).setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acc.add(responses.get(i));
                }
                responses.clear();

            } else if (responses.size() < 4 && responses.size() >= 2) {
                System.out.println(this.myAgent.getName() + " Can start a 2 players game");

                try {
                    
                    ACLMessage msg1 = responses.get(0);
                    ACLMessage msg2 = responses.get(1);

                    juegosTablero.elementos.Ficha token1 = new juegosTablero.elementos.Ficha(OntologiaQuoridor.COLOR_FICHA_1); //new juegosTablero.elementos.Ficha(__BLUE__);
                    juegosTablero.elementos.Ficha token2 = new juegosTablero.elementos.Ficha(OntologiaQuoridor.COLOR_FICHA_2);

                    gamo.setNumJugadores(2);
                    games.add(gamo);

                    gamo.setJugador(0, new juegosTablero.elementos.Jugador(msg1.getSender(), token1));
                    gamo.setJugador(1, new juegosTablero.elementos.Jugador(msg2.getSender(), token2));

                    msg1 = msg1.createReply();
                    msg2 = msg2.createReply();

                    msg1.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                    msg2.setLanguage(FIPANames.ContentLanguage.FIPA_SL);

                    msg1.setOntology(juegoQuoridor.OntologiaQuoridor.ONTOLOGY_NAME);
                    msg2.setOntology(juegoQuoridor.OntologiaQuoridor.ONTOLOGY_NAME);
                    
       
                    juegoQuoridor.elementos.FichaEntregada ficha1 = new juegoQuoridor.elementos.FichaEntregada(token1, OntologiaQuoridor.JUGADOR1);
                    juegoQuoridor.elementos.FichaEntregada ficha2 = new juegoQuoridor.elementos.FichaEntregada(token2, OntologiaQuoridor.JUGADOR2);

                    cm.fillContent(msg1, ficha1);
                    cm.fillContent(msg2, ficha2);

                    msg1.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    msg2.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

                    acc.add(msg1);
                    acc.add(msg2);

                    salidaPartida.addReceiver(responses.get(0).getSender());
                    salidaPartida.addReceiver(responses.get(1).getSender());

                } catch (Codec.CodecException | OntologyException exc) {
                    exc.printStackTrace();
                    System.err.println("ExcepciÃ³n no controlada");
                }
                for (int i = 2; i < responses.size(); i++) {
                    responses.set(i, responses.get(i).createReply());
                    responses.get(i).setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acc.add(responses.get(i));
                }

            } else if (responses.size() >= 4) {

                try {
                    System.out.println(this.myAgent.getName() + "Can start a 4 players game");

                    ACLMessage msg1 = responses.get(0);
                    ACLMessage msg2 = responses.get(1);
                    ACLMessage msg3 = responses.get(2);
                    ACLMessage msg4 = responses.get(3);

                    juegosTablero.elementos.Ficha token1 = new juegosTablero.elementos.Ficha(juegoQuoridor.OntologiaQuoridor.COLOR_FICHA_1);
                    juegosTablero.elementos.Ficha token2 = new juegosTablero.elementos.Ficha(juegoQuoridor.OntologiaQuoridor.COLOR_FICHA_2);
                    juegosTablero.elementos.Ficha token3 = new juegosTablero.elementos.Ficha(juegoQuoridor.OntologiaQuoridor.COLOR_FICHA_3);
                    juegosTablero.elementos.Ficha token4 = new juegosTablero.elementos.Ficha(juegoQuoridor.OntologiaQuoridor.COLOR_FICHA_4);

                    gamo.setNumJugadores(4);
                    games.add(gamo);
                    gamo.setJugador(0, new juegosTablero.elementos.Jugador(msg1.getSender(), token1));
                    gamo.setJugador(1, new juegosTablero.elementos.Jugador(msg2.getSender(), token2));
                    gamo.setJugador(2, new juegosTablero.elementos.Jugador(msg3.getSender(), token3));
                    gamo.setJugador(3, new juegosTablero.elementos.Jugador(msg4.getSender(), token4));

                    msg1 = msg1.createReply();
                    msg2 = msg2.createReply();
                    msg3 = msg3.createReply();
                    msg4 = msg4.createReply();

                    msg1.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                    msg2.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                    msg3.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                    msg4.setLanguage(FIPANames.ContentLanguage.FIPA_SL);

                    msg1.setOntology(juegoQuoridor.OntologiaQuoridor.ONTOLOGY_NAME);
                    msg2.setOntology(juegoQuoridor.OntologiaQuoridor.ONTOLOGY_NAME);
                    msg3.setOntology(juegoQuoridor.OntologiaQuoridor.ONTOLOGY_NAME);
                    msg4.setOntology(juegoQuoridor.OntologiaQuoridor.ONTOLOGY_NAME);

                    msg1.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    msg2.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    msg3.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    msg4.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

                    juegoQuoridor.elementos.FichaEntregada ficha1 = new juegoQuoridor.elementos.FichaEntregada(token1, OntologiaQuoridor.JUGADOR1);
                    juegoQuoridor.elementos.FichaEntregada ficha2 = new juegoQuoridor.elementos.FichaEntregada(token2, OntologiaQuoridor.JUGADOR2);
                    juegoQuoridor.elementos.FichaEntregada ficha3 = new juegoQuoridor.elementos.FichaEntregada(token3, OntologiaQuoridor.JUGADOR3);
                    juegoQuoridor.elementos.FichaEntregada ficha4 = new juegoQuoridor.elementos.FichaEntregada(token4, OntologiaQuoridor.JUGADOR4);

                    //FichaEntegada ficha1 = new FichaEntregada(token1);
                    cm.fillContent(msg1, ficha1);
                    cm.fillContent(msg2, ficha2);
                    cm.fillContent(msg3, ficha3);
                    cm.fillContent(msg4, ficha4);

                    acc.add(msg1);
                    acc.add(msg2);
                    acc.add(msg3);
                    acc.add(msg4);

                    salidaPartida.addReceiver(responses.get(0).getSender());
                    salidaPartida.addReceiver(responses.get(1).getSender());
                    salidaPartida.addReceiver(responses.get(2).getSender());
                    salidaPartida.addReceiver(responses.get(3).getSender());

                    for (int i = 4; i < responses.size(); i++) {
                        responses.set(i, responses.get(i).createReply());
                        responses.get(i).setPerformative(ACLMessage.REJECT_PROPOSAL);
                        acc.add(responses.get(i));
                    }
                } catch (Exception exc) {
                    exc.printStackTrace();
                    System.err.println("ExcepciÃ³n no controlada");
                }
            } else {
                boardDriver.showStart();
            }

        }

        @Override
        protected void handleInform(ACLMessage msg) {
            informCounter++;
            if (informCounter == 1) {
                
                codec = new SLCodec();
                cm = new ContentManager();
                cm.registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL);

                try {
                    cm.registerOntology(OntologiaQuoridor.getInstance(), OntologiaQuoridor.ONTOLOGY_NAME);
                } catch (BeanOntologyException ex) {
                    Logger.getLogger(BoardAgent.class.getName()).log(Level.SEVERE, null, ex);
                }

                juegosTablero.elementos.Posicion pos = new juegosTablero.elementos.Posicion(juegoQuoridor.OntologiaQuoridor.POSICION_NULA, juegoQuoridor.OntologiaQuoridor.POSICION_NULA);
                //juegoQuoridor.elementos.Muro m1 = new Muro();
                juegoQuoridor.elementos.Movimiento mov = new Movimiento(null, pos);
                Action act = new Action(myAgent.getAID(), new juegoQuoridor.elementos.JugarPartida(gamo.getPartida(), mov, gamo.getJugador(0)));
                try {
                    cm.fillContent(salidaPartida, act);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(BoardAgent.class.getName()).log(Level.SEVERE, null, ex);
                }

                myAgent.addBehaviour(new Partida(myAgent, salidaPartida, gamo));
                myAgent.addBehaviour(new suscribe(myAgent,SubscriptionResponder.createMessageTemplate(ACLMessage.SUBSCRIBE), gamo));
                
            }
        }
    }
// </editor-fold>
    
// <editor-fold defaultstate="collapsed" desc="Clase Propose para los turnos">

    /**
     *  Clase que implementa el metodo propose en nuestro tablero, se inicia cada vez que creamos una partida nueva y se le llama recursivamente cada vez que un jugador nos contesta con el movimiento que ha realizado (ACCEPT_PROPOSAL)
     *  @author Ratones y mazmorras
     *  @param father es el agente que ha llamado a la clase partida
     *  @param nexTurn es el ACLmensaje que lleva el siguiente turno de la partida (con el agentAction jugarpartida)
     *  @param idGame es el id del juego que se esta jugando para realizar el movimiento (es un PartidaQuoridor para facilitar la entrada a la partida deseada)
     * 
     */
    protected class Partida extends ProposeInitiator {

        Agent father;
        ACLMessage nextTurn;
        PartidaQuoridor idGame;

        public Partida(Agent a, ACLMessage msg, PartidaQuoridor idGame) {
            super(a, msg);
            father = a;
            nextTurn = new ACLMessage(ACLMessage.PROPOSE);
            nextTurn.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
            nextTurn.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
            nextTurn.setOntology(juegoQuoridor.OntologiaQuoridor.ONTOLOGY_NAME);
            for (Iterator iterator = msg.getAllReceiver(); iterator.hasNext();) {
                AID r = (AID) iterator.next();
                nextTurn.addReceiver(r);
            }
            this.idGame = idGame;

        }

        @Override
        protected void handleAcceptProposal(ACLMessage accept_proposal) {
            super.handleAcceptProposal(accept_proposal); //To change body of generated methods, choose Tools | Templates.

            try {
                juegoQuoridor.elementos.JugarPartida jp = new juegoQuoridor.elementos.JugarPartida();
                //Saco el movimiento que me pasan y lo guardo en el historial
                juegoQuoridor.elementos.MovimientoRealizado mr = (juegoQuoridor.elementos.MovimientoRealizado) cm.extractContent(accept_proposal);
                //juegoQuoridor.elementos.MovimientoRealizado mr = (juegoQuoridor.elementos.MovimientoRealizado) Activo.getAction();
                idGame.addMoviento(mr.getMovimiento());
                jugadorActual = (jugadorActual + 1) % idGame.getNumJugadores();
                jp.setJugadorActivo(idGame.getJugador(jugadorActual));
                jp.setMovimientoAnterior(mr.getMovimiento());
                jp.setPartida(idGame.getPartida());
                
                cm.fillContent(nextTurn, new Action(this.myAgent.getAID(), jp));

                if (!idGame.finDeJuego(mr.getJugador().getFicha(), mr.getMovimiento())) //si el movimiento realizado no es condicion de fin de juego generamos el proximo turno 
                {
                    father.addBehaviour(new Partida(father, nextTurn, this.idGame));
                    count++;
                } else {
                    /*
                        si es fin de juego debemos lanzar el metodo subcribe para notificar el ganador de la partida
                        ademas se podria añadir el inicio del metodo pinta de la partida para la visualizacion de esta.
                     */

                    Pinta draw = new Pinta(idGame);
                    draw.start();
                    for(Subscription s:idGame.lagentuza){
                        ACLMessage inf = new ACLMessage(ACLMessage.INFORM);
                        inf.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                        inf.setOntology(juegoQuoridor.OntologiaQuoridor.ONTOLOGY_NAME);
                        juegosTablero.elementos.GanadorPartida gp = new juegosTablero.elementos.GanadorPartida();
                        gp.setJugador(mr.getJugador());
                        gp.setPartida(idGame.getPartida());
                        cm.fillContent(inf, gp);

                        s.notify(inf);
                    }
                     System.out.println("La partida con id "+idGame.getPartida().getIdPartida()+" ha finalizado");
                    
                }
                
            } catch (Codec.CodecException | OntologyException ex) {
                ex.printStackTrace();
            }

        }

        @Override
        protected void handleRejectProposal(ACLMessage reject_proposal) {
            super.handleRejectProposal(reject_proposal); //To change body of generated methods, choose Tools | Templates.
        }

    }
    // </editor-fold>

//<editor-fold defaultstate="collapsed" desc="Clase Suscribe">
    /**
     *  Clase que implementa el metodo subscribe en nuestro tablero, se lanza cuando finalize una partida y este avisa del ganador de la partida a los jugadores subscritos
     *  @author Ratones y mazmorras
     *  @param idGame es el id del juego que ha finalizado para saber ha quienes debemos enviarle el mensaje de fin de juego
     */
    private class suscribe extends SubscriptionResponder {
        PartidaQuoridor idGame;
        
        public suscribe(Agent a, MessageTemplate mt, PartidaQuoridor idGame) {
            super(a, mt);
            this.idGame = idGame;
        }

        @Override
        protected ACLMessage handleSubscription(ACLMessage subscription) throws NotUnderstoodException, RefuseException {
            Subscription tmp;
            System.out.println("Mensaje de suscripción recibido de " + subscription.getSender().getName());
            if (compruebaMensaje(subscription.getSender())) {
                tmp = this.createSubscription(subscription);
                
                idGame.gestor.register(tmp);

                ACLMessage agree = subscription.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;

            }
             //Rechaza la propuesta y la envía
                ACLMessage refuse = subscription.createReply();
                refuse.setPerformative(ACLMessage.REFUSE);
                return refuse;
        }        
        
        
        private boolean compruebaMensaje(AID propuesta) {
            for (int i = 0; i < idGame.getNumJugadores(); i++) {
                //Si el mensaje de suscripción viene de algún jugador de la partida, lo aceptamos, si no, no.
                if (true){//idGame.getJugador(i).getAgenteJugador() == propuesta) {
                    return true;
                }
            }
            return false;
        }
    }

    //</editor-fold>
    
//<editor-fold defaultstate="collapsed" desc="Clase para pintar el tablero"> 
    /**
     *  Clase que implementa el metodo para pintar una partida ya jugada.
     *  @author Ratones y mazmorras
     *  @param game es el juego el cual ya ha finalizado y vamos ha visualizar.
     */
    class Pinta extends Thread{
    
         PartidaQuoridor game;
        public Pinta(PartidaQuoridor game)
        {
            super();
            this.game = game; 
        }
        
        @Override
         public void run() {

             ArrayList<Movimiento> list = game.getListaMovimientos();
             Integer funcionaHostia = game.getNumJugadores();
             if (funcionaHostia > 1) {
                TableroBack myTableroBack = new TableroBack(game.getNumJugadores().intValue(), true);
                while (list.size() != 0) {

                    try {

                        Movimiento mov = list.get(0);
                        if (mov.getElementoJuego() instanceof Ficha) {
                            //System.out.print(mov.getPosicion().getCoorX() + " " + mov.getPosicion().getCoorY() + " FICHA: ");
                            myTableroBack.Turno(TableroBack.__MUEVE__, mov.getPosicion(), true);

                        } else {
                            //System.out.print(mov.getPosicion().getCoorX() + " " + mov.getPosicion().getCoorY() + " MURO: ");
                            if (((Muro) (mov.getElementoJuego())).getAlineacion().equals(juegoQuoridor.OntologiaQuoridor.ALINEACION_HORIZONTAL)) {
                                myTableroBack.Turno(TableroBack.__MURO__, mov.getPosicion(), TableroBack.__HORIZONTAL__);
                            } else {
                                myTableroBack.Turno(TableroBack.__MURO__, mov.getPosicion(), TableroBack.__VERTICAL__);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error with movement, bad construction: " + e.toString());
                    }
                    list.remove(0);

                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        System.err.println(getAID() + ": Internal Error.");
                    }
                }
                myTableroBack = null; 
                boardDriver.showStart();
            }
         }
     }
    //</editor-fold>
     
}
