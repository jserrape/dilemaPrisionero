/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dilemaPrisionero.elementos;

import jade.content.Predicate;
import juegos.elementos.Partida;

/**
 *
 * @author pedroj
 */
public class ResultadoJugada implements Predicate {
    
    private Partida partida;
    private CondenaRecibida condena;

    public ResultadoJugada() {
    }

    public ResultadoJugada(Partida partida, CondenaRecibida condena) {
        this.partida = partida;
        this.condena = condena;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public CondenaRecibida getCondena() {
        return condena;
    }

    public void setCondena(CondenaRecibida condena) {
        this.condena = condena;
    }

    @Override
    public String toString() {
        return "ResultadoJugada{" + "partida=" + partida + ", condena=" 
                + condena + '}';
    }
}
