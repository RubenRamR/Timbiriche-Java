/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.ensamblador;

import com.mycompany.manejadorjuego.ManejadorJuego;
import com.mycompany.red.ClienteTCP;
import com.mycompany.red.DataDTO;
import com.mycompany.red.EmisorCliente;
import com.mycompany.red.EnvioQueue;
import com.mycompany.red.ISerializador;
import com.mycompany.red.JsonSerializador;
import com.mycompany.red.ReceptorCliente;
import interfaces.dispatcher.IDispatcher;

/**
 *
 * @author Serva
 */
public class EnsambladorApp {

 public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   🚀 INICIANDO SISTEMA MULTI-PROYECTO 🚀      ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");
        
        // ─────────────────────────────────────────────────────────────────
        // PASO 1: Crear componentes de RED (infraestructura)
        // ─────────────────────────────────────────────────────────────────
        System.out.println("📦 [1/5] Creando componentes de RED...");
        
        ISerializador serializador = new JsonSerializador();
        EnvioQueue envioQueue = new EnvioQueue();
        
        // ─────────────────────────────────────────────────────────────────
        // PASO 2: Crear adaptador de SALIDA (EmisorCliente)
        // ─────────────────────────────────────────────────────────────────
        System.out.println("📤 [2/5] Creando adaptador de SALIDA...");
        
        IDispatcher emisor = new EmisorCliente(serializador, envioQueue);
        
        // ─────────────────────────────────────────────────────────────────
        // PASO 3: Iniciar hilo consumidor (ClienteTCP)
        // ─────────────────────────────────────────────────────────────────
        System.out.println("🔄 [3/5] Iniciando hilo consumidor TCP...");
        
        ClienteTCP clienteTCP = new ClienteTCP(envioQueue);
        Thread hiloConsumidor = new Thread(clienteTCP, "tcp-consumer");
        hiloConsumidor.setDaemon(true);
        hiloConsumidor.start();
        
        // ─────────────────────────────────────────────────────────────────
        // PASO 4: Crear LÓGICA DE NEGOCIO
        // ─────────────────────────────────────────────────────────────────
        System.out.println("🧠 [4/5] Creando manejador de lógica...");
        
        ManejadorJuego manejadorJuego = new ManejadorJuego(emisor);
        
        // ─────────────────────────────────────────────────────────────────
        // PASO 5: Crear y arrancar adaptador de ENTRADA (ReceptorCliente)
        // ─────────────────────────────────────────────────────────────────
        System.out.println("📥 [5/5] Iniciando adaptador de ENTRADA...");
        
        ReceptorCliente receptor = new ReceptorCliente(
            8080, 
            manejadorJuego, 
            serializador, 
            DataDTO.class
        );
        Thread hiloReceptor = new Thread(receptor, "receptor-server");
        hiloReceptor.start();
        
        // ─────────────────────────────────────────────────────────────────
        // Sistema iniciado - Enviar mensaje de prueba
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║        ✅ SISTEMA INICIADO CORRECTAMENTE       ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println("📍 Escuchando en puerto: 8080");
        System.out.println("🔄 Hilo consumidor: ACTIVO");
        System.out.println("💡 Envíe mensajes JSON a localhost:8080\n");
        
        // Ejemplo de envío
        DataDTO mensajePrueba = new DataDTO("JUGADA");
        mensajePrueba.setPayload("Movimiento de prueba");
        mensajePrueba.setProyectoOrigen("ENSAMBLADOR");
        emisor.notificarActualizacion(mensajePrueba, "127.0.0.1", 9000);
        
        // ─────────────────────────────────────────────────────────────────
        // Mantener aplicación corriendo
        // ─────────────────────────────────────────────────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Deteniendo sistema...");
            receptor.detener();
            clienteTCP.detener();
            System.out.println("👋 Sistema detenido correctamente");
        }));
        
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.err.println("⚠️ Aplicación interrumpida");
        }
    }
}
