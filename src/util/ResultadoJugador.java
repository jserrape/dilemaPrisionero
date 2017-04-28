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
    private int tiempoCondena;
    private boolean activo;

    public ResultadoJugador(Jugador jugador, int tiempoCondena) {
        this.jugador = jugador;
        this.tiempoCondena = tiempoCondena;
        this.activo = false;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public int getTiempoCondena() {
        return tiempoCondena;
    }

    public void setTiempoCondena(int tiempoCondena) {
        this.tiempoCondena = tiempoCondena;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isActivo() {
        return activo;
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
