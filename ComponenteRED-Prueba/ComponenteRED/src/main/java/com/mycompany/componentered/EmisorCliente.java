/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.mycompany.componentered;

import com.mycompany.libreriacomun.DTOs.DataDTO;
import com.mycompany.libreriacomun.Interfaces.IDispatcher;

/**
 *
 * @author rramirez
 */
public class EmisorCliente implements IDispatcher {

    private final ISerializador serializador;
    private final EnvioQueue queue;

    private final String ipDefault;
    private final int puertoDefault;

    public EmisorCliente(ISerializador serializador, EnvioQueue queue, String ipDefault, int puertoDefault) {
        this.serializador = serializador;
        this.queue = queue;
        this.ipDefault = ipDefault;
        this.puertoDefault = puertoDefault;
    }

    @Override
    public void enviar(DataDTO datos) {
        this.enviar(datos, this.ipDefault, this.puertoDefault);
    }

    @Override
    public void enviar(DataDTO datos, String ip, int puerto) {
        try
        {
            String json = serializador.serializar(datos);
            EnvioPacket packet = new EnvioPacket(json, ip, puerto);
            queue.agregar(packet);
            System.out.println("[RED-Emisor] Encolado para " + ip + ":" + puerto);
        } catch (Exception e)
        {
            System.err.println("[RED-Emisor] Error: " + e.getMessage());
        }
    }
}
