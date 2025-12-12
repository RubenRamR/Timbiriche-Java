/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.imotorjuego;

import com.mycompany.dominio.Jugador;
import com.mycompany.dominio.Tablero;
import java.util.List;

/**
 *
 * @author rramirez
 */
public interface IMotorJuegoListener {

    void onJuegoActualizado(Tablero tablero, Jugador turnoActual);

    void onJuegoTerminado(Jugador ganador);

    void onError(String mensaje);

    // ============================================
    // NUEVO: EVENTOS DE LOBBY
    // ============================================
    /**
     * Notifica que la lista de jugadores cambió
     */
    void onListaJugadoresActualizada(List<Jugador> jugadores);

    /**
     * Notifica que la partida ha iniciado
     */
    void onPartidaIniciada(int dimension);

    /**
     * Notifica que el inicio fue rechazado
     */
    void onInicioRechazado(String motivo);
}
