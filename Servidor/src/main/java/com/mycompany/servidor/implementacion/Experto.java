/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.servidor.implementacion;

import com.mycompany.dtos.DataDTO;
import com.mycompany.protocolo.Protocolo;
import com.mycompany.servidor.Blackboard;
import com.mycompany.servidor.Evento;
import com.mycompany.servidor.IFuenteConocimiento;
import java.util.List;
import com.mycompany.servidor.EventosSistema;
import java.util.ArrayList;

/**
 *
 * @author rramirez
 */
public class Experto implements IFuenteConocimiento {

    private Object configuracionGuardada;
    private Blackboard blackboard;
    private List<Object> lista;

    public Experto(Object configuracionGuardada, Blackboard bb) {
        this.configuracionGuardada = configuracionGuardada;
        this.blackboard = bb;
        lista = new ArrayList<>();
    }

    @Override
    public void setBlackboard(Blackboard bb) {
        this.blackboard = bb;
    }

    @Override
    public void procesarEvento(Evento evento) {
        String origen = (String) evento.getOrigen();
        Object dato = evento.getDato();
        if (evento.getTipo().equals(Protocolo.INTENTO_JUGADA.name()))
        {

            String quienJugo = (String) evento.getOrigen();
            System.out.println("[Experto] Procesando jugada recibida de: " + quienJugo);

            Object datoOpaco = evento.getDato();

            ejecutarLogica(datoOpaco, quienJugo);
        }
        String tipo = evento.getTipo();
        Object payload = evento.getDato();

        // ---------------------------------------------------------------------
        // GUARDAR CONFIGURACIÓN (Solo guarda)
        // ---------------------------------------------------------------------
        if (tipo.equals(Protocolo.CREAR_PARTIDA.name()))
        {
            System.out.println("[Experto] Configuración recibida y guardada.");
            this.configuracionGuardada = payload;
        } // ---------------------------------------------------------------------
        // CASO 2: UNIRSE A LA PARTIDA (Aquí entra el Host Y el Guest)
        // ---------------------------------------------------------------------
        else if (tipo.equals(Protocolo.UNIRSE_PARTIDA.name()) || tipo.equals("REGISTRO"))
        {
            System.out.println("[Experto] Procesando ingreso de: " + origen);

            // A. Agregar a la lista (Evitar duplicados exactos)
            if (!lista.contains(payload))
            {
                lista.add(payload);
            }

            // B. Si hay una configuración guardada, se la enviamos al recién llegado
            if (this.configuracionGuardada != null)
            {
                DataDTO dtoConfig = new DataDTO(Protocolo.CREAR_PARTIDA);
                dtoConfig.setPayload(this.configuracionGuardada);
                dtoConfig.setProyectoDestino(origen); // Solo para él
                generarEventoSalida(dtoConfig);
            }

            publicarLista();
        }
    }

    private void ejecutarLogica(Object payloadObjeto, String quienJugo) {

        // 1. VALIDACIONES
        // Comparamos objetos
        if (!validarEstado(payloadObjeto))
        {
            System.out.println("[Experto] Jugada inválida (ya existe en historial).");
            return;
        }

        // 2. GUARDAR EN HISTORIA
        Evento hecho = new Evento(
                Protocolo.ACTUALIZAR_TABLERO.name(),
                payloadObjeto,
                quienJugo
        );
        blackboard.agregarEvento(hecho);

        // 3. GENERAR RESPUESTA PARA EL CLIENTE
        DataDTO respuesta = new DataDTO(Protocolo.ACTUALIZAR_TABLERO);

        // CAMBIO 4: Devolvemos el Objeto. 
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

    private void publicarLista() {
        DataDTO dto = new DataDTO(Protocolo.LISTA_JUGADORES);
        dto.setPayload(new ArrayList<>(this.lista));
        System.out.println("[Experto] Enviando lista de " + lista.size() + " jugadores a todos.");
        generarEventoSalida(dto);
    }
}
