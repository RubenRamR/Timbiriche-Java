/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
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
public class ServidorPrueba {

    public static void main(String[] args) throws Exception {
        System.out.println("");
        System.out.println("SERVIDOR PRUEBA - ESCENARIO 1");
        System.out.println("\n");
        
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
        
        // PASO 1: Escuchar paquete de IDA
        System.out.println("");
        System.out.println(" PASO 1: Esperando PAQUETE DE IDA");
        System.out.println("\n");
        System.out.println(" Escuchando en puerto 8080...\n");
        
        DataDTO paqueteRecibido = recibirPaquete(serializador);
        
        System.out.println("");
        System.out.println(" PAQUETE DE IDA RECIBIDO:");
        System.out.println("");
        System.out.println(" Contenido:");
        System.out.println("   - Tipo: " + paqueteRecibido.getTipo());
        System.out.println("   - Payload: " + paqueteRecibido.getPayload());
        System.out.println("   - Origen: " + paqueteRecibido.getProyectoOrigen());
        System.out.println("   - Destino: " + paqueteRecibido.getProyectoDestino());
        System.out.println("\n");
        
        // PASO 2: Enviar respuesta (paquete de VUELTA)
        System.out.println("");
        System.out.println("PASO 2: Enviando PAQUETE DE VUELTA");
        System.out.println("\n");
        
        DataDTO paqueteVuelta = new DataDTO("RESPUESTA");
        paqueteVuelta.setPayload("Este es el paquete de VUELTA desde ServidorPrueba. Recibí: " + 
                                 paqueteRecibido.getPayload());
        paqueteVuelta.setProyectoOrigen("SERVIDOR_PRUEBA");
        paqueteVuelta.setProyectoDestino("CLIENTE_PRUEBA");
        
        System.out.println("Paquete de respuesta:");
        System.out.println("   - Tipo: " + paqueteVuelta.getTipo());
        System.out.println("   - Payload: " + paqueteVuelta.getPayload());
        System.out.println("   - Origen: " + paqueteVuelta.getProyectoOrigen());
        System.out.println("   - Destino: " + paqueteVuelta.getProyectoDestino());
        System.out.println("");
        
        // Enviar a localhost:9000 (donde el cliente está escuchando)
        emisor.notificarActualizacion(paqueteVuelta, "127.0.0.1", 9000);
        System.out.println("✅ Paquete VUELTA enviado a puerto 9000\n");
        
        // Esperar a que se envíe
        Thread.sleep(1000);
        
        System.out.println("");
        System.out.println("PRUEBA COMPLETADA");
        System.out.println("");
        
        // Cleanup
        clienteTCP.detener();
    }
    
    private static DataDTO recibirPaquete(ISerializador serializador) throws Exception {
        try (ServerSocket servidor = new ServerSocket(8080)) {
            
            try (Socket socket = servidor.accept();
                 BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {
                
                String jsonRecibido = in.readLine();
                
                if (jsonRecibido != null && !jsonRecibido.isEmpty()) {
                    // Deserializar el JSON a DataDTO
                    DataDTO dto = serializador.deserializar(jsonRecibido, DataDTO.class);
                    return dto;
                } else {
                    throw new Exception("Paquete vacío recibido");
                }
            }
        }
    }
}