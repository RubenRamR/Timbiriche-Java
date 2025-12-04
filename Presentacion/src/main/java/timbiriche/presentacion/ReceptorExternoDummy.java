package timbiriche.presentacion;

import com.mycompany.dtos.DataDTO;
import com.mycompany.interfacesreceptor.IReceptorExterno;

/**
 * Implementación de IReceptorExterno SOLO para pruebas de presentación.
 *
 * - No habla con MotorJuego ni con la RED.
 * - Simplemente imprime el DTO que le llega para que veas en consola
 *   que GameView -> ControllerView -> IReceptorExterno funciona correctamente.
 */
public class ReceptorExternoDummy implements IReceptorExterno {

    @Override
    public void recibirMensaje(DataDTO dto) {
        System.out.println("[Receptor 'falso''] Recibí un DataDTO desde la UI:");
        System.out.println("  Tipo   : " + dto.getTipo());
        System.out.println("  Origen : " + dto.getProyectoOrigen());
        System.out.println("  Payload: " + dto.getPayload());
    }
}
