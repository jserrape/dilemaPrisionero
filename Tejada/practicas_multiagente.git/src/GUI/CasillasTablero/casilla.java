package GUI.CasillasTablero;

import javax.swing.*;

/**
 * Clase para usar de herencia con CasillaJ y CasillaM
 * @author cyane
 */
public class casilla extends JLabel{
    
    
    public casilla()
    {
        super();
    }
    /**
     * Method for override
     */
    public void activar(){}
    
    /**
     * Method for override
     */
    public void activar( int i ){}
    
    /**
     * Method for override
     */
    public void desactivar(){}
}
