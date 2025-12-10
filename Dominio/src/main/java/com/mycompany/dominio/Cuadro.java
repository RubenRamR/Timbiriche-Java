/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dominio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rramirez
 */
public class Cuadro implements Serializable{

    private List<Linea> lineas; // Debe contener exactamente 4 líneas
    public Jugador propietario;
    public boolean completado;

    public Cuadro(List<Linea> lineas) {
        this.lineas = lineas;
        this.completado = false;
        this.propietario = null;
    }

    /**
     * Verifica si las 4 líneas del cuadro han sido dibujadas en el tablero
     * global. Nota: El UML pasa List<Linea> en el constructor, pero la
     * verificación depende de saber qué líneas están dibujadas.
     *
     * @param lineasDibujadas Lista global de líneas en el tablero.
     */
    public boolean verificarCompletado(List<Linea> lineasDibujadas) {
        if (completado)
        {
            return true;
        }

        int count = 0;
        for (Linea lineaNecesaria : this.lineas)
        {
            for (Linea lineaDibujada : lineasDibujadas)
            {
                if (lineaNecesaria.esIgual(lineaDibujada))
                {
                    count++;
                    break;
                }
            }
        }

        if (count == 4)
        {
            this.completado = true;
            return true;
        }
        return false;
    }

    public void setPropietario(Jugador jugador) {
        this.propietario = jugador;
    }

    public Jugador getPropietario() {
        return propietario;
    }

    public boolean isCompletado() {
        return completado;
    }

    public List<Linea> getLineas() {
        return lineas;
    }

    public void setLineas(List<Linea> lineas) {
        this.lineas = lineas;
    }

    public void setCompletado(boolean completado) {
        this.completado = completado;
    }

}
