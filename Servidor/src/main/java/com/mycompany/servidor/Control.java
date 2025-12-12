/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.servidor;

import com.mycompany.dtos.DataDTO;
import com.mycompany.interfacesdispatcher.IDispatcher;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import com.mycompany.protocolo.Protocolo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author rramirez
 */
public class Control implements IReceptorExterno, IFuenteConocimiento {

    private IDispatcher dispatcher;
    private final Blackboard blackboard;

    // Mapa: ID del Proyecto -> ClienteRemoto (IP + Puerto)
    private Map<String, ClienteRemoto> sesiones = new HashMap<>();

    // Lista de Jugadores (Cada uno es un Map<String, Object> para ser flexible)
    private final List<Map<String, Object>> listaJugadores;

    // Constantes
    private static final int MIN_JUGADORES = 2;
    private static final int DIMENSION_POR_DEFECTO = 3;

    public Control() {
        this.blackboard = new Blackboard();
        this.sesiones = new HashMap<>();
        this.listaJugadores = new ArrayList<>();
        this.blackboard.suscribir(this);
    }

    public void setDispatcher(IDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void recibirMensaje(DataDTO datos) {
        if (datos == null) {
            return;
        }

        System.out.println("[Control] Recibido: " + datos.getTipo());

        String ipRemitente = datos.getIpRemitente() != null ? datos.getIpRemitente() : "127.0.0.1";

        if (datos.getTipo().equals(Protocolo.REGISTRO.name())) {
            manejarRegistroFisico(datos, ipRemitente);
        }

        Evento evento = convertirDTOaEvento(datos);
        blackboard.publicarEvento(evento);
    }

    private void manejarRegistroFisico(DataDTO datos, String ip) {
        try {
            if (datos.getPayload() instanceof Map) {
                Map<String, Object> p = (Map<String, Object>) datos.getPayload();
                String nombre = (String) p.get("nombre");
                int puerto = ((Number) p.get("puertoEscucha")).intValue();

                // Guardamos sesión de red
                sesiones.put(nombre, new ClienteRemoto(ip, puerto));

                // Añadimos estado "listo" inicial al payload para la lógica del lobby
                p.put("listo", false);

                System.out.println("[Control] Sesión guardada: " + nombre);
            }
        } catch (Exception e) {
            System.err.println("[Control] Error en registro físico: " + e.getMessage());
        }
    }

    @Override
    public void procesarEvento(Evento evento) {
        // --- 1. REGISTRO ---
        if (evento.getTipo().equals(Protocolo.REGISTRO.name())) {
            Map<String, Object> nuevoJugador = (Map<String, Object>) evento.getDato();
            String nombre = (String) nuevoJugador.get("nombre");

            // Evitar duplicados en la lista lógica
            boolean existe = listaJugadores.stream().anyMatch(j -> j.get("nombre").equals(nombre));

            if (!existe) {
                listaJugadores.add(nuevoJugador);
                System.out.println("[Control] Jugador en lista: " + nombre + ". Total: " + listaJugadores.size());
                enviarListaActualizada();
            }

            // --- 2. VOTACIÓN / INICIO ---
        } else if (evento.getTipo().equals(Protocolo.SOLICITUD_INICIO_PARTIDA.name())) {
            manejarVotacion(evento);

            // --- 3. ENVÍO GENÉRICO ---
        } else if (evento.getTipo().equals(EventosSistema.SOLICITUD_ENVIO)) {
            if (evento.getDato() instanceof DataDTO) {
                broadcastReal((DataDTO) evento.getDato());
            }
        }
    }

    private void manejarVotacion(Evento evento) {
        String nombreVotante = (String) evento.getOrigen();

        // 1. Marcar al jugador como listo en la lista
        for (Map<String, Object> jugador : listaJugadores) {
            if (jugador.get("nombre").equals(nombreVotante)) {
                jugador.put("listo", true);
                System.out.println("[Control] Voto registrado: " + nombreVotante);
                break;
            }
        }

        // 2. Contar cuántos están listos
        long listos = listaJugadores.stream().filter(j -> (boolean) j.get("listo")).count();
        int conectados = sesiones.size();

        // 3. Notificar a todos el cambio de estado (para que vean el Check ✅)
        enviarListaActualizada();

        // 4. ¿Todos listos?
        if (listos >= conectados && conectados >= MIN_JUGADORES) {
            System.out.println("[Control] 🚀 ¡Todos listos! Iniciando partida " + DIMENSION_POR_DEFECTO + "x" + DIMENSION_POR_DEFECTO);

            DataDTO inicio = new DataDTO();
            inicio.setTipo(Protocolo.INICIO_PARTIDA.name());

            Map<String, Object> config = new HashMap<>();
            config.put("dimension", DIMENSION_POR_DEFECTO);
            config.put("mensaje", "¡Partida iniciada por unanimidad!");
            inicio.setPayload(config);

            broadcastReal(inicio);
        } else {
            System.out.println("[Control] Votos: " + listos + "/" + conectados + ". Esperando...");
        }
    }

    private void enviarListaActualizada() {
        DataDTO syncDTO = new DataDTO();
        syncDTO.setTipo(Protocolo.LISTA_JUGADORES.name());
        syncDTO.setPayload(new ArrayList<>(listaJugadores));
        broadcastReal(syncDTO);
    }

    private void broadcastReal(DataDTO dto) {
        if (dispatcher == null) {
            return;
        }
        for (ClienteRemoto cliente : sesiones.values()) {
            dispatcher.enviar(dto, cliente.ip(), cliente.puerto());
        }
    }

    private Evento convertirDTOaEvento(DataDTO dto) {
        return new Evento(dto.getTipo(), dto.getPayload(), dto.getProyectoOrigen());
    }

    public Blackboard getBlackboard() {
        return blackboard;
    }

    @Override
    public void setBlackboard(Blackboard bb) {
    }

    private record ClienteRemoto(String ip, int puerto) {

    }
}
