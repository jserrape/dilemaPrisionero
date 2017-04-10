/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dilemaPrisionero.elementos;

import jade.content.Predicate;
import juegos.elementos.Jugador;
import juegos.elementos.Partida;

/**
 *
 * @author pedroj
 */
public class JugadaEntregada implements Predicate {
    
    private Partida partida;
    private Jugador jugador;
    private Jugada respuesta;

    public JugadaEntregada() {
    }

    public JugadaEntregada(Partida partida, Jugador jugador, Jugada respuesta) {
        this.partida = partida;
        this.jugador = jugador;
        this.respuesta = respuesta;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public Jugada getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(Jugada respuesta) {
        this.respuesta = respuesta;
    }

    @Override
    public String toString() {
        return "JugadaEntregada{" + "partida=" + partida + ", jugador=" 
                + jugador + ", respuesta=" + respuesta + '}';
    }
}
