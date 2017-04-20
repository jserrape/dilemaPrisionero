/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auxiliar;

import juegosTablero.elementos.Posicion;


/**
 *
 * @author fjlen
 */
public class Jugador {

    int color;
    Posicion pActual;
    int nMuros;
    
    
    /**
     * Creador de jugadores.
     * @author etb
     * @param ncolor: color del jugador
     * @param pActual: posición actual del jugador(Puede ser vacía) 
     */
    public Jugador(int ncolor, Posicion npActual, Integer nnMuros)
    {
        color=ncolor;
        pActual=npActual;
        nMuros = nnMuros;
    }
    
    /**
     * Otro constructor, sin el nMuros
     * @param ncolor: color del jugador
     * @param npActual:  Posición actual del jugador(Puede ser vacía)
     */
    public Jugador(int ncolor, Posicion npActual)
    {
        color=ncolor;
        pActual=npActual;
    }
    
    
    /**
     * Funcion para obtener el color del jugador.
     * @return 
     */
    public int getColor() {
        return color;
    }
    
    /**
     * Funcion para obtener la posicion del jugador.
     * @return 
     */
    public Posicion getPosicion() {
        return pActual;
    }
    
    /**
     * Funcion para cambiar el color del jugador
     * @param Color 
     */
    public void setColor(int Color) {
        this.color = Color;
    }
    /**
     * Funcion para cambiar la posicion del jugador.
     * @param p 
     */
    public void setPosicion(Posicion p) {
        this.pActual = p;
    }

    public void ponerMuro(){
        nMuros-=1;
    }
}
