/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

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
    private int ronda;
    private int numResultados;

    public RegistroPartida(DilemaPrisionero juego, String idPartida) {
        this.juego = juego;
        this.clasificacion = null;
        this.partidaGUI = new PartidaJFrame(idPartida);
        this.ronda = 0;
        this.numResultados = 0;
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
        if ( ronda >= juego.getRondas()) {
            if ( Math.random()*100 < juego.getProbabilidadFin() ) {
                return true;
            }
        }
        return false;
    }
}
