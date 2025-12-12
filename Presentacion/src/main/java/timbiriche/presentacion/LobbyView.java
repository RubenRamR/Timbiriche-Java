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
    private JLabel lblEstado;

    public LobbyView(ControllerView controlador, IModelViewLeible modelo) {
        this.controlador = controlador;
        this.modelo = modelo;

        // Suscribirse al modelo
        this.modelo.agregarObservador(this);

        initComponents();
        setTitle("Lobby - Sala de Espera");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));

        // ===== PANEL SUPERIOR: TÍTULO =====
        JPanel pnlTitulo = new JPanel();
        pnlTitulo.setBackground(new Color(70, 130, 180));
        JLabel lblTitulo = new JLabel("🎮 SALA DE ESPERA");
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

        // ===== PANEL INFERIOR: ESTADO E INICIO =====
        JPanel pnlInferior = new JPanel();
        pnlInferior.setLayout(new BoxLayout(pnlInferior, BoxLayout.Y_AXIS));
        pnlInferior.setBackground(new Color(245, 245, 245));
        pnlInferior.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        lblEstado = new JLabel("Esperando que el host inicie la partida...");
        lblEstado.setFont(new Font("Arial", Font.ITALIC, 14));
        lblEstado.setAlignmentX(CENTER_ALIGNMENT);
        pnlInferior.add(lblEstado);
        pnlInferior.add(Box.createRigidArea(new Dimension(0, 15)));

        // SOLO EL HOST VE EL BOTÓN DE INICIAR
        if (modelo.esHost()) {
            btnIniciar = new JButton("🚀 INICIAR PARTIDA");
            btnIniciar.setFont(new Font("Arial", Font.BOLD, 16));
            btnIniciar.setBackground(new Color(50, 205, 50));
            btnIniciar.setForeground(Color.WHITE);
            btnIniciar.setFocusPainted(false);
            btnIniciar.setAlignmentX(CENTER_ALIGNMENT);
            btnIniciar.setMaximumSize(new Dimension(250, 50));
            btnIniciar.setEnabled(true); // Siempre habilitado (sin validación de jugadores)

            btnIniciar.addActionListener(e -> solicitarInicio());

            pnlInferior.add(btnIniciar);
        } else {
            JLabel lblEspera = new JLabel("Esperando a que el host inicie...");
            lblEspera.setFont(new Font("Arial", Font.PLAIN, 16));
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

        // Verificar si hubo rechazo
        if (modelo instanceof ModelView) {
            String rechazo = ((ModelView) modelo).consumirMensajeRechazo();
            if (rechazo != null) {
                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo iniciar:\n" + rechazo,
                        "Inicio Rechazado",
                        JOptionPane.WARNING_MESSAGE
                );

                if (btnIniciar != null) {
                    btnIniciar.setEnabled(true);
                    btnIniciar.setText("🚀 INICIAR PARTIDA");
                    lblEstado.setText("Esperando que el host inicie la partida...");
                }
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
            if (modelo.esHost()) {
                lblEstado.setText(count + " jugador(es) en la sala");
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
    // SOLICITUD DE INICIO (SOLO HOST - SIN CONFIGURACIÓN)
    // =========================================================================
    private void solicitarInicio() {
        System.out.println("[LobbyView] Host solicitando inicio de partida...");

        // FLUJO: Vista → Controlador → Modelo → Motor
        // Dimensión hardcodeada (puedes cambiarla aquí: 3-20)
        int dimensionTablero = 3; // <-- CAMBIAR AQUÍ SI QUIERES OTRA DIMENSIÓN

        controlador.onSolicitarInicioPartida(dimensionTablero);

        // Deshabilitar botón mientras espera respuesta
        btnIniciar.setEnabled(false);
        btnIniciar.setText("Iniciando...");
        lblEstado.setText("Iniciando partida...");
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
