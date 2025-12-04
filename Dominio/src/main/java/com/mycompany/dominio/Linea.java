/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dominio;

/**
 *
 * @author rramirez
 */
public class Linea {
    private Punto p1;
    private Punto p2;

    public Linea(Punto p1, Punto p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Punto getP1() { return p1; }
    public Punto getP2() { return p2; }

    /**
     * Verifica si esta línea es igual a otra, sin importar el orden de los puntos.
     * Ejemplo: (0,0)->(0,1) es igual a (0,1)->(0,0).
     */
    public boolean esIgual(Linea otra) {
        if (otra == null) return false;
        return (this.p1.equals(otra.p1) && this.p2.equals(otra.p2)) ||
               (this.p1.equals(otra.p2) && this.p2.equals(otra.p1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Linea linea = (Linea) o;
        return esIgual(linea);
    }

    @Override
    public int hashCode() {
        return p1.hashCode() + p2.hashCode();
    }
    
    @Override
    public String toString() {
        return "[" + p1 + " - " + p2 + "]";
    }
}