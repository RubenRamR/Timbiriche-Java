/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.mycompany.servidorprueba;

import com.mycompany.componentered.FabricaRED;
import com.mycompany.libreriacomun.Interfaces.IDispatcher;
import java.util.Scanner;

/**
 *
 * @author rramirez
 */
public class ServidorMain {

    public static void main(String[] args) {
        System.out.println("=== INICIANDO SERVIDOR (Puerto 8080) ===");

        ServidorLogica logica = new ServidorLogica();

        IDispatcher dispatcher = FabricaRED.configurarRed(8080, null, 0, logica);

        logica.setDispatcher(dispatcher);

        System.out.println("Servidor escuchando... (Presiona ENTER para salir)");
        new Scanner(System.in).nextLine();

        FabricaRED.detenerTodo();
    }
}
