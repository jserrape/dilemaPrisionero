/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auxiliar;

import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.domain.FIPANames;
import jade.proto.SubscriptionResponder;
import juegosTablero.elementos.Partida;
import juegosTablero.elementos.Jugador;
import juegosTablero.elementos.Tablero;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import juegoQuoridor.OntologiaQuoridor;

public class PartidaQuoridor {

    protected Jugador[] listaJugadores;
    protected ArrayList<juegoQuoridor.elementos.Movimiento> listaMovimientos;
    protected Partida game;
    protected Integer numJugadores;
    protected juegosTablero.elementos.Posicion[] finMov;
    protected ContentManager cm;
    
    public Set<SubscriptionResponder.Subscription> lagentuza;
    public SubscriptionResponder.SubscriptionManager gestor;

    public PartidaQuoridor(int numeroJugadores, Partida ngame) {
        
        game = ngame;
        if(numeroJugadores == 2){
            listaJugadores = new Jugador[2];
            numJugadores = 2;
            finMov = new juegosTablero.elementos.Posicion[2];
            
            finMov[0] = new juegosTablero.elementos.Posicion();
            finMov[0].setCoorY(8);
           
            finMov[1] = new juegosTablero.elementos.Posicion();
            finMov[1].setCoorY(0);
        }
        if(numeroJugadores == 4){
            listaJugadores = new Jugador[4];
            numJugadores = 4;
            finMov = new juegosTablero.elementos.Posicion[4];
            
            finMov[0] = new juegosTablero.elementos.Posicion();
            finMov[0].setCoorY(8);
           
            finMov[1] = new juegosTablero.elementos.Posicion();
            finMov[1].setCoorX(0);
            
            finMov[2] = new juegosTablero.elementos.Posicion();
            finMov[2].setCoorY(0);
           
            finMov[3] = new juegosTablero.elementos.Posicion();
            finMov[3].setCoorX(8);
        }
        
        listaMovimientos = new ArrayList<>();
        lagentuza = new HashSet<>();
        gestor = new SubscriptionResponder.SubscriptionManager() {

            @Override
            public boolean register(SubscriptionResponder.Subscription suscripcion) {
                lagentuza.add(suscripcion);
                return true;
            }

            @Override
            public boolean deregister(SubscriptionResponder.Subscription suscripcion) {
                lagentuza.remove(suscripcion);
                return true;
            }
        };
    }
    
    public PartidaQuoridor(Partida ngame)
    {
        game = ngame;
        
        listaMovimientos = new ArrayList<>();
        lagentuza = new HashSet<>();
        gestor = new SubscriptionResponder.SubscriptionManager() {

            @Override
            public boolean register(SubscriptionResponder.Subscription suscripcion) {
                lagentuza.add(suscripcion);
                return true;
            }

            @Override
            public boolean deregister(SubscriptionResponder.Subscription suscripcion) {
                lagentuza.remove(suscripcion);
                return true;
            }
        };
    }

    public Partida getPartida()
    {
        return game; 
    }
    
    public void setNumJugadores(Integer n)
    {
        numJugadores = n;
        listaJugadores = new Jugador[n];
    }
    
    public Integer getNumJugadores()
    {
        return numJugadores;
    }
    
    public ArrayList getListaMovimientos(){
        return listaMovimientos; 
    }
    /**
     * Método para obtener un jugador específico.
     * @param i: Número del jugador (1 -> numeroJugadores)
     * @return Jugador seleccionado
     */
    public Jugador getJugador(int i){
        return listaJugadores[i];
    }
    public void setJugador(int i, Jugador j) throws IndexOutOfBoundsException{
        listaJugadores[i] = j;
    }
    
    public void addMoviento( juegoQuoridor.elementos.Movimiento nmov )
    {
        listaMovimientos.add(nmov);
    }
    
    public Boolean finDeJuego(juegosTablero.elementos.Ficha ficha, juegoQuoridor.elementos.Movimiento ultimoMov)
    {
        
        switch(ficha.getColor())
        {
            case juegoQuoridor.OntologiaQuoridor.COLOR_FICHA_1:
                    if(ultimoMov.getPosicion().getCoorY() == finMov[0].getCoorY())
                    {
                        return true;
                    }
            break;
                
            case juegoQuoridor.OntologiaQuoridor.COLOR_FICHA_2:
                    if(numJugadores == 2)
                    {
                        if(ultimoMov.getPosicion().getCoorY() == finMov[1].getCoorY())
                        {
                            return true;
                        }
                    }
                    else
                    {
                        if(ultimoMov.getPosicion().getCoorX() == finMov[1].getCoorX())
                        {
                            return true;
                        }
                    }
            break;
                
            case juegoQuoridor.OntologiaQuoridor.COLOR_FICHA_3:
                    if(ultimoMov.getPosicion().getCoorY() == finMov[2].getCoorY())
                    {
                        return true;
                    }
            break;
                
            case juegoQuoridor.OntologiaQuoridor.COLOR_FICHA_4:
                    if(ultimoMov.getPosicion().getCoorX() == finMov[3].getCoorX())
                    {
                        return true;
                    }
            break;
        }
        
        
        return false;
    }
    
}
