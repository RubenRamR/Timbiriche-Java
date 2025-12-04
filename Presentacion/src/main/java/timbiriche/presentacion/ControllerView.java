package timbiriche.presentacion;

import com.mycompany.dominio.*;
import com.mycompany.dtos.DataDTO;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import com.mycompany.protocolo.Protocolo;

/**
 * Controlador de la vista.
 *
 * - No conoce MotorJuego ni RED. - Solo conoce la interfaz IReceptorExterno. -
 * Cuando la GameView le dice "el usuario hizo clic", construye un DataDTO y lo
 * envía a través de esa interfaz.
 */
public class ControllerView {

    private IReceptorExterno receptorLogica;
    private Jugador jugadorLocal;

    public ControllerView() {

    }

    /**
     * Constructor
     *
     * @param receptorLogica Interfaz para comunicar con la capa de negocio/red.
     * @param jugadorLocal Referencia al jugador local para firmar los mensajes.
     */
    public ControllerView(IReceptorExterno receptorLogica, Jugador jugadorLocal) {
        this.receptorLogica = receptorLogica;
        this.jugadorLocal = jugadorLocal;
    }

    /**
     * Procesa el intento de jugada desde la interfaz gráfica.
     *
     * @param linea Objeto del dominio con los puntos p1 y p2 seleccionados.
     */
    public void onClicRealizarJugada(Linea linea) {
        // Validación defensiva
        if (linea == null || jugadorLocal == null)
        {
            return;
        }

        // 1. Armar DTO usando Protocolo
        // El constructor del DTO recibe el Enum del Protocolo directamente
        DataDTO dto = new DataDTO(Protocolo.INTENTO_JUGADA);

        // 2. Construir el Payload
        // Como "estamos usando Puntos", serializamos las coordenadas exactas.
        // Formato sugerido: "x1,y1,x2,y2" (fácil de partir con .split(","))
        String payload = linea.getP1().getX() + "," + linea.getP1().getY() + ","
                + linea.getP2().getX() + "," + linea.getP2().getY();

        dto.setPayload(payload);

        // Asignamos quién realiza la acción (id o nombre del jugador)
        dto.setProyectoOrigen(jugadorLocal.getNombre());

        // 3. Mandar la jugada hacia la capa lógica a través de la interfaz.
        if (receptorLogica != null)
        {
            receptorLogica.recibirMensaje(dto);
        }
    }
}
