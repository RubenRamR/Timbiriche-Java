/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.clientemainprueba;

import com.mycompany.componentered.FabricaRED;
import com.mycompany.dtos.DataDTO;
import com.mycompany.interfacesdispatcher.IDispatcher;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import com.mycompany.protocolo.Protocolo;

/**
 *
 * @author rramirez
 */
public class ClienteMain {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CLIENTE DE PRUEBAS (Simulador) ===");

        // 1. Receptor que solo imprime lo que llega
        IReceptorExterno receptorSimulado = (datos) ->
        {
            if (datos.es(Protocolo.ACTUALIZAR_TABLERO))
            {
                System.out.println("[CLIENTE] ÉXITO: El servidor validó la línea: " + datos.getPayload());
            } else
            {
                System.out.println("[CLIENTE] Recibí otro mensaje: " + datos.getTipo());
            }
        };

        // 2. Configurar Red (Puerto 9000)
        IDispatcher emisor = FabricaRED.configurarRed(9000, "127.0.0.1", 8080);
        FabricaRED.establecerReceptor(receptorSimulado);

        Thread.sleep(1000);

        // --- PRUEBA DE JUGADA ---
        System.out.println("\n--- ENVIANDO JUGADA (0,0)-(0,1) ---");

        DataDTO jugada = new DataDTO(Protocolo.INTENTO_JUGADA);
        jugada.setPayload("{id: 'L1', p1:0, p2:1}"); // JSON simulado
        jugada.setProyectoOrigen("Jugador 1");
        jugada.setProyectoDestino("SERVIDOR");

        emisor.enviar(jugada);

        Thread.sleep(2000);

        System.out.println("\n=== FIN DE LA SIMULACIÓN ===");
        FabricaRED.detenerTodo();
        System.exit(0);
    }
}
