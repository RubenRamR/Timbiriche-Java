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
 * - Contiene un PanelTablero donde eventualmente pintaremos las líneas reales.
 * - De momento, solo dibuja puntos y manda una jugada de ejemplo al hacer clic.
 * - Nunca conoce a MotorJuego ni a RED directamente: solo habla con ControllerView.
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
        
        // Botón para probar envío de jugada sin usar el mouse
        JButton btnJugadaDemo = new JButton("Enviar jugada demo (H;0;0)");
        btnJugadaDemo.addActionListener(e ->
            controller.onClicRealizarJugada("H", 0, 0)
        );
        add(btnJugadaDemo, BorderLayout.SOUTH);
        
        // Listener de ratón: por ahora manda siempre la misma jugada.
        panelTablero.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO: aquí más adelante convertiremos (x,y) a (tipoLinea,fila,col)
                controller.onClicRealizarJugada("H", 0, 0);
            }
        });
    }
    
    /**
     * Panel interno donde dibujaremos el tablero.
     * Por ahora solo dibuja una cuadrícula de puntos.
     * Más adelante se conectará a un modelo visual (ModelView) o ModeloJuego.
     */
    private static class PanelTablero extends JPanel {
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            int tam = 5;       // Número de puntos por fila/columna (5x5 ejemplo)
            int margen = 50;
            int ancho = getWidth() - 2 * margen;
            int alto  = getHeight() - 2 * margen;
            
            int pasoX = ancho / (tam - 1);
            int pasoY = alto / (tam - 1);
            
            // Dibujar puntos del tablero
            for (int i = 0; i < tam; i++) {
                for (int j = 0; j < tam; j++) {
                    int x = margen + j * pasoX;
                    int y = margen + i * pasoY;
                    g.fillOval(x - 4, y - 4, 8, 8);
                }
            }
            
            // Aquí, más adelante, dibujaremos también las líneas que vengan del modelo.
        }
    }
}
