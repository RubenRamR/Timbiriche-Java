/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.mycompany.componentered;

/**
 *
 * @author rramirez
 */
public class EnvioPacket {

    private final String jsonString;
    private final String ipDestino;
    private final int puertoDestino;

    public EnvioPacket(String jsonString, String ipDestino, int puertoDestino) {
        this.jsonString = jsonString;
        this.ipDestino = ipDestino;
        this.puertoDestino = puertoDestino;
    }

    public String getJsonString() {
        return jsonString;
    }

    public String getIp() {
        return ipDestino;
    }

    public int getPuerto() {
        return puertoDestino;
    }

    @Override
    public String toString() {
        return "EnvioPacket{" + "jsonString=" + jsonString + ", ipDestino=" + ipDestino + ", puertoDestino=" + puertoDestino + '}';
    }
    
}
