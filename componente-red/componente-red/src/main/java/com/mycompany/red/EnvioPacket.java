/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.red;

/**
 *
 * @author Serva
 */
public class EnvioPacket {
    private String jsonString;
    private String ip;
    private int puerto;

    public EnvioPacket() {}
    
    public EnvioPacket(String jsonString, String ip, int puerto) {
        this.jsonString = jsonString;
        this.ip = ip;
        this.puerto = puerto;
    }

    public String getJsonString() { return jsonString; }
    public void setJsonString(String jsonString) { this.jsonString = jsonString; }
    
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    
    public int getPuerto() { return puerto; }
    public void setPuerto(int puerto) { this.puerto = puerto; }

    @Override
    public String toString() {
        return "EnvioPacket{ip='" + ip + "', puerto=" + puerto + "}";
    }
}