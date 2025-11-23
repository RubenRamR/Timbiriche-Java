/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.manejadorjuego;

import com.mycompany.red.DataDTO;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Serva
 */
public class EstadoJuego {
    private List<String> jugadas = new ArrayList<>();
    private int turnoActual = 0;

    public void procesarJugada(DataDTO datos) {
        jugadas.add(datos.getPayload());
        turnoActual++;
        System.out.println("[EstadoJuego] 📊 Jugadas totales: " + jugadas.size());
    }

    public void actualizarMovimiento(DataDTO datos) {
        System.out.println("[EstadoJuego] 🔄 Movimiento actualizado: " + datos.getPayload());
    }

    public List<String> getJugadas() {
        return new ArrayList<>(jugadas);
    }

    public int getTurnoActual() {
        return turnoActual;
    }
}