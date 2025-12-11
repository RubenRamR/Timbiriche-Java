package timbiriche.presentacion;

import com.mycompany.dominio.*;


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
     * jugar.
     * @param linea La línea calculada por la vista visual.
     */
    public void onClicRealizarJugada(Linea linea) {
        if (linea == null)
        {
            return;
        }

        modeloModificable.actualizarJugadaLocal(linea);
    }
}
