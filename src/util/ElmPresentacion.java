/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.List;

/**
 *
 * @author pedroj
 */
public class ElmPresentacion {
    private String idPartida;
    private int ronda;
    private boolean finPartida;
    private List<String> agentesJugador;
    private List<String> nombresJugador;
    private List<String> condenas;

    public ElmPresentacion(String idPartida, int ronda, List<String> agentesJugador, 
            List<String> nombresJugador, List<String> condenas) {
        this.idPartida = idPartida;
        this.ronda = ronda;
        this.finPartida = false;
        this.agentesJugador = agentesJugador;
        this.nombresJugador = nombresJugador;
        this.condenas = condenas;
    }

    public String getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(String idPartida) {
        this.idPartida = idPartida;
    }

    public int getRonda() {
        return ronda;
    }

    public void setRonda(int ronda) {
        this.ronda = ronda;
    }

    public boolean isFinPartida() {
        return finPartida;
    }

    public void setFinPartida(boolean finPartida) {
        this.finPartida = finPartida;
    }
    
    public List<String> getAgentesJugador() {
        return agentesJugador;
    }

    public void setAgentesJugador(List<String> agentesJugador) {
        this.agentesJugador = agentesJugador;
    }

    public List<String> getNombresJugador() {
        return nombresJugador;
    }

    public void setNombresJugador(List<String> nombresJugador) {
        this.nombresJugador = nombresJugador;
    }

    public List<String> getCondenas() {
        return condenas;
    }

    public void setCondenas(List<String> condenas) {
        this.condenas = condenas;
    }
}
