/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.mycompany.clienteprueba;

import com.mycompany.red.ClienteTCP;
import com.mycompany.red.DataDTO;
import com.mycompany.red.EmisorCliente;
import com.mycompany.red.EnvioQueue;
import com.mycompany.red.ISerializador;
import com.mycompany.red.JsonSerializador;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


/**
 *
 * @author Serva
 */
public class ClientePrueba2 {

    private static final int PUERTO_SERVIDOR = 8080;
    private static final int PUERTO_CLIENTE = 9000;
    private static int contadorPaquetes = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║        CLIENTE PRUEBA - ESCENARIO 2       ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
        
        // Inicializar componentes de RED
        System.out.println(" Inicializando componentes...\n");
        ISerializador serializador = new JsonSerializador();
        EnvioQueue envioQueue = new EnvioQueue();
        EmisorCliente emisor = new EmisorCliente(serializador, envioQueue);
        
        // Iniciar consumidor TCP (hilo de envío)
        ClienteTCP clienteTCP = new ClienteTCP(envioQueue);
        Thread hiloConsumidor = new Thread(clienteTCP, "tcp-consumer");
        hiloConsumidor.setDaemon(true);
        hiloConsumidor.start();
        
        // Dar tiempo para que se inicie
        Thread.sleep(500);
        
        // Iniciar receptor en segundo plano
        Thread hiloReceptor = new Thread(() -> {
            try {
                recibirRespuestas();
            } catch (Exception e) {
                System.err.println(" Error en receptor: " + e.getMessage());
            }
        });
        hiloReceptor.start();
        
        Thread.sleep(500);
        
        // ENVIAR 3 PAQUETES
        System.out.println("═══════════════════════════════════════════════");
        System.out.println(" ENVIANDO 3 PAQUETES DE IDA");
        System.out.println("═══════════════════════════════════════════════\n");
        
        String[] mensajes = {
            "Primer mensaje: Iniciando comunicación",
            "Segundo mensaje: Validando conectividad",
            "Tercer mensaje: Completando prueba"
        };
        
        for (int i = 0; i < mensajes.length; i++) {
            enviarPaquete(i + 1, mensajes[i], emisor);
            Thread.sleep(1000); // Esperar 1 segundo entre paquetes
        }
        
        System.out.println(" Todos los paquetes IDA han sido enviados\n");
        
        // Esperar a recibir todas las respuestas
        System.out.println("═══════════════════════════════════════════════");
        System.out.println(" Esperando 3 paquetes de VUELTA");
        System.out.println("═══════════════════════════════════════════════\n");
        
        // Dar tiempo para que lleguen todas las respuestas
        Thread.sleep(5000);
        
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║              PRUEBA COMPLETADA           ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        
        // Cleanup
        clienteTCP.detener();
    }
    
    private static void enviarPaquete(int numero, String mensaje, EmisorCliente emisor) {
        System.out.println(" PAQUETE " + numero + ":");
        System.out.println("   Mensaje: " + mensaje);
        
        DataDTO paquete = new DataDTO("PRUEBA_" + numero);
        paquete.setPayload(mensaje);
        paquete.setProyectoOrigen("CLIENTE_PRUEBA");
        paquete.setProyectoDestino("SERVIDOR_PRUEBA");
        
        try {
            emisor.notificarActualizacion(paquete, "127.0.0.1", PUERTO_SERVIDOR);
            System.out.println("    Enviado a puerto " + PUERTO_SERVIDOR + "\n");
        } catch (Exception e) {
            System.err.println("    Error: " + e.getMessage() + "\n");
        }
    }
    
    private static void recibirRespuestas() throws Exception {
        try (ServerSocket servidor = new ServerSocket(PUERTO_CLIENTE)) {
            System.out.println(" Receptor escuchando en puerto " + PUERTO_CLIENTE + "...\n");
            
            // Esperar múltiples conexiones (una por cada respuesta)
            for (int i = 0; i < 3; i++) {
                servidor.setSoTimeout(15000); // 15 segundos por respuesta
                
                try (Socket socket = servidor.accept();
                     BufferedReader in = new BufferedReader(
                         new InputStreamReader(socket.getInputStream()))) {
                    
                    String respuesta = in.readLine();
                    contadorPaquetes++;
                    
                    if (respuesta != null && !respuesta.isEmpty()) {
                        System.out.println("═══════════════════════════════════════════════");
                        System.out.println("️  PAQUETE DE VUELTA #" + contadorPaquetes + " RECIBIDO:");
                        System.out.println("═══════════════════════════════════════════════");
                        System.out.println(respuesta);
                        System.out.println("═══════════════════════════════════════════════\n");
                    } else {
                        System.out.println("️  Respuesta #" + contadorPaquetes + " vacía recibida\n");
                    }
                    
                } catch (java.net.SocketTimeoutException e) {
                    System.out.println(" TIMEOUT en respuesta #" + (i + 1) + ": No se recibió en 15 segundos\n");
                }
            }
        }
    }
}
