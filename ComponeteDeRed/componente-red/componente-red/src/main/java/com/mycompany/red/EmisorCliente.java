/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.red;

import interfaces.dispatcher.IDispatcher;

/**
 *
 * @author Serva
 */
public class EmisorCliente implements IDispatcher {
    private final ISerializador serializador;
    private final EnvioQueue queue;

    public EmisorCliente(ISerializador serializador, EnvioQueue queue) {
        this.serializador = serializador;
        this.queue = queue;
    }

    @Override
    public void notificarActualizacion(DataDTO estado, String ip, int puerto) {
        try {
            String jsonString = serializador.serializar(estado);
            EnvioPacket packet = new EnvioPacket(jsonString, ip, puerto);
            queue.agregar(packet);
            
            System.out.println("[EmisorCliente] 📤 Encolado para " + ip + ":" + puerto);
            
        } catch (Exception e) {
            System.err.println("[EmisorCliente] ❌ Error: " + e.getMessage());
            throw new RuntimeException("Error en dispatch", e);
        }
    }
}