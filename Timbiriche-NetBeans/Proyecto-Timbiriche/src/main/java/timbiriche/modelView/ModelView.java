package timbiriche.modelView;

import timbiriche.back.*;
import java.util.ArrayList;
import java.util.List;

public class ModelView implements ModelViewLeible, ModelViewModificable, Subject {

    private final List<Observer> observadores = new ArrayList<>();
    private TamanoTablero tamanoSeleccionado;
    private final List<Linea> lineasDibujadas = new ArrayList<>();
    private final List<Cuadro> cuadrosRellenos = new ArrayList<>();
    private Jugador turnoActual = Jugador.A;

    public ModelView(){}

    // Subject
    @Override public void agregarObservador(Observer o){ if(o!=null && !observadores.contains(o)) observadores.add(o); }
    @Override public void quitarObservador(Observer o){ observadores.remove(o); }
    @Override public void notificarObservadores(){ for(Observer o: observadores) o.actualizar(); }

    // Modificable
    @Override
    public void setEstadoVisual(ResultadoJugada datos) {
        if (datos == null) return;
        if (datos.getLineaDibujada()!=null) lineasDibujadas.add(datos.getLineaDibujada());
        if (datos.getCuadrosCompletados()!=null) cuadrosRellenos.addAll(datos.getCuadrosCompletados());
        if (datos.getProximoTurno()!=null) turnoActual = datos.getProximoTurno();
        notificarObservadores();
    }

    @Override
    public void setTamano(TamanoTablero tamano) {
        this.tamanoSeleccionado = tamano;
        lineasDibujadas.clear();
        cuadrosRellenos.clear();
        turnoActual = Jugador.A;
        notificarObservadores();
    }

    // Leible
    @Override
    public EstadoVisual getEstadoVisual() {
        return new EstadoVisual(
            new ArrayList<>(lineasDibujadas),
            new ArrayList<>(cuadrosRellenos),
            turnoActual,
            tamanoSeleccionado
        );
    }
}
