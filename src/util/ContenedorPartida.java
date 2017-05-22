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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import juegos.elementos.Jugador;

/**
 * Objeto destinado a contener todo lo sucedido durante una ronda y generar
 * respuesta a una peticion
 *
 * @author jcsp0003
 */
public class ContenedorPartida {

    private final DilemaPrisionero condiciones;
    private final String miNombre;
    private String miRespuestaAnterior;

    private int numeroTurnos;
    private int condenaAcumulada;

    private String oponenteActual;

    private final ArrayList<String> jugadasFichero;
    private final Map<String, ArrayList<String>> rondas;

    private final ArrayList<String> mensajesPendientes;

    /**
     * Constructor parametrizado
     *
     * @param _condiciones Condenas de la partida
     * @param _miNombre Nombre del agente ladron al que pertenece el contenedor
     * @param mensajesPendientes Array con los mensajes para consola
     */
    public ContenedorPartida(DilemaPrisionero _condiciones, String _miNombre, ArrayList<String> mensajesPendientes) {
        this.condenaAcumulada = 0;
        this.condiciones = _condiciones;
        this.miRespuestaAnterior = "";
        this.miNombre = _miNombre;

        this.numeroTurnos = 0;

        this.jugadasFichero = new ArrayList();
        this.rondas = new HashMap<>();
        this.mensajesPendientes = mensajesPendientes;
    }

    /**
     * Funcion para aumentar la condena recibida y añadir a una lista la jugada
     * realizada por el oponente
     *
     * @param resultado condena recibida
     */
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

        rondas.get(this.oponenteActual).add(jugadasFichero.get(jugadasFichero.size() - 1));

        ++numeroTurnos;
        this.condenaAcumulada += resultado.getCondenaRecibida();
    }

    /**
     * Modifica la respuesta anterior de mi agente ladron
     *
     * @param respA Nueva respuesta del agente ladron
     */
    public void setRespuestaAnterior(String respA) {
        this.miRespuestaAnterior = respA;
    }

    /**
     * Lee el rivar de la nueva ronda
     *
     * @param jugadores Lista de los 2 jugadores que participan en la ronda
     */
    public void insertarRival(List jugadores) {
        Iterator it = jugadores.iterator();
        Jugador jugador = (Jugador) it.next();
        Jugador jugador2 = (Jugador) it.next();
        if (jugador.getNombre().equals(this.miNombre)) {
            jugadasFichero.add(jugador2.getNombre());
            oponenteActual = jugador2.getNombre();
        } else {
            jugadasFichero.add(jugador.getNombre());
            oponenteActual = jugador.getNombre();
        }
        if (!rondas.containsKey(oponenteActual)) {
            rondas.put(oponenteActual, new ArrayList());
        }
    }

    /**
     * Recide si hablar o callar
     *
     * @return hablar o callar
     */
    public String decidirAccion() {

        if (rondas.get(oponenteActual).isEmpty() || rondas.get(oponenteActual).size() == 1) { //Es la primera/segunda ronda contra este oponente y no tengo informacion, mejor canto
            setRespuestaAnterior(OntologiaDilemaPrisionero.HABLAR);
            jugadasFichero.add(OntologiaDilemaPrisionero.HABLAR);
            return OntologiaDilemaPrisionero.HABLAR;
        }


        int numero = ((int) (Math.random() * 1000)) % 2;
        if (numero == 1) {
            setRespuestaAnterior(OntologiaDilemaPrisionero.HABLAR);
            jugadasFichero.add(OntologiaDilemaPrisionero.HABLAR);
            return OntologiaDilemaPrisionero.HABLAR;
        } else {
            setRespuestaAnterior(OntologiaDilemaPrisionero.CALLAR);
            jugadasFichero.add(OntologiaDilemaPrisionero.CALLAR);
            return OntologiaDilemaPrisionero.CALLAR;
        }
    }

    /**
     * Crea un fichero con todo lo sucedido en la partida
     */
    public void crearFichero() {
        java.util.Date utilDate = new java.util.Date();
        long lnMilisegundos = utilDate.getTime();
        java.sql.Date sqlDate = new java.sql.Date(lnMilisegundos);
        java.sql.Time sqlTime = new java.sql.Time(lnMilisegundos);
        String nombreFichero = this.miNombre + "--" + sqlDate + "--" + sqlTime;
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

        Iterator<Map.Entry<String, ArrayList<String>>> entries = rondas.entrySet().iterator();
        String mensaje = "";
        while (entries.hasNext()) {
            Map.Entry<String, ArrayList<String>> entry = entries.next();
            mensaje += entry.getKey() + ": ";
            for (int i = 0; i < entry.getValue().size(); i++) {
                mensaje += entry.getValue().get(i) + " ";
            }
            mensaje += "\n";
        }
        this.mensajesPendientes.add(mensaje);
    }

    /**
     * Muestra la condena recibida
     */
    public void mostrarCondenaFinal() {
        System.out.println("Condena final del agente " + this.miNombre + " es de " + this.condenaAcumulada);
    }

    /**
     * @return condenaAcumulada
     */
    public String getCondena() {
        return Integer.toString(condenaAcumulada);
    }

}
