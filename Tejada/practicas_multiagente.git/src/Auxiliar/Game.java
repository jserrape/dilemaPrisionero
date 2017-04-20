package Auxiliar;

import Backend.TableroBack;
import jade.core.*; 
import jade.domain.*;
import javafx.util.Pair;
import juegosTablero.elementos.Posicion;

/**
 * Class to store the token, game, and AID of the board
 * @author cyane
 * @param token token to use in the game
 * @param game game that we are playing
 * @param AIDboard AID of the board Agent
 */
public class Game {
    
    private juegosTablero.elementos.Ficha token;
    private juegosTablero.elementos.Partida game;
    private AID boardAgent;
    private TableroBack board; 
    private Integer walls; 
    private Posicion start;
    private Posicion end;
    
    public Game(juegosTablero.elementos.Ficha ntoken, juegosTablero.elementos.Partida ngame, AID nboardAgent){
        
        token = ntoken;
        game = ngame; 
        boardAgent = nboardAgent;
        board = new TableroBack(game.getNumeroJugadores(), false);
        
        if(ngame.getNumeroJugadores() == 2)
        {
            walls = 10;
        }else{
            walls = 5;
        }
    }
    
    public Game(juegosTablero.elementos.Ficha ntoken, AID nboardAgent){
        
        token = ntoken;
        game = null;
        boardAgent = nboardAgent;
    }

    /**
     * @return the token
     */
    public juegosTablero.elementos.Ficha getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(juegosTablero.elementos.Ficha token) {
        this.token = token;
    }

    /**
     * @return the game
     */
    public juegosTablero.elementos.Partida getGame() {
        return game;
    }

    /**
     * @param game the game to set
     */
    public void setGame(juegosTablero.elementos.Partida game) {
        this.game = game;
        
        if(game.getNumeroJugadores() == 2)
        {
            setWalls((Integer) 10);
        }else{
            setWalls((Integer) 5);
        }
    }

    /**
     * @return the boardAgent
     */
    public AID getBoardAgent() {
        return boardAgent;
    }

    /**
     * @param boardAgent the boardAgent to set
     */
    public void setBoardAgent(AID boardAgent) {
        this.boardAgent = boardAgent;
    }
    
    public String getIdPartida()
    {
        return game.getIdPartida();
    }
    
    public TableroBack getBoard()
    {
        return board; 
    }
    
    public void putWall(){
        setWalls((Integer) (getWalls() - 1));
    }

    /**
     * @return the walls
     */
    public Integer getWalls() {
        return walls;
    }

    /**
     * @param walls the walls to set
     */
    public void setWalls(Integer walls) {
        this.walls = walls;
    }

    /**
     * @return the start
     */
    public Posicion getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(Posicion start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public Posicion getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(Posicion end) {
        this.end = end;
    }

    /**
     * @return the actual
     */
    
}
