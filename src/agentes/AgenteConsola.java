/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentes;

import gui.Consola;
import dilemaPrisionero.OntologiaDilemaPrisionero;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.Iterator;
import util.MensajeConsola;

/**
 *
 * @author pedroj
 */
public class AgenteConsola extends Agent {
    private ArrayList<Consola> myGui;
    private ArrayList<MensajeConsola> mensajesPendientes;
    
    /**
     * Se ejecuta cuando se inicia el agente
     */
    @Override
    protected void setup() {
        //Incialización de variables
        myGui = new ArrayList();
        mensajesPendientes = new ArrayList();
        
        //Regisro de la Ontología
        
        //Registro en Página Amarillas
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
	ServiceDescription sd = new ServiceDescription();
	sd.setType("GUI");
	sd.setName(OntologiaDilemaPrisionero.REGISTRO_CONSOLA);
	dfd.addServices(sd);
	try {
            DFService.register(this, dfd);
	}
	catch (FIPAException fe) {
            fe.printStackTrace();
	}
        
        // Se añaden las tareas principales
       addBehaviour(new RecepcionMensajes());
       
       System.out.println("Se ha iniciado el agente: " + this.getName());
    }
    
    /**
     * Se ejecuta al finalizar el agente
     */
    @Override
    protected void takeDown() {
        //Desregistro de las Páginas Amarillas
        try {
            DFService.deregister(this);
	}
            catch (FIPAException fe) {
            fe.printStackTrace();
	}
        
        //Se liberan los recuros y se despide
        
        cerrarConsolas();
        System.out.println("Finaliza la ejecución de " + this.getName());
    }

    //Métodos de utilidad para el agente consola
    private Consola buscarConsola(String nombreAgente) {
        // Obtenemos la consola donde se presentarán los mensajes
        Iterator<Consola> it = myGui.iterator();
        while (it.hasNext()) {
            Consola gui = it.next();
            if (gui.getNombreAgente().compareTo(nombreAgente) == 0)
                return gui;
        }
        
        return null;
    }
    
    private void cerrarConsolas() {
        //Se eliminan las consolas que están abiertas
        Iterator<Consola> it = myGui.iterator();
        while (it.hasNext()) {
            Consola gui = it.next();
            gui.dispose();
        }
    }
    
    //Tareas del agente consola
    public class RecepcionMensajes extends CyclicBehaviour {

        @Override
        public void action() {
            //Solo se atenderán mensajes INFORM
            MessageTemplate plantilla = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage mensaje = myAgent.receive(plantilla);
            if (mensaje != null) {
                //procesamos el mensaje
                MensajeConsola mensajeConsola = new MensajeConsola(mensaje.getSender().getName(),
                                    mensaje.getContent());
                mensajesPendientes.add(mensajeConsola);
                addBehaviour(new PresentarMensaje());
            } 
            else
                block();
            
        }
    
    }
    
    public class PresentarMensaje extends OneShotBehaviour {

        @Override
        public void action() {
            //Se coge el primer mensaje
            MensajeConsola mensajeConsola = mensajesPendientes.remove(0);
            
            //Se busca la ventana de consola o se crea una nueva
            Consola gui = buscarConsola(mensajeConsola.getNombreAgente());
            if (gui == null) {
                gui = new Consola(mensajeConsola.getNombreAgente());
                myGui.add(gui);
            } 
            
            gui.presentarSalida(mensajeConsola);
        }
        
    }


}
