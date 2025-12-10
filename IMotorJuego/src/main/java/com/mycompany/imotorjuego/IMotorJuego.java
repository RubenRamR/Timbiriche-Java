/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.imotorjuego;

import com.mycompany.dominio.Jugador;
import com.mycompany.dominio.Linea;
import com.mycompany.dominio.Tablero;
import java.util.List;
import com.mycompany.interfacesdispatcher.IDispatcher;

/**
 *
 * @author rramirez
 */
public interface IMotorJuego {

    // ==========================================
    // INTERACCIÓN DESDE LA UI (JUGADOR LOCAL)
    // ==========================================
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

    // ==========================================
    // INTERACCIÓN DESDE LA RED (JUGADAS REMOTAS)
    // ==========================================
    /**
     * Ejecuta una jugada que viene desde el servidor (otro jugador). El motor
     * debe confiar en esta jugada y actualizar el estado.
     */
    void realizarJugadaRemota(Linea linea, Jugador jugadorRemitente);

    /**
     * Recibe la lista actualizada de jugadores desde el servidor.
     */
    void actualizarListaDeJugadores(List<Jugador> nuevosJugadores);

    // ==========================================
    // CONFIGURACIÓN E INICIALIZACIÓN
    // ==========================================
    /**
     * Define quién es el usuario en esta máquina.
     */
    void setJugadorLocal(Jugador jugador);

    /**
     * Agrega un despachador para enviar mensajes a la red.
     */
    void addDispatcher(IDispatcher dispatcher);

    /**
     * (Opcional) Si permites cambiar tamaño desde la UI o Config.
     */
    void configurarTablero(int dimension);

    // ==========================================
    // GETTERS DE ESTADO (CONSULTAS)
    // ==========================================
    Tablero getTablero();

    Jugador getTurnoActual();

    Jugador getJugadorLocal();

    /**
     * Necesario para que el Receptor pueda buscar remitentes de mensajes y para
     * que la UI muestre la lista.
     */
    List<Jugador> getJugadores();
}
