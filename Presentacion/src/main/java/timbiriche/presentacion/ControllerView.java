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

    private IReceptorExterno receptorLogica;
    private Jugador jugadorLocal;
    private ObjectMapper jsonMapper;

    public ControllerView(IReceptorExterno receptorLogica, Jugador jugadorLocal) {
        this.receptorLogica = receptorLogica;
        this.jugadorLocal = jugadorLocal;
        this.jsonMapper = new ObjectMapper();
    }

    /**
     * Procesa el clic del usuario en el tablero.
     * Serializa la jugada y la envía como un DTO al receptor externo.
     * @param linea La línea calculada por la vista.
     */
    public void onClicRealizarJugada(Linea linea) {
        if (linea == null) {
            return;
        }

        try {
            // 1. Crear DTO
            DataDTO dto = new DataDTO(Protocolo.INTENTO_JUGADA);
            
            // 2. Serializar Payload
            String lineaJson = jsonMapper.writeValueAsString(linea);
            dto.setPayload(lineaJson);

            // 3. Asignar Origen (Vital para el servidor)
            if (jugadorLocal != null) {
                dto.setProyectoOrigen(jugadorLocal.getNombre());
            } else {
                dto.setProyectoOrigen("Anonimo");
            }

            // 4. Enviar
            // Nota: Aunque el receptor suele ser para entrada, en esta arquitectura 
            // el controlador lo usa para inyectar la jugada en el flujo del sistema.
            receptorLogica.recibirMensaje(dto);

        } catch (JsonProcessingException e) {
            System.err.println("ControllerView: Error al serializar jugada. " + e.getMessage());
        }
    }

    /**
     * Método del diagrama para procesar DTOs entrantes directamente si fuera necesario.
     * Retorna true si se procesó correctamente.
     */
    public boolean actualizarDesdeDTO(DataDTO estado) {
        if (estado == null) return false;
        // En esta arquitectura, el receptor maneja esto, pero cumplimos con el método del diagrama.
        receptorLogica.recibirMensaje(estado);
        return true;
    }

    /**
     * Configura mapeo de colores para los jugadores (Lógica visual).
     */
    public void configurarColores(Map<Jugador, Color> colores) {
        // Lógica para asignar colores específicos a jugadores si se requiere personalización
        // Por ahora, el ModelView ya extrae el color del objeto Jugador.
        System.out.println("Configurando colores personalizados para " + colores.size() + " jugadores.");
    }
}