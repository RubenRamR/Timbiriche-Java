package timbiriche.view;

import timbiriche.modelView.ModelViewLeible;
import timbiriche.modelView.Observer;
import timbiriche.controller.ControllerView;
import timbiriche.back.*;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * GameView: JPanel Swing que dibuja el tablero de Timbiriche.
 * - Observa el ModelView (Observer.actualizar() -> repaint()).
 * - Convierte clics del mouse a Linea y delega en ControllerView.realizarJugada(...).
 */
public class GameView extends JPanel implements Observer {

    private final ModelViewLeible modeloLeible;
    private final ControllerView controladorView;

    // Config visual
    private static final int PREF_W = 640;
    private static final int PREF_H = 640;
    private static final int MARGIN = 40;     // margen alrededor del tablero
    private static final int DOT_RADIUS = 6;  // radio de los puntos

    public GameView(ModelViewLeible modeloLeible, ControllerView controladorView) {
        this.modeloLeible = modeloLeible;
        this.controladorView = controladorView;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(PREF_W, PREF_H));
        inicializarListeners();
    }

    // === Observer ===
    @Override
    public void actualizar() {
        // Redibujar al recibir notificación del ModelView
        SwingUtilities.invokeLater(this::repaint);
    }

    // === Dibujo ===
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        EstadoVisual ev = modeloLeible.getEstadoVisual();
        if (ev == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int n = mapearTamano(ev.getTamano()); // cuadros por lado
        int points = n + 1;

        // tamaño útil y espaciamiento
        int usableW = getWidth() - 2 * MARGIN;
        int usableH = getHeight() - 2 * MARGIN;
        int cell = Math.min(usableW, usableH) / n;  // tamaño de cada cuadro
        int offsetX = (getWidth() - (cell * n)) / 2;  // centrar tablero
        int offsetY = (getHeight() - (cell * n)) / 2;

        // 1) Dibujar cuadros rellenados
        List<Cuadro> cuadros = ev.getCuadrosRellenos();
        if (cuadros != null) {
            for (Cuadro c : cuadros) {
                int x = offsetX + c.getX() * cell;
                int y = offsetY + c.getY() * cell;
                // Color suave según dueño
                g2.setColor(c.getDueno() == Jugador.A ? new Color(220, 240, 255) : new Color(255, 235, 220));
                g2.fillRect(x + 2, y + 2, cell - 3, cell - 3);
            }
        }

        // 2) Dibujar líneas jugadas
        List<Linea> lineas = ev.getLineasDibujadas();
        if (lineas != null) {
            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(50, 80, 160));
            for (Linea l : lineas) {
                int x1 = offsetX + l.getX1() * cell;
                int y1 = offsetY + l.getY1() * cell;
                int x2 = offsetX + l.getX2() * cell;
                int y2 = offsetY + l.getY2() * cell;
                g2.drawLine(x1, y1, x2, y2);
            }
        }

        // 3) Dibujar puntos
        g2.setColor(Color.DARK_GRAY);
        for (int y = 0; y < points; y++) {
            for (int x = 0; x < points; x++) {
                int px = offsetX + x * cell;
                int py = offsetY + y * cell;
                g2.fillOval(px - DOT_RADIUS, py - DOT_RADIUS, DOT_RADIUS * 2, DOT_RADIUS * 2);
            }
        }

        // 4) Info de turno (opcional)
        g2.setColor(Color.BLACK);
        if (ev.getTurnoActual() != null) {
            g2.drawString("Turno: " + ev.getTurnoActual(), 10, 16);
        }

        g2.dispose();
    }

    private int mapearTamano(TamanoTablero t) {
        if (t == null) return 3; // por defecto
        switch (t) {
            case PEQUENO: return 3;
            case MEDIANO: return 5;
            case GRANDE:  return 7;
            default:      return 3;
        }
    }

    // === Interacción: clic → Linea ===
    private void inicializarListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                EstadoVisual ev = modeloLeible.getEstadoVisual();
                if (ev == null || ev.getTamano() == null) return;

                int n = mapearTamano(ev.getTamano());
                int usableW = getWidth() - 2 * MARGIN;
                int usableH = getHeight() - 2 * MARGIN;
                int cell = Math.min(usableW, usableH) / n;
                int offsetX = (getWidth() - (cell * n)) / 2;
                int offsetY = (getHeight() - (cell * n)) / 2;

                int mx = e.getX();
                int my = e.getY();

                // Fuera del tablero
                if (mx < offsetX || my < offsetY || mx > offsetX + cell * n || my > offsetY + cell * n) {
                    return;
                }

                // Coordenadas relativas en celdas
                double cx = (mx - offsetX) / (double) cell; // 0..n
                double cy = (my - offsetY) / (double) cell; // 0..n

                // Identificar celda
                int i = (int) Math.floor(cx);
                int j = (int) Math.floor(cy);

                // Click justo en la última fila/col -> no hay celda abajo/derecha; clamp
                if (i >= n) i = n - 1;
                if (j >= n) j = n - 1;
                if (i < 0 || j < 0) return;

                // Distancias a bordes de la celda (0..1)
                double dx = cx - i; // 0..1
                double dy = cy - j; // 0..1

                // Elegir borde más cercano: top, bottom, left o right
                // Criterio: distancia al borde más cercano dentro de la celda
                double distTop = dy;
                double distBottom = 1.0 - dy;
                double distLeft = dx;
                double distRight = 1.0 - dx;

                double min = Math.min(Math.min(distTop, distBottom), Math.min(distLeft, distRight));
                Linea l;

                if (min == distTop && j >= 0) {
                    // borde superior: (i,j)-(i+1,j)
                    l = new Linea(i, j, i + 1, j);
                } else if (min == distBottom && j + 1 <= n) {
                    // borde inferior: (i,j+1)-(i+1,j+1)
                    l = new Linea(i, j + 1, i + 1, j + 1);
                } else if (min == distLeft && i >= 0) {
                    // borde izquierdo: (i,j)-(i,j+1)
                    l = new Linea(i, j, i, j + 1);
                } else {
                    // borde derecho: (i+1,j)-(i+1,j+1)
                    l = new Linea(i + 1, j, i + 1, j + 1);
                }

                controladorView.realizarJugada(l);
            }
        });
    }
}
