/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.servidor.implementacion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.dtos.DataDTO;
import com.mycompany.protocolo.Protocolo;
import com.mycompany.servidor.Blackboard;
import com.mycompany.servidor.Evento;
import com.mycompany.servidor.IFuenteConocimiento;
import java.util.List;
import com.mycompany.servidor.EventosSistema;

/**
 *
 * @author rramirez
 */
public class Experto implements IFuenteConocimiento {

    private final Object identificador;
    private Blackboard blackboard;
    private final ObjectMapper mapper;

    public Experto(Object identificador, Blackboard bb) {
        this.identificador = identificador;
        this.blackboard = bb;
        this.mapper = new ObjectMapper();
    }

    @Override
    public void setBlackboard(Blackboard bb) {
        this.blackboard = bb;
    }

    @Override
    public void procesarEvento(Evento evento) {
        if (evento.getTipo().equals(Protocolo.INTENTO_JUGADA.name()))
        {
            // Pasamos también el origen (quién hizo la jugada) para devolverlo en la respuesta
            String quienJugo = (String) evento.getOrigen();
            System.out.println("[Experto] Procesando jugada recibida de: " + quienJugo);

            ejecutarLogica((String) evento.getDato(), quienJugo);
        }
    }

    // Modificamos para recibir quién hizo la jugada
    private void ejecutarLogica(String payloadJson, String quienJugo) {
        // 1. VALIDACIONES (Simplificadas para la prueba)
        if (!validarEstado(payloadJson))
        {
            System.out.println("[Experto] Jugada inválida (ya existe en historial).");
            return;
        }

        // 2. GUARDAR EN HISTORIA (Persistence/Log)
        Evento hecho = new Evento(
                Protocolo.ACTUALIZAR_TABLERO.name(),
                payloadJson,
                quienJugo // Guardamos quién la hizo
        );
        blackboard.agregarEvento(hecho);

        // 3. GENERAR RESPUESTA PARA EL CLIENTE
        // Preparamos el DTO de vuelta
        DataDTO respuesta = new DataDTO(Protocolo.ACTUALIZAR_TABLERO);
        respuesta.setPayload(payloadJson); // Devolvemos la línea para que la pinten
        respuesta.setProyectoOrigen(quienJugo); // Decimos quién la pintó (para el color)

        System.out.println("[Experto] Jugada válida. Enviando ACTUALIZAR_TABLERO a la red.");
        generarEventoSalida(respuesta);
    }

    private boolean validarEstado(String datos) {
        // Evitar líneas duplicadas
        List<Evento> historia = blackboard.obtenerEventos();
        for (Evento e : historia)
        {
            if (e.getTipo().equals(Protocolo.ACTUALIZAR_TABLERO.name())
                    && e.getDato().equals(datos))
            {
                return false;
            }
        }
        return true;
    }

    private void generarEventoSalida(DataDTO dtoRespuesta) {
        // Creamos el evento que el Dispatcher del servidor escuchará para enviar por Socket
        Evento solicitud = new Evento(
                EventosSistema.SOLICITUD_ENVIO,
                dtoRespuesta,
                "SERVER"
        );
        blackboard.publicarEvento(solicitud);
    }
}
