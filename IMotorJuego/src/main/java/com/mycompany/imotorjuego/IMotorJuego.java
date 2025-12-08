/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.imotorjuego;

import com.mycompany.dominio.Jugador;
import com.mycompany.dominio.Linea;
import com.mycompany.dominio.Tablero;
import com.mycompany.modelojuego.IMotorJuegoListener;
import java.util.List;

/**
 *
 * @author rramirez
 */
public interface IMotorJuego {

    /**
     * El jugador local intenta hacer una jugada.
     *
     * @param linea La línea trazada.
     */
    void realizarJugadaLocal(Linea linea);

    /**
     * Registra a alguien (la UI) que quiere ser notificado de cambios.
     */
    void registrarListener(IMotorJuegoListener listener);

    // --- Getters de Estado ---
    Tablero getTablero();

    Jugador getTurnoActual();

    Jugador getJugadorLocal();

    /**
     * Necesario para que el Receptor pueda buscar remitentes de mensajes.
     */
    List<Jugador> getJugadores();
}
