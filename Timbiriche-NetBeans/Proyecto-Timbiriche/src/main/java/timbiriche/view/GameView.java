package timbiriche.view;

import timbiriche.modelView.ModelViewLeible;
import timbiriche.modelView.Observer;
import timbiriche.controller.ControllerView;
import timbiriche.back.EstadoVisual;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

public class GameView implements Observer {

    private final ModelViewLeible modeloLeible;
    private final ControllerView controladorView;

    public GameView(ModelViewLeible modeloLeible, ControllerView controladorView) {
        this.modeloLeible = modeloLeible;
        this.controladorView = controladorView;
    }

    @Override
    public void actualizar() {
        // Aquí redibujarías con Swing/JavaFX. Por ahora lo dejamos mínimo.
        EstadoVisual ev = modeloLeible.getEstadoVisual();
        // Ejemplo debug (si quieres):
        // System.out.println("Vista actualizada. Turno: " + ev.getTurnoActual());
    }

    /** Llamado por el framework de UI real (pendiente) */
    public void paintComponent(Graphics g) {
        // Pintado de líneas y cuadros usando ev = modeloLeible.getEstadoVisual()
    }

    /** Registrar listeners reales de ratón/teclado aquí cuando uses UI */
    public void inicializarListeners() { /* pendiente */ }

    /** Traductor de un clic a una Linea y delega al controlador (pendiente de grid) */
    public void manejarClic(MouseEvent evt) {
        // TODO: mapear coordenadas a una Linea y llamar:
        // controladorView.realizarJugada(nuevaLinea);
    }
}
