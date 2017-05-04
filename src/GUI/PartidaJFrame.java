/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import agentes.AgentePolicia;
import java.util.Iterator;
import util.ElmPresentacion;

/**
 *
 * @author pedroj
 */
public class PartidaJFrame extends javax.swing.JFrame {
    private final String idPartida;
    private final AgentePolicia myAgent;

    /**
     * Creates new form PartidaJFrame
     * @param idPartida
     */
    public PartidaJFrame(String idPartida, AgentePolicia agent) {
        initComponents();
        
        this.idPartida = idPartida;
        this.myAgent = agent;
        this.setTitle("Partida: " + idPartida);
        finPartida.setVisible(false);
    }

    public void presentarResultados(ElmPresentacion clasificacion, int rondaFinal) {
        this.setVisible(true);
        ronda.setText(Integer.toString(clasificacion.getRonda()));
        minimoRondas.setText(Integer.toString(rondaFinal));
        finPartida.setVisible(clasificacion.isFinPartida());
        
        // Limpiamos resultados anteriores
        agenteJugador.setText(null);
        nombreJugador.setText(null);
        condena.setText(null);
         
        // Presentamos los nuevo datos
        Iterator itAgentes = clasificacion.getAgentesJugador().iterator();
        Iterator itNombres = clasificacion.getNombresJugador().iterator();
        Iterator itCondenas = clasificacion.getCondenas().iterator();
        while (itAgentes.hasNext()) {
           agenteJugador.append((String) itAgentes.next() + "\n");
           nombreJugador.append((String) itNombres.next() + "\n");
           condena.append((String) itCondenas.next() + "\n");
        }
    } 
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        ronda = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        agenteJugador = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        nombreJugador = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        condena = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        minimoRondas = new javax.swing.JLabel();
        finPartida = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel1.setText("Agente Jugador");

        jLabel2.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel2.setText("Nombre Jugador");

        jLabel3.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel3.setText("Años de Condena");

        jLabel4.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel4.setText("Ronda");

        ronda.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        ronda.setText("jLabel5");

        agenteJugador.setEditable(false);
        agenteJugador.setColumns(20);
        agenteJugador.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        agenteJugador.setRows(5);
        jScrollPane2.setViewportView(agenteJugador);

        nombreJugador.setEditable(false);
        nombreJugador.setColumns(20);
        nombreJugador.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        nombreJugador.setRows(5);
        jScrollPane3.setViewportView(nombreJugador);

        condena.setEditable(false);
        condena.setFont(new java.awt.Font("Courier New", 0, 14)); // NOI18N
        condena.setRows(3);
        condena.setPreferredSize(new java.awt.Dimension(130, 58));
        jScrollPane1.setViewportView(condena);

        jLabel5.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel5.setText("Mínimo Rondas");

        minimoRondas.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        minimoRondas.setText("jLabel5");

        finPartida.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        finPartida.setText("FIN PARTIDA");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(minimoRondas))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel4)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ronda)))
                    .addComponent(finPartida))
                .addContainerGap(38, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(11, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jLabel2))
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(ronda))
                        .addGap(49, 49, 49)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(minimoRondas))
                        .addGap(63, 63, 63)
                        .addComponent(finPartida))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1)))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        if (!myAgent.finPartida(idPartida))
            myAgent.cancelaPartida(idPartida);
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea agenteJugador;
    private javax.swing.JTextArea condena;
    private javax.swing.JLabel finPartida;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel minimoRondas;
    private javax.swing.JTextArea nombreJugador;
    private javax.swing.JLabel ronda;
    // End of variables declaration//GEN-END:variables
}
