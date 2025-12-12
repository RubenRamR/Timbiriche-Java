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

    // Lista genérica. El servidor NO sabe qué es un Jugador, solo guarda datos.
    private final List<Object> lista;

    // Constante para el mínimo de jugadores
    private static final int MIN_JUGADORES = 2;

    public Control() {
        this.blackboard = new Blackboard();
        this.sesiones = new HashMap<>();
        this.lista = new ArrayList<>();
        this.blackboard.suscribir(this);
    }

    public void setDispatcher(IDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    // =========================================================================
    // ENTRADA (Receptor de Red)
    // =========================================================================
    @Override
    public void recibirMensaje(DataDTO datos) {
        if (datos == null) {
            System.err.println("[Control] Error: DTO nulo.");
            return;
        }

        System.out.println("[Control] Recibido DTO Tipo: " + datos.getTipo());

        String ipRemitente = datos.getIpRemitente();
        if (ipRemitente == null || ipRemitente.isEmpty()) {
            ipRemitente = "127.0.0.1";
        }

        // Usamos el enum para REGISTRO
        if (datos.getTipo().equals(Protocolo.REGISTRO.name())) {
            try {
                Object payload = datos.getPayload();
                int puertoDelJugador = 0;
                String nombreJugador = datos.getProyectoOrigen();

                if (payload instanceof Map) {
                    Map<?, ?> mapaDatos = (Map<?, ?>) payload;
                    if (mapaDatos.containsKey("puertoEscucha")) {
                        Object val = mapaDatos.get("puertoEscucha");
                        puertoDelJugador = ((Number) val).intValue();
                    }
                }

                if (puertoDelJugador > 0) {
                    sesiones.put(
                            nombreJugador,
                            new ClienteRemoto(ipRemitente, puertoDelJugador)
                    );
                    System.out.println("[Control] Sesión guardada (Dinámica): " + nombreJugador
                            + " -> " + ipRemitente + ":" + puertoDelJugador);
                }

            } catch (Exception e) {
                System.err.println("[Control] Error extrayendo puerto del payload genérico: " + e.getMessage());
            }
        }
        // -----------------------------------------------------------

        Evento evento = convertirDTOaEvento(datos);
        blackboard.publicarEvento(evento);
    }

    // =========================================================================
    // PROCESADOR DE EVENTOS (IFuenteConocimiento)
    // =========================================================================
    @Override
    public void procesarEvento(Evento evento) {

        // 1. MANEJAR REGISTRO
        if (evento.getTipo().equals(Protocolo.REGISTRO.name())) {
            Object nuevoJugador = evento.getDato();

            if (!lista.contains(nuevoJugador)) {
                lista.add(nuevoJugador);
                System.out.println("[Control] Jugador agregado. Total: " + lista.size());

                DataDTO syncDTO = new DataDTO();
                syncDTO.setTipo(Protocolo.LISTA_JUGADORES.name()); // Usamos el enum
                syncDTO.setPayload(new ArrayList<>(lista));

                broadcastReal(syncDTO);
            } else {
                System.out.println("[Control] Jugador ya existe, omitiendo.");
            }

        // 2. MANEJAR SOLICITUD DE INICIO DE PARTIDA (¡El punto de corrección!)
        } else if (evento.getTipo().equals(Protocolo.SOLICITUD_INICIO_PARTIDA.name())) {
            manejarSolicitudInicio(evento);

        // 3. MANEJAR SOLICITUD DE ENVÍO (Para Expertos)
        } else if (evento.getTipo().equals(EventosSistema.SOLICITUD_ENVIO)) {
            System.out.println("[Control] Ejecutando envío a la red.");
            if (evento.getDato() instanceof DataDTO) {
                if (dispatcher != null) {
                    broadcastReal((DataDTO) evento.getDato());
                } else {
                    System.err.println("[Control-ERROR] Dispatcher es NULL.");
                }
            }
        }
    }

    // =========================================================================
    // LÓGICA DE INICIO DE PARTIDA (NUEVO MÉTODO)
    // =========================================================================
    private void manejarSolicitudInicio(Evento evento) {
        
        // 1. VALIDACIÓN: Mínimo de jugadores
        if (sesiones.size() < MIN_JUGADORES) {
            System.out.println("[Control-Rechazo] Inicio rechazado. Jugadores insuficientes (" + sesiones.size() + "/" + MIN_JUGADORES + ").");

            // Crear DTO de rechazo
            DataDTO rechazo = new DataDTO(Protocolo.INICIO_RECHAZADO); // Usamos el enum
            rechazo.setPayload("Mínimo " + MIN_JUGADORES + " jugadores requeridos.");

            // Enviar rechazo SOLO al host (el que solicitó)
            ClienteRemoto host = sesiones.get(evento.getOrigen());
            if (host != null) {
                System.out.println("[Control-Rechazo] Enviando rechazo a: " + host.ip() + ":" + host.puerto());
                dispatcher.enviar(rechazo, host.ip(), host.puerto());
            }
            return;
        }

        // 2. EXTRACCIÓN DE PARÁMETROS: Obtener la dimensión
        int dimension = 3; // Dimensión por defecto
        if (evento.getDato() instanceof Map<?, ?> mapa) {
            Object dimObj = mapa.get("dimension");
            if (dimObj instanceof Number) {
                dimension = ((Number) dimObj).intValue();
            }
        }

        System.out.println("[Control] 🎉 Partida iniciada. Enviando broadcast con dimensión: " + dimension);

        // 3. CREACIÓN DTO DE CONFIRMACIÓN: INICIO_PARTIDA
        DataDTO respuesta = new DataDTO(Protocolo.INICIO_PARTIDA);
        Map<String, Object> config = new HashMap<>();
        config.put("dimension", dimension);
        config.put("mensaje", "¡El juego ha comenzado!");
        respuesta.setPayload(config);

        // 4. BROADCAST a todos los clientes conectados
        broadcastReal(respuesta);
    }

    // =========================================================================
    // BROADCAST (USANDO SESIONES GUARDADAS)
    // =========================================================================
    private void broadcastReal(DataDTO dto) {
        if (dispatcher == null) {
            return;
        }

        for (ClienteRemoto cliente : sesiones.values()) {
            try {
                if (cliente == null || cliente.ip == null) {
                    continue;
                }

                System.out.println("[Control] Enviando a remoto: " + cliente.ip + ":" + cliente.puerto);
                dispatcher.enviar(dto, cliente.ip, cliente.puerto);

            } catch (Exception e) {
                System.err.println("[Control] Error enviando a " + cliente.ip);
            }
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

    // CLASE AUXILIAR PRIVADA
    private record ClienteRemoto(String ip, int puerto) {
    }
}