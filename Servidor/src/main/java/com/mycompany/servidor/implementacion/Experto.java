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
        System.out.println("[Experto] Procesando jugada de: " + this.identificador);
        if (evento.getOrigen().equals(this.identificador))
        {

            if (evento.getTipo().equals(Protocolo.INTENTO_JUGADA.name()))
            {

                ejecutarLogica((String) evento.getDato());
            }
        }
    }

    private void ejecutarLogica(String payloadJson) {
        // 1. VALIDAR TURNO 
        Evento ultimoTurno = blackboard.obtenerUltimoEvento("TURNO_ACTUAL");
        // Nota: Si es el primer turno y devuelve null, quizás debas dejar pasar o validar inicio.
        if (ultimoTurno != null && !ultimoTurno.getDato().equals(this.identificador))
        {
            return;
        }

        // 2. VALIDAR ESTADO
        if (!validarEstado(payloadJson))
        {
            return;
        }

        // 3. GUARDAR 
        Evento hecho = new Evento(
                Protocolo.ACTUALIZAR_TABLERO.name(),
                payloadJson,
                this.identificador
        );
        blackboard.agregarEvento(hecho);

        // 4. EVALUAR REGLAS
        int puntos = evaluarReglas(payloadJson);

        // 5. RESPONDER (Paso 29)
        DataDTO respuesta = new DataDTO(Protocolo.ACTUALIZAR_TABLERO);
        generarEventoSalida(respuesta);
    }

    private boolean validarEstado(String datos) {
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
        System.out.println("[Experto] Publicando solicitud de envío: " + dtoRespuesta.getTipo());
        Evento solicitud = new Evento(
                EventosSistema.SOLICITUD_ENVIO,
                dtoRespuesta,
                this.identificador
        );
        blackboard.publicarEvento(solicitud);
    }

    private int evaluarReglas(String datos) {
        // Lógica simulada: retorna 0 por ahora
        return 0;
    }
}
