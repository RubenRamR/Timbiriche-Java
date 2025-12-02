/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces.dispatcher;

import com.mycompany.red.DataDTO;

/**
 *
 * @author Serva
 */
public interface IDispatcher {
    /**
     * Notifica una actualización de estado a un destino específico.
     */
    void notificarActualizacion(DataDTO estado, String ip, int puerto);
}
