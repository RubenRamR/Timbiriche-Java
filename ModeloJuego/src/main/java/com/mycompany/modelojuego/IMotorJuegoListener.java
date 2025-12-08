/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.modelojuego;

import com.mycompany.dominio.Jugador;
import com.mycompany.dominio.Tablero;

/**
 *
 * @author rramirez
 */
public interface IMotorJuegoListener {

    void onJuegoActualizado(Tablero tablero, Jugador turnoActual);

    void onJuegoTerminado(Jugador ganador);

    void onError(String mensaje);
}
