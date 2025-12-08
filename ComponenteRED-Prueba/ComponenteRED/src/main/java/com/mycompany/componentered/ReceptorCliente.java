/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.mycompany.componentered;

import com.mycompany.dtos.DataDTO;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author rramirez
 */
public class ReceptorCliente implements Runnable {

    private final int puerto;
    private volatile IReceptorExterno receptorExterno;
    private final ISerializador serializador;
    private ServerSocket serverSocket;
    private volatile boolean ejecutando = true;

    public ReceptorCliente(int puerto, ISerializador serializador) {
        this.puerto = puerto;
        this.serializador = serializador;
    }

    public void setReceptor(IReceptorExterno receptor) {
        this.receptorExterno = receptor;
    }

    @Override
    public void run() {
        try
        {
            serverSocket = new ServerSocket(puerto);
            System.out.println("[RED-Entrada] Escuchando en puerto local " + puerto);

            while (ejecutando)
            {
                try
                {
                    // Bloqueo esperando conexión
                    Socket socket = serverSocket.accept();

                    // Hilo efímero para procesar lectura (permite concurrencia de paquetes)
                    new Thread(() -> procesarConexion(socket)).start();

                } catch (Exception e)
                {
                    if (ejecutando)
                    {
                        System.err.println("[RED-Entrada] Error en accept: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e)
        {
            System.err.println("[RED-Entrada] No se pudo abrir puerto " + puerto + ": " + e.getMessage());
        }
    }

    private void procesarConexion(Socket socket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            // DEBUG 1: Confirmar conexión física
            System.out.println("[RED-Entrada] Cliente conectado desde: " + socket.getInetAddress());

            String linea;
            while ((linea = in.readLine()) != null) {
                
                // DEBUG 2: Ver lo que viaja por el cable (JSON crudo)
                System.out.println("[RED-Entrada] Bytes recibidos: " + linea);

                try {
                    // DEBUG 3: Intento de deserialización
                    DataDTO dto = (DataDTO) serializador.deserializar(linea, DataDTO.class);
                    System.out.println("[RED-Entrada] Objeto reconstruido: " + dto.getTipo());

                    if (dto != null) {
                        
                        if (socket.getInetAddress() != null) {
                            String ipReal = socket.getInetAddress().getHostAddress();
                            dto.setIpRemitente(ipReal); 
                            System.out.println("[RED-IP] IP Inyectada al DTO: " + ipReal);
                        }
                        // =========================================================

                        if (receptorExterno != null) {
                            
                            // DEBUG 4: Entrega a la capa superior
                            System.out.println("[RED-Entrada] Entregando a: " + receptorExterno.getClass().getSimpleName());
                            
                            receptorExterno.recibirMensaje(dto);
                            
                            System.out.println("[RED-Entrada] Mensaje procesado con éxito.");
                            
                        } else {
                            System.err.println("[RED-ERROR] Recibí mensaje pero NO hay lógica conectada (receptorExterno es null).");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[RED-ERROR] Falló la conversión del mensaje: " + e.getMessage());
                }
            }
        } catch (Throwable t) {
            System.err.println("[RED-CRITICAL] El hilo de lectura murió: " + t.getMessage());
            t.printStackTrace();
        } finally {
            try {
                System.out.println("[RED-Entrada] Cerrando socket.");
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception ignore) {}
        }
    }

    public void detener() {
        ejecutando = false;
        try
        {
            if (serverSocket != null)
            {
                serverSocket.close();
            }
        } catch (Exception e)
        {
        }
    }
}
