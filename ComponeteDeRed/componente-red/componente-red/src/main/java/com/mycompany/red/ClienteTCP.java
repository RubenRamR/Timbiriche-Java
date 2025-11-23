
package com.mycompany.red;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClienteTCP implements Runnable {
    private final EnvioQueue queue;
    private volatile boolean ejecutando = true;

    public ClienteTCP(EnvioQueue queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        System.out.println("[ClienteTCP] 🚀 Consumidor iniciado");
        
        while (ejecutando) {
            try {
                EnvioPacket packet = queue.tomarSiguiente();
                if (packet != null) {
                    enviarPaquete(packet);
                }
            } catch (Exception e) {
                System.err.println("[ClienteTCP] ⚠️ Error en ciclo: " + e.getMessage());
            }
        }
        
        System.out.println("[ClienteTCP] ⏹️ Consumidor detenido");
    }

    private void enviarPaquete(EnvioPacket packet) {
        Socket socket = null;
        PrintWriter out = null;
        
        try {
            socket = new Socket(packet.getIp(), packet.getPuerto());
            out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), 
                true
            );
            
            out.println(packet.getJsonString());
            
            System.out.println("[ClienteTCP] ✅ Enviado a " + packet.getIp() 
                + ":" + packet.getPuerto());
                
        } catch (Exception e) {
            System.err.println("[ClienteTCP] ❌ Error enviando a " + packet.getIp() 
                + ":" + packet.getPuerto() + " - " + e.getMessage());
                
        } finally {
            try {
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (Exception ignore) {}
        }
    }

    public void detener() {
        ejecutando = false;
    }
}
