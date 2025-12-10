package timbiriche.presentacion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.dominio.*;
import com.mycompany.dtos.DataDTO;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import com.mycompany.protocolo.Protocolo;
import java.awt.Color;
import java.util.Map;

/**
 * Controlador de la vista.
 *
 * - No conoce MotorJuego ni RED. - Solo conoce la interfaz IReceptorExterno. -
 * Cuando la GameView le dice "el usuario hizo clic", construye un DataDTO y lo
 * envía a través de esa interfaz.
 */
public class ControllerView {

    private IModelViewModificable modeloModificable;

    /**
     * Constructor.
     *
     * @param modeloModificable La interfaz del ModelView que permite acciones.
     */
    public ControllerView(IModelViewModificable modeloModificable) {
        this.modeloModificable = modeloModificable;
    }

    /**
     * Procesa el clic del usuario en el tablero. Solo envía la intención de
     * jugar. La validación lógica y la conversión a DTO/JSON ocurrirán más
     * adelante (en el Motor y Dispatcher).
     *
     * @param linea La línea calculada por la vista visual.
     */
    public void onClicRealizarJugada(Linea linea) {
        // 1. Validación básica de UI (no nulos)
        if (linea == null)
        {
            return;
        }

        // 2. Delegación pura
        // El Controller dice: "El usuario quiere poner esta línea".
        // No le importa si se va por Red, si es Local o si es contra una IA.
        modeloModificable.actualizarJugadaLocal(linea);
    }
}
