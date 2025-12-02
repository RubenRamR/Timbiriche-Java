package timbiriche.app;

import timbiriche.back.*;
import timbiriche.controller.ControllerView;
import timbiriche.modelView.*;
import timbiriche.view.GameView;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Lanzar en EDT
        SwingUtilities.invokeLater(() -> {
            // Modelo-View
            ModelView modelView = new ModelView();

            // Motor con tamaño (ajusta PEQUENO/MEDIANO/GRANDE)
            MotorJuego motor = new MotorJuegoSimulado(TamanoTablero.MEDIANO);

            // Controller
            ControllerView controller = new ControllerView(modelView, motor);

            // Vista Swing
            GameView view = new GameView(modelView, controller);
            modelView.agregarObservador(view);

            // Inicializar tamaño en el modelo (dispara primer repaint)
            modelView.setTamano(TamanoTablero.MEDIANO);

            // Frame
            JFrame frame = new JFrame("Timbiriche (MVC/Fachada) — Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(view);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
