/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.servidor;

/**
 *
 * @author rramirez
 */
import com.mycompany.componentered.FabricaRED;
import com.mycompany.interfacesdispatcher.IDispatcher;
import com.mycompany.servidor.implementacion.Experto;
import java.util.Scanner;

public class ServidorMain {

    public static void main(String[] args) {
        System.out.println("=== SERVIDOR BLACKBOARD ACTIVO ===");

        // 1. Crear Cerebro (Control y Blackboard implícito)
        Control control = new Control();

        // 2. Crear Experto (Las reglas del Timbiriche)
        Experto experto = new Experto("Arbitro_Principal", control.getBlackboard());
        control.getBlackboard().suscribir(experto);

        // 3. Levantar Red (Puerto 8080)
        IDispatcher dispatcher = FabricaRED.configurarRed(8080, null, 0);

        // Vinculamos la red con el control
        FabricaRED.establecerReceptor(control); // Para recibir
        control.setDispatcher(dispatcher);      // Para responder

        System.out.println("Esperando jugadas... [ENTER para salir]");
        new Scanner(System.in).nextLine();
        FabricaRED.detenerTodo();
    }
}
