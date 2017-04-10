/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dilemaPrisionero.elementos;

import jade.content.AgentAction;
import juegos.elementos.Partida;

/**
 *
 * @author pedroj
 */
public class ProponerPartida implements AgentAction {
    
    private Partida partida;
    private DilemaPrisionero condiciones;

    public ProponerPartida() {
    }

    public ProponerPartida(Partida partida, DilemaPrisionero condiciones) {
        this.partida = partida;
        this.condiciones = condiciones;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public DilemaPrisionero getCondiciones() {
        return condiciones;
    }

    public void setCondiciones(DilemaPrisionero condiciones) {
        this.condiciones = condiciones;
    }

    @Override
    public String toString() {
        return "ProponerPartida{" + "partida=" + partida + ", condiciones=" 
                + condiciones + '}';
    }
}
