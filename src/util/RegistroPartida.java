/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import agentes.AgentePolicia;
import dilemaPrisionero.elementos.DilemaPrisionero;
import gui.PartidaJFrame;
import java.util.List;

/**
 *
 * @author pedroj
 */
public class RegistroPartida {
    private DilemaPrisionero juego;
    private List<ResultadoJugador> clasificacion;
    private final PartidaJFrame partidaGUI;
    private int ronda; // ronda de juego
    private int numResultados; // enfrentamientos completados de la ronda
    private boolean finPartida; 
    private boolean cancelada; 
    private String error; // causas de finalización de la partida

    public RegistroPartida(DilemaPrisionero juego, String idPartida, AgentePolicia agent) {
        this.juego = juego;
        this.clasificacion = null;
        this.partidaGUI = new PartidaJFrame(idPartida, agent);
        this.ronda = 0;
        this.numResultados = 0;
        this.finPartida = false;
        this.cancelada = false;
    }

    public DilemaPrisionero getJuego() {
        return juego;
    }

    public void setJuego(DilemaPrisionero juego) {
        this.juego = juego;
    }

    public List<ResultadoJugador> getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(List<ResultadoJugador> clasificacion) {
        this.clasificacion = clasificacion;
    }

    public PartidaJFrame getPartidaGUI() {
        return partidaGUI;
    }

    public int getRonda() {
        return ronda;
    }

    public void setRonda(int ronda) {
        this.ronda = ronda;
    }

    public int getNumEnfrentamientos() {
        return numResultados;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isCancelada() {
        return cancelada;
    }

    public void setCancelada(boolean cancelada) {
        this.cancelada = cancelada;
        this.finPartida = cancelada;
    }
   
    public void aumentarRonda() {
        ronda++;
        numResultados = 0;
    }
    
    public void aumentarResultados() {
        numResultados++;
    }
    
    public boolean finRonda() {
        return numResultados == (clasificacion.size() / 2);
    }
    
    public boolean finPartida() {
        if ( (ronda >= juego.getRondas()) && !finPartida) {
            if ( Math.random()*100 < juego.getProbabilidadFin() ) {
                finPartida = true;
            }
        }
        return finPartida;
    }
}
