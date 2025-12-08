/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dominio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author rramirez
 */
public class Tablero {

    public int dimension;
    public List<Linea> lineasDibujadas;
    public List<Cuadro> cuadros;

    public Tablero(int dimension) {
        this.dimension = dimension;
        this.lineasDibujadas = new ArrayList<>();
        this.cuadros = new ArrayList<>();
        inicializarCuadros();
    }

    private void inicializarCuadros() {
        // Crea la matriz lógica de cuadros posibles basada en la dimensión de puntos
        for (int y = 0; y < dimension - 1; y++)
        {
            for (int x = 0; x < dimension - 1; x++)
            {
                Punto p1 = new Punto(x, y);
                Punto p2 = new Punto(x + 1, y);
                Punto p3 = new Punto(x, y + 1);
                Punto p4 = new Punto(x + 1, y + 1);

                List<Linea> lineasCuadro = new ArrayList<>();
                lineasCuadro.add(new Linea(p1, p2)); // Top
                lineasCuadro.add(new Linea(p3, p4)); // Bottom
                lineasCuadro.add(new Linea(p1, p3)); // Left
                lineasCuadro.add(new Linea(p2, p4)); // Right

                cuadros.add(new Cuadro(lineasCuadro));
            }
        }
    }

    public boolean agregarLinea(Linea linea) {
        if (existeLinea(linea))
        {
            return false;
        }
        lineasDibujadas.add(linea);
        return true;
    }

    // Método auxiliar no explícito en diagrama pero necesario para agregarLinea
    public boolean existeLinea(Linea l) {
        for (Linea existente : lineasDibujadas)
        {
            if (existente.esIgual(l))
            {
                return true;
            }
        }
        return false;
    }

    public boolean verificarCuadroCerrado(Linea linea) {
        boolean cerroAlmenosUno = false;
        // Revisamos todos los cuadros para ver si la nueva línea completó alguno
        for (Cuadro cuadro : cuadros)
        {
            if (!cuadro.isCompletado())
            {
                // Pasamos las líneas dibujadas para que el cuadro se autoevalúe
                if (cuadro.verificarCompletado(this.lineasDibujadas))
                {
                    cerroAlmenosUno = true;
                }
            }
        }
        return cerroAlmenosUno;
    }

    public Object getEstado() {
        return this; // Retorna el objeto completo o un DTO si se requiriera
    }

    public Map<Jugador, Integer> calcularPuntajes() {
        Map<Jugador, Integer> puntajes = new HashMap<>();
        for (Cuadro c : cuadros)
        {
            if (c.isCompletado() && c.getPropietario() != null)
            {
                puntajes.put(c.getPropietario(),
                        puntajes.getOrDefault(c.getPropietario(), 0) + 1);
            }
        }
        return puntajes;
    }

    public List<Cuadro> getCuadros() {
        return cuadros;
    }
}
