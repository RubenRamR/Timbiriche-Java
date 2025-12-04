/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.mycompany.componentered;

import com.mycompany.interfacesdispatcher.IDispatcher;
import com.mycompany.interfacesreceptor.IReceptorExterno;

/**
 *
 * @author rramirez Fachada estática para inicializar el subsistema de
 * comunicación.
 */
public class FabricaRED {

    private static ReceptorCliente hiloReceptor;
    private static ClienteTCP hiloSalida;

    public static IDispatcher configurarRed(int puertoLocal, String ipServidor, int puertoServidor) {

        ISerializador serializador = new JsonSerializador();
        EnvioQueue cola = new EnvioQueue();
        EmisorCliente emisor = new EmisorCliente(serializador, cola, ipServidor, puertoServidor);

        detenerTodo();

        // Creamos el receptor SIN lógica todavía (desconectado)
        hiloReceptor = new ReceptorCliente(puertoLocal, serializador);
        hiloSalida = new ClienteTCP(cola);

        new Thread(hiloReceptor, "RED-Receptor").start();
        new Thread(hiloSalida, "RED-Emisor").start();

        return emisor;
    }

    // Metodo estatico para Ensamblador lo conecte después
    public static void establecerReceptor(IReceptorExterno receptor) {
        if (hiloReceptor != null)
        {
            hiloReceptor.setReceptor(receptor);
        }
    }

    public static void detenerTodo() {
        if (hiloReceptor != null)
        {
            hiloReceptor.detener();
        }
        if (hiloSalida != null)
        {
            hiloSalida.detener();
        }
    }
}
