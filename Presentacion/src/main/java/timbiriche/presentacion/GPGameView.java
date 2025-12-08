package timbiriche.presentacion;

import com.mycompany.dominio.Linea;
import com.mycompany.dominio.Punto;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Vista principal del cliente Timbiriche.
 *
 * - Dibuja puntos y líneas que el usuario va marcando. - Detecta en qué
 * segmento (H/V, fila, col) hizo clic el usuario. - Envía esa jugada al
 * ControllerView.
 */
public class GPGameView extends JFrame {

    private final ControllerView controller;
    private final PanelTablero panelTablero;

    public GPGameView(ControllerView controller) {
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

        // Botón Demo actualizado para usar Objetos de Dominio
        JButton btnJugadaDemo = new JButton("Jugada demo (0,0 -> 1,0)");
        btnJugadaDemo.addActionListener(e ->
        {
            // Simular una línea horizontal en la primera posición
            Punto p1 = new Punto(0, 0);
            Punto p2 = new Punto(1, 0);
            Linea lineaDemo = new Linea(p1, p2);
            controller.onClicRealizarJugada(lineaDemo);
        });
        add(btnJugadaDemo, BorderLayout.SOUTH);
    }

    /**
     * Método público para permitir que el Modelo (vía Observer) actualice la
     * vista cuando llegue confirmación del servidor. (Por ahora mantiene su
     * propia caché visual 'optimista' en el panel).
     */
    public void repintarTablero() {
        panelTablero.repaint();
    }

    // =========================================================================
    //                            CLASE INTERNA: PANEL
    // =========================================================================
    private class PanelTablero extends JPanel {

        // --- Parámetros visuales ---
        private final int tam = 5;      // Dimensiones lógicas (5x5 puntos)
        private final int margen = 50;
        private final int tolerancia = 10; // Sensibilidad del clic

        // Cache visual simple (En MVC puro, esto debería venir del Modelo, 
        // pero lo mantenemos aquí para respuesta inmediata visual si se desea)
        private final boolean[][] lineasHorizontales;
        private final boolean[][] lineasVerticales;

        public PanelTablero() {
            this.lineasHorizontales = new boolean[tam][tam - 1];
            this.lineasVerticales = new boolean[tam - 1][tam];

            setBackground(Color.WHITE);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    procesarClick(e.getX(), e.getY());
                }
            });
        }

        /**
         * Lógica clave: Convierte (Pixel X, Pixel Y) -> Objeto Linea(P1, P2)
         */
        private void procesarClick(int xClick, int yClick) {
            int ancho = getWidth() - 2 * margen;
            int alto = getHeight() - 2 * margen;

            if (tam <= 1)
            {
                return;
            }

            int pasoX = ancho / (tam - 1);
            int pasoY = alto / (tam - 1);

            String tipoLinea = null;
            int fila = -1; // Y lógica
            int col = -1;  // X lógica

            // 1) Buscar coincidencia con LÍNEA HORIZONTAL
            // Una línea H conecta (col, fila) con (col+1, fila)
            for (int f = 0; f < tam; f++)
            {
                for (int c = 0; c < tam - 1; c++)
                {
                    int x1 = margen + c * pasoX;
                    int x2 = margen + (c + 1) * pasoX;
                    int y = margen + f * pasoY;

                    boolean dentroX = (xClick >= x1 && xClick <= x2);
                    boolean cercaY = Math.abs(yClick - y) <= tolerancia;

                    if (dentroX && cercaY)
                    {
                        tipoLinea = "H";
                        fila = f;
                        col = c;
                        break;
                    }
                }
                if (tipoLinea != null)
                {
                    break;
                }
            }

            // 2) Buscar coincidencia con LÍNEA VERTICAL
            // Una línea V conecta (col, fila) con (col, fila+1)
            if (tipoLinea == null)
            {
                for (int f = 0; f < tam - 1; f++)
                {
                    for (int c = 0; c < tam; c++)
                    {
                        int x = margen + c * pasoX;
                        int y1 = margen + f * pasoY;
                        int y2 = margen + (f + 1) * pasoY;

                        boolean dentroY = (yClick >= y1 && yClick <= y2);
                        boolean cercaX = Math.abs(xClick - x) <= tolerancia;

                        if (dentroY && cercaX)
                        {
                            tipoLinea = "V";
                            fila = f;
                            col = c;
                            break;
                        }
                    }
                    if (tipoLinea != null)
                    {
                        break;
                    }
                }
            }

            // 3) Si no fue un clic válido, salir
            if (tipoLinea == null)
            {
                return;
            }

            // 4) CONSTRUCCIÓN DEL OBJETO DE DOMINIO
            Punto p1, p2;

            if ("H".equals(tipoLinea))
            {
                // Horizontal: (col, fila) -> (col+1, fila)
                p1 = new Punto(col, fila);
                p2 = new Punto(col + 1, fila);

                // Actualización visual optimista (opcional)
                if (!lineasHorizontales[fila][col])
                {
                    lineasHorizontales[fila][col] = true;
                    repaint();
                }
            } else
            {
                // Vertical: (col, fila) -> (col, fila+1)
                p1 = new Punto(col, fila);
                p2 = new Punto(col, fila + 1);

                // Actualización visual optimista (opcional)
                if (!lineasVerticales[fila][col])
                {
                    lineasVerticales[fila][col] = true;
                    repaint();
                }
            }

            Linea lineaJugada = new Linea(p1, p2);

            System.out.println("[UI] Click -> Generada Linea: " + lineaJugada);

            // 5) ENVIAR AL CONTROLADOR
            controller.onClicRealizarJugada(lineaJugada);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(3)); // Líneas más gruesas

            int ancho = getWidth() - 2 * margen;
            int alto = getHeight() - 2 * margen;

            if (tam <= 1)
            {
                return;
            }

            int pasoX = ancho / (tam - 1);
            int pasoY = alto / (tam - 1);

            // A. Dibujar Líneas Horizontales Marcadas
            g2.setColor(Color.BLUE);
            for (int f = 0; f < tam; f++)
            {
                for (int c = 0; c < tam - 1; c++)
                {
                    if (lineasHorizontales[f][c])
                    {
                        int x1 = margen + c * pasoX;
                        int y = margen + f * pasoY;
                        int x2 = margen + (c + 1) * pasoX;
                        g2.drawLine(x1, y, x2, y);
                    }
                }
            }

            // B. Dibujar Líneas Verticales Marcadas
            g2.setColor(Color.RED);
            for (int f = 0; f < tam - 1; f++)
            {
                for (int c = 0; c < tam; c++)
                {
                    if (lineasVerticales[f][c])
                    {
                        int x = margen + c * pasoX;
                        int y1 = margen + f * pasoY;
                        int y2 = margen + (f + 1) * pasoY;
                        g2.drawLine(x, y1, x, y2);
                    }
                }
            }

            // C. Dibujar Puntos (Grid)
            g2.setColor(Color.BLACK);
            for (int i = 0; i < tam; i++)
            {
                for (int j = 0; j < tam; j++)
                {
                    int x = margen + j * pasoX;
                    int y = margen + i * pasoY;
                    g2.fillOval(x - 5, y - 5, 10, 10);
                }
            }
        }
    }
}
