/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.mycompany.componentered;

import com.mycompany.libreriacomun.Interfaces.IDispatcher;
import com.mycompany.libreriacomun.Interfaces.IReceptorExterno;

/**
 *
 * @author rramirez Fachada estática para inicializar el subsistema de
 * comunicación.
 */
public class FabricaRED {

    private static ReceptorCliente hiloReceptor;
    private static ClienteTCP hiloSalida;

    /**
     * Configura y arranca la red.
     *
     * @param puertoLocal Puerto donde escuchar
     * @param receptor Instancia de tu lógica que recibirá los mensajes.
     * @return Instancia de IDispatcher para enviar mensajes.
     */
    public static IDispatcher configurarRed(int puertoLocal, String ipServidor, int puertoServidor, IReceptorExterno receptor) {

        ISerializador serializador = new JsonSerializador();
        EnvioQueue cola = new EnvioQueue();

        EmisorCliente emisor = new EmisorCliente(serializador, cola, ipServidor, puertoServidor);

        detenerTodo();

        hiloReceptor = new ReceptorCliente(puertoLocal, receptor, serializador);
        hiloSalida = new ClienteTCP(cola);

        new Thread(hiloReceptor, "RED-Receptor").start();
        new Thread(hiloSalida, "RED-Emisor").start();

        return emisor;
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
