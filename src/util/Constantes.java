/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 *
 * @author pedroj
 */
public interface Constantes {
    
    public final long TIME_OUT = 2000; // 2seg
    public final long RETARDO_PRESENTACION = 1500; // 1,5 seg 
    public final long BUSCAR_AGENTES = 500; // 0,5 seg
    public final int   MINIMO_LADRONES = 4; // mínimo número de jugadores 
    public static final int NUM_RONDAS = 10;
    public static final int PROB_FINAL = 25; // 25% una vez alcanzadas las rondas
    public static final int TENTACION = 1; 
    public static final int RECOMPENSA = 2; // por colaboración
    public static final int CASTIGO = 5; // mutua traición
    public static final int PRIMO = 10; // pena del pardillo
    public static final int PRIMERA_RONDA = 0; // identifica la primera ronda de la partida
    public static final int CONDENA_INICIAL = 0;
    public static final int PRIMERO = 0; // índice para el primer jugador
    public static final int SEGUNDO = 1; // índice para el segundo jugador
    public final int MAX_PARTIDAS = 3;
    public final double DECISION = 0.5;
}
