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
    private String payload;
    private String proyectoOrigen;
    private String proyectoDestino;

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

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
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

    @Override
    public String toString() {
        return "DTO[" + tipo + "] " + proyectoOrigen + " -> " + proyectoDestino;
    }
}