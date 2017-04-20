/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;
import javax.swing.*;
import java.awt.*;
import java.net.URL;

import Auxiliar.*;
import GUI.Tablero;
import java.awt.event.WindowAdapter;
import juegosTablero.elementos.Posicion;

/**
 *
 * @author sak
 */
 
public class Ventana extends JFrame{
    GridBagConstraints gbc;
    Tablero tQuoridor;
    int numMuros1, numMuros2, numMuros3, numMuros4, numJugadores;
    
    JFrame ventanaM;
    JLabel jugador1, jugador2, jugador3, jugador4;
    
    public Ventana (int nnumJugadores){
        
        numJugadores = nnumJugadores;
        if(numJugadores == 4)
        {
            numMuros1 = numMuros2 = numMuros3 = numMuros4 = 5;
        }
        else
        {
            numMuros1 = numMuros2 = 10;
        }
        
        tQuoridor = new Tablero(numJugadores);
        
        tQuoridor.setLocation(100, 100);
        
        ventanaM = new JFrame("Quoridor");
        ventanaM.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
        //finaliza el programa cuando se da click en la X
        ventanaM.setSize(674, 674);//configurando tamaño de la ventana
  
        ColocarMarcadores(ventanaM, numJugadores);
        
        ventanaM.add(tQuoridor);

        ventanaM.setVisible(true);//configurando visualización de la ventana  
        
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    

    
    private void ColocarMarcadores(JFrame v, int numJugadores)
    {
        if(numJugadores == 4)
        {
            URL imgJugador1 = getClass().getClassLoader().getResource("GUI/assets/jugador1.png");
            URL imgJugador2 = getClass().getClassLoader().getResource("GUI/assets/jugador2.png");
            URL imgJugador3 = getClass().getClassLoader().getResource("GUI/assets/jugador3.png");
            URL imgJugador4 = getClass().getClassLoader().getResource("GUI/assets/jugador4.png");
        
            ImageIcon icon1 = new ImageIcon(imgJugador1); 
            ImageIcon icon2 = new ImageIcon(imgJugador2);
            ImageIcon icon3 = new ImageIcon(imgJugador3);
            ImageIcon icon4 = new ImageIcon(imgJugador4);
        
                // incluimos cada marcador de muros en la ventana
            jugador1 = new JLabel("<html><body>Nº Muros: <br>  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;"+ numMuros1 + "</body></html>", icon1, JLabel.LEFT); 
            jugador1.setLocation(100, 550); // posicion en la ventana
            jugador1.setSize(130, 50); // tamaño del label
            v.add(jugador1);  //lo añadimos en nuestra ventana
        
            jugador2 = new JLabel("<html><body>Nº Muros: <br>  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;"+ numMuros2 +"</body></html>", icon2, JLabel.LEFT); 
            jugador2.setLocation(220, 550); // posicion en la ventana
            jugador2.setSize(130, 50); // tamaño del label
            v.add(jugador2); //lo añadimos en nuestra ventana  
        
            jugador3 = new JLabel("<html><body>Nº Muros: <br>  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;"+ numMuros3  +"</body></html>", icon3, JLabel.LEFT); 
            jugador3.setLocation(340, 550); // posicion en la ventana
            jugador3.setSize(130, 50); // tamaño del label
            v.add(jugador3); //lo añadimos en nuestra ventana  
        
            jugador4 = new JLabel("<html><body>Nº Muros: <br>  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;"+ numMuros4 +"</body></html>", icon4, JLabel.LEFT); 
            jugador4.setLocation(460, 550); // posicion en la ventana
            jugador4.setSize(130, 50); // tamaño del label
            v.add(jugador4); //lo añadimos en nuestra ventana 
        }
        else
        {
            URL imgJugador1 = getClass().getClassLoader().getResource("GUI/assets/jugador1.png");
            URL imgJugador2 = getClass().getClassLoader().getResource("GUI/assets/jugador2.png");
        
            ImageIcon icon1 = new ImageIcon(imgJugador1); 
            ImageIcon icon2 = new ImageIcon(imgJugador2);
        
                // incluimos cada marcador de muros en la ventana
            jugador1 = new JLabel("<html><body>Nº Muros: <br>  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;"+ numMuros1 + "</body></html>", icon1, JLabel.LEFT); 
            jugador1.setLocation(100, 550); // posicion en la ventana
            jugador1.setSize(130, 50); // tamaño del label
            v.add(jugador1);  //lo añadimos en nuestra ventana
        
            jugador2 = new JLabel("<html><body>Nº Muros: <br>  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;"+ numMuros2 +"</body></html>", icon2, JLabel.LEFT); 
            jugador2.setLocation(460, 550); // posicion en la ventana
            jugador2.setSize(130, 50); // tamaño del label
            v.add(jugador2); //lo añadimos en nuestra ventana 
        }
    }
    
    public void moverJugador(Jugador j, Posicion p)
    {
        tQuoridor.mueveJugador(j, p);
    }
    
    public void ponerMuro(Jugador j, Posicion p, boolean orientacion)
    {
        switch(j.getColor())
        {
            case 0:
                numMuros1--;
                actualizaMuros(jugador1, numMuros1);
            break;
            case 1:
                numMuros2--;
                actualizaMuros(jugador2, numMuros2);
            break;
            case 2:
                numMuros3--;
                actualizaMuros(jugador3, numMuros3);
            break;
            case 3:
                numMuros4--;
                actualizaMuros(jugador4, numMuros4);
            break;
        }
        tQuoridor.ponMuro(p, orientacion);
	ColocarMarcadores(ventanaM, numJugadores);
    }
    
    private void actualizaMuros ( JLabel jugador, Integer numMuros )
    {
        jugador.setText("<html><body>Nº Muros: <br>  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;"+ numMuros +"</body></html>");
    }
    
   
}
