package GUI.CasillasTablero;

import javax.swing.*;
import java.net.URL;

public class CasillaJ extends casilla{
    
        protected ImageIcon icon;
    
        /**
         * Constructor de label con la imagen del jugador cullo número se pase
         * @param i número de la imagen de jugador.
         */
        public CasillaJ( Integer i )
        {
            super();
            ImageIcon icon=null;
            URL iconURL = getClass().getClassLoader().getResource("GUI/assets/peon" +i+ ".png");
            icon = new ImageIcon(iconURL);
            this.setIcon(icon);
        }
        
        /**
         * Constructor de casilla de jugador vacía (Transparente)
         */
        public CasillaJ()
        {
            super();
            ImageIcon icon=null;
            URL iconURL = getClass().getClassLoader().getResource("GUI/assets/peonV.png");
            icon = new ImageIcon(iconURL);
            this.setIcon(icon);
        }
        
        /**
         * Activa la casilla de jugador, asignándole la imagen del jugador correspondiente
         * @param jugador color del jugador a asignar al JLabel
         */
        @Override
        public void activar( int jugador )
        {
            ImageIcon icon=null;
            URL iconURL = getClass().getClassLoader().getResource("GUI/assets/peon" +(jugador + 1)+ ".png");
            icon = new ImageIcon(iconURL);
            this.setIcon(icon);
        }
        
        /**
         * desactiva la casilla, volviéndola tranparente
         */
        @Override
        public void desactivar()
        {
            ImageIcon icon=null;
            URL iconURL = getClass().getClassLoader().getResource("GUI/assets/peonV.png");
            icon = new ImageIcon(iconURL);
            this.setIcon(icon);
        }
}
