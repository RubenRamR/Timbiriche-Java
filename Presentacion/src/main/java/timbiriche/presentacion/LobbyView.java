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

    private JPanel pnlJugadores;
    private JButton btnListo;
    private JLabel lblEstado;

    public LobbyView(ControllerView controlador, IModelViewLeible modelo) {
        this.controlador = controlador;
        this.modelo = modelo;
        this.modelo.agregarObservador(this);

        initComponents();
        setTitle("Lobby - Sala de Espera");
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // --- PANEL DE LISTA (CENTRO) ---
        pnlJugadores = new JPanel();
        pnlJugadores.setLayout(new BoxLayout(pnlJugadores, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(pnlJugadores);
        add(scroll, BorderLayout.CENTER);

        // --- PANEL INFERIOR (BOTÓN) ---
        JPanel pnlInferior = new JPanel(new BorderLayout());
        pnlInferior.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        lblEstado = new JLabel("Presiona el botón para indicar que estás listo");
        lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
        
        btnListo = new JButton("👍 ¡ESTOY LISTO!");
        btnListo.setBackground(new Color(46, 204, 113));
        btnListo.setForeground(Color.WHITE);
        btnListo.setFont(new Font("Arial", Font.BOLD, 16));
        
        // La acción del botón
        btnListo.addActionListener(e -> {
            btnListo.setEnabled(false); // Deshabilita para evitar doble clic
            btnListo.setText("Esperando a los demás...");
            // Enviamos la solicitud de inicio (votación)
            controlador.onSolicitarInicioPartida(3); 
        });

        pnlInferior.add(lblEstado, BorderLayout.NORTH);
        pnlInferior.add(btnListo, BorderLayout.CENTER);
        add(pnlInferior, BorderLayout.SOUTH);
    }

    // =========================================================================
    // MÉTODO CLAVE: ACTUALIZACIÓN VISUAL
    // =========================================================================
    @Override
    public void actualizar() {
        // La lista de Jugador ya viene actualizada por el ModelView
        actualizarListaJugadores(modelo.getJugadores());

        // Manejar el cierre de la ventana si la partida ya comenzó
        if (!modelo.isEnLobby()) {
            this.dispose(); 
            return;
        }

        // Manejar el botón del jugador local después de un voto (si el servidor lo "des-listó" por error)
        Jugador yo = modelo.getJugadorLocal();
        if (yo != null && yo.isListo()) {
             btnListo.setEnabled(false);
             btnListo.setText("Esperando a los demás...");
        } else if (yo != null && !yo.isListo() && !btnListo.isEnabled()) {
            // Caso de rechazo o error, volvemos a habilitar el botón si es necesario
            btnListo.setEnabled(true);
            btnListo.setText("👍 ¡ESTOY LISTO!");
        }
    }

    private void actualizarListaJugadores(List<Jugador> jugadores) {
        SwingUtilities.invokeLater(() -> {
            pnlJugadores.removeAll();
            int listos = 0;
            
            for (Jugador j : jugadores) {
                if(j.isListo()) listos++;
                pnlJugadores.add(crearPanelJugador(j));
                pnlJugadores.add(Box.createRigidArea(new Dimension(0, 5)));
            }
            
            // Actualiza el estado general de votos
            lblEstado.setText("Votos: " + listos + " / " + jugadores.size());
            
            pnlJugadores.revalidate();
            pnlJugadores.repaint();
        });
    }

    private JPanel crearPanelJugador(Jugador j) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panel.setMaximumSize(new Dimension(480, 60));

        // 1. Color del jugador
        JPanel pnlColor = new JPanel();
        pnlColor.setPreferredSize(new Dimension(30, 30));
        try {
            pnlColor.setBackground(Color.decode(j.getColor()));
        } catch (Exception e) {
            pnlColor.setBackground(Color.GRAY);
        }
        panel.add(pnlColor, BorderLayout.WEST);

        // 2. Nombre
        String textoNombre = j.getNombre();
        if (j.getNombre().equals(modelo.getJugadorLocal().getNombre())) {
            textoNombre += " (TÚ)";
        }
        JLabel lblNombre = new JLabel(textoNombre);
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblNombre, BorderLayout.CENTER);

        // 3. INDICADOR DE ESTADO (La "Palomita")
        if (j.isListo()) { // <<-- AQUI ESTÁ LA LÓGICA DE LA PALOMITA
            JLabel lblCheck = new JLabel("✅ LISTO");
            lblCheck.setForeground(new Color(39, 174, 96)); // Verde oscuro
            lblCheck.setFont(new Font("Arial", Font.BOLD, 14));
            panel.add(lblCheck, BorderLayout.EAST);
            panel.setBackground(new Color(230, 255, 230)); // Fondo verde claro
        } else {
            JLabel lblEspera = new JLabel("⏳ Esperando...");
            lblEspera.setForeground(Color.GRAY);
            panel.add(lblEspera, BorderLayout.EAST);
            panel.setBackground(Color.WHITE);
        }

        return panel;
    }
}