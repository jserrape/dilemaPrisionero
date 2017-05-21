/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import dilemaPrisionero.OntologiaDilemaPrisionero;
import dilemaPrisionero.elementos.DilemaPrisionero;
import dilemaPrisionero.elementos.ResultadoJugada;
import jade.util.leap.List;
import java.util.ArrayList;
import java.util.Iterator;
import juegos.elementos.Jugador;
import juegos.elementos.Partida;

/**
 *
 * @author Xenahort
 */
public class ContenedorPartida {

    private final Partida partida;
    private final DilemaPrisionero condiciones;
    private final String id;
    private String nombreRival;
    private final String miNombre;

    private String miRespuestaAnterior;

    private int condenaAcumulada;

    private final ArrayList<String> jugadasOponente;

    public ContenedorPartida(Partida p, String _id, DilemaPrisionero _condiciones, String _miNombre) {
        this.partida = p;
        this.condenaAcumulada = 0;
        this.id = _id;
        this.condiciones = _condiciones;
        this.miRespuestaAnterior = "";
        this.nombreRival = "";
        this.miNombre = _miNombre;

        this.jugadasOponente = new ArrayList();
    }

    public String getCondena() {
        return Integer.toString(condenaAcumulada);
    }

    public void nuevaJugadaOponente(ResultadoJugada resultado) {
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

        this.condenaAcumulada += resultado.getCondenaRecibida();
    }

    public void setRespuestaAnterior(String respA) {
        this.miRespuestaAnterior = respA;
    }

    public void insertarRival(List jugadores) {
        if ("".equals(this.nombreRival)) {
            Iterator it = jugadores.iterator();
            Jugador jugador = (Jugador) it.next();
            Jugador jugador2 = (Jugador) it.next();
            if (jugador.getNombre().equals(this.miNombre)) {
                nombreRival = jugador2.getNombre();
            } else {
                nombreRival = jugador.getNombre();
            }
            System.out.println("Soy " + miNombre + ", juego contra " + this.nombreRival);
        }
    }

    public String decidirAccion() {
        int numero = ((int) (Math.random() * 1000)) % 2;
        if (numero == 1) {
            return OntologiaDilemaPrisionero.CALLAR;
        } else {
            return OntologiaDilemaPrisionero.HABLAR;
        }
    }

}
