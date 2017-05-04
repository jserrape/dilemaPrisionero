/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 * Constante que se usarán por los agentes presentes en el juego del 
 * Dilema del prisionero
 * @author pedroj
 */
public interface Constantes {
    
    public final long TIME_OUT = 2000; // 2seg
    public final long RETARDO_PRESENTACION = 2000; // 2 seg 
    public final long BUSCAR_AGENTES = 500; // 0,5 seg
    public final int MINIMO_LADRONES = 4; // mínimo número de jugadores 
    public final int NUM_RONDAS = 10;
    public final int PROB_FINAL = 25; // 25% una vez alcanzadas las rondas
    public final int TENTACION = 1; 
    public final int RECOMPENSA = 2; // por colaboración
    public final int CASTIGO = 5; // mutua traición
    public final int PRIMO = 10; // pena del pardillo
    public final int PRIMERA_RONDA = 0; // identifica la primera ronda de la partida
    public final int CONDENA_INICIAL = 0;
    public final int PRIMERO = 0; // índice para el primer jugador
    public final int SEGUNDO = 1; // índice para el segundo jugador
    public final int MAX_PARTIDAS = 3;
    public final double DECISION = 0.5;
    public final String PARTIDA_CANCELADA = "Partida Cancelada: ";
    public final String INSUFICIENTES_JUGADORES = "número insuficiente de jugadores";
    public final String CANCELACION_USUARIO = "cancelación solicitada por el usuario";
    public final String JUGADOR_ABANDONO = " ha abandonado el juego ";
    public final String NOMBRE_AGENTE_IMPAR = "AgenteImpar";
    public final String NOMBRE_JUGADOR_IMPAR = "Jugador Impar";
}
