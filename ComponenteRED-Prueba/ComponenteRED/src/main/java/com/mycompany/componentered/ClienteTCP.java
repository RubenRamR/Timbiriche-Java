/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.mycompany.componentered;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author rramirez
 */
public class ClienteTCP implements Runnable {

    private final EnvioQueue queue;
    private volatile boolean ejecutando = true;

    public ClienteTCP(EnvioQueue queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        System.out.println("[RED-Worker] Hilo de Salida (ClienteTCP) iniciado.");
        while (ejecutando)
        {
            try
            {
                // 1. Espera Pasiva: Se duerme aquí si no hay mensajes
                EnvioPacket packet = queue.tomarSiguiente();

                // 2. Envío Físico
                enviar(packet);

            } catch (InterruptedException e)
            {
                System.out.println("[RED-Worker] Salida interrumpida, cerrando.");
                Thread.currentThread().interrupt();
            }
        }
    }

    private void enviar(EnvioPacket packet) {
        // Abre socket, envía y cierra.
        try (Socket socket = new Socket(packet.getIp(), packet.getPuerto()); PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true))
        {

            out.println(packet.getJsonString());
            System.out.println("[RED-Salida] Enviado bytes a " + packet.getIp());

        } catch (IOException e)
        {
            System.err.println("[RED-Salida] Fallo conexión con " + packet.getIp() + ": " + e.getMessage());
        }
    }

    public void detener() {
        ejecutando = false;
    }
}
