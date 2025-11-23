/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.red;

import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Serva
 */
public class EnvioQueue {
private final Queue<EnvioPacket> queue = new LinkedList<>();

    public synchronized void agregar(EnvioPacket paquete) {
        queue.offer(paquete);
        System.out.println("[EnvioQueue] ➕ Encolado: " + paquete);
        notify();
    }

    public synchronized EnvioPacket tomarSiguiente() {
        while (queue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return queue.poll();
    }

    public synchronized int tamano() {
        return queue.size();
    }
}