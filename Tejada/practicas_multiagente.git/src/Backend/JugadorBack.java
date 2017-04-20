/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Backend;

import juegosTablero.elementos.Jugador;
import juegosTablero.elementos.Posicion;

/**
 *
 * @author fjlen
 */

public class JugadorBack {

    Jugador player;
    Posicion pActual;
    
    /**
     * Funcion para obtener el color del jugador.
     * @return 
     */
    public Jugador getPlayer() {
        return player; 
    }
    
    /**
     * Funcion para obtener la posicion del jugador.
     * @return 
     */
    public Posicion getPosicion() {
        return pActual;
    }
    
    /**
     * Funcion para cambiar el jugador
     * @param nPlayer
     */
    public void setPLayer(Jugador nPlayer) {
        this.player = nPlayer;
    }
    /**
     * Funcion para cambiar la posicion del jugador.
     * @param p 
     */
    public void setPosicion(Posicion p) {
        this.pActual = p;
    }

}
