package timbiriche.back;

import java.util.List;

/** “Snapshot” para que la vista dibuje. */
public class EstadoVisual {
    private final List<Linea> lineasDibujadas;
    private final List<Jugador> propietariosLineas; // NUEVO: mismo orden que lineasDibujadas
    private final List<Cuadro> cuadrosRellenos;
    private final Jugador turnoActual;
    private final TamanoTablero tamano;

    public EstadoVisual(List<Linea> lineasDibujadas,
                        List<Jugador> propietariosLineas,
                        List<Cuadro> cuadrosRellenos,
                        Jugador turnoActual,
                        TamanoTablero tamano) {
        this.lineasDibujadas = lineasDibujadas;
        this.propietariosLineas = propietariosLineas;
        this.cuadrosRellenos = cuadrosRellenos;
        this.turnoActual = turnoActual;
        this.tamano = tamano;
    }

    public List<Linea> getLineasDibujadas(){ return lineasDibujadas; }

    /** Devuelve una lista paralela a getLineasDibujadas() con el jugador que trazó cada línea. */
    public List<Jugador> getPropietariosLineas() { return propietariosLineas; }

    public List<Cuadro> getCuadrosRellenos(){ return cuadrosRellenos; }
    public Jugador getTurnoActual(){ return turnoActual; }
    public TamanoTablero getTamano(){ return tamano; }
}
