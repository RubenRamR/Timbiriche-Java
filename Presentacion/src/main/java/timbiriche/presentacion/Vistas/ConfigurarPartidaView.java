/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package timbiriche.presentacion.Vistas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import timbiriche.presentacion.ControllerView;
import timbiriche.presentacion.IModelViewLeible;
import timbiriche.presentacion.Observer;

/**
 *
 * @author rramirez
 */
public class ConfigurarPartidaView extends javax.swing.JDialog implements Observer {

    private final ControllerView controlador;
    private final IModelViewLeible modeloLeible;

    // Componentes manuales (Títulos)
    private JPanel pnlTitulo;
    private JPanel pnlSubtitulo;

    public ConfigurarPartidaView(ControllerView controlador, IModelViewLeible modeloLeible) {
        this.controlador = controlador;
        this.modeloLeible = modeloLeible;

        initComponents();
        configurarVista();
    }

    /**
     * Configuración manual para centrar y estilizar la pantalla.
     */
    private void configurarVista() {
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(screenSize);
        this.setLocation(0, 0);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(JPnlFondo, BorderLayout.CENTER);

        if (modeloLeible != null)
        {
            modeloLeible.agregarObservador(this);
        }

        CboxDimensiones.removeAllItems();
        CboxDimensiones.addItem("10x10");
        CboxDimensiones.addItem("20x20");
        CboxDimensiones.addItem("30x30");

        JPnlFondo.removeAll();
        JPnlFondo.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 30, 0);

        pnlTitulo = new JPanel();
        pnlTitulo.setBackground(new Color(204, 204, 204));
        pnlTitulo.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        JLabel lblTitulo = new JLabel("Configurar Partida");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(15, 60, 15, 60));
        pnlTitulo.add(lblTitulo);

        gbc.anchor = GridBagConstraints.NORTH;
        JPnlFondo.add(pnlTitulo, gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER; //
        gbc.insets = new Insets(100, 0, 20, 0);

        pnlSubtitulo = new JPanel();
        pnlSubtitulo.setBackground(new Color(204, 204, 204));
        pnlSubtitulo.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        JLabel lblSubtitulo = new JLabel("Tamaño Tablero");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblSubtitulo.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        pnlSubtitulo.add(lblSubtitulo);

        JPnlFondo.add(pnlSubtitulo, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(100, 0, 0, 0);

        CboxDimensiones.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        CboxDimensiones.setBackground(Color.WHITE); // Opcional

        JPnlFondo.add(CboxDimensiones, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(175, 0, 100, 0);
        gbc.weighty = 2.0;
        gbc.anchor = GridBagConstraints.NORTH;

        JPanel pnlBotones = new JPanel();
        pnlBotones.setOpaque(false);
        pnlBotones.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 150, 0));

        BtnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 36));
        BtnCrear.setFont(new Font("Segoe UI", Font.BOLD, 36));

        pnlBotones.add(BtnCancelar);
        pnlBotones.add(BtnCrear);

        JPnlFondo.add(pnlBotones, gbc);

        this.revalidate();
        this.repaint();

        for (ActionListener al : BtnCrear.getActionListeners())
        {
            BtnCrear.removeActionListener(al);
        }
        for (ActionListener al : BtnCancelar.getActionListeners())
        {
            BtnCancelar.removeActionListener(al);
        }

        BtnCrear.addActionListener(e -> onClicCrear());
        BtnCancelar.addActionListener(e ->
        {
            dispose();
            System.exit(0);
        });
    }

    private void onClicCrear() {
        String seleccion = (String) CboxDimensiones.getSelectedItem();

        if (seleccion != null && !seleccion.isEmpty())
        {
            try
            {
                String soloNumero = seleccion.toLowerCase().split("x")[0].trim();
                int dimension = Integer.parseInt(soloNumero);

                // Feedback visual
                BtnCrear.setEnabled(false);
                BtnCrear.setText("Cargando...");

                if (controlador != null)
                {
                    controlador.onClicCrearPartida(dimension);
                } else
                {
                    System.out.println("[VISTA] Error: Controlador es null");
                }

            } catch (NumberFormatException ex)
            {
                JOptionPane.showMessageDialog(this, "Error: El formato debe ser Numero x Numero");
                BtnCrear.setEnabled(true);
                BtnCrear.setText("Crear");
            }
        }
    }

    @Override
    public void actualizar() {
        if (modeloLeible.getDimension() > 0)
        {
            SwingUtilities.invokeLater(() ->
            {
                System.out.println("[Vista] Configuración recibida. Cerrando diálogo...");
                this.dispose();
            });
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

        JPnlFondo = new javax.swing.JPanel();
        CboxDimensiones = new javax.swing.JComboBox<>();
        BtnCancelar = new javax.swing.JButton();
        BtnCrear = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        JPnlFondo.setBackground(new java.awt.Color(255, 255, 255));

        CboxDimensiones.setBackground(new java.awt.Color(255, 255, 255));
        CboxDimensiones.setFont(new java.awt.Font("Segoe UI", 0, 36)); // NOI18N
        CboxDimensiones.setForeground(new java.awt.Color(0, 0, 0));

        BtnCancelar.setBackground(new java.awt.Color(255, 51, 51));
        BtnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        BtnCancelar.setForeground(new java.awt.Color(255, 255, 255));
        BtnCancelar.setText("Cancelar");

        BtnCrear.setBackground(new java.awt.Color(0, 255, 0));
        BtnCrear.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        BtnCrear.setForeground(new java.awt.Color(255, 255, 255));
        BtnCrear.setText("Crear");

        javax.swing.GroupLayout JPnlFondoLayout = new javax.swing.GroupLayout(JPnlFondo);
        JPnlFondo.setLayout(JPnlFondoLayout);
        JPnlFondoLayout.setHorizontalGroup(
            JPnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JPnlFondoLayout.createSequentialGroup()
                .addContainerGap(190, Short.MAX_VALUE)
                .addGroup(JPnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, JPnlFondoLayout.createSequentialGroup()
                        .addComponent(BtnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(329, 329, 329)
                        .addComponent(BtnCrear, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(218, 218, 218))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, JPnlFondoLayout.createSequentialGroup()
                        .addComponent(CboxDimensiones, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(367, 367, 367))))
        );
        JPnlFondoLayout.setVerticalGroup(
            JPnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JPnlFondoLayout.createSequentialGroup()
                .addGap(328, 328, 328)
                .addComponent(CboxDimensiones, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 164, Short.MAX_VALUE)
                .addGroup(JPnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BtnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BtnCrear, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(103, 103, 103))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(JPnlFondo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(JPnlFondo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex)
        {
            java.util.logging.Logger.getLogger(ConfigurarPartidaView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                // 1. CREAMOS UN MOTOR FALSO (MOCK)
                // Este es el final de la cadena. Si llega aquí, todo funciona.
                com.mycompany.imotorjuego.IMotorJuego motorMock = new com.mycompany.imotorjuego.IMotorJuego() {
                    @Override
                    public void crearPartida(int dimension) {
                        System.out.println("-------------------------------------------------");
                        System.out.println("[MOTOR MOCK] ¡LLEGARON LOS DATOS AL NÚCLEO!");
                        System.out.println("[MOTOR MOCK] Configurando tablero de: " + dimension);
                        System.out.println("-------------------------------------------------");
                    }

                    // --- Métodos vacíos obligatorios por la interfaz (ignorarlos por ahora) ---
                    @Override
                    public void realizarJugadaLocal(com.mycompany.dominio.Linea linea) {
                    }

                    @Override
                    public void registrarListener(com.mycompany.imotorjuego.IMotorJuegoListener listener) {
                    }

                    @Override
                    public void realizarJugadaRemota(com.mycompany.dominio.Linea linea, com.mycompany.dominio.Jugador jugadorRemitente) {
                    }

                    @Override
                    public void actualizarListaDeJugadores(java.util.List<com.mycompany.dominio.Jugador> nuevosJugadores) {
                    }

                    @Override
                    public void setJugadorLocal(com.mycompany.dominio.Jugador jugador) {
                    }

                    @Override
                    public void addDispatcher(com.mycompany.interfacesdispatcher.IDispatcher dispatcher) {
                    }

                    @Override
                    public void configurarTablero(int dimension) {
                    }

                    @Override
                    public com.mycompany.dominio.Tablero getTablero() {
                        return null;
                    }

                    @Override
                    public com.mycompany.dominio.Jugador getTurnoActual() {
                        return null;
                    }

                    @Override
                    public com.mycompany.dominio.Jugador getJugadorLocal() {
                        return null;
                    }

                    @Override
                    public java.util.List<com.mycompany.dominio.Jugador> getJugadores() {
                        return null;
                    }
                };

                // 2. INSTANCIAMOS EL MODELVIEW REAL (EL QUE QUEREMOS PROBAR)
                // Le inyectamos el motor falso.
                timbiriche.presentacion.ModelView modeloReal = new timbiriche.presentacion.ModelView(motorMock);

                // 3. INSTANCIAMOS EL CONTROLADOR REAL
                // Le inyectamos el modelo real.
                ControllerView controladorReal = new ControllerView(modeloReal);

                // 4. INCIAMOS LA VISTA CON TODO CONECTADO
                System.out.println("=== PRUEBA DE INTEGRACIÓN COMPLETA (VISTA -> CTRL -> MODEL -> MOTOR) ===");
                new ConfigurarPartidaView(controladorReal, modeloReal).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BtnCancelar;
    private javax.swing.JButton BtnCrear;
    private javax.swing.JComboBox<String> CboxDimensiones;
    private javax.swing.JPanel JPnlFondo;
    // End of variables declaration//GEN-END:variables
}
