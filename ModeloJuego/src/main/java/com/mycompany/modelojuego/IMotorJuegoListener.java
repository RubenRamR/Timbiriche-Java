/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.modelojuego;

import com.mycompany.dominio.Jugador;

/**
 *
 * @author rramirez
 */
public interface IMotorJuegoListener {
    /**
     * Se llama para cambios visuales continuos (se dibujó línea, cambió puntaje).
     * La vista debe "jalar" (get) los datos del modelo para repintar.
     */
    void actualizarEstado(); 

    /**
     * Se llama ÚNICA y EXCLUSIVAMENTE cuando el juego finaliza.
     * La vista debe reaccionar mostrando el ganador.
     */
    void onJuegoTerminado(Jugador ganador);
}