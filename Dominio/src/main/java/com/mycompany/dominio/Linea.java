/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dominio;

import java.util.Objects;

/**
 *
 * @author rramirez
 */
public class Linea {

    public Punto p1;
    public Punto p2;
    private Jugador propietario;

    public Linea() {

    }

    public Linea(Punto pA, Punto pB) {
        // Normalizamos la línea para que (0,0)->(0,1) sea igual a (0,1)->(0,0)
        // Esto simplifica el esIgual
        if (pA.getX() < pB.getX() || (pA.getX() == pB.getX() && pA.getY() < pB.getY()))
        {
            this.p1 = pA;
            this.p2 = pB;
        } else
        {
            this.p1 = pB;
            this.p2 = pA;
        }
    }

    public boolean esIgual(Linea otra) {
        if (otra == null)
        {
            return false;
        }
        return this.equals(otra);
    }

    public boolean esHorizontal() {
        return p1.getY() == p2.getY();
    }

    public boolean verificarCompleta() {
        return p1 != null && p2 != null;
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
        Linea linea = (Linea) o;
        return Objects.equals(p1, linea.p1) && Objects.equals(p2, linea.p2);
    }

    @Override
    public String toString() {
        return "Linea{" + "p1=" + p1 + ", p2=" + p2 + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(p1, p2);
    }

    public void setPropietario(Jugador propietario) {
        this.propietario = propietario;
    }

    public Jugador getPropietario() {
        return this.propietario;
    }
}
