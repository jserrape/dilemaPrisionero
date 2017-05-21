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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import juegos.elementos.Jugador;
import juegos.elementos.Partida;

/**
 *
 * @author Xenahort
 */
public class ContenedorPartida {

    private final DilemaPrisionero condiciones;
    private final String id;

    private final String miNombre;

    private int numeroTurnos;

    private String miRespuestaAnterior;

    private int condenaAcumulada;

    private final ArrayList<String> jugadasFichero;

    public ContenedorPartida(String _id, DilemaPrisionero _condiciones, String _miNombre) {
        this.condenaAcumulada = 0;
        this.id = _id;
        this.condiciones = _condiciones;
        this.miRespuestaAnterior = "";
        this.miNombre = _miNombre;

        this.numeroTurnos = 0;

        this.jugadasFichero = new ArrayList();
    }

    public String getCondena() {
        return Integer.toString(condenaAcumulada);
    }

    public void nuevaJugadaOponente(ResultadoJugada resultado) {

        if (this.miRespuestaAnterior.equals(OntologiaDilemaPrisionero.HABLAR) && resultado.getCondenaRecibida() == condiciones.getTiempoCondena().getCastigo()) {
            this.jugadasFichero.add(OntologiaDilemaPrisionero.HABLAR);
        }

        if (this.miRespuestaAnterior.equals(OntologiaDilemaPrisionero.HABLAR) && resultado.getCondenaRecibida() == condiciones.getTiempoCondena().getTentacion()) {
            this.jugadasFichero.add(OntologiaDilemaPrisionero.CALLAR);
        }

        if (this.miRespuestaAnterior.equals(OntologiaDilemaPrisionero.CALLAR) && resultado.getCondenaRecibida() == condiciones.getTiempoCondena().getPrimo()) {
            this.jugadasFichero.add(OntologiaDilemaPrisionero.HABLAR);
        }
        if (this.miRespuestaAnterior.equals(OntologiaDilemaPrisionero.CALLAR) && resultado.getCondenaRecibida() == condiciones.getTiempoCondena().getRecompensa()) {
            this.jugadasFichero.add(OntologiaDilemaPrisionero.CALLAR);
        }
        ++numeroTurnos;
        this.condenaAcumulada += resultado.getCondenaRecibida();
    }

    public void setRespuestaAnterior(String respA) {
        this.miRespuestaAnterior = respA;
    }

    public void insertarRival(List jugadores) {
        Iterator it = jugadores.iterator();
        Jugador jugador = (Jugador) it.next();
        Jugador jugador2 = (Jugador) it.next();
        if (jugador.getNombre().equals(this.miNombre)) {
            jugadasFichero.add(jugador2.getNombre());
        } else {
            jugadasFichero.add(jugador.getNombre());
        }
    }

    public String decidirAccion() {
        int numero = ((int) (Math.random() * 1000)) % 2;
        if (numero == 1) {
            setRespuestaAnterior(OntologiaDilemaPrisionero.CALLAR);
            jugadasFichero.add(OntologiaDilemaPrisionero.CALLAR);
            return OntologiaDilemaPrisionero.CALLAR;
        } else {
            setRespuestaAnterior(OntologiaDilemaPrisionero.HABLAR);
            jugadasFichero.add(OntologiaDilemaPrisionero.HABLAR);
            return OntologiaDilemaPrisionero.HABLAR;
        }
    }

    public void crearFichero() {
        java.util.Date utilDate = new java.util.Date(); //fecha actual
        long lnMilisegundos = utilDate.getTime();
        java.sql.Date sqlDate = new java.sql.Date(lnMilisegundos);
        java.sql.Time sqlTime = new java.sql.Time(lnMilisegundos);
        String nombreFichero = this.miNombre + "--" + sqlDate + "--" + sqlTime + "--" + id;
        nombreFichero = nombreFichero.replace(":", "-");
        try {
            try (BufferedWriter ficheroSalida = new BufferedWriter(new FileWriter(new File("partidasAnteriores/" + nombreFichero + ".txt")))) {
                ficheroSalida.write(Integer.toString(numeroTurnos));
                ficheroSalida.newLine();
                for (int i = 0; i < jugadasFichero.size(); i++) {
                    ficheroSalida.write(jugadasFichero.get(i));
                    ficheroSalida.newLine();
                }
            }
        } catch (IOException errorDeFichero) {
            System.out.println("Ha habido problemas: " + errorDeFichero.getMessage());
        }
    }

    public void mostrarCondenaFinal() {
        System.out.println("Condena final del agente " + this.miNombre + " es de " + this.condenaAcumulada);
    }
}
