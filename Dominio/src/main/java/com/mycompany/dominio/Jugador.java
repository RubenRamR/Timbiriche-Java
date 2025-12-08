/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dominio;

import java.awt.Color;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author rramirez
 */
public class Jugador {

    public String nombre;
    public String rutaAvatar;
    public String color;
    public int puntaje;

    public Jugador() {
        this.puntaje = 0;
    }

    public Jugador(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
        this.puntaje = 0;
        this.rutaAvatar = "";
    }

    public void sumarPuntos(int puntos) {
        this.puntaje += puntos;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public String getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Jugador jugador = (Jugador) o;
        return Objects.equals(nombre, jugador.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }
}
