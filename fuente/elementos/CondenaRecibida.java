/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dilemaPrisionero.elementos;

import jade.content.Concept;

/**
 *
 * @author pedroj
 */
public class CondenaRecibida implements Concept {
    
    private String condena;

    public CondenaRecibida() {
    }

    public CondenaRecibida(String condena) {
        this.condena = condena;
    }

    public String getCondena() {
        return condena;
    }

    public void setCondena(String condena) {
        this.condena = condena;
    }

    @Override
    public String toString() {
        return "CondenaRecibida{" + "condena=" + condena + '}';
    }
}
