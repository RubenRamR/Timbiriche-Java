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
    // ENTRADA
    // =========================================================================
    @Override
    public void recibirMensaje(DataDTO datos) {
        if (datos == null)
        {
            System.err.println("[Control] Error: DTO nulo.");
            return;
        }

        System.out.println("[Control] Recibido DTO Tipo: " + datos.getTipo());

        String ipRemitente = datos.getIpRemitente();
        if (ipRemitente == null || ipRemitente.isEmpty())
        {
            ipRemitente = "127.0.0.1";
        }

        // --- LÓGICA 3.3 CORREGIDA: Sin dependencia de Jugador ---
        if (datos.getTipo().equals("REGISTRO"))
        {
            try
            {
                Object payload = datos.getPayload();
                int puertoDelJugador = 0;
                String nombreJugador = datos.getProyectoOrigen(); // Usamos el ID del DTO

                // INTROSPECCIÓN DINÁMICA:
                // Si el servidor no conoce la clase Jugador, Jackson lo convierte en un Map.
                if (payload instanceof Map)
                {
                    Map<?, ?> mapaDatos = (Map<?, ?>) payload;
                    // Extraemos el puerto buscando la llave "puertoEscucha"
                    if (mapaDatos.containsKey("puertoEscucha"))
                    {
                        Object val = mapaDatos.get("puertoEscucha");
                        puertoDelJugador = (Integer) val;
                    }
                }

                if (puertoDelJugador > 0)
                {
                    sesiones.put(
                            nombreJugador,
                            new ClienteRemoto(ipRemitente, puertoDelJugador)
                    );
                    System.out.println("[Control] Sesión guardada (Dinámica): " + nombreJugador
                            + " -> " + ipRemitente + ":" + puertoDelJugador);
                }

            } catch (Exception e)
            {
                System.err.println("[Control] Error extrayendo puerto del payload genérico: " + e.getMessage());
            }
        }
        // -----------------------------------------------------------

        Evento evento = convertirDTOaEvento(datos);
        blackboard.publicarEvento(evento);
    }

    @Override
    public void procesarEvento(Evento evento) {

        if (evento.getTipo().equals("REGISTRO"))
        {
            Object nuevoJugador = evento.getDato();

            // Java sabe comparar Maps por contenido. 
            // Si llega el mismo JSON, generará un Map igual, así que contains funciona.
            if (!lista.contains(nuevoJugador))
            {
                lista.add(nuevoJugador);
                System.out.println("[Control] Jugador agregado. Total: " + lista.size());

                DataDTO syncDTO = new DataDTO();
                syncDTO.setTipo("LISTA_JUGADORES");
                // Reenviamos la lista de Maps tal cual llegaron
                syncDTO.setPayload(new ArrayList<>(lista));

                broadcastReal(syncDTO);
            } else
            {
                System.out.println("[Control] Jugador ya existe, omitiendo.");
            }

        } else if (evento.getTipo().equals(EventosSistema.SOLICITUD_ENVIO))
        {
            System.out.println("[Control] Ejecutando envío a la red.");
            if (evento.getDato() instanceof DataDTO)
            {
                if (dispatcher != null)
                {
                    broadcastReal((DataDTO) evento.getDato());
                } else
                {
                    System.err.println("[Control-ERROR] Dispatcher es NULL.");
                }
            }
        }
    }

    // =========================================================================
    // BROADCAST (USANDO SESIONES GUARDADAS)
    // =========================================================================
    private void broadcastReal(DataDTO dto) {
        if (dispatcher == null)
        {
            return;
        }

        for (ClienteRemoto cliente : sesiones.values())
        {
            try
            {
                if (cliente == null || cliente.ip == null)
                {
                    continue;
                }

                System.out.println("[Control] Enviando a remoto: " + cliente.ip + ":" + cliente.puerto);
                dispatcher.enviar(dto, cliente.ip, cliente.puerto);

            } catch (Exception e)
            {
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
    private class ClienteRemoto {

        String ip;
        int puerto;

        public ClienteRemoto(String ip, int puerto) {
            this.ip = ip;
            this.puerto = puerto;
        }
    }
}
