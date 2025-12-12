package timbiriche.presentacion;

import com.mycompany.dominio.*;

public class ControllerView {

    private IModelViewModificable modeloModificable;

    /**
     * @param modeloModificable La interfaz del ModelView que permite acciones.
     */
    public ControllerView(IModelViewModificable modeloModificable) {
        this.modeloModificable = modeloModificable;
    }

    public void onClicRealizarJugada(Linea linea) {
        if (linea == null)
        {
            return;
        }

        modeloModificable.actualizarJugadaLocal(linea);
    }

    public void onClicCrearPartida(int tamano) {
        System.out.println("[CONTROLLER] Llego el tamaño" + tamano);
        modeloModificable.crearPartida(tamano);
    }
}
