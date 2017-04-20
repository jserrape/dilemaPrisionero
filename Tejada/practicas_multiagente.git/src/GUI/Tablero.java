package GUI;

import Auxiliar.*;
import GUI.CasillasTablero.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;
import javax.swing.*;
import juegosTablero.elementos.Posicion;



public class Tablero extends JPanel{
    
    JLabel tablero;
    GridBagConstraints gbc;
    casilla[][] iconos; 
    Jugador players[]; 
    
    /**
     * Este constructor es lo que más trabajo hace en el tablero, creando una matriz de 17x17 y rellenándola con los jLabels de jugadores y muros
     * @param numJugadores: Entero especificando el número de jugadores
     */
    public Tablero(Integer numJugadores)
    {
        super (new GridBagLayout());
        
        players= new Jugador[numJugadores];
        
        ImageIcon icon=null;
        URL iconURL = getClass().getClassLoader().getResource("GUI/assets/tablero.png");
        tablero = new JLabel();
        icon = new ImageIcon(iconURL);
        tablero.setIcon(icon);
        
        
        this.add(tablero, gbc, -1);
        tablero.setLayout(null);
        
        Integer X = 0;
        Integer Y = 0;
        iconos = new casilla[17][17];
        for( int i = 0; i<17; i++)
        {
            for( int j = 0; j<17; j++)
            {
                
                if( i % 2 == 1 && j % 2 == 1)
                {
                    iconos[i][j] = new CasillaM(true);
                    iconos[i][j].setBounds(X, Y, 5, 5);
                    X += 1;
                    
                }else{
                    if( i % 2 == 1)
                    {
                        iconos[i][j] = new CasillaM(false);
                        iconos[i][j].setBounds(X, Y+17, 41, 5);
                        X += 41;
                    
                    }else{
                    
                        if( j % 2 == 1)
                        {
                            iconos[i][j] = new CasillaM(true);
                            iconos[i][j].setBounds(X, Y, 5, 35);
                            X += 5;
                        
                        }else{
                        
                            iconos[i][j] = new CasillaJ();
                            iconos[i][j].setBounds(X, Y, 35, 35);
                            X +=37;
                        }
                    }
                }
                tablero.add(iconos[i][j]);
          
            }
            Y += 21;
            X = 0;
        }
    }
    
    /**
     * Este constructor crea un tablero vacío, sin jugadores.
     */
    public Tablero()
    {
        super (new GridBagLayout());
        
        
        ImageIcon icon=null;
        URL iconURL = getClass().getClassLoader().getResource("GUI/assets/tablero.png");
        tablero = new JLabel();
        icon = new ImageIcon(iconURL);
        tablero.setIcon(icon);
        
        
        this.add(tablero, gbc, -1);
        tablero.setLayout(null);
        
        Integer X = 0;
        Integer Y = 0;
        iconos = new casilla[17][17];
        for( int i = 0; i<17; i++)
        {
            for( int j = 0; j<17; j++)
            {
                
                if( i % 2 == 1 && j % 2 == 1)
                {
                    iconos[i][j] = new CasillaM(true);
                    iconos[i][j].setBounds(X, Y, 5, 5);
                    X += 1;
                    
                }else{
                    if( i % 2 == 1)
                    {
                        iconos[i][j] = new CasillaM(false);
                        iconos[i][j].setBounds(X, Y+17, 41, 5);
                        X += 41;
                    
                    }else{
                    
                        if( j % 2 == 1)
                        {
                            iconos[i][j] = new CasillaM(true);
                            iconos[i][j].setBounds(X, Y, 5, 35);
                            X += 5;
                        
                        }else{
                        
                            iconos[i][j] = new CasillaJ();
                            iconos[i][j].setBounds(X, Y, 35, 35);
                            X +=37;
                        }
                    }
                }
                tablero.add(iconos[i][j]);
          
            }
            Y += 21;
            X = 0;
        }
    }
    
    /**
     * Funcion para especificar el numero de jugadores en el juego.
     * @param nJugadores 
     */
    public void setNumJugadores(Integer nJugadores)
    {
        players = new Jugador[nJugadores];
    }
    
    /**
     * Funcion que devuelve el numero de jugadores en la partida
     * @return El numero de jugadores en la partida
     */
    public Integer getNumJugadores()
    {
        return players.length;
    }
    
    /**
     * Funcion que pone los muros en la posición especificad, con la orientazión especificada
     * @param pos posicion dode será colocado el muro
     * @param o orientación con la que el muro será posicionado
     */
    public void ponMuro( Posicion pos, boolean o )
    {
        if (o)
        {
            iconos[ pos.getCoorY()*2 ][ (pos.getCoorX()*2)+1 ].activar();
            iconos[ (pos.getCoorY()*2)+2 ][ (pos.getCoorX()*2)+1 ].activar();
            
        }else{
            
            iconos[ (pos.getCoorY()*2)+1 ][ pos.getCoorX()*2 ].activar();
            iconos[ (pos.getCoorY()*2)+1 ][ (pos.getCoorX()*2)+2 ].activar();
        }
    }
    
    /**
     * Funcion para mover jugadores, DEBE SER LLAMADA PARA COLOCARLOS también al inicio del juego.
     * @param j jugador a colocar 
     * @param p posición donde colocarlo.
     */
    public void mueveJugador( Jugador j, Posicion p )
    {
        try{
            
            iconos [ players[j.getColor()].getPosicion().getCoorY()*2 ][ players[j.getColor()].getPosicion().getCoorX()*2 ].desactivar();
            
        }catch(java.lang.NullPointerException e){
            
            players[j.getColor()] = new Jugador ( j.getColor(), p);
            
        }
        
        iconos[ p.getCoorY()*2 ][ p.getCoorX()*2 ].activar(j.getColor());
        players[j.getColor()] = new Jugador ( j.getColor(), p);
    }
}
