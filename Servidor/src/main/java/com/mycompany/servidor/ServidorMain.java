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

        // 1. Crear Cerebro
        Control control = new Control();

        // 2. Crear Experto (Reglas Timbiriche)
        // Simulamos que "Jugador1" ya está en la mesa para la prueba
        Experto experto = new Experto("Jugador 1", control.getBlackboard());
        control.getBlackboard().suscribir(experto);

        // 3. Levantar Red (Puerto 8080)
        IDispatcher dispatcher = FabricaRED.configurarRed(8080, null, 0);
        FabricaRED.establecerReceptor(control);
        control.setDispatcher(dispatcher);

        System.out.println("Esperando jugadas... [ENTER para salir]");
        new Scanner(System.in).nextLine();
        FabricaRED.detenerTodo();
    }
}
