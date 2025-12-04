/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dominio;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rramirez
 */

public class Cuadro {

    private List<Linea> lineas; // Líneas que actualmente rodean este cuadro
    private Jugador propietario;
    private boolean completado;

    // Atributos auxiliares para saber qué líneas DEBERÍAN formar este cuadro
    // Esto ayuda a validar si una línea nueva pertenece aquí.
    // No están en el UML explícito, pero son necesarios para la lógica interna,
    // o bien se calculan en el Tablero. Aquí asumiremos que Tablero le pasa las líneas.
    public Cuadro() {
        this.lineas = new ArrayList<>();
        this.completado = false;
        this.propietario = null;
    }

    // Constructor opcional si queremos inicializar lista
    public Cuadro(List<Linea> lineasIniciales) {
        this.lineas = lineasIniciales;
        this.completado = verificarCompletado();
    }

    /**
     * Agrega una línea al cuadro si no existe ya.
     */
    public void agregarLinea(Linea linea) {
        if (!contieneLinea(linea))
        {
            lineas.add(linea);
        }
    }

    private boolean contieneLinea(Linea l) {
        for (Linea existente : lineas)
        {
            if (existente.esIgual(l))
            {
                return true;
            }
        }
        return false;
    }

    public boolean verificarCompletado() {
        // En Timbiriche un cuadro se cierra con 4 líneas
        if (lineas.size() == 4)
        {
            this.completado = true;
            return true;
        }
        return false;
    }

    public Jugador getPropietario() {
        return propietario;
    }

    public void setPropietario(Jugador propietario) {
        this.propietario = propietario;
    }

    public boolean isCompletado() {
        return completado;
    }
}
