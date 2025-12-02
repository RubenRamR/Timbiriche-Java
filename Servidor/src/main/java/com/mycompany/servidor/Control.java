/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.servidor;

import com.mycompany.dtos.DataDTO;
import com.mycompany.interfacesdispatcher.IDispatcher;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import com.mycompany.protocolo.Protocolo;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rramirez
 */
public class Control implements IReceptorExterno, IFuenteConocimiento {

    private IDispatcher dispatcher;
    private final Blackboard blackboard;
    private final Map<String, String> sesiones;

    public Control() {
        this.blackboard = new Blackboard();
        this.sesiones = new HashMap<>();
        this.blackboard.suscribir(this);
    }

    public void setDispatcher(IDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Blackboard getBlackboard() {
        return blackboard;
    }

    // ENTRADA
    @Override
    public void recibirMensaje(DataDTO datos) {
        System.out.println("[Control] Recibido DTO: " + datos.getTipo());
        if (datos.getTipo().equals(Protocolo.SOLICITUD_LOGIN.name()))
        {
            sesiones.put(datos.getProyectoOrigen(), datos.getPayload());
            System.out.println("[Control] Sesión registrada: " + datos.getProyectoOrigen());
        }

        Evento evento = convertirDTOaEvento(datos);
        blackboard.publicarEvento(evento);
    }

    // SALIDA LOOPBACK
    @Override
    public void procesarEvento(Evento evento) {

        if (evento.es(Protocolo.SOLICITUD_ENVIO))
        {

            System.out.println("[Control] Solicitud de envío detectada.");
            System.out.println("[Control] Ejecutando envío a la red.");

            if (evento.getDato() instanceof DataDTO)
            {
                if (dispatcher != null)
                {
                    broadcastReal((DataDTO) evento.getDato());
                } else
                {
                    System.err.println("[Control-ERROR] ¡El dispatcher es NULL! No puedo enviar.");
                }
            } else
            {
                System.err.println("[Control-ERROR] El dato no es un DataDTO.");
            }
        }
    }

    private void broadcastReal(DataDTO dto) {
        if (dispatcher == null)
        {
            return;
        }
        for (String idRed : sesiones.keySet())
        {
            dispatcher.enviar(dto, "127.0.0.1", 9000);
        }
    }

    private Evento convertirDTOaEvento(DataDTO dto) {
        return new Evento(
                dto.getTipo(),
                dto.getPayload(),
                dto.getProyectoOrigen()
        );
    }

    @Override
    public void setBlackboard(Blackboard bb) {
    }
}
