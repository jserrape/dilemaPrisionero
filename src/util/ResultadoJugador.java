/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import juegos.elementos.Jugador;

/**
 *
 * @author pedroj
 * 
 * Almacena la información de un jugador en una partida 
 */
public class ResultadoJugador implements Comparable<ResultadoJugador> {
    
    private Jugador jugador;
    private double tiempoCondena;

    public ResultadoJugador(Jugador jugador, double tiempoCondena) {
        this.jugador = jugador;
        this.tiempoCondena = tiempoCondena;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public double getTiempoCondena() {
        return tiempoCondena;
    }

    public void setTiempoCondena(double tiempoCondena) {
        this.tiempoCondena = tiempoCondena;
    }

    @Override
    public String toString() {
        return jugador.getNombre() + "\t" + tiempoCondena + "\n";
    }

    @Override
    public int compareTo(ResultadoJugador elm) {
        // Ordenado de menor a mayor por el tiempo de condena
        return (int) (tiempoCondena - elm.getTiempoCondena());
    }   
}
