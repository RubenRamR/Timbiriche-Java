/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.mycompany.componentered;

import com.mycompany.libreriacomun.DTOs.DataDTO;
import com.mycompany.libreriacomun.Interfaces.IReceptorExterno;
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
    private IReceptorExterno receptorExterno;
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
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)))
        {

            String linea;
            while ((linea = in.readLine()) != null)
            {
                try
                {
                    // Deserializar y entregar
                    DataDTO dto = (DataDTO) serializador.deserializar(linea, DataDTO.class);
                    if (dto != null)
                    {
                        receptorExterno.recibirMensaje(dto);
                    }
                } catch (Exception e)
                {
                    System.err.println("[RED-Entrada] Paquete corrupto recibido.");
                }
            }
        } catch (Exception e)
        {
        } finally
        {
            try
            {
                socket.close();
            } catch (Exception ignore)
            {
            }
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
