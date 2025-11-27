/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.mycompany.componentered;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author rramirez
 */
public class EnvioQueue {

    private final BlockingQueue<EnvioPacket> queue = new LinkedBlockingQueue<>();

    public void agregar(EnvioPacket packet) {
        try
        {
            queue.put(packet);
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            System.err.println("[RED-Cola] Interrupción al agregar paquete: " + e.getMessage());
        }
    }

    public EnvioPacket tomarSiguiente() throws InterruptedException {
        return queue.take();
    }
}
