/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.servidor;

import com.mycompany.dtos.DataDTO;
import com.mycompany.interfacesdispatcher.IDispatcher;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rramirez
 */
public class Control implements IReceptorExterno, IFuenteConocimiento {

    private IDispatcher dispatcher;
    private final Blackboard blackboard;
    // Mapa: Clave = NombreJugador, Valor = IP Real (Ej. "192.168.1.5")
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

    // =========================================================================
    // ENTRADA
    // =========================================================================
    @Override
    public void recibirMensaje(DataDTO datos) {
        System.out.println("[Control] Recibido DTO: " + datos.getTipo());

        // Registramos la sesión usando la IP real que viene en el DTO
        if (!sesiones.containsKey(datos.getProyectoOrigen()))
        {

            String ipReal = datos.getIpRemitente();

            // Fallback de seguridad por si llega nulo
            if (ipReal == null || ipReal.isEmpty())
            {
                ipReal = "127.0.0.1";
            }

            sesiones.put(datos.getProyectoOrigen(), ipReal);
            System.out.println("[Control] Nueva sesión registrada: " + datos.getProyectoOrigen() + " -> IP: " + ipReal);
        }

        Evento evento = convertirDTOaEvento(datos);
        blackboard.publicarEvento(evento);
    }

    // =========================================================================
    // SALIDA (Procesamiento de Eventos)
    // =========================================================================
    @Override
    public void procesarEvento(Evento evento) {

        if (evento.getTipo().equals(EventosSistema.SOLICITUD_ENVIO))
        {

            System.out.println("[Control] Solicitud de envío detectada: Envío");
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

    // =========================================================================
    // BROADCAST (LÓGICA DE ENVÍO)
    // =========================================================================
    private void broadcastReal(DataDTO dto) {
        if (dispatcher == null)
        {
            return;
        }

        // -----------------------------------------------------------------
        // BLOQUE A: SIMULACIÓN LOCAL
        // Descomenta esto SOLO si quieres probar 3 ventanas en UNA sola PC.
        // -----------------------------------------------------------------
        /*
        // System.out.println("[Control] Enviando Broadcast Simulado (9000, 9001, 9002)...");
        // dispatcher.enviar(dto, "127.0.0.1", 9000); // Azul
        // dispatcher.enviar(dto, "127.0.0.1", 9001); // Rojo
        // dispatcher.enviar(dto, "127.0.0.1", 9002); // Morado
        // return; // Cortamos aquí para no ejecutar el bloque B
         */
        // -----------------------------------------------------------------
        // BLOQUE B: PRODUCCIÓN LAN
        // Usa las IPs reales guardadas y envía al puerto estándar 9000.
        // -----------------------------------------------------------------
        for (String ipDestino : sesiones.values())
        {

            // Validación mínima
            if (ipDestino == null || ipDestino.isEmpty())
            {
                continue;
            }

            System.out.println("[Control] Enviando a remoto: " + ipDestino);

            dispatcher.enviar(dto, ipDestino, 9000);
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
