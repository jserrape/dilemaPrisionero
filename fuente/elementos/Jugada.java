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
public class Jugada implements Concept {
    
    private String respuesta;

    public Jugada() {
    }

    public Jugada(String respuesta) {
        this.respuesta = respuesta;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    @Override
    public String toString() {
        return "Jugada{" + "respuesta=" + respuesta + '}';
    }
}
