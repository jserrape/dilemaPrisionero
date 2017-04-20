package Backend;

import Auxiliar.Jugador;
import java.util.ArrayList;
import GUI.*;
import javax.swing.*;
import java.awt.event.*;
import Agentes.BoardAgent;

import juegosTablero.elementos.Posicion;

/**
 *
 * @author fjlen
 */
public class TableroBack {

    public static final boolean __MUEVE__ = true;
    public static final boolean __MURO__ = false;
    public static final int __JUGADOR__ = 2;

    public static final boolean __VERTICAL__ = true;
    public static final boolean __HORIZONTAL__ = false;

    private int[][] tablero;
    private ArrayList<Jugador> jugadores;
    private int turno;
    private int nJugadores;
    Ventana Main;
    BoardAgent parent;
    JFrame ventana;
    

    
    public TableroBack(int numJugadores, Boolean gui)
    {
        //FOR THE AGENTS
        tablero = new int[17][17];
        this.jugadores = new ArrayList<>();
        turno = 0;
        nJugadores = numJugadores;
        if(gui){
            Main = new Ventana(nJugadores);
            Main.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        
        if (nJugadores == 4) {
            
            this.jugadores.add(new Jugador(0, new Posicion (4,0)));
            if(gui)Main.moverJugador(this.jugadores.get(0), this.jugadores.get(0).getPosicion());
            
            this.jugadores.add(new Jugador(1, new Posicion (8,4)));
            if(gui)Main.moverJugador(this.jugadores.get(1), this.jugadores.get(1).getPosicion());
            
            this.jugadores.add(new Jugador(2, new Posicion (4,8)));
            if(gui)Main.moverJugador(this.jugadores.get(2), this.jugadores.get(2).getPosicion());
            
            this.jugadores.add(new Jugador(3, new Posicion (0,4)));
            if(gui)Main.moverJugador(this.jugadores.get(3), this.jugadores.get(3).getPosicion());

        }
        if (nJugadores == 2) {
            
            this.jugadores.add(new Jugador(0, new Posicion (4,0)));
            if(gui)Main.moverJugador(this.jugadores.get(0), this.jugadores.get(0).getPosicion());
            
            this.jugadores.add(new Jugador(1, new Posicion (4,8)));
            if(gui)Main.moverJugador(this.jugadores.get(1), this.jugadores.get(1).getPosicion());
        }
    }
    
    /**
     * Constructor del juego de tablero.
     * @param nparent 
     */
    public TableroBack( BoardAgent nparent )
    {
        parent = nparent; 
        ventana = new JFrame(parent.getName() + ": Iniciar juego.");
        JButton accept = new JButton(parent.getName() + " --->  Start searching for a gamo!");
        
        accept.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                parent.addTareaBusca();
                ventana.setVisible(false);
            }
        }); 
        
        
        ventana.add(accept);
        ventana.pack();
        ventana.setVisible(true);
    }
    
  

    /**
     * Función para obtener la posición de un jugador específico.
     *
     * @param jug
     * @return
     */
    public Posicion posicionJugador(int jug) {
        try {
            return jugadores.get(jug).getPosicion();
        } catch (Exception e) {
            System.err.println("Numero de jugador incorrecto: " + jug);
        }
        return new Posicion();
    }
    
    /**
     * Funcion para obtener la posicion del jugador actual.
     * 
     * @return
     */
    public Posicion getPosicionJugadorActual(){
        return jugadores.get(turno).getPosicion();
    }
    
    /**
     * Funcion para preguntar si hay un muro en una posicion
     *
     * @param p
     * @return
     */
    public boolean hayMuro(Posicion p) {
        return esMuro(p) && tablero[p.getCoorX()][p.getCoorY()] != 0;
    }

    /**
     * Funcion para realizar un movimiento
     *
     * @param p
     */
    protected void movimiento(Posicion p) {
        int x = jugadores.get(turno).getPosicion().getCoorX();
        int y = jugadores.get(turno).getPosicion().getCoorY();
        tablero[x * 2][y * 2] = 0;
        jugadores.get(turno).setPosicion(p);
        tablero[p.getCoorX() * 2][p.getCoorY() * 2] = 2;
        //GUI.mover(Jugador j, Posicion p)
        if(Main != null) Main.moverJugador(jugadores.get(turno), jugadores.get(turno).getPosicion());
    }
    
    /**
     * Funcion para obtener la posicion
     * @param jugador
     * @return 
     */
    protected Posicion getMovimiento(int jugador){
        return jugadores.get(jugador).getPosicion();
    }
    
    /**
     * Funcion para poner Muro
     *
     * @param p
     * @param orientacion
     */
    protected void ponerMuro(Posicion p, boolean orientacion) {
        if (orientacion) {
            if (esMuro(p) && (p.getCoorY() * 2 <= 16)) {
                tablero[p.getCoorX() * 2+1][p.getCoorY() * 2] = 1;
                tablero[p.getCoorX() * 2+1][p.getCoorY() * 2+1] = 1;
                tablero[p.getCoorX() * 2+1][p.getCoorY() * 2 + 2] = 1;
                jugadores.get(turno).ponerMuro();
            }
        } else if ((p.getCoorX() * 2 <= 16)) {
            tablero[p.getCoorX() * 2][p.getCoorY() * 2+1] = 1;
            tablero[p.getCoorX() * 2+1][p.getCoorY() * 2+1] = 1;
            tablero[p.getCoorX() * 2+2][p.getCoorY() * 2+1] = 1;
            jugadores.get(turno).ponerMuro();

        }
        //Imprime();
        if(Main != null)Main.ponerMuro(jugadores.get(turno), p, orientacion);

    }
    
    public void showStart()
    {
        ventana.setVisible(true);
    }

    /**
     * La funcion importante, esta funcion es la que realiza los movimientos
     * pertinentes. Tipo es un booleano que será falso si se va a mover y
     * verdadero si se va a poner un muro. P será el movimiento a usar si se
     * mueve o el muro a poner si se pone un muro. InfoExtra sólo sirve para la
     * horientación en el caso de que se quiera poner un muro, es irrelevante
     * para un movimiento.
     *
     * @param tipo tipo de turno (TableroBack.__MUEVE__ / TableroBack.__MURO__).
     * @param p posición a la que mover.
     * @param infoExtra orientación si uusas un muro. (TableroBack.__HORIZONTAL__ /
 TableroBack.__VERTICAL__ )
     */
    public void Turno(boolean tipo, Posicion p, boolean infoExtra) {
        if (tipo) {
            movimiento(p);
        } else {
            ponerMuro(p, infoExtra);
        }
        turno = (turno + 1) % nJugadores;
    }


    
    public boolean esMuro(Posicion p){
        return (p.getCoorX()%2==1 || p.getCoorY()%2==1);
    }
}
