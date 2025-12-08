/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.modelojuego;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.dominio.Jugador;
import com.mycompany.dominio.Linea;
import com.mycompany.dtos.DataDTO;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import com.mycompany.protocolo.Protocolo;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rramirez
 */
public class ReceptorExternoImpl implements IReceptorExterno {

    private MotorJuego motorJuego;
    private ObjectMapper jsonMapper;

    public ReceptorExternoImpl(MotorJuego motorJuego) {
        this.motorJuego = motorJuego;
        this.jsonMapper = new ObjectMapper();
    }

    @Override
    public void recibirMensaje(DataDTO datos) {
        if (datos == null || datos.getTipo() == null) {
            return;
        }

        try {
            // Convertimos el String del DTO al Enum Protocolo
            Protocolo protocolo = Protocolo.valueOf(datos.getTipo());

            switch (protocolo) {
                // ============================================================
                // CASO 1: JUGADA LOCAL (Viene de tu ControllerView)
                // ============================================================
                case INTENTO_JUGADA:
                    System.out.println("Receptor: Recibido INTENTO_JUGADA local.");
                    Linea lineaLocal = deserializarLinea(datos.getPayload());
                    if (lineaLocal != null) {
                        motorJuego.realizarJugadaLocal(lineaLocal);
                    } else {
                        System.err.println("Receptor: Error al deserializar línea local.");
                    }
                    break;

                // ============================================================
                // CASO 2: JUGADA REMOTA (Viene del Servidor)
                // ============================================================
                case ACTUALIZAR_TABLERO:
                    System.out.println("Receptor: Recibido ACTUALIZAR_TABLERO del servidor.");
                    procesarJugadaRemota(datos, false);
                    break;

                case CUADRO_CERRADO:
                    System.out.println("Receptor: Recibido CUADRO_CERRADO del servidor.");
                    procesarJugadaRemota(datos, true);
                    break;

                // ============================================================
                // CASO 3: GESTIÓN DE SALA / LOBBY (NUEVO)
                // ============================================================
                case LISTA_JUGADORES:
                    System.out.println("Receptor: Recibida lista de jugadores actualizada.");
                    // Le pasamos el JSON crudo al motor para que actualice su lista
                    motorJuego.actualizarListaDeJugadores(datos.getPayload());
                    break;

                case JUGADA_INVALIDA:
                    System.err.println("SERVIDOR: Jugada Rechazada.");
                    break;

                // Otros casos que no requieren acción inmediata
                case REGISTRO: // El cliente envía REGISTRO, pero raramente lo recibe de vuelta
                case SOLICITUD_LOGIN:
                case INICIO_PARTIDA:
                case SOLICITUD_ENVIO:
                    break;

                default:
                    System.out.println("Receptor: Protocolo no manejado -> " + protocolo);
                    break;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Receptor Error: Protocolo desconocido: " + datos.getTipo());
        }
    }

    private void procesarJugadaRemota(DataDTO datos, boolean cerroCuadro) {
        Linea linea = deserializarLinea(datos.getPayload());

        if (linea == null)
        {
            return;
        }

        String nombreRemitente = datos.getProyectoOrigen();
        Jugador jugadorRemitente = buscarJugadorPorNombre(nombreRemitente);

        if (jugadorRemitente == null && nombreRemitente != null)
        {
            jugadorRemitente = new Jugador(nombreRemitente, "#000000");
        }

        motorJuego.realizarJugadaRemota(linea, jugadorRemitente);
    }

    private Linea deserializarLinea(String json) {
        try
        {
            return jsonMapper.readValue(json, Linea.class);
        } catch (JsonProcessingException ex)
        {
            Logger.getLogger(ReceptorExternoImpl.class.getName()).log(Level.SEVERE, "Error JSON", ex);
            return null;
        }
    }

    private Jugador buscarJugadorPorNombre(String nombre) {
        if (nombre == null)
        {
            return null;
        }
        List<Jugador> jugadores = motorJuego.getJugadores();
        for (Jugador j : jugadores)
        {
            if (j.getNombre().equals(nombre))
            {
                return j;
            }
        }
        return null;
    }
}
