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

    private Map<String, ClienteRemoto> sesiones = new HashMap<>();

    // Lista genérica.
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
            return;
        }

        System.out.println("[Control] Recibido DTO Tipo: " + datos.getTipo());

        //Registro        
        registrarSesionAutomatica(datos);

        Evento evento = convertirDTOaEvento(datos);
        blackboard.publicarEvento(evento);
    }

    @Override
    public void procesarEvento(Evento evento) {

        if (evento.getTipo().equals("REGISTRO"))
        {
            Object nuevoJugador = evento.getDato();

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

    private void registrarSesionAutomatica(DataDTO datos) {
        try
        {
            String tipo = datos.getTipo(); // String puro
            String ip = datos.getIpRemitente() != null ? datos.getIpRemitente() : "127.0.0.1";
            String nombre = datos.getProyectoOrigen();
            Object payload = datos.getPayload();
            int puerto = 0;

            // CASO A: UNIRSE_PARTIDA o REGISTRO (Payload es el Jugador directo)
            if ("UNIRSE_PARTIDA".equals(tipo) || "REGISTRO".equals(tipo))
            {
                if (payload instanceof Map)
                {
                    puerto = getIntFromMap((Map) payload, "puertoEscucha");
                }
            } // CASO B: CREAR_PARTIDA (Payload es {jugador: {...}, dimension: X})
            else if ("CREAR_PARTIDA".equals(tipo))
            {
                if (payload instanceof Map)
                {
                    Map root = (Map) payload;
                    Object jugadorObj = root.get("jugador");
                    if (jugadorObj instanceof Map)
                    {
                        puerto = getIntFromMap((Map) jugadorObj, "puertoEscucha");
                    }
                }
            }

            // Si encontramos puerto y nombre, guardamos la sesión
            if (puerto > 0 && nombre != null)
            {
                sesiones.put(nombre, new ClienteRemoto(ip, puerto));
                System.out.println("[Control] Sesión registrada: " + nombre + " -> " + ip + ":" + puerto);
            }

        } catch (Exception e)
        {
            System.err.println("[Control] Error al registrar sesión: " + e.getMessage());
        }
    }

    private void enviarUnicast(DataDTO dto, String destinatario) {
        if (dispatcher == null)
        {
            return;
        }

        ClienteRemoto cliente = sesiones.get(destinatario);
        if (cliente != null)
        {
            System.out.println("[Control] Enviando DIRECTO a " + destinatario + " (" + cliente.ip + ":" + cliente.puerto + ")");
            dispatcher.enviar(dto, cliente.ip, cliente.puerto);
        } else
        {
            System.err.println("[Control] No se encontró sesión para: " + destinatario);
        }
    }

    private int getIntFromMap(Map map, String key) {
        if (map.containsKey(key))
        {
            Object val = map.get(key);
            if (val instanceof Number)
            {
                return ((Number) val).intValue();
            }
        }
        return 0;
    }

    // CLASE AUXILIAR PRIVADA
    private record ClienteRemoto(String ip, int puerto) {

    }
}
