/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.clienteprueba;

import com.mycompany.componentered.FabricaRED;
import com.mycompany.libreriacomun.DTOs.DataDTO;
import com.mycompany.libreriacomun.Interfaces.IDispatcher;

/**
 *
 * @author rramirez
 */
public class ClienteMain {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CLIENTE DE PRUEBAS ===");

        // 1. Instanciar la lógica (Quien recibe)
        ClienteLogica logica = new ClienteLogica();

        // A. Configurar Red (Solo infraestructura, sin receptor aún)
        IDispatcher emisor = FabricaRED.configurarRed(9000, "127.0.0.1", 8080);

        // B. Establecer Receptor (Chamba del Ensamblador: Conectar cables)
        FabricaRED.establecerReceptor(logica);

        // -----------------------------------------------------------
        Thread.sleep(1000);

        // -------------------------------------------------
        // ESCENARIO A: 1 PAQUETE DE IDA Y 1 DE VUELTA
        // -------------------------------------------------
        System.out.println("\n========================================");
        System.out.println(" ESCENARIO A: 1 IDA -> 1 VUELTA");
        System.out.println("========================================");

        DataDTO paquete1 = new DataDTO(Protocolo.INTENTO_JUGADA);
        paquete1.setPayload("Jugada Solitaria (Caso 1)");
        paquete1.setProyectoOrigen("Cliente");

        System.out.println("[CLIENTE] Enviando 1 Jugada...");

        // Usamos el método sobrecargado (sin IP, ya la tiene configurada)
        emisor.enviar(paquete1);

        Thread.sleep(3000);

        // -------------------------------------------------
        // ESCENARIO B: 3 PAQUETES DE IDA Y 3 DE VUELTA
        // -------------------------------------------------
        System.out.println("\n========================================");
        System.out.println(" ESCENARIO B: 3 IDA -> 3 VUELTA");
        System.out.println("========================================");

        for (int i = 1; i <= 3; i++)
        {
            DataDTO paqueteN = new DataDTO(Protocolo.INTENTO_JUGADA);
            paqueteN.setPayload("Jugada Rápida #" + i + " (Caso 2)");
            paqueteN.setProyectoOrigen("Cliente");

            System.out.println("[CLIENTE] Enviando Jugada " + i + " de 3...");
            emisor.enviar(paqueteN);

            Thread.sleep(500);
        }

        Thread.sleep(4000);
        System.out.println("\n=== FIN ===");
        FabricaRED.detenerTodo();
        System.exit(0);
    }
}
