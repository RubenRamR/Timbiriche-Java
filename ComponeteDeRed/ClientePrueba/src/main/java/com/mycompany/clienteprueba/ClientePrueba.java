/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
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
public class ClientePrueba {

    public static void main(String[] args) throws Exception {
        System.out.println("");
        System.out.println("CLIENTE PRUEBA - ESCENARIO 1");
        System.out.println("\n");
        
        // Inicializar componentes de RED
        System.out.println("Inicializando componentes...\n");
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
        
        // PASO 1: Enviar paquete de IDA
        System.out.println("");
        System.out.println(" PASO 1: Enviando PAQUETE DE IDA");
        System.out.println("\n");
        
        DataDTO paqueteIda = new DataDTO("PRUEBA");
        paqueteIda.setPayload("Este es el paquete de IDA desde ClientePrueba");
        paqueteIda.setProyectoOrigen("CLIENTE_PRUEBA");
        paqueteIda.setProyectoDestino("SERVIDOR_PRUEBA");
        
        System.out.println("Paquete a enviar:");
        System.out.println("   - Tipo: " + paqueteIda.getTipo());
        System.out.println("   - Payload: " + paqueteIda.getPayload());
        System.out.println("   - Origen: " + paqueteIda.getProyectoOrigen());
        System.out.println("   - Destino: " + paqueteIda.getProyectoDestino());
        System.out.println("");
        
        // Enviar a localhost:8080 (donde está el servidor)
        emisor.notificarActualizacion(paqueteIda, "127.0.0.1", 8080);
        System.out.println(" Paquete IDA enviado a puerto 8080\n");
        
        // PASO 2: Esperar respuesta (paquete de VUELTA)
        System.out.println("");
        System.out.println("PASO 2: Esperando PAQUETE DE VUELTA");
        System.out.println("\n");
        
        recibirRespuesta();
        
        System.out.println("\n");
        System.out.println("PRUEBA COMPLETADA");
        System.out.println("");
        
        // Cleanup
        clienteTCP.detener();
    }
    
    private static void recibirRespuesta() throws Exception {
        // Crear servidor local en puerto 9000 para recibir respuesta
        try (ServerSocket servidor = new ServerSocket(9000)) {
            System.out.println("Escuchando respuesta en puerto 9000...\n");
            
            // Con timeout de 10 segundos
            servidor.setSoTimeout(10000);
            
            try (Socket socket = servidor.accept();
                 BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {
                
                String respuesta = in.readLine();
                
                if (respuesta != null && !respuesta.isEmpty()) {
                    System.out.println("");
                    System.out.println("  PAQUETE DE VUELTA RECIBIDO:");
                    System.out.println("");
                    System.out.println(respuesta);
                    System.out.println("\n");
                } else {
                    System.out.println(" ️  Respuesta vacía recibida\n");
                }
                
            } catch (java.net.SocketTimeoutException e) {
                System.out.println(" TIMEOUT: No se recibió respuesta en 10 segundos\n");
                throw e;
            }
        }
    }
}
