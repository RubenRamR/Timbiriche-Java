/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package timbiriche.presentacion;

import com.mycompany.dominio.Cuadro;
import com.mycompany.dominio.Jugador;
import com.mycompany.dominio.Linea;
import com.mycompany.dominio.Punto;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author rramirez
 */
public class GameView extends javax.swing.JFrame implements Observer {

    private final ControllerView controlador;
    private final IModelViewLeible modeloLeible;

    // Componentes de la UI
    private final JPanel pnlLienzo;   // Centro: Tablero
    private final JPanel pnlMarcador; // Derecha: Nombres y Puntos

    // Componente específico para el turno (Nuevo)
    private JLabel lblEstadoTurno;

    // Configuración Visual Tablero
    private static final int RADIO_PUNTO = 12;
    private static final int GROSOR_LINEA = 6;
    private static final int TOLERANCIA_CLIC = 20;

    public GameView(ControllerView controlador, IModelViewLeible modeloLeible) {
        this.controlador = controlador;
        this.modeloLeible = modeloLeible;

        initComponents();

        // 1. Configuración de Ventana
        actualizarTitulo();
//        setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

        // 2. Observer
        this.modeloLeible.agregarObservador(this);

        // 3. Layout Principal
        PnlFondo.setLayout(new BorderLayout());

        // --- A. PANEL DERECHO (MARCADOR Y TURNO) ---
        pnlMarcador = new JPanel();
        pnlMarcador.setPreferredSize(new Dimension(320, 0)); // Un poco más ancho
        pnlMarcador.setBackground(new Color(245, 245, 245));
        pnlMarcador.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));
        pnlMarcador.setLayout(new BoxLayout(pnlMarcador, BoxLayout.Y_AXIS));

        PnlFondo.add(pnlMarcador, BorderLayout.EAST);

        // --- B. PANEL CENTRAL (TABLERO) ---
        pnlLienzo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujarJuegoCentrado((Graphics2D) g);
            }
        };
        pnlLienzo.setBackground(Color.WHITE);

        pnlLienzo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                manejarClic(e.getX(), e.getY());
            }
        });

        PnlFondo.add(pnlLienzo, BorderLayout.CENTER);

        // Carga inicial visual
        actualizarMarcador();
    }

    private void actualizarMarcador() {
        pnlMarcador.removeAll();

        // ---------------------------------------------------------
        // CASO A: JUEGO TERMINADO (MOSTRAR TABLA FINAL)
        // ---------------------------------------------------------
        if (modeloLeible.esJuegoTerminado())
        {
            JLabel lblFin = new JLabel("¡JUEGO TERMINADO!");
            lblFin.setFont(new Font("Arial", Font.BOLD, 24));
            lblFin.setForeground(Color.RED);
            lblFin.setAlignmentX(CENTER_ALIGNMENT);
            lblFin.setBorder(new EmptyBorder(30, 0, 20, 0));
            pnlMarcador.add(lblFin);

            // Ordenar jugadores por puntaje (Burbuja rápida o Stream)
            List<Jugador> ranking = new ArrayList<>(modeloLeible.getJugadores());
            ranking.sort((j1, j2) -> Integer.compare(j2.getPuntaje(), j1.getPuntaje())); // Descendente

            // Crear Tabla de Resultados
            JPanel pnlTabla = new JPanel();
            pnlTabla.setLayout(new BoxLayout(pnlTabla, BoxLayout.Y_AXIS));
            pnlTabla.setBackground(Color.WHITE);
            pnlTabla.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            // Encabezado
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Color.LIGHT_GRAY);
            header.add(new JLabel("  Posición "), BorderLayout.WEST);
            header.add(new JLabel("Jugador"), BorderLayout.CENTER);
            header.add(new JLabel("Pts  "), BorderLayout.EAST);
            pnlTabla.add(header);

            // Filas
            int pos = 1;
            for (Jugador j : ranking)
            {
                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(Color.WHITE);
                row.setBorder(new EmptyBorder(10, 10, 10, 10));

                JLabel lblPos = new JLabel(" " + pos + "º ");
                lblPos.setFont(new Font("Arial", Font.BOLD, 22));
                if (pos == 1)
                {
                    lblPos.setForeground(new Color(218, 165, 32)); // Oro
                } else
                {
                    lblPos.setForeground(Color.DARK_GRAY);
                }

                JLabel lblName = new JLabel(j.getNombre());
                lblName.setFont(new Font("Segoe UI", Font.BOLD, 24));
                lblName.setForeground(decodificarColor(j.getColor()));

                JLabel lblPts = new JLabel(j.getPuntaje() + " pts  ");
                lblPts.setFont(new Font("Arial", Font.BOLD, 20)); // AUMENTADO A 20
                lblPts.setForeground(Color.BLACK);

                row.add(lblPos, BorderLayout.WEST);
                row.add(lblName, BorderLayout.CENTER);
                row.add(lblPts, BorderLayout.EAST);

                pnlTabla.add(row);

                pnlTabla.add(javax.swing.Box.createRigidArea(new Dimension(0, 5)));

                pos++;
            }

            pnlMarcador.add(pnlTabla);

            // pnlMarcador.add(new JButton("Salir"));
        } // ---------------------------------------------------------
        // CASO B: JUEGO EN CURSO (MOSTRAR TURNO NORMAL)
        // ---------------------------------------------------------
        else
        {
            JLabel lblTituloTurno = new JLabel("TURNO ACTUAL:");
            lblTituloTurno.setFont(new Font("Arial", Font.PLAIN, 16));
            lblTituloTurno.setAlignmentX(CENTER_ALIGNMENT);
            lblTituloTurno.setBorder(new EmptyBorder(30, 0, 10, 0));

            lblEstadoTurno = new JLabel("Esperando...");
            lblEstadoTurno.setFont(new Font("Segoe UI", Font.BOLD, 28));
            lblEstadoTurno.setAlignmentX(CENTER_ALIGNMENT);

            Jugador jugadorTurno = modeloLeible.getTurnoActual();
            if (jugadorTurno != null)
            {
                lblEstadoTurno.setText(jugadorTurno.getNombre());
                lblEstadoTurno.setForeground(decodificarColor(jugadorTurno.getColor()));
            }

            pnlMarcador.add(lblTituloTurno);
            pnlMarcador.add(lblEstadoTurno);
            // ... el resto de la lista de jugadores ...
            pnlMarcador.add(javax.swing.Box.createRigidArea(new Dimension(0, 40)));
            // Agregar lista de puntajes parciales...
            List<Jugador> jugadores = modeloLeible.getJugadores();
            if (jugadores != null)
            {
                for (Jugador j : jugadores)
                {
                    pnlMarcador.add(crearPanelJugador(j));
                    pnlMarcador.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        }

        pnlMarcador.revalidate();
        pnlMarcador.repaint();
    }

    private JPanel crearPanelJugador(Jugador j) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        // Borde redondeado o simple
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(280, 80));

        // Avatar Placeholder
        JPanel pnlAvatar = new JPanel();
        pnlAvatar.setPreferredSize(new Dimension(50, 50));
        pnlAvatar.setBackground(Color.decode("#EEEEEE"));
        pnlAvatar.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        pnlAvatar.add(new JLabel("IMG"));
        panel.add(pnlAvatar, BorderLayout.WEST);

        // Info
        JPanel pnlInfo = new JPanel();
        pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.Y_AXIS));
        pnlInfo.setBackground(Color.WHITE);

        JLabel lblNombre = new JLabel(j.getNombre());
        lblNombre.setFont(new Font("Arial", Font.BOLD, 16));
        lblNombre.setForeground(decodificarColor(j.getColor())); // Color del jugador

        JLabel lblPuntos = new JLabel(j.getPuntaje() + " pts");
        lblPuntos.setFont(new Font("Arial", Font.BOLD, 14));
        lblPuntos.setForeground(Color.DARK_GRAY);

        pnlInfo.add(lblNombre);
        pnlInfo.add(lblPuntos);

        panel.add(pnlInfo, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // LÓGICA DE DIBUJO Y CLICS (Sin cambios en lógica, solo visualización)
    // =========================================================================
    private void dibujarJuegoCentrado(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        MetricasTablero m = calcularMetricas();

        // Puntos
        g2.setColor(Color.BLACK);
        for (int row = 0; row < m.dim; row++)
        {
            for (int col = 0; col < m.dim; col++)
            {
                int px = m.originX + (col * m.espacio);
                int py = m.originY + (row * m.espacio);
                g2.fillOval(px - RADIO_PUNTO / 2, py - RADIO_PUNTO / 2, RADIO_PUNTO, RADIO_PUNTO);
            }
        }

        // 2. Líneas
        List<Linea> lineas = modeloLeible.getLineasDibujadas();
        if (lineas != null) {
            g2.setStroke(new BasicStroke(GROSOR_LINEA));
            
            for (Linea l : lineas) {
                if (l.getPropietario() != null) {
                    g2.setColor(decodificarColor(l.getPropietario().getColor()));
                } else {
                    g2.setColor(Color.BLACK);
                }
                // ------------------------------

                int x1 = m.originX + (l.p1.getX() * m.espacio);
                int y1 = m.originY + (l.p1.getY() * m.espacio);
                int x2 = m.originX + (l.p2.getX() * m.espacio);
                int y2 = m.originY + (l.p2.getY() * m.espacio);
                g2.drawLine(x1, y1, x2, y2);
            }
        }

        // Cuadros
        for (Cuadro c : modeloLeible.getCuadrosRellenos())
        {
            if (c.getPropietario() != null)
            {
                Punto topLeft = getTopLeft(c.getLineas());
                if (topLeft != null)
                {
                    int px = m.originX + (topLeft.getX() * m.espacio);
                    int py = m.originY + (topLeft.getY() * m.espacio);

                    Color colorJug = decodificarColor(c.getPropietario().getColor());
                    g2.setColor(colorJug);
                    int offsetRelleno = GROSOR_LINEA;
                    int tamRelleno = m.espacio - (GROSOR_LINEA * 2);
                    g2.fillRect(px + offsetRelleno, py + offsetRelleno, tamRelleno, tamRelleno);

                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Arial", Font.BOLD, m.espacio / 2));
                    String letra = c.getPropietario().getNombre().substring(0, 1).toUpperCase();
                    int textW = g2.getFontMetrics().stringWidth(letra);
                    int textH = g2.getFontMetrics().getAscent();
                    g2.drawString(letra, px + (m.espacio / 2) - (textW / 2), py + (m.espacio / 2) + (textH / 4));
                }
            }
        }
    }

    private void manejarClic(int mouseX, int mouseY) {
        MetricasTablero m = calcularMetricas();
        int tolerancia = Math.min(TOLERANCIA_CLIC, m.espacio / 3);
        Linea mejorLinea = null;
        double menorDistancia = Double.MAX_VALUE;

        // Horizontales
        for (int row = 0; row < m.dim; row++)
        {
            for (int col = 0; col < m.dim - 1; col++)
            {
                int x1 = m.originX + (col * m.espacio);
                int y1 = m.originY + (row * m.espacio);
                int x2 = x1 + m.espacio;
                if (mouseX >= x1 && mouseX <= x2)
                {
                    double dist = Math.abs(mouseY - y1);
                    if (dist <= tolerancia && dist < menorDistancia)
                    {
                        menorDistancia = dist;
                        mejorLinea = new Linea(new Punto(col, row), new Punto(col + 1, row));
                    }
                }
            }
        }
        // Verticales
        for (int col = 0; col < m.dim; col++)
        {
            for (int row = 0; row < m.dim - 1; row++)
            {
                int x1 = m.originX + (col * m.espacio);
                int y1 = m.originY + (row * m.espacio);
                int y2 = y1 + m.espacio;
                if (mouseY >= y1 && mouseY <= y2)
                {
                    double dist = Math.abs(mouseX - x1);
                    if (dist <= tolerancia && dist < menorDistancia)
                    {
                        menorDistancia = dist;
                        mejorLinea = new Linea(new Punto(col, row), new Punto(col, row + 1));
                    }
                }
            }
        }
        if (mejorLinea != null)
        {
            controlador.onClicRealizarJugada(mejorLinea);
        }
    }

    private MetricasTablero calcularMetricas() {
        int dim = modeloLeible.getDimension();
        int w = pnlLienzo.getWidth();
        int h = pnlLienzo.getHeight();
        int ladoTablero = Math.min(w, h) - 100;
        if (ladoTablero < 100)
        {
            ladoTablero = 100;
        }
        int espacio = ladoTablero / (dim - 1);
        int ladoReal = espacio * (dim - 1);
        int offsetX = (w - ladoReal) / 2;
        int offsetY = (h - ladoReal) / 2;
        return new MetricasTablero(offsetX, offsetY, espacio, dim);
    }

    private record MetricasTablero(int originX, int originY, int espacio, int dim) {

    }

    private Punto getTopLeft(List<Linea> lineas) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (Linea l : lineas)
        {
            minX = Math.min(minX, Math.min(l.p1.getX(), l.p2.getX()));
            minY = Math.min(minY, Math.min(l.p1.getY(), l.p2.getY()));
        }
        if (minX == Integer.MAX_VALUE)
        {
            return null;
        }
        return new Punto(minX, minY);
    }

    private Color decodificarColor(String hex) {
        try
        {
            return Color.decode(hex);
        } catch (Exception e)
        {
            return Color.GRAY;
        }
    }

    private void actualizarTitulo() {
        if (modeloLeible == null)
        {
            return;
        }
        StringBuilder sb = new StringBuilder("Timbiriche");
        if (modeloLeible.getJugadorLocal() != null)
        {
            sb.append(" | Soy: ").append(modeloLeible.getJugadorLocal().getNombre());
        }
        setTitle(sb.toString());
    }

    @Override
    public void actualizar() {
        actualizarTitulo(); 
        actualizarMarcador(); 
        pnlLienzo.repaint(); 
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PnlFondo = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        PnlFondo.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout PnlFondoLayout = new javax.swing.GroupLayout(PnlFondo);
        PnlFondo.setLayout(PnlFondoLayout);
        PnlFondoLayout.setHorizontalGroup(
            PnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 755, Short.MAX_VALUE)
        );
        PnlFondoLayout.setVerticalGroup(
            PnlFondoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 648, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PnlFondo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PnlFondo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        // 1. Configurar Look and Feel
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
            java.util.logging.Logger.getLogger(GameView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        // 2. Ejecutar la ventana
        java.awt.EventQueue.invokeLater(() ->
        {

            // --- DATOS DUMMY ---
            Jugador yo = new Jugador("Tester_UI", "#0000FF"); // Azul
            Jugador rival = new Jugador("Rival", "#FF0000"); // Rojo
            java.util.List<Jugador> listaJugadores = new java.util.ArrayList<>();
            listaJugadores.add(yo);
            listaJugadores.add(rival);

            // --- 3. MOCK DEL MODELO (Corregido) ---
            IModelViewLeible mockModelo = new IModelViewLeible() {
                @Override
                public int getDimension() {
                    return 10;
                }

                @Override
                public List<Linea> getLineasDibujadas() {
                    return new java.util.ArrayList<>();
                }

                @Override
                public List<Cuadro> getCuadrosRellenos() {
                    return new java.util.ArrayList<>();
                }

                @Override
                public List<Jugador> getJugadores() {
                    return listaJugadores;
                }

                @Override
                public Jugador getTurnoActual() {
                    return yo;
                }

                @Override
                public Jugador getJugadorLocal() {
                    return yo;
                }

                // --- MÉTODOS VISUALES IMPLEMENTADOS ---
                @Override
                public String getAvatarJugador(Jugador jugador) {
                    return "default.png"; // Retorno seguro
                }

                @Override
                public Color getColorJugador(Jugador jugador) {
                    // Lógica simple para convertir el String hex del jugador a Color real
                    if (jugador != null && jugador.getColor() != null)
                    {
                        try
                        {
                            return Color.decode(jugador.getColor());
                        } catch (NumberFormatException e)
                        {
                            return Color.BLACK; // Fallback si el hex está mal
                        }
                    }
                    return Color.BLACK;
                }

                @Override
                public boolean esJuegoTerminado() {
                    return false;
                }

                // Métodos vacíos de suscripción
                @Override
                public void agregarObservador(Observer o) {
                }

                @Override
                public void removerObservador(Observer o) {
                }

                @Override
                public void notificarObservadores() {
                }

                @Override
                public boolean isEnLobby() {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public boolean esHost() {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }
            };

            // --- 4. MOCK DEL CONTROLADOR ---
            ControllerView mockController = new ControllerView(null) {
                @Override
                public void onClicRealizarJugada(Linea linea) {
                    System.out.println("[TEST UI] Clic detectado: " + linea);
                }
            };

            // --- 5. INICIAR VENTANA ---
            new GameView(mockController, mockModelo).setVisible(true);
        });
    }
 

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PnlFondo;
    // End of variables declaration//GEN-END:variables
}
