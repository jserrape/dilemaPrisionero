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
 */
public class AcumuladoJugador implements Comparable<AcumuladoJugador>{
    private Jugador jugador;
    private int totalCondena;
    private int partidasJugadas;

    public AcumuladoJugador(Jugador jugador, int totalCondena, int partidasJugadas) {
        this.jugador = jugador;
        this.totalCondena = totalCondena;
        this.partidasJugadas = partidasJugadas;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public int getTotalCondena() {
        return totalCondena;
    }

    public void setTotalCondena(int totalCondena) {
        this.totalCondena = totalCondena;
    }

    public int getPartidasJugadas() {
        return partidasJugadas;
    }

    public void setPartidasJugadas(int partidasJugadas) {
        this.partidasJugadas = partidasJugadas;
    }

    @Override
    public String toString() {
        return "AcumuladoJugador{" + "jugador=" + jugador + 
                ", totalCondena=" + totalCondena + 
                ", partidasJugadas=" + partidasJugadas + '}';
    }

    @Override
    public int compareTo(AcumuladoJugador elm) {
        if ( (partidasJugadas - elm.getPartidasJugadas()) == 0 ) {
            return totalCondena - elm.getTotalCondena();
        } 
        
        return (partidasJugadas - elm.getPartidasJugadas()) * (-1);
    }
    
}
