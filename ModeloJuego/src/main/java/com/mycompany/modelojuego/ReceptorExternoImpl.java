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
import com.mycompany.imotorjuego.IMotorJuego;
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
// CORRECCIÓN 1: Usamos la Interfaz, no la clase concreta

    private IMotorJuego motorJuego;
    private ObjectMapper jsonMapper;

    // Inyección de dependencia a través de la interfaz
    public ReceptorExternoImpl(IMotorJuego motorJuego) {
        this.motorJuego = motorJuego;
        this.jsonMapper = new ObjectMapper();
    }

    @Override
    public void recibirMensaje(DataDTO datos) {
        if (datos == null || datos.getTipo() == null)
        {
            return;
        }

        try
        {
            Protocolo protocolo = Protocolo.valueOf(datos.getTipo());

            switch (protocolo)
            {
                // ============================================================
                // CASO: JUGADAS REMOTAS (DEL RIVAL)
                // ============================================================
                case ACTUALIZAR_TABLERO:
                    System.out.println("Receptor: Recibido ACTUALIZAR_TABLERO del servidor.");
                    procesarJugadaRemota(datos);
                    break;

                case CUADRO_CERRADO:
                    System.out.println("Receptor: Recibido CUADRO_CERRADO del servidor.");
                    procesarJugadaRemota(datos);
                    break;

                // ============================================================
                // CASO: GESTIÓN DE LOBBY
                // ============================================================
                case LISTA_JUGADORES:
                    System.out.println("Receptor: Recibida lista de jugadores actualizada.");
                    motorJuego.actualizarListaDeJugadores(datos.getPayload());
                    break;

                case JUGADA_INVALIDA:
                    System.err.println("SERVIDOR: La jugada fue rechazada.");
                    // Aquí podrías llamar a un motorJuego.onError(...) si quisieras mostrarlo en la UI
                    break;

                default:
                    // Ignoramos protocolos de handshake (LOGIN, REGISTRO, ETC) 
                    // que quizas maneja otra clase o se ignoran una vez en juego.
                    break;
            }
        } catch (IllegalArgumentException e)
        {
            System.err.println("Receptor Error: Protocolo desconocido: " + datos.getTipo());
        }
    }

    private void procesarJugadaRemota(DataDTO datos) {
        Linea linea = deserializarLinea(datos.getPayload());

        if (linea == null)
        {
            return;
        }

        String nombreRemitente = datos.getProyectoOrigen();
        Jugador jugadorRemitente = buscarJugadorPorNombre(nombreRemitente);

        if (jugadorRemitente == null && nombreRemitente != null)
        {
            jugadorRemitente = new Jugador(nombreRemitente, "#999999");
        }

        // Delegamos al Motor (a través de la interfaz IMotorJuego)
        motorJuego.realizarJugadaRemota(linea, jugadorRemitente);
    }

    private Linea deserializarLinea(String json) {
        try
        {
            return jsonMapper.readValue(json, Linea.class);
        } catch (JsonProcessingException ex)
        {
            Logger.getLogger(ReceptorExternoImpl.class.getName()).log(Level.SEVERE, "Error JSON Linea", ex);
            return null;
        }
    }

    private Jugador buscarJugadorPorNombre(String nombre) {
        if (nombre == null)
        {
            return null;
        }

        List<Jugador> jugadores = motorJuego.getJugadores();
        if (jugadores == null)
        {
            return null;
        }

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
