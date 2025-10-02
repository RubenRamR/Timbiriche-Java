package timbiriche.back;

import java.util.List;

/** “Snapshot” para que la vista dibuje. */
public class EstadoVisual {
    private final List<Linea> lineasDibujadas;
    private final List<Cuadro> cuadrosRellenos;
    private final Jugador turnoActual;
    private final TamanoTablero tamano;

    public EstadoVisual(List<Linea> lineasDibujadas, List<Cuadro> cuadrosRellenos,
                        Jugador turnoActual, TamanoTablero tamano) {
        this.lineasDibujadas = lineasDibujadas;
        this.cuadrosRellenos = cuadrosRellenos;
        this.turnoActual = turnoActual;
        this.tamano = tamano;
    }
    public List<Linea> getLineasDibujadas(){ return lineasDibujadas; }
    public List<Cuadro> getCuadrosRellenos(){ return cuadrosRellenos; }
    public Jugador getTurnoActual(){ return turnoActual; }
    public TamanoTablero getTamano(){ return tamano; }
}
