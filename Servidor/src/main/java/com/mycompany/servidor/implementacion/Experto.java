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
    // CAMBIO 1: Eliminamos el ObjectMapper. El experto no toca JSON.

    public Experto(Object identificador, Blackboard bb) {
        this.identificador = identificador;
        this.blackboard = bb;
        // Eliminado: this.mapper = new ObjectMapper();
    }

    @Override
    public void setBlackboard(Blackboard bb) {
        this.blackboard = bb;
    }

    @Override
    public void procesarEvento(Evento evento) {
        if (evento.getTipo().equals(Protocolo.INTENTO_JUGADA.name()))
        {

            String quienJugo = (String) evento.getOrigen();
            System.out.println("[Experto] Procesando jugada recibida de: " + quienJugo);

            // CAMBIO 2: No hacemos cast a String. Tomamos el Objeto tal cual llega.
            Object datoOpaco = evento.getDato();

            ejecutarLogica(datoOpaco, quienJugo);
        }
    }

    // CAMBIO 3: Recibimos Object, no String
    private void ejecutarLogica(Object payloadObjeto, String quienJugo) {

        // 1. VALIDACIONES
        // Comparamos objetos, no textos
        if (!validarEstado(payloadObjeto))
        {
            System.out.println("[Experto] Jugada inválida (ya existe en historial).");
            return;
        }

        // 2. GUARDAR EN HISTORIA
        Evento hecho = new Evento(
                Protocolo.ACTUALIZAR_TABLERO.name(),
                payloadObjeto, // Guardamos el objeto vivo (Linea, etc.)
                quienJugo
        );
        blackboard.agregarEvento(hecho);

        // 3. GENERAR RESPUESTA PARA EL CLIENTE
        DataDTO respuesta = new DataDTO(Protocolo.ACTUALIZAR_TABLERO);

        // CAMBIO 4: Devolvemos el Objeto. 
        // La capa de red del servidor (JsonSerializador) lo convertirá a JSON al salir.
        respuesta.setPayload(payloadObjeto);
        respuesta.setProyectoOrigen(quienJugo);

        System.out.println("[Experto] Jugada válida. Reenviando Objeto a la red.");
        generarEventoSalida(respuesta);
    }

    // CAMBIO 5: Validación por igualdad de Objetos
    private boolean validarEstado(Object datosNuevos) {
        List<Evento> historia = blackboard.obtenerEventos();

        for (Evento e : historia)
        {
            // Solo comparamos contra eventos del mismo tipo (ACTUALIZAR_TABLERO)
            if (e.getTipo().equals(Protocolo.ACTUALIZAR_TABLERO.name()))
            {
                Object datosAntiguos = e.getDato();

                // USAMOS EQUALS:
                // Esto requiere que la clase 'Linea' en el cliente tenga implementado 
                // el método equals(). Si no, Java comparará referencias de memoria y 
                // esto podría fallar (dejar pasar duplicados).
                if (datosAntiguos != null && datosAntiguos.equals(datosNuevos))
                {
                    return false; // Ya existe, es inválido
                }
            }
        }
        return true;
    }

    private void generarEventoSalida(DataDTO dtoRespuesta) {
        Evento solicitud = new Evento(
                EventosSistema.SOLICITUD_ENVIO,
                dtoRespuesta,
                "SERVER"
        );
        blackboard.publicarEvento(solicitud);
    }
}
