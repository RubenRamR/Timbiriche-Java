package timbiriche.back;

import java.util.*;

/**
 * Motor simulado con reglas básicas de Timbiriche:
 * - Rechaza líneas repetidas o fuera del tablero.
 * - Detecta cierre de cuadros y los asigna al jugador actual.
 * - Si se cierra al menos un cuadro, el jugador repite turno; si no, alterna.
 *
 * El tamaño del tablero (n) es la cantidad de CUADROS por lado.
 * Existen (n+1) x (n+1) puntos con coordenadas de 0..n.
 */
public class MotorJuegoSimulado implements MotorJuego {

    private final int n; // cuadros por lado
    private final Set<Linea> lineas = new HashSet<>();
    private final Set<String> cuadrosTomados = new HashSet<>(); // "x,y" del topleft del cuadro
    private Jugador turno = Jugador.A;

    public MotorJuegoSimulado() {
        this(TamanoTablero.PEQUENO);
    }

    public MotorJuegoSimulado(TamanoTablero tamano) {
        this.n = mapearTamano(tamano);
    }

    private int mapearTamano(TamanoTablero t) {
        switch (t) {
            case PEQUENO: return 3;   // 3x3 cuadros (4x4 puntos)
            case MEDIANO: return 5;   // 5x5
            case GRANDE:  return 7;   // 7x7
            default:      return 3;
        }
    }

    @Override
    public ResultadoJugada procesarJugada(Linea linea) {
        if (linea == null) {
            // No cambia nada
            return new ResultadoJugada(null, Collections.emptyList(), turno);
        }

        // Validar que los puntos estén dentro (0..n)
        if (!estaDentro(linea)) {
            // Jugada inválida: no cambia turno
            return new ResultadoJugada(null, Collections.emptyList(), turno);
        }

        // Rechazar repetidos
        if (lineas.contains(linea)) {
            // Jugada repetida: no cambia turno
            return new ResultadoJugada(null, Collections.emptyList(), turno);
        }

        // Aceptar la línea
        lineas.add(linea);

        // Detectar nuevos cuadros cerrados asociados a esta línea
        List<Cuadro> nuevos = detectarCuadrosCerrados(linea, turno);

        // Próximo turno: si hubo cuadro, mantiene; si no, alterna
        Jugador proximo = nuevos.isEmpty() ? alternar(turno) : turno;
        this.turno = proximo;

        return new ResultadoJugada(linea, nuevos, proximo);
    }

    private boolean estaDentro(Linea l) {
        return enRango(l.getX1()) && enRango(l.getY1()) && enRango(l.getX2()) && enRango(l.getY2());
    }

    private boolean enRango(int v) {
        return 0 <= v && v <= n;
    }

    private Jugador alternar(Jugador j) {
        return (j == Jugador.A) ? Jugador.B : Jugador.A;
    }

    private List<Cuadro> detectarCuadrosCerrados(Linea l, Jugador jugador) {
        List<Cuadro> res = new ArrayList<>();

        boolean horizontal = (l.getY1() == l.getY2());
        int xMin = Math.min(l.getX1(), l.getX2());
        int yMin = Math.min(l.getY1(), l.getY2());

        if (horizontal) {
            // Posible cuadro ARRIBA: topleft = (xMin, yMin-1)
            if (yMin - 1 >= 0 && yMin - 1 < n) checarCuadro(xMin, yMin - 1, jugador, res);
            // Posible cuadro ABAJO: topleft = (xMin, yMin)
            if (yMin >= 0 && yMin < n) checarCuadro(xMin, yMin, jugador, res);
        } else {
            // vertical
            // Posible cuadro IZQUIERDA: topleft = (xMin - 1, yMin)
            if (xMin - 1 >= 0 && xMin - 1 < n) checarCuadro(xMin - 1, yMin, jugador, res);
            // Posible cuadro DERECHA: topleft = (xMin, yMin)
            if (xMin >= 0 && xMin < n) checarCuadro(xMin, yMin, jugador, res);
        }

        return res;
    }

    /**
     * Un cuadro (cx, cy) está cerrado si existen sus 4 lados:
     *  arriba: (cx,cy)-(cx+1,cy)
     *  abajo:  (cx,cy+1)-(cx+1,cy+1)
     *  izq:    (cx,cy)-(cx,cy+1)
     *  der:    (cx+1,cy)-(cx+1,cy+1)
     */
    private void checarCuadro(int cx, int cy, Jugador jugador, List<Cuadro> salida) {
        Linea arriba = new Linea(cx, cy, cx + 1, cy);
        Linea abajo  = new Linea(cx, cy + 1, cx + 1, cy + 1);
        Linea izq    = new Linea(cx, cy, cx, cy + 1);
        Linea der    = new Linea(cx + 1, cy, cx + 1, cy + 1);

        if (lineas.contains(arriba) && lineas.contains(abajo) &&
            lineas.contains(izq) && lineas.contains(der)) {

            String key = cx + "," + cy;
            if (cuadrosTomados.add(key)) { // solo si no estaba tomado
                salida.add(new Cuadro(cx, cy, jugador));
            }
        }
    }
}
