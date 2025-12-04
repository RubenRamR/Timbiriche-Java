/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.ensamblador.app;

/**
 *
 * @author rramirez
 */

import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicación Cliente.
 */
public class TimbiricheApp {

    public static void main(String[] args) {
        // Es vital iniciar la UI dentro del hilo de despacho de eventos de Swing (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Instanciamos el Ensamblador (el arquitecto)
                Ensamblador app = new Ensamblador();
                
                // 2. Le ordenamos que construya y conecte todo
                app.iniciarJuego();
                
            } catch (Exception e) {
                System.err.println("Error fatal al iniciar la aplicación:");
                e.printStackTrace();
            }
        });
    }
}