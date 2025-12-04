/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ensamblador.app;

import com.mycompany.componentered.FabricaRED;
import com.mycompany.dominio.Jugador;
import com.mycompany.interfacesdispatcher.IDispatcher;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import com.mycompany.modelojuego.IMotorJuegoListener;
import com.mycompany.modelojuego.MotorJuego;
import com.mycompany.modelojuego.ReceptorExternoImpl;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import timbiriche.presentacion.ControllerView;
import timbiriche.presentacion.GameView;

/**
 *
 * @author rramirez
 */
public class Ensamblador {

    public void iniciarJuego() {
        System.out.println("[ENSAMBLADOR] Iniciando configuración...");

        // -----------------------------------------------------------
        // 1. CONFIGURACIÓN DE JUGADORES (Estado inicial)
        // -----------------------------------------------------------
        Jugador jugadorLocal = new Jugador("MiUsuario", Color.BLUE);
        List<Jugador> listaJugadores = new ArrayList<>();
        listaJugadores.add(jugadorLocal); 

        // -----------------------------------------------------------
        // 2. CREAR EL NÚCLEO (MOTOR)
        // -----------------------------------------------------------
        MotorJuego motor = new MotorJuego(5, jugadorLocal, listaJugadores);

        // -----------------------------------------------------------
        // 3. CREAR EL PUENTE DE ENTRADA (RECEPTOR)
        // Este recibe los DTOs de la red y llama a los métodos del motor.
        // -----------------------------------------------------------
        IReceptorExterno receptorLogico = new ReceptorExternoImpl(motor);

        // -----------------------------------------------------------
        // 4. CONFIGURAR LA RED (Usando FabricaRED)
        // -----------------------------------------------------------
        FabricaRED fabricaRed = new FabricaRED();
        
        // A. Conectamos el receptor lógico a la infraestructura de red.
        //    (Así, cuando ReceptorCliente reciba algo del socket, se lo pasará a este receptor).
        fabricaRed.establecerReceptor(receptorLogico);

        // B. Configuramos la conexión y obtenemos el Dispatcher (Emisor).
        //    Parámetros ejemplo: puertoLocal=0 (dinámico) o fijo, IP="localhost", PuertoServer=5000
        //    AJUSTA ESTOS VALORES según tu configuración de servidor real.
        IDispatcher dispatcherRed = fabricaRed.configurarRed(0, "127.0.0.1", 8080);

        if (dispatcherRed != null) {
            // C. Conectamos Motor -> Red
            //    Ahora el motor tiene a quién enviarle los mensajes (al EmisorCliente devuelto).
            motor.addDispatcher(dispatcherRed);
            System.out.println("[ENSAMBLADOR] Red configurada correctamente.");
        } else {
            System.err.println("[ENSAMBLADOR] Error crítico: No se pudo obtener el Dispatcher de red.");
        }

        // -----------------------------------------------------------
        // 5. CREAR LA PRESENTACIÓN
        // -----------------------------------------------------------
        // El Controller envía datos al mismo 'receptorLogico' para simular flujo local 
        // o para que el receptor decida si enviar a red o procesar local.
        ControllerView controller = new ControllerView(receptorLogico, jugadorLocal);
        GameView view = new GameView(controller);

        // -----------------------------------------------------------
        // 6. CONECTAR MOTOR -> VISTA (OBSERVER)
        // -----------------------------------------------------------
        motor.registrarListener(new IMotorJuegoListener() {
            @Override
            public void actualizarEstado() {
                // Sincronización visual
                System.out.println("[OBSERVER] El motor cambió, repintando vista...");
                view.repintarTablero(); 
            }

            @Override
            public void onJuegoTerminado(Jugador ganador) {
                String nombre = (ganador != null) ? ganador.getNombre() : "Empate";
                JOptionPane.showMessageDialog(view, "¡Juego Terminado! Ganador: " + nombre);
            }
        });

        // 7. Mostrar Ventana
        view.setVisible(true);
    }
}