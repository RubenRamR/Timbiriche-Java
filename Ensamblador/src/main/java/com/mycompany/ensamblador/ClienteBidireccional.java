/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.mycompany.ensamblador;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Serva
 */
public class ClienteBidireccional {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║     🔄 CLIENTE BIDIRECCIONAL               ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
        
        // Puerto para recibir respuestas
        int puertoLocal = 9000;
        
        // Iniciar receptor en hilo separado
        new Thread(() -> iniciarReceptor(puertoLocal)).start();
        
        // Esperar a que el receptor inicie
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        
        // Enviar mensaje al servidor
        System.out.println("📤 Enviando mensaje al servidor...");
        enviarMensaje("localhost", 8080, 
            "{\"tipo\":\"JUGADA\",\"payload\":\"Prueba bidireccional\"," +
            "\"proyectoOrigen\":\"CLIENTE_BI\",\"proyectoDestino\":\"SERVER\"}");
        
        System.out.println("\n⏳ Esperando respuestas en puerto " + puertoLocal + "...");
        System.out.println("   (Presiona Ctrl+C para salir)\n");
        
        // Mantener vivo
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("\n👋 Cerrando cliente...");
        }
    }
    
    private static void iniciarReceptor(int puerto) {
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("📥 Receptor iniciado en puerto " + puerto + "\n");
            
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(
                         new InputStreamReader(clientSocket.getInputStream()))) {
                    
                    String mensaje;
                    while ((mensaje = in.readLine()) != null) {
                        System.out.println("✉️ RESPUESTA RECIBIDA: " + mensaje);
                    }
                    
                } catch (IOException e) {
                    System.err.println("⚠️ Error recibiendo: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error iniciando receptor: " + e.getMessage());
        }
    }
    
    private static void enviarMensaje(String servidor, int puerto, String mensaje) {
        try (Socket socket = new Socket(servidor, puerto);
             PrintWriter out = new PrintWriter(
                 new OutputStreamWriter(socket.getOutputStream()), true)) {
            
            out.println(mensaje);
            System.out.println("   ✓ Mensaje enviado");
            
        } catch (IOException e) {
            System.err.println("   ✗ Error: " + e.getMessage());
        }
    }
}
