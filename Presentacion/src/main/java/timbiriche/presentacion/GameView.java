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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author rramirez
 */
public class GameView extends javax.swing.JFrame implements Observer {

    // Dependencias MVC
    private final ControllerView controlador;
    private final IModelViewLeible modeloLeible;

    // Componentes visuales
    private final JPanel pnlLienzo; // Panel interno donde pintaremos
    
    // Constantes de Diseño (GUI Logic)
    private static final int DIMENSION_LOGICA = 10; // 10x10 puntos
    private static final int MARGEN = 30;
    private static final int RADIO_PUNTO = 10;
    private static final int GROSOR_LINEA = 4;
    private static final int TOLERANCIA_CLIC = 15; // Píxeles de error permitidos al hacer clic

    /**
     * Constructor.
     * @param controlador Para enviar acciones (inputs).
     * @param modeloLeible Para leer el estado a dibujar (outputs).
     */
    public GameView(ControllerView controlador, IModelViewLeible modeloLeible) {
        this.controlador = controlador;
        this.modeloLeible = modeloLeible;

        // 1. Inicializar contenedor base (Generado por NetBeans)
        initComponents();
        
        // 2. Suscribirse al Modelo (Observer Pattern)
        this.modeloLeible.agregarObservador(this);

        // 3. Configurar Título y Ventana
        actualizarTitulo();
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        // 4. Inicializar el Lienzo de Dibujo Personalizado
        // Usamos una clase anónima o interna para sobreescribir paintComponent
        pnlLienzo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujarEstadoJuego((Graphics2D) g);
            }
        };
        pnlLienzo.setBackground(Color.WHITE);
        
        // Configurar listener de mouse para detectar jugadas
        pnlLienzo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                manejarClicTablero(e.getX(), e.getY());
            }
        });

        // 5. Agregar el lienzo al Panel de Fondo (PnlFondo)
        // Aseguramos que PnlFondo tenga un Layout que expanda el lienzo
        PnlFondo.setLayout(new BorderLayout());
        PnlFondo.add(pnlLienzo, BorderLayout.CENTER);
        
        // Refrescar estructura visual
        this.pack(); 
        // Forzamos un tamaño preferido si pack() queda muy chico
        if (this.getWidth() < 650) this.setSize(650, 680);
    }

    // =========================================================
    // LÓGICA DE DIBUJO (GUI PURA)
    // =========================================================

    /**
     * Método principal de dibujo. Se llama cada vez que el modelo notifica cambios.
     */
    private void dibujarEstadoJuego(Graphics2D g2) {
        // Configuración de calidad
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Cálculos de geometría visual
        int anchoPanel = pnlLienzo.getWidth();
        int altoPanel = pnlLienzo.getHeight();
        int ladoMenor = Math.min(anchoPanel, altoPanel);
        int areaUtil = ladoMenor - (2 * MARGEN);
        int espacio = areaUtil / (DIMENSION_LOGICA - 1); // Distancia entre puntos

        // A. DIBUJAR PUNTOS (GRID)
        g2.setColor(Color.BLACK);
        for (int row = 0; row < DIMENSION_LOGICA; row++) {
            for (int col = 0; col < DIMENSION_LOGICA; col++) {
                int x = MARGEN + (col * espacio);
                int y = MARGEN + (row * espacio);
                g2.fillOval(x - RADIO_PUNTO / 2, y - RADIO_PUNTO / 2, RADIO_PUNTO, RADIO_PUNTO);
            }
        }

        // B. DIBUJAR LÍNEAS YA CONFIRMADAS (Leemos del Modelo)
        List<Linea> lineas = modeloLeible.getLineasDibujadas();
        if (lineas != null) {
            g2.setStroke(new BasicStroke(GROSOR_LINEA));
            g2.setColor(Color.BLUE); 

            for (Linea l : lineas) {
                // Convertir coordenadas lógicas (0,0) a píxeles (50,50)
                int x1 = MARGEN + (l.p1.getX() * espacio);
                int y1 = MARGEN + (l.p1.getY() * espacio);
                int x2 = MARGEN + (l.p2.getX() * espacio);
                int y2 = MARGEN + (l.p2.getY() * espacio);

                g2.drawLine(x1, y1, x2, y2);
            }
        }

        // C. DIBUJAR CUADROS COMPLETADOS (Leemos del Modelo)
        List<Cuadro> cuadros = modeloLeible.getCuadrosRellenos();
        if (cuadros != null) {
            for (Cuadro c : cuadros) {
                if (c.getPropietario() != null) {
                    // Determinar coordenadas visuales del cuadro
                    // Asumimos que el cuadro conoce sus líneas o su posición lógica
                    // Lógica visual: encontrar la esquina superior izquierda de las líneas del cuadro
                    Punto topLeft = calcularEsquinaSuperiorIzquierda(c.getLineas()); // Helper visual
                    
                    if (topLeft != null) {
                        int px = MARGEN + (topLeft.getX() * espacio);
                        int py = MARGEN + (topLeft.getY() * espacio);
                        
                        // 1. Rellenar con color del jugador
                        // Usamos un color fijo o decodificamos el hex del jugador
                        Color colorJugador = decodificarColor(c.getPropietario().getColor());
                        g2.setColor(colorJugador);
                        // Ajuste visual (+2, -4) para no tapar las líneas negras/azules
                        g2.fillRect(px + GROSOR_LINEA, py + GROSOR_LINEA, espacio - (GROSOR_LINEA*2), espacio - (GROSOR_LINEA*2));
                        
                        // 2. Dibujar Inicial
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("SansSerif", Font.BOLD, 24));
                        String inicial = c.getPropietario().getNombre().substring(0, 1).toUpperCase();
                        
                        // Centrar texto
                        int textX = px + (espacio / 2) - 8;
                        int textY = py + (espacio / 2) + 8;
                        g2.drawString(inicial, textX, textY);
                    }
                }
            }
        }
    }

    // =========================================================
    // MANEJO DE ENTRADA (MOUSE -> CONTROLADOR)
    // =========================================================

    private void manejarClicTablero(int mouseX, int mouseY) {
        // Transformación geométrica: Pixel -> Objeto Dominio
        Linea lineaIntentada = calcularLineaDesdePixeles(mouseX, mouseY);

        if (lineaIntentada != null) {
            System.out.println("[GameView] Clic detectado en línea: " + lineaIntentada.toString());
            // DELEGACIÓN: La vista NO decide si es válida, solo avisa al controlador
            controlador.onClicRealizarJugada(lineaIntentada);
        }
    }

    /**
     * Algoritmo de "Hitbox" (Caja de colisión).
     * Convierte un clic (x,y) en la línea lógica más cercana si está dentro de la tolerancia.
     */
    private Linea calcularLineaDesdePixeles(int mx, int my) {
        int anchoPanel = pnlLienzo.getWidth();
        int altoPanel = pnlLienzo.getHeight();
        int ladoMenor = Math.min(anchoPanel, altoPanel);
        int areaUtil = ladoMenor - (2 * MARGEN);
        int espacio = areaUtil / (DIMENSION_LOGICA - 1);

        // 1. Revisar cercanía a líneas HORIZONTALES
        for (int row = 0; row < DIMENSION_LOGICA; row++) {
            for (int col = 0; col < DIMENSION_LOGICA - 1; col++) {
                int x1 = MARGEN + (col * espacio);
                int y1 = MARGEN + (row * espacio);
                int x2 = x1 + espacio; // La línea termina en el siguiente punto horizontal

                // ¿El mouse está dentro del segmento X y cerca del eje Y?
                if (mx >= x1 && mx <= x2 && Math.abs(my - y1) <= TOLERANCIA_CLIC) {
                    return new Linea(new Punto(col, row), new Punto(col + 1, row));
                }
            }
        }

        // 2. Revisar cercanía a líneas VERTICALES
        for (int col = 0; col < DIMENSION_LOGICA; col++) {
            for (int row = 0; row < DIMENSION_LOGICA - 1; row++) {
                int x1 = MARGEN + (col * espacio);
                int y1 = MARGEN + (row * espacio);
                int y2 = y1 + espacio; // La línea termina en el siguiente punto vertical

                // ¿El mouse está dentro del segmento Y y cerca del eje X?
                if (my >= y1 && my <= y2 && Math.abs(mx - x1) <= TOLERANCIA_CLIC) {
                    return new Linea(new Punto(col, row), new Punto(col, row + 1));
                }
            }
        }

        return null; // Clic en espacio vacío
    }

    // =========================================================
    // IMPLEMENTACIÓN DE OBSERVER (MODELO -> VISTA)
    // =========================================================

    @Override
    public void actualizar() {
        // El modelo cambió (llegó jugada de red o local).
        // 1. Actualizar textos (Turnos)
        actualizarTitulo();
        
        // 2. Repintar el lienzo
        pnlLienzo.repaint();
    }

    private void actualizarTitulo() {
        if (modeloLeible == null) return;
        
        StringBuilder sb = new StringBuilder("Timbiriche");
        
        Jugador local = modeloLeible.getJugadorLocal();
        if (local != null) sb.append(" | Soy: ").append(local.getNombre());
        
        Jugador turno = modeloLeible.getTurnoActual();
        if (turno != null) sb.append(" | Turno de: ").append(turno.getNombre());
        
        this.setTitle(sb.toString());
    }

    // =========================================================
    // MÉTODOS AUXILIARES VISUALES
    // =========================================================

    private Color decodificarColor(String hexColor) {
        if (hexColor == null || hexColor.isEmpty()) return Color.GRAY;
        try {
            return Color.decode(hexColor);
        } catch (NumberFormatException e) {
            return Color.GRAY;
        }
    }

    /**
     * Algoritmo visual para encontrar la esquina superior-izquierda de un cuadro
     * basado en sus 4 líneas. Necesario para saber dónde hacer fillRect.
     */
    private Punto calcularEsquinaSuperiorIzquierda(List<Linea> lineasCuadro) {
        /* Lógica: El punto (x,y) más pequeño de todas las líneas es la esquina TL. */
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        for (Linea l : lineasCuadro) {
            if (l.p1.getX() < minX) minX = l.p1.getX();
            if (l.p2.getX() < minX) minX = l.p2.getX();
            
            if (l.p1.getY() < minY) minY = l.p1.getY();
            if (l.p2.getY() < minY) minY = l.p2.getY();
        }
        
        if (minX == Integer.MAX_VALUE) return null; // Error data
        return new Punto(minX, minY);
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
            .addGap(0, 680, Short.MAX_VALUE)
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
//        try
//        {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
//            {
//                if ("Nimbus".equals(info.getName()))
//                {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (Exception ex)
//        {
//            java.util.logging.Logger.getLogger(GameView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        // Crear instancias (ejemplo; inyectar reales)
//        MotorJuego motor = new MotorJuego(new JsonSerializer());
//        ModelView model = new ModelView(motor);
//        ReceptorExternoImpl receptor = new ReceptorExternoImpl(motor, new JsonSerializer());
//        Jugador local = new Jugador("Player1", Color.RED);
//        ControllerView ctrl = new ControllerView(receptor, local, model);
//        java.awt.EventQueue.invokeLater(() -> new GameView(ctrl, model).setVisible(true));
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PnlFondo;
    // End of variables declaration//GEN-END:variables
}
