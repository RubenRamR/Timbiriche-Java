/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.servidorprueba;

import com.mycompany.libreriacomun.DTOs.DataDTO;
import com.mycompany.libreriacomun.Interfaces.IDispatcher;
import com.mycompany.libreriacomun.Interfaces.IReceptorExterno;

/**
 *
 * @author rramirez Implementación de IReceptorExterno
 */
public class ServidorLogica implements IReceptorExterno {

    private IDispatcher dispatcher;

    public void setDispatcher(IDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void recibirMensaje(DataDTO datos) {
        System.out.println("[SERVIDOR] -Llegó: " + datos.getTipo() + " -> " + datos.getPayload());

        if (datos.es(Protocolo.INTENTO_JUGADA))
        {

            String contenido = datos.getPayload();

            responder("Validado: " + contenido, Protocolo.ACTUALIZAR_TABLERO, "127.0.0.1", 9000);
        }
    }

    private void responder(String texto, Protocolo tipo, String ip, int puerto) {
        DataDTO respuesta = new DataDTO(tipo);
        respuesta.setPayload(texto);
        respuesta.setProyectoOrigen("Servidor");

        if (dispatcher != null)
        {
            dispatcher.enviar(respuesta, ip, puerto);
            System.out.println("[SERVIDOR] -Respondí: " + tipo);
        }
    }
}
