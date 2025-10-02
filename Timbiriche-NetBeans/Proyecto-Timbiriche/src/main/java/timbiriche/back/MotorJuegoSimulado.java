package timbiriche.back;

import java.util.*;

/**
 * Motor simulado con reglas básicas:
 * - Rechaza líneas repetidas o fuera del tablero.
 * - Detecta cierre de cuadros y los asigna al jugador actual.
 * - Si se cierra al menos un cuadro, el jugador repite turno; si no, alterna.
 *
 * El tamaño del tablero (n) es la cantidad de CUADROS por lado.
 * Hay (n+1)x(n+1) puntos.
 */
public class MotorJuegoSimulado implements MotorJuego {

    private final int n; // cuadros por lado
    private final Set<Linea> lineas = new HashSet<>();
    private final Set<String> cuadrosTomados = new HashSet<>(); // "x,y" de la celda tomada
    private Jugador turno = Jugador.A;

    public MotorJuegoSimulado() {
        this(TamanoTablero.PEQUENO);
    }

    public MotorJuegoSimulado(TamanoTablero tamano) {
        this.n = mapearTamano(tamano);
    }

    private int mapearTamano(TamanoTablero t) {
        switch (t) {
            case PEQUENO: return 3;
            case MEDIANO: return 5;
            case GRANDE:  return 7;
            default:      return 3;
        }
    }

    @Override
    public ResultadoJugada procesarJugada(Linea linea) {
        if (linea == null) {
            return new ResultadoJugada(null, Collections.emptyList(), turno);
        }
        // Validar que los puntos están dentro del tablero (0..n)
        if (!estaDentro(linea)) {
            return new ResultadoJugada(null, Collections.emptyList(), turno);
        }
        // Rechazar jugada repetida
        if (lineas.contains(linea)) {
            return new ResultadoJugada(null, Collections.emptyList(), turno);
        }

        // Aceptar la línea
        lineas.add(linea);

        // Detectar cuadros nuevos asociados a esta línea
        List<Cuadro> nuevos = detectarCuadrosCerrados(linea, turno);

        // Decidir próximo turno
        Jugador proximo = nuevos.isEmpty() ? alternar(turno) : turno;
        this.turno = proximo;

        return new ResultadoJugada(linea, nuevos, proximo);
    }

    private boolean estaDentro(Linea l) {
        // puntos deben estar entre 0 y n (inclusive)
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

        // Una línea puede pertenecer a 2 cuadros: arriba/abajo (si horizontal) o izquierda/derecha (si vertical)
        if (horizontal) {
            // Posible cuadro ARRIBA: top-left = (xMin, yMin-1)
            if (yMin - 1 >= 0 && yMin - 1 < n) {
                checarCuadro(xMin, yMin - 1, jugador, res);
            }
            // Posible cuadro ABAJO: top-left = (xMin, yMin)
            if (yMin >= 0 && yMin < n) {
                checarCuadro(xMin, yMin, jugador, res);
            }
        } else { // vertical
            // Posible cuadro IZQUIERDA: top-left = (xMin - 1, yMin)
            if (xMin - 1 >= 0 && xMin - 1 < n) {
                checarCuadro(xMin - 1, yMin, jugador, res);
            }
            // Posible cuadro DERECHA: top-left = (xMin, yMin)
            if (xMin >= 0 && xMin < n) {
                checarCuadro(xMin, yMin, jugador, res);
            }
        }
        return res;
    }

    /**
     * Un cuadro con top-left (cx, cy) está cerrado si existen sus 4 líneas.
     * Si está cerrado y aún no estaba tomado, lo marcamos y lo agregamos como nuevo.
     */
    private void checarCuadro(int cx, int cy, Jugador jugador, List<Cuadro> salida) {
        // Líneas del cuadro (unidad):
        Linea arriba = new Linea(cx, cy, cx + 1, cy);
        Linea abajo  = new Linea(cx, cy + 1, cx + 1, cy + 1);
        Linea izq    = new Linea(cx, cy, cx, cy + 1);
        Linea der    = new Linea(cx + 1, cy, cx + 1, cy + 1);

        if (lineas.contains(arriba) && lineas.contains(abajo) && lineas.contains(izq) && lineas.contains(der)) {
            String key = cx + "," + cy;
            if (cuadrosTomados.add(key)) { // solo si no estaba tomado
                salida.add(new Cuadro(cx, cy, jugador));
            }
        }
    }
}
