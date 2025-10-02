package timbiriche.app;

import timbiriche.back.*;
import timbiriche.controller.ControllerView;
import timbiriche.modelView.*;
import timbiriche.view.GameView;

public class Main {
    public static void main(String[] args) {
        // ModelView
        ModelView modelView = new ModelView();

        // Back simulado
        MotorJuego motor = new MotorJuegoSimulado(TamanoTablero.PEQUENO);

        // Controller
        ControllerView controller = new ControllerView(modelView, motor);

        // Vista (observa cambios; sin UI real)
        GameView view = new GameView(modelView, controller);
        modelView.agregarObservador(view);

        // Inicializar tamaño
        modelView.setTamano(TamanoTablero.PEQUENO);

        // Probar una jugada
        Linea l = new Linea(0, 0, 1, 0);
        controller.realizarJugada(l);

        // Dump simple
        EstadoVisual ev = modelView.getEstadoVisual();
        System.out.println("Lineas: " + ev.getLineasDibujadas().size()
                + " | Cuadros: " + ev.getCuadrosRellenos().size()
                + " | Turno: " + ev.getTurnoActual());
    }
}
