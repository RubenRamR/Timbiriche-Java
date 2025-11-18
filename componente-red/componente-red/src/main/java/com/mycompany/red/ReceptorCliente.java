
package com.mycompany.red;

import com.mycompany.ireceptorexterno.IReceptorExterno;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class ReceptorCliente implements Runnable {
    private final int puerto;
    private final IReceptorExterno receptorExterno;
    private final ISerializador serializador;
    private final Class<?> tipoClase;
    private ServerSocket serverSocket;
    private volatile boolean ejecutando = true;

    public ReceptorCliente(
        int puerto, 
        IReceptorExterno receptorExterno, 
        ISerializador serializador,
        Class<?> tipoClase
    ) {
        this.puerto = puerto;
        this.receptorExterno = receptorExterno;
        this.serializador = serializador;
        this.tipoClase = tipoClase;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(puerto);
            System.out.println("[ReceptorCliente] 🎧 Escuchando en puerto " + puerto);
            
            while (ejecutando) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[ReceptorCliente] 🔗 Cliente conectado: " 
                        + clientSocket.getRemoteSocketAddress());
                    
                    new Thread(() -> manejarCliente(clientSocket), "cliente-handler").start();
                        
                } catch (Exception e) {
                    if (ejecutando) {
                        System.err.println("[ReceptorCliente] ⚠️ Error: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ReceptorCliente] ❌ Error fatal: " + e.getMessage());
        }
    }

    private void manejarCliente(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8))) {
            
            String data;
            while ((data = in.readLine()) != null) {
                try {
                    DataDTO dto = (DataDTO) serializador.deserializar(data, tipoClase);
                    
                    // Enrutar según tipo de mensaje
                    if ("JUGADA".equals(dto.getTipo())) {
                        receptorExterno.realizarJugada(dto);
                    } else if ("MOVIMIENTO".equals(dto.getTipo())) {
                        receptorExterno.recibirMovimientoRed(dto);
                    } else {
                        // Por defecto, enviar como jugada
                        receptorExterno.realizarJugada(dto);
                    }
                    
                } catch (Exception e) {
                    System.err.println("[ReceptorCliente] ⚠️ Error procesando: " 
                        + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("[ReceptorCliente] ⚠️ Error cliente: " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (Exception ignore) {}
        }
    }

    public void detener() {
        ejecutando = false;
        try {
            if (serverSocket != null) serverSocket.close();
            System.out.println("[ReceptorCliente] ⏹️ Detenido");
        } catch (Exception e) {
            System.err.println("[ReceptorCliente] ⚠️ Error al cerrar: " + e.getMessage());
        }
    }
}