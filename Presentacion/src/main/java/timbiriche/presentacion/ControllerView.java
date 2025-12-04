package timbiriche.presentacion;

import com.mycompany.dtos.DataDTO;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import com.mycompany.protocolo.Protocolo;

/**
 * Controlador de la vista.
 *
 * - No conoce MotorJuego ni RED.
 * - Solo conoce la interfaz IReceptorExterno.
 * - Cuando la GameView le dice "el usuario hizo clic", construye un DataDTO
 *   y lo envía a través de esa interfaz.
 */
public class ControllerView {
    
    private final IReceptorExterno receptor;  // Puede ser ReceptorExternoImpl más adelante
    private final String idJugador;

    public ControllerView(IReceptorExterno receptor, String idJugador) {
        this.receptor = receptor;
        this.idJugador = idJugador;
    }
    
    /**
     * Llamado desde GameView cuando el usuario intenta una jugada.
     * tipoLinea: "H" o "V"
     * fila, col : coordenadas lógicas del segmento (a definir después).
     */
    public void onClicRealizarJugada(String tipoLinea, int fila, int col) {
        
        // 1. Armar DTO usando Protocolo
        DataDTO dto = new DataDTO(Protocolo.INTENTO_JUGADA);
        
        // Por ahora usamos un payload súper simple: "H;fila;col"
        String payload = tipoLinea + ";" + fila + ";" + col;
        
        dto.setPayload(payload);         
        dto.setProyectoOrigen(idJugador); // quién está realizando la jugada
        
        // 2. Mandar la jugada hacia la capa lógica a través de la interfaz.
        // En este momento se irá a un receptor "falso" de prueba.
        receptor.recibirMensaje(dto);
    }
}
