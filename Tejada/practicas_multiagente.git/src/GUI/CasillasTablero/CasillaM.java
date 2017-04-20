package GUI.CasillasTablero;
import javax.swing.*;
import java.net.URL;

public class CasillaM extends casilla{
    
    protected boolean activo;
    protected ImageIcon icon;
    protected boolean vertical;
    
    /**
     * Constructor de label vertical/horizontal para los muros, siempre inactivos.
     * @param nvertical especifica si es horizontal o no.
     */
    public CasillaM( boolean nvertical )
    {
        super();
        URL iconURL = null; 
        
        if (nvertical)
        {
            iconURL = getClass().getClassLoader().getResource("GUI/assets/muroVV.png");
            activo = false; 
            vertical = true;
        }else{
            
            iconURL = getClass().getClassLoader().getResource("GUI/assets/muroHV.png");
            activo = false; 
            vertical = false; 
        }
        
        icon  = new ImageIcon(iconURL);
        this.setIcon(icon);
    }
   
    
    /**
     * Activa el muro.
     */
    @Override
    public void activar()
    {     
        
        URL iconURL = null;
        
        if (vertical)
        {
            iconURL = getClass().getClassLoader().getResource("GUI/assets/muroV.png");
            activo = true; 
        }else{
            
            iconURL = getClass().getClassLoader().getResource("GUI/assets/muroH.png");
            activo = true; 
        }
        
        icon  = new ImageIcon(iconURL);
        this.setIcon(icon);
    }
}
