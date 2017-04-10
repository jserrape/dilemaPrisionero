/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dilemaPrisionero.elementos;

import jade.content.Concept;

/**
 * Nos permite establecer los costes para un juego del Dilema del Prisionero
 * 
 * @author pedroj
 */
public class Condenas implements Concept {
    private int tentacion;  // Incentivo por traicionar
    private int recompensa; // Recompensa por colaborar
    private int castigo;    // Castigo por doble traición
    private int primo;      // Pena por ingénuo

    public Condenas() {
    }

    public Condenas(int tentacion, int recompensa, int castigo, int primo) {
        this.tentacion = tentacion;
        this.recompensa = recompensa;
        this.castigo = castigo;
        this.primo = primo;
    }

    public int getTentacion() {
        return tentacion;
    }

    public void setTentacion(int tentacion) {
        this.tentacion = tentacion;
    }

    public int getRecompensa() {
        return recompensa;
    }

    public void setRecompensa(int recompensa) {
        this.recompensa = recompensa;
    }

    public int getCastigo() {
        return castigo;
    }

    public void setCastigo(int castigo) {
        this.castigo = castigo;
    }

    public int getPrimo() {
        return primo;
    }

    public void setPrimo(int primo) {
        this.primo = primo;
    }

    @Override
    public String toString() {
        return "TiposCondena{" + "tentacion=" + tentacion + ", recompensa=" 
                + recompensa + ", castigo=" + castigo + ", primo=" + primo + '}';
    }
    
    
}
