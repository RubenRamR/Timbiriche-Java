/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dominio;

import java.awt.Color; // Asumimos java.awt para el color

/**
 *
 * @author rramirez
 */
public class Jugador {

    private String nombre;
    private String rutaAvatar;
    private Color color;
    private int puntaje;

    public Jugador() {
        this.puntaje = 0;
    }

    public Jugador(String nombre, Color color) {
        this.nombre = nombre;
        this.color = color;
        this.puntaje = 0;
        this.rutaAvatar = ""; 
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRutaAvatar() {
        return rutaAvatar;
    }

    public void setRutaAvatar(String rutaAvatar) {
        this.rutaAvatar = rutaAvatar;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public void agregarPunto() {
        this.puntaje++;
    }
}
