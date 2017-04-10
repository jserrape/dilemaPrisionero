package dilemaPrisionero;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import juegos.OntologiaJuegos;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Ontología de comunicación para jugar al Dilema del Prisionero
 * 
 * @author pedroj
 */
public class OntologiaDilemaPrisionero extends BeanOntology {
    private static final long serialVersionUID = 1L;

    // NOMBRE
    public static final String ONTOLOGY_NAME = "Ontologia_Dilema_Prisionero";
        
    // VOCABULARIO
    public static final String REGISTRO_PRISIONERO = "Prisionero";
    public static final String REGISTRO_POLICIA = "Policía";
    public static final String REGISTRO_CONSOLA = "Consola";
       
        
    // Jugadas del juego
    public static final String HABLAR = "Hablar"; 
    public static final String CALLAR = "Callar";

    // The singleton instance of this ontology
    private static Ontology INSTANCE;

    public synchronized final static Ontology getInstance() throws BeanOntologyException {
	if (INSTANCE == null) {
            INSTANCE = new OntologiaDilemaPrisionero();
        }
            return INSTANCE;
    }

    /**
    * Constructor
    * 
    * @throws BeanOntologyException
    */
    private OntologiaDilemaPrisionero() throws BeanOntologyException {
	
        super(ONTOLOGY_NAME, OntologiaJuegos.getInstance());
        
        add("dilemaPrisionero.elementos");
    }
}
