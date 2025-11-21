/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.mycompany.servidorprueba;

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
public class ServidorPrueba2 {

    private static final int PUERTO_SERVIDOR = 8080;
    private static final int PUERTO_CLIENTE = 9000;
    private static int contadorRecibidos = 0;
    private static int contadorEnviados = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║       SERVIDOR PRUEBA - ESCENARIO 2      ║");
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
        
        // PASO 1: Escuchar 3 paquetes de IDA
        System.out.println("═══════════════════════════════════════════════");
        System.out.println(" ESPERANDO 3 PAQUETES DE IDA");
        System.out.println("═══════════════════════════════════════════════\n");
        System.out.println(" Escuchando en puerto " + PUERTO_SERVIDOR + "...\n");
        
        recibirYResponder(serializador, emisor);
        
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║             PRUEBA COMPLETADA           ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println(" Estadísticas:");
        System.out.println("   - Paquetes recibidos: " + contadorRecibidos);
        System.out.println("   - Paquetes enviados: " + contadorEnviados + "\n");
        
        // Cleanup
        clienteTCP.detener();
    }
    
    private static void recibirYResponder(ISerializador serializador, EmisorCliente emisor) throws Exception {
        try (ServerSocket servidor = new ServerSocket(PUERTO_SERVIDOR)) {
            
            // Esperar 3 paquetes
            for (int i = 0; i < 3; i++) {
                servidor.setSoTimeout(20000); // 20 segundos timeout
                
                try (Socket socket = servidor.accept();
                     BufferedReader in = new BufferedReader(
                         new InputStreamReader(socket.getInputStream()))) {
                    
                    String jsonRecibido = in.readLine();
                    
                    if (jsonRecibido != null && !jsonRecibido.isEmpty()) {
                        contadorRecibidos++;
                        
                        // Deserializar
                        DataDTO paqueteRecibido = serializador.deserializar(jsonRecibido, DataDTO.class);
                        
                        System.out.println("═══════════════════════════════════════════════");
                        System.out.println(" PAQUETE #" + contadorRecibidos + " RECIBIDO:");
                        System.out.println("═══════════════════════════════════════════════");
                        System.out.println("   Tipo: " + paqueteRecibido.getTipo());
                        System.out.println("   Payload: " + paqueteRecibido.getPayload());
                        System.out.println("   Origen: " + paqueteRecibido.getProyectoOrigen());
                        System.out.println("═══════════════════════════════════════════════\n");
                        
                        // Crear y enviar respuesta
                        DataDTO respuesta = new DataDTO("RESPUESTA_" + contadorRecibidos);
                        respuesta.setPayload(" Confirmación del servidor: " + paqueteRecibido.getPayload());
                        respuesta.setProyectoOrigen("SERVIDOR_PRUEBA");
                        respuesta.setProyectoDestino("CLIENTE_PRUEBA");
                        
                        System.out.println(" Enviando respuesta #" + (contadorRecibidos) + "...");
                        emisor.notificarActualizacion(respuesta, "127.0.0.1", PUERTO_CLIENTE);
                        contadorEnviados++;
                        System.out.println("    Enviada a puerto " + PUERTO_CLIENTE + "\n");
                        
                    } else {
                        System.out.println("️  Paquete #" + (i + 1) + " vacío recibido\n");
                    }
                    
                } catch (java.net.SocketTimeoutException e) {
                    System.out.println(" TIMEOUT esperando paquete #" + (i + 1) + "\n");
                    break;
                }
            }
        }
    }
}
