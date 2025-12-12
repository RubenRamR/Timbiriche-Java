/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package timbiriche.presentacion;

import com.mycompany.dominio.Jugador;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 *
 * @author Serva
 */
public class LobbyView extends JFrame implements Observer {
    
    private final ControllerView controlador;
    private final IModelViewLeible modelo;
    
    // Componentes UI
    private JPanel pnlJugadores;
    private JButton btnIniciar;
    private JSpinner spnDimension;
    private JLabel lblEstado;
    private String mensajeRechazo = null;

    public LobbyView(ControllerView controlador, IModelViewLeible modelo) {
        this.controlador = controlador;
        this.modelo = modelo;
        
        // Suscribirse al modelo
        this.modelo.agregarObservador(this);
        
        initComponents();
        setTitle("Lobby - Esperando jugadores");
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        // ===== PANEL SUPERIOR: TÍTULO =====
        JPanel pnlTitulo = new JPanel();
        pnlTitulo.setBackground(new Color(70, 130, 180));
        JLabel lblTitulo = new JLabel("🎮 LOBBY DE TIMBIRICHE");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        pnlTitulo.add(lblTitulo);
        add(pnlTitulo, BorderLayout.NORTH);

        // ===== PANEL CENTRAL: LISTA DE JUGADORES =====
        JPanel pnlCentro = new JPanel(new BorderLayout(10, 10));
        pnlCentro.setBackground(Color.WHITE);
        pnlCentro.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblSubtitulo = new JLabel("Jugadores conectados:");
        lblSubtitulo.setFont(new Font("Arial", Font.BOLD, 16));
        pnlCentro.add(lblSubtitulo, BorderLayout.NORTH);

        pnlJugadores = new JPanel();
        pnlJugadores.setLayout(new BoxLayout(pnlJugadores, BoxLayout.Y_AXIS));
        pnlJugadores.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(pnlJugadores);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        pnlCentro.add(scrollPane, BorderLayout.CENTER);

        add(pnlCentro, BorderLayout.CENTER);

        // ===== PANEL INFERIOR: CONFIGURACIÓN E INICIO =====
        JPanel pnlInferior = new JPanel();
        pnlInferior.setLayout(new BoxLayout(pnlInferior, BoxLayout.Y_AXIS));
        pnlInferior.setBackground(new Color(245, 245, 245));
        pnlInferior.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        lblEstado = new JLabel("Esperando más jugadores...");
        lblEstado.setFont(new Font("Arial", Font.ITALIC, 14));
        lblEstado.setAlignmentX(CENTER_ALIGNMENT);
        pnlInferior.add(lblEstado);
        pnlInferior.add(Box.createRigidArea(new Dimension(0, 15)));

        // SOLO EL HOST VE LA CONFIGURACIÓN
        if (modelo.esHost()) {
            // Panel configuración
            JPanel pnlConfig = new JPanel(new FlowLayout(FlowLayout.CENTER));
            pnlConfig.setBackground(new Color(245, 245, 245));
            
            JLabel lblDim = new JLabel("Tamaño del tablero:");
            lblDim.setFont(new Font("Arial", Font.PLAIN, 14));
            
            spnDimension = new JSpinner(new SpinnerNumberModel(10, 3, 20, 1));
            spnDimension.setPreferredSize(new Dimension(70, 30));
            
            pnlConfig.add(lblDim);
            pnlConfig.add(spnDimension);
            pnlInferior.add(pnlConfig);
            pnlInferior.add(Box.createRigidArea(new Dimension(0, 10)));

            // Botón iniciar
            btnIniciar = new JButton("🚀 INICIAR PARTIDA");
            btnIniciar.setFont(new Font("Arial", Font.BOLD, 16));
            btnIniciar.setBackground(new Color(50, 205, 50));
            btnIniciar.setForeground(Color.WHITE);
            btnIniciar.setFocusPainted(false);
            btnIniciar.setAlignmentX(CENTER_ALIGNMENT);
            btnIniciar.setMaximumSize(new Dimension(250, 50));
            btnIniciar.setEnabled(false); // Deshabilitado hasta tener 2+ jugadores
            
            btnIniciar.addActionListener(e -> solicitarInicio());
            
            pnlInferior.add(btnIniciar);
        } else {
            JLabel lblEspera = new JLabel("Esperando a que el host inicie...");
            lblEspera.setFont(new Font("Arial", Font.ITALIC, 16));
            lblEspera.setForeground(Color.GRAY);
            lblEspera.setAlignmentX(CENTER_ALIGNMENT);
            pnlInferior.add(lblEspera);
        }

        add(pnlInferior, BorderLayout.SOUTH);
    }

    // =========================================================================
    // IMPLEMENTAR Observer
    // =========================================================================
    
    @Override
    public void actualizar() {
        // Actualizar lista de jugadores
        List<Jugador> jugadores = modelo.getJugadores();
        actualizarListaJugadores(jugadores);
        
        // Si hay mensaje de rechazo, mostrarlo
        if (mensajeRechazo != null) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo iniciar:\n" + mensajeRechazo,
                "Inicio Rechazado",
                JOptionPane.WARNING_MESSAGE
            );
            mensajeRechazo = null;
            
            if (btnIniciar != null) {
                btnIniciar.setEnabled(true);
                btnIniciar.setText("🚀 INICIAR PARTIDA");
            }
        }
        
        // Si la partida inició, cerrar
        if (!modelo.isEnLobby()) {
            System.out.println("[LobbyView] Partida iniciada, cerrando lobby...");
            this.dispose();
        }
    }

    // =========================================================================
    // ACTUALIZACIÓN DE LISTA DE JUGADORES
    // =========================================================================
    
    private void actualizarListaJugadores(List<Jugador> jugadores) {
        SwingUtilities.invokeLater(() -> {
            pnlJugadores.removeAll();
            
            for (Jugador j : jugadores) {
                pnlJugadores.add(crearPanelJugador(j));
                pnlJugadores.add(Box.createRigidArea(new Dimension(0, 10)));
            }
            
            // Actualizar estado
            int count = jugadores.size();
            lblEstado.setText(count + " jugador(es) conectado(s)");
            
            // Habilitar botón si hay suficientes jugadores (SOLO HOST)
            if (modelo.esHost() && btnIniciar != null) {
                btnIniciar.setEnabled(count >= 2 && count <= 4);
            }
            
            pnlJugadores.revalidate();
            pnlJugadores.repaint();
        });
    }

    private JPanel crearPanelJugador(Jugador j) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(450, 60));

        // Avatar placeholder
        JPanel avatar = new JPanel();
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setBackground(decodificarColor(j.getColor()));
        avatar.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        panel.add(avatar, BorderLayout.WEST);

        // Nombre
        JLabel lblNombre = new JLabel(j.getNombre());
        lblNombre.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(lblNombre, BorderLayout.CENTER);

        // Indicador de jugador local
        if (j.getNombre().equals(modelo.getJugadorLocal().getNombre())) {
            JLabel lblYo = new JLabel("(TÚ)");
            lblYo.setFont(new Font("Arial", Font.ITALIC, 12));
            lblYo.setForeground(Color.BLUE);
            panel.add(lblYo, BorderLayout.EAST);
        }

        return panel;
    }

    // =========================================================================
    // SOLICITUD DE INICIO (VÍA CONTROLADOR)
    // =========================================================================
    
    private void solicitarInicio() {
        int dimension = (int) spnDimension.getValue();
        
        System.out.println("[LobbyView] Solicitando inicio con dimensión: " + dimension);
        
        // FLUJO CORRECTO: Vista → Controlador → Modelo → Motor
        controlador.onSolicitarInicioPartida(dimension);
        
        // Deshabilitar botón mientras espera respuesta
        btnIniciar.setEnabled(false);
        btnIniciar.setText("Iniciando...");
        lblEstado.setText("Iniciando partida...");
    }

    // =========================================================================
    // MANEJO DE RECHAZO (llamado desde el modelo)
    // =========================================================================
    
    /**
     * El ModelView llama a este método cuando recibe INICIO_RECHAZADO
     */
    public void mostrarRechazo(String motivo) {
        this.mensajeRechazo = motivo;
        // El actualizar() mostrará el diálogo
    }

    // =========================================================================
    // UTILIDADES
    // =========================================================================
    
    private Color decodificarColor(String hex) {
        try {
            return Color.decode(hex);
        } catch (Exception e) {
            return Color.GRAY;
        }
    }
}