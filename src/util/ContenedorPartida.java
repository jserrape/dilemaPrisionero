/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import dilemaPrisionero.OntologiaDilemaPrisionero;
import dilemaPrisionero.elementos.DilemaPrisionero;
import dilemaPrisionero.elementos.ResultadoJugada;
import java.util.ArrayList;
import juegos.elementos.Partida;

/**
 *
 * @author Xenahort
 */
public class ContenedorPartida {

    private Partida partida;
    private DilemaPrisionero condiciones;
    private String id;

    private String miRespuestaAnterior;

    int condenaAcumulada;

    private ArrayList<String> jugadasOponente;

    public ContenedorPartida(Partida p, String _id, DilemaPrisionero _condiciones) {
        this.partida = p;
        this.condenaAcumulada = 0;
        this.id = _id;
        this.condiciones = _condiciones;
        this.miRespuestaAnterior = "";

        this.jugadasOponente = new ArrayList();
    }
    
    public String getCondena(){
        return Integer.toString(condenaAcumulada);
    }

    public void nuevaJugada(ResultadoJugada resultado) {
        if (this.miRespuestaAnterior.equals(OntologiaDilemaPrisionero.HABLAR) && resultado.getCondenaRecibida() == condiciones.getTiempoCondena().getCastigo()) {
            this.jugadasOponente.add(OntologiaDilemaPrisionero.HABLAR);
        }
        
        if (this.miRespuestaAnterior.equals(OntologiaDilemaPrisionero.HABLAR) && resultado.getCondenaRecibida() == condiciones.getTiempoCondena().getTentacion()) {
            this.jugadasOponente.add(OntologiaDilemaPrisionero.CALLAR);
        }
        
        if (this.miRespuestaAnterior.equals(OntologiaDilemaPrisionero.CALLAR) && resultado.getCondenaRecibida() == condiciones.getTiempoCondena().getPrimo()) {
            this.jugadasOponente.add(OntologiaDilemaPrisionero.HABLAR);
        }
        if (this.miRespuestaAnterior.equals(OntologiaDilemaPrisionero.CALLAR) && resultado.getCondenaRecibida() == condiciones.getTiempoCondena().getRecompensa()) {
            this.jugadasOponente.add(OntologiaDilemaPrisionero.CALLAR);
        }
        
        this.condenaAcumulada+=resultado.getCondenaRecibida();
    }

    public void setRespuestaAnterior(String respA) {
        this.miRespuestaAnterior = respA;
    }
}
