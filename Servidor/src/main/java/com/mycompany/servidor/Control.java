/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.servidor;

import com.mycompany.dtos.DataDTO;
import com.mycompany.interfacesdispatcher.IDispatcher;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Componente de control del lado servidor.
 * 
 * - Implementa IReceptorExterno: recibe DataDTO desde la red.
 * - Implementa IFuenteConocimiento: se suscribe al Blackboard y procesa eventos.
 * 
 * NOTA IMPORTANTE (Blackboard):
 * El servidor NO conoce el dominio concreto (Jugador, etc.).
 * Solo manipula datos genéricos (Object / Map) y los trata como eventos.
 */
public class Control implements IReceptorExterno, IFuenteConocimiento {

    private IDispatcher dispatcher;
    private final Blackboard blackboard;

    // Mapa: ID del Proyecto -> ClienteRemoto (IP + Puerto)
    // En este caso se usa el nombre del jugador (proyectoOrigen) como clave.
    private Map<String, ClienteRemoto> sesiones = new HashMap<>();

    // Lista genérica de "jugadores".
    // El servidor NO sabe qué es un Jugador, solo guarda datos (normalmente Maps).
    private final List<Object> lista;

    public Control() {
        this.blackboard = new Blackboard();
        this.sesiones = new HashMap<>();
        this.lista = new ArrayList<>();
        this.blackboard.suscribir(this);   // Se suscribe como fuente de conocimiento
    }

    public void setDispatcher(IDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    // =========================================================================
    // ENTRADA DESDE LA RED (IReceptorExterno)
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

        // ---------------------------------------------------------------------
        // CASO DE USO: REGISTRAR JUGADOR
        // ---------------------------------------------------------------------
        if (EventosSistema.REGISTRO.equals(datos.getTipo())) {
            try {
                Object payload = datos.getPayload();
                int puertoDelJugador = 0;
                String nombreJugador = datos.getProyectoOrigen();

                // INTROSPECCIÓN DINÁMICA DEL PAYLOAD:
                // El cliente envía un objeto Jugador, pero al llegar aquí
                // ya fue transformado en un Map (por JSON).
                if (payload instanceof Map) {
                    Map<?, ?> mapaDatos = (Map<?, ?>) payload;
                    if (mapaDatos.containsKey("puertoEscucha")) {
                        Object val = mapaDatos.get("puertoEscucha");
                        if (val instanceof Integer) {
                            puertoDelJugador = (Integer) val;
                        } else if (val instanceof Number) {
                            puertoDelJugador = ((Number) val).intValue();
                        }
                    }
                }

                if (puertoDelJugador > 0) {
                    sesiones.put(
                            nombreJugador,
                            new ClienteRemoto(ipRemitente, puertoDelJugador)
                    );
                    System.out.println("[Control] Sesión guardada: " + nombreJugador
                            + " -> " + ipRemitente + ":" + puertoDelJugador);
                } else {
                    System.err.println("[Control] No se pudo obtener puertoEscucha válido del payload.");
                }

            } catch (Exception e) {
                System.err.println("[Control] Error extrayendo puerto del payload genérico: " + e.getMessage());
            }
        }

        // -----------------------------------------------------------
        // CUALQUIER DTO SE CONVIERTE EN EVENTO Y SE PUBLICA EN LA BB
        // -----------------------------------------------------------
        Evento evento = convertirDTOaEvento(datos);
        blackboard.publicarEvento(evento);
    }

    // =========================================================================
    // PROCESAMIENTO COMO FUENTE DE CONOCIMIENTO (IFuenteConocimiento)
    // =========================================================================
    @Override
    public void procesarEvento(Evento evento) {

        // -----------------------------------------------------------
        // CASO DE USO: REGISTRAR JUGADOR
        // -----------------------------------------------------------
        if (EventosSistema.REGISTRO.equals(evento.getTipo())) {

            Object nuevoJugador = evento.getDato();

            // IMPORTANTE:
            // El servidor aquí maneja "nuevoJugador" como Object (normalmente Map),
            // no como Jugador de dominio.
            // Esto cumple con el modelo Blackboard: datos genéricos en el espacio común.
            if (!lista.contains(nuevoJugador)) {
                lista.add(nuevoJugador);
                System.out.println("[Control] Jugador agregado. Total: " + lista.size());

                // Creamos un DTO de sincronización con TODOS los jugadores
                DataDTO syncDTO = new DataDTO();
                syncDTO.setTipo("LISTA_JUGADORES");
                // Reenviamos la lista de Maps tal cual se han ido acumulando.
                syncDTO.setPayload(new ArrayList<>(lista));

                // Enviamos a todos los clientes registrados (broadcast real)
                broadcastReal(syncDTO);
            } else {
                System.out.println("[Control] Jugador ya existe, omitiendo.");
            }

        // -----------------------------------------------------------
        // OTRO TIPO DE EVENTO: SOLICITUD_ENVIO
        // -----------------------------------------------------------
        } else if (EventosSistema.SOLICITUD_ENVIO.equals(evento.getTipo())) {
            System.out.println("[Control] Ejecutando envío a la red (SOLICITUD_ENVIO).");
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
    // BROADCAST (USANDO SESIONES GUARDADAS)
    // =========================================================================
    private void broadcastReal(DataDTO dto) {
        if (dispatcher == null) {
            System.err.println("[Control] Dispatcher es NULL, no se puede enviar broadcast.");
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
                System.err.println("[Control] Error enviando a " + cliente.ip + ":" + cliente.puerto);
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
        // En esta implementación ya tenemos un Blackboard interno propio,
        // por eso este método queda vacío.
    }

    // CLASE AUXILIAR PRIVADA
    private record ClienteRemoto(String ip, int puerto) {}
}
