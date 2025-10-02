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
import java.awt.AlphaComposite;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

/**
 * GameView: JPanel Swing que dibuja el tablero de Timbiriche.
 * - Observa el ModelView.
 * - Dibuja líneas/cuadros por color según jugador.
 * - Hover de línea candidata (color del jugador en turno).
 * - Convierte el clic en Linea y delega al controlador.
 */
public class GameView extends JPanel implements Observer {

    private final ModelViewLeible modeloLeible;
    private final ControllerView controladorView;

    // Config visual
    private static final int PREF_W = 720;
    private static final int PREF_H = 720;
    private static final int MARGIN = 40;
    private static final int DOT_RADIUS = 6;
    private static final float LINE_WIDTH = 4f;

    // Colores por jugador
    private static final Color COLOR_A_LINEA = new Color(30, 90, 200);
    private static final Color COLOR_A_CUADRO = new Color(210, 230, 255);
    private static final Color COLOR_B_LINEA = new Color(200, 40, 40);
    private static final Color COLOR_B_CUADRO = new Color(255, 220, 210);
    private static final Color COLOR_GRID = new Color(60, 60, 60);
    private static final Color COLOR_HOVER = new Color(0, 0, 0, 120); // se sobreescribe a color del turno

    // Hover actual (puede ser nulo)
    private Linea hoverLinea = null;

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

        int n = mapearTamano(ev.getTamano());
        int points = n + 1;

        int usableW = getWidth() - 2 * MARGIN;
        int usableH = getHeight() - 2 * MARGIN;
        int cell = Math.min(usableW, usableH) / n;
        int offsetX = (getWidth() - (cell * n)) / 2;
        int offsetY = (getHeight() - (cell * n)) / 2;

        // 1) Cuadros rellenados por color de dueño
        List<Cuadro> cuadros = ev.getCuadrosRellenos();
        if (cuadros != null) {
            for (Cuadro c : cuadros) {
                int x = offsetX + c.getX() * cell;
                int y = offsetY + c.getY() * cell;
                g2.setColor(c.getDueno() == Jugador.A ? COLOR_A_CUADRO : COLOR_B_CUADRO);
                g2.fillRect(x + 2, y + 2, cell - 3, cell - 3);
            }
        }

        // 2) Líneas jugadas por color del propietario
        List<Linea> lineas = ev.getLineasDibujadas();
        List<Jugador> owners = ev.getPropietariosLineas();
        if (lineas != null && owners != null && owners.size() == lineas.size()) {
            g2.setStroke(new BasicStroke(LINE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int idx = 0; idx < lineas.size(); idx++) {
                Linea l = lineas.get(idx);
                Jugador j = owners.get(idx);
                g2.setColor(j == Jugador.A ? COLOR_A_LINEA : COLOR_B_LINEA);
                int x1 = offsetX + l.getX1() * cell;
                int y1 = offsetY + l.getY1() * cell;
                int x2 = offsetX + l.getX2() * cell;
                int y2 = offsetY + l.getY2() * cell;
                g2.drawLine(x1, y1, x2, y2);
            }
        }

        // 3) Línea hover (semi-transparente, color del jugador en turno)
        if (hoverLinea != null) {
            g2.setStroke(new BasicStroke(LINE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Color turnoColor = (ev.getTurnoActual() == Jugador.A) ? COLOR_A_LINEA : COLOR_B_LINEA;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            g2.setColor(turnoColor);
            int x1 = offsetX + hoverLinea.getX1() * cell;
            int y1 = offsetY + hoverLinea.getY1() * cell;
            int x2 = offsetX + hoverLinea.getX2() * cell;
            int y2 = offsetY + hoverLinea.getY2() * cell;
            g2.drawLine(x1, y1, x2, y2);
            g2.setComposite(AlphaComposite.SrcOver);
        }

        // 4) Puntos de la grilla
        g2.setColor(COLOR_GRID);
        for (int y = 0; y < points; y++) {
            for (int x = 0; x < points; x++) {
                int px = offsetX + x * cell;
                int py = offsetY + y * cell;
                g2.fillOval(px - DOT_RADIUS, py - DOT_RADIUS, DOT_RADIUS * 2, DOT_RADIUS * 2);
            }
        }

        // 5) Marcador y turno
        int puntosA = 0, puntosB = 0;
        if (cuadros != null) {
            for (Cuadro c : cuadros) {
                if (c.getDueno() == Jugador.A) puntosA++;
                else puntosB++;
            }
        }
        g2.setColor(Color.BLACK);
        String marcador = "A: " + puntosA + "  |  B: " + puntosB + "   —   Turno: " + ev.getTurnoActual();
        g2.drawString(marcador, 10, 18);

        g2.dispose();
    }

    private int mapearTamano(TamanoTablero t) {
        if (t == null) return 3;
        switch (t) {
            case PEQUENO: return 3;
            case MEDIANO: return 5;
            case GRANDE:  return 7;
            default:      return 3;
        }
    }

    // === Interacción: hover + clic → Linea ===
    private void inicializarListeners() {
        // Hover
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverLinea = calcularLineaDesdeMouse(e.getX(), e.getY());
                repaint();
            }
        });

        // Click
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Linea l = calcularLineaDesdeMouse(e.getX(), e.getY());
                if (l != null) {
                    controladorView.realizarJugada(l);
                }
            }
        });
    }

    /**
     * Traduce coordenadas de mouse a la línea más cercana de la celda bajo el cursor.
     * Si el cursor está fuera del tablero o no hay celda válida, devuelve null.
     */
    private Linea calcularLineaDesdeMouse(int mx, int my) {
        EstadoVisual ev = modeloLeible.getEstadoVisual();
        if (ev == null || ev.getTamano() == null) return null;

        int n = mapearTamano(ev.getTamano());
        int usableW = getWidth() - 2 * MARGIN;
        int usableH = getHeight() - 2 * MARGIN;
        int cell = Math.min(usableW, usableH) / n;
        int offsetX = (getWidth() - (cell * n)) / 2;
        int offsetY = (getHeight() - (cell * n)) / 2;

        // Fuera del tablero
        if (mx < offsetX || my < offsetY || mx > offsetX + cell * n || my > offsetY + cell * n) {
            return null;
        }

        // Coordenadas relativas
        double cx = (mx - offsetX) / (double) cell; // 0..n
        double cy = (my - offsetY) / (double) cell; // 0..n

        int i = (int) Math.floor(cx);
        int j = (int) Math.floor(cy);

        if (i >= n) i = n - 1;
        if (j >= n) j = n - 1;
        if (i < 0 || j < 0) return null;

        double dx = cx - i;
        double dy = cy - j;

        double distTop = dy;
        double distBottom = 1.0 - dy;
        double distLeft = dx;
        double distRight = 1.0 - dx;

        double min = Math.min(Math.min(distTop, distBottom), Math.min(distLeft, distRight));
        Linea l;

        if (min == distTop && j >= 0) {
            l = new Linea(i, j, i + 1, j);
        } else if (min == distBottom && j + 1 <= n) {
            l = new Linea(i, j + 1, i + 1, j + 1);
        } else if (min == distLeft && i >= 0) {
            l = new Linea(i, j, i, j + 1);
        } else {
            l = new Linea(i + 1, j, i + 1, j + 1);
        }

        // Si ya existe esa línea, seguimos mostrando hover (visual),
        // pero al hacer click, el motor la rechazará; opcionalmente podemos ocultar hover si ya está jugada:
        List<Linea> existentes = ev.getLineasDibujadas();
        if (existentes != null) {
            for (Linea ex : existentes) {
                if (ex.equals(l)) {
                    // ya existe; si prefieres no mostrar hover en líneas ocupadas, descomenta:
                    // return null;
                    break;
                }
            }
        }
        return l;
    }
}
