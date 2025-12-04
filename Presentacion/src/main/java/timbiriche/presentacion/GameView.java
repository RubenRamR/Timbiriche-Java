package timbiriche.presentacion;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Vista principal del cliente Timbiriche.
 *
 * - Dibuja puntos y líneas que el usuario va marcando.
 * - Detecta en qué segmento (H/V, fila, col) hizo clic el usuario.
 * - Envía esa jugada al ControllerView.
 */
public class GameView extends JFrame {
    
    private final ControllerView controller;
    private final PanelTablero panelTablero;

    public GameView(ControllerView controller) {
        this.controller = controller;
        this.panelTablero = new PanelTablero();
        
        initUI();
    }

    private void initUI() {
        setTitle("Timbiriche - Cliente (Presentación)");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setLayout(new BorderLayout());
        add(panelTablero, BorderLayout.CENTER);
        
        // Botón para probar una jugada fija (para seguir probando)
        JButton btnJugadaDemo = new JButton("Jugada demo (H;0;0)");
        btnJugadaDemo.addActionListener(e ->
            controller.onClicRealizarJugada("H", 0, 0)
        );
        add(btnJugadaDemo, BorderLayout.SOUTH);
    }
    
    /**
     * Panel interno donde dibujamos el tablero y detectamos clicks.
     * 
     * Aquí guardamos también el estado de las líneas ya marcadas,
     * solo a nivel visual (presentación), sin MotorJuego todavía.
     */
    private class PanelTablero extends JPanel {
        
        // --- Parámetros del tablero ---
        private final int tam = 5;     // 5x5 puntos
        private final int margen = 50; // margen alrededor
        private final int tolerancia = 10; // px de "zona clickeable" alrededor de las líneas
        
        // Líneas dibujadas (true = ya dibujada)
        private final boolean[][] lineasHorizontales; // [fila][col]
        private final boolean[][] lineasVerticales;   // [fila][col]

        public PanelTablero() {
            this.lineasHorizontales = new boolean[tam][tam - 1];
            this.lineasVerticales = new boolean[tam - 1][tam];
            
            // Listener para detectar en qué segmento hizo clic el usuario
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    procesarClick(e.getX(), e.getY());
                }
            });
        }

        /**
         * Procesa el click del mouse en coordenadas de píxel (x, y),
         * detecta si está sobre una línea horizontal o vertical,
         * marca la línea y avisa al ControllerView.
         */
        private void procesarClick(int xClick, int yClick) {
            // Calculamos paso entre puntos tal como en paintComponent
            int ancho = getWidth() - 2 * margen;
            int alto  = getHeight() - 2 * margen;
            
            if (tam <= 1) {
                return;
            }
            
            int pasoX = ancho / (tam - 1);
            int pasoY = alto  / (tam - 1);
            
            String tipoLineaSeleccionada = null;
            int filaSeleccionada = -1;
            int colSeleccionada = -1;
            
            // 1) Buscar si hizo clic cerca de alguna LÍNEA HORIZONTAL
            for (int fila = 0; fila < tam; fila++) {
                for (int col = 0; col < tam - 1; col++) {
                    int x1 = margen + col * pasoX;
                    int x2 = margen + (col + 1) * pasoX;
                    int y  = margen + fila * pasoY;
                    
                    boolean dentroX = (xClick >= x1 && xClick <= x2);
                    boolean cercaY  = Math.abs(yClick - y) <= tolerancia;
                    
                    if (dentroX && cercaY) {
                        tipoLineaSeleccionada = "H";
                        filaSeleccionada = fila;
                        colSeleccionada = col;
                        break;
                    }
                }
                if (tipoLineaSeleccionada != null) {
                    break;
                }
            }
            
            // 2) Si no encontró horizontal, buscamos LÍNEA VERTICAL
            if (tipoLineaSeleccionada == null) {
                for (int fila = 0; fila < tam - 1; fila++) {
                    for (int col = 0; col < tam; col++) {
                        int x  = margen + col * pasoX;
                        int y1 = margen + fila * pasoY;
                        int y2 = margen + (fila + 1) * pasoY;
                        
                        boolean dentroY = (yClick >= y1 && yClick <= y2);
                        boolean cercaX  = Math.abs(xClick - x) <= tolerancia;
                        
                        if (dentroY && cercaX) {
                            tipoLineaSeleccionada = "V";
                            filaSeleccionada = fila;
                            colSeleccionada = col;
                            break;
                        }
                    }
                    if (tipoLineaSeleccionada != null) {
                        break;
                    }
                }
            }
            
            // 3) Si no se detectó ningún segmento, salimos
            if (tipoLineaSeleccionada == null) {
                System.out.println("[UI] Click fuera de una línea válida.");
                return;
            }
            
            // 4) Marcar visualmente la línea (si no estaba ya marcada)
            if ("H".equals(tipoLineaSeleccionada)) {
                if (!lineasHorizontales[filaSeleccionada][colSeleccionada]) {
                    lineasHorizontales[filaSeleccionada][colSeleccionada] = true;
                    repaint();
                }
            } else { // "V"
                if (!lineasVerticales[filaSeleccionada][colSeleccionada]) {
                    lineasVerticales[filaSeleccionada][colSeleccionada] = true;
                    repaint();
                }
            }
            
            // 5) Notificar al ControllerView la jugada real detectada
            System.out.println("[UI] Click detectado en " + tipoLineaSeleccionada 
                    + ";" + filaSeleccionada + ";" + colSeleccionada);
            
            controller.onClicRealizarJugada(
                    tipoLineaSeleccionada, 
                    filaSeleccionada, 
                    colSeleccionada
            );
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            int ancho = getWidth() - 2 * margen;
            int alto  = getHeight() - 2 * margen;
            
            if (tam <= 1) {
                return;
            }
            
            int pasoX = ancho / (tam - 1);
            int pasoY = alto  / (tam - 1);
            
            // --- 1. Dibujar puntos ---
            for (int i = 0; i < tam; i++) {
                for (int j = 0; j < tam; j++) {
                    int x = margen + j * pasoX;
                    int y = margen + i * pasoY;
                    g.fillOval(x - 4, y - 4, 8, 8);
                }
            }
            
            // --- 2. Dibujar líneas horizontales ya marcadas ---
            for (int fila = 0; fila < tam; fila++) {
                for (int col = 0; col < tam - 1; col++) {
                    if (lineasHorizontales[fila][col]) {
                        int x1 = margen + col * pasoX;
                        int y  = margen + fila * pasoY;
                        int x2 = margen + (col + 1) * pasoX;
                        g.drawLine(x1, y, x2, y);
                    }
                }
            }
            
            // --- 3. Dibujar líneas verticales ya marcadas ---
            for (int fila = 0; fila < tam - 1; fila++) {
                for (int col = 0; col < tam; col++) {
                    if (lineasVerticales[fila][col]) {
                        int x  = margen + col * pasoX;
                        int y1 = margen + fila * pasoY;
                        int y2 = margen + (fila + 1) * pasoY;
                        g.drawLine(x, y1, x, y2);
                    }
                }
            }
        }
    }
}
