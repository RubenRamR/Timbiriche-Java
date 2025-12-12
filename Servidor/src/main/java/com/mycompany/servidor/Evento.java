/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.servidor;

public class Evento {

    private final String tipo;
    private final Object dato;
    private final Object origen;

    public Evento(Enum<?> tipoEnum, Object dato, Object origen) {
        this.tipo = tipoEnum.name();
        this.dato = dato;
        this.origen = origen;
    }

    public Evento(String tipo, Object dato, Object origen) {
        this.tipo = tipo;
        this.dato = dato;
        this.origen = origen;
    }

    public boolean es(Enum<?> tipoEnum) {
        return this.tipo.equals(tipoEnum.name());
    }

    public String getTipo() {
        return tipo;
    }

    public Object getDato() {
        return dato;
    }

    public Object getOrigen() {
        return origen;
    }
}
