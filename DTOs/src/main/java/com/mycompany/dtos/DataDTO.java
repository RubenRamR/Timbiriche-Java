/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.mycompany.dtos;

import java.io.Serializable;

/**
 *
 * @author rramirez
 */
public class DataDTO implements Serializable {

    private String tipo;
    private Object payload;
    private String proyectoOrigen;
    private String proyectoDestino;

    private String ipRemitente;
    // ----------------------------

    public DataDTO() {
    }

    /**
     *
     * @param tipoEnum El valor del Enum
     */
    public DataDTO(Enum<?> tipoEnum) {
        this();
        this.tipo = tipoEnum.name();
    }

    public boolean es(Enum<?> tipoEnum) {
        return this.tipo != null && this.tipo.equals(tipoEnum.name());
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Object getPayload() {
        return payload;
    }

    public String getProyectoOrigen() {
        return proyectoOrigen;
    }

    public void setProyectoOrigen(String proyectoOrigen) {
        this.proyectoOrigen = proyectoOrigen;
    }

    public String getProyectoDestino() {
        return proyectoDestino;
    }

    public void setProyectoDestino(String proyectoDestino) {
        this.proyectoDestino = proyectoDestino;
    }

    public String getIpRemitente() {
        return ipRemitente;
    }

    public void setIpRemitente(String ipRemitente) {
        this.ipRemitente = ipRemitente;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "DTO[" + tipo + "] De: " + proyectoOrigen + " (" + (ipRemitente != null ? ipRemitente : "N/A") + ")";
    }
}
