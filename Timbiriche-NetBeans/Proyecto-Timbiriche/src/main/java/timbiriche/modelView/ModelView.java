package timbiriche.modelView;

import timbiriche.back.*;
import java.util.ArrayList;
import java.util.List;

public class ModelView implements ModelViewLeible, ModelViewModificable, Subject {

    private final List<Observer> observadores = new ArrayList<>();
    private TamanoTablero tamanoSeleccionado;

    private final List<Linea> lineasDibujadas = new ArrayList<>();
    private final List<Jugador> propietariosLineas = new ArrayList<>(); // NUEVO: dueño por línea
    private final List<Cuadro> cuadrosRellenos = new ArrayList<>();
    private Jugador turnoActual = Jugador.A;

    public ModelView(){}

    // Subject
    @Override public void agregarObservador(Observer o){ if(o!=null && !observadores.contains(o)) observadores.add(o); }
    @Override public void quitarObservador(Observer o){ observadores.remove(o); }
    @Override public void notificarObservadores(){ for(Observer o: observadores) o.actualizar(); }

    // ModelViewModificable
    @Override
    public void setEstadoVisual(ResultadoJugada datos) {
        if (datos == null) return;

        // El jugador que hizo la jugada es el turnoActual ANTES de aplicar el resultado
        Jugador jugadorQueJugo = turnoActual;

        if (datos.getLineaDibujada()!=null) {
            lineasDibujadas.add(datos.getLineaDibujada());
            propietariosLineas.add(jugadorQueJugo); // registrar dueño de la línea
        }
        if (datos.getCuadrosCompletados()!=null) {
            cuadrosRellenos.addAll(datos.getCuadrosCompletados());
        }
        if (datos.getProximoTurno()!=null) {
            turnoActual = datos.getProximoTurno();
        }
        notificarObservadores();
    }

    @Override
    public void setTamano(TamanoTablero tamano) {
        this.tamanoSeleccionado = tamano;
        lineasDibujadas.clear();
        propietariosLineas.clear(); // limpiar dueños también
        cuadrosRellenos.clear();
        turnoActual = Jugador.A;
        notificarObservadores();
    }

    // ModelViewLeible
    @Override
    public EstadoVisual getEstadoVisual() {
        return new EstadoVisual(
            new ArrayList<>(lineasDibujadas),
            new ArrayList<>(propietariosLineas), // NUEVO: copia defensiva
            new ArrayList<>(cuadrosRellenos),
            turnoActual,
            tamanoSeleccionado
        );
    }
}
