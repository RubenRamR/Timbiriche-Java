package timbiriche.presentacion;


/**
 * Punto de entrada SOLO para probar la presentación.
 *
 * No arranca RED, no arranca Servidor, no arranca ModeloJuego.
 * Solo construye:
 *   GameView -> ControllerView -> ReceptorExterno
 */
public class PresentacionMain {
    
    public static void main(String[] args) {
        
        String idJugador = "Jugador_1";  // identificador solo para pruebas
        
        // 1) Crear receptor "falso" que solo imprime lo que llega
        ReceptorExternoDummy receptorDummy = new ReceptorExternoDummy();
        
        // 2) Crear ControllerView y GameView
        ControllerView controller = new ControllerView(receptorDummy, idJugador);
        GameView vista = new GameView(controller);
        
        // 3) Mostrar la UI en el hilo de eventos de Swing
        java.awt.EventQueue.invokeLater(() -> vista.setVisible(true));
    }
}
