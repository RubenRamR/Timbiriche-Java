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

public class Tablero {

    private int dimension; // Cantidad de puntos por lado (ej. 10x10 puntos)
    private List<Linea> lineasDibujadas;
    private List<Cuadro> cuadros;

    public Tablero(int dimension) {
        this.dimension = dimension;
        this.lineasDibujadas = new ArrayList<>();
        this.cuadros = new ArrayList<>();
        inicializarCuadros();
    }

    /**
     * Crea la estructura lógica de cuadros vacíos. Si la dimensión es N puntos,
     * hay (N-1)*(N-1) cuadros.
     */
    private void inicializarCuadros() {
        // Este método prepara la lista para que 'cuadros' tenga el tamaño correcto.
        int cantidadCuadros = (dimension - 1) * (dimension - 1);
        for (int i = 0; i < cantidadCuadros; i++)
        {
            cuadros.add(new Cuadro());
        }
    }

    /**
     * Intenta agregar una línea al tablero.
     *
     * @return true si la línea fue agregada (no existía), false si ya estaba.
     */
    public boolean agregarLinea(Linea linea) {
        // Verificar si ya existe
        for (Linea l : lineasDibujadas)
        {
            if (l.esIgual(linea))
            {
                return false; // Ya existe
            }
        }
        lineasDibujadas.add(linea);
        return true;
    }

    /**
     * Verifica si la línea recién colocada cerró algún cuadro. Este método
     * contiene la MATEMÁTICA del grid.
     *
     * @param linea La línea que se acaba de poner.
     * @return true si se cerró al menos un cuadro.
     */
    public boolean verificarCuadroCerrado(Linea linea) {
        boolean cerroAlgo = false;

        // Determinar coordenadas normalizadas (menor y mayor)
        // Esto asume que los puntos son adyacentes y ortogonales.
        Punto p1 = linea.getP1();
        Punto p2 = linea.getP2();

        // Verificar si la linea es horizontal o vertical
        boolean esHorizontal = (p1.getY() == p2.getY());

        // Lógica para mapear la línea a los cuadros adyacentes.
        // Un cuadro se identifica por su esquina superior izquierda (x, y).
        // El índice en la lista 'cuadros' se puede calcular como: index = y * (dim-1) + x
        int x = Math.min(p1.getX(), p2.getX());
        int y = Math.min(p1.getY(), p2.getY());

        // CASO 1: Línea Horizontal
        if (esHorizontal)
        {
            // Cuadro de ARRIBA de la línea (si existe) -> Coord esquina sup-izq: (x, y-1)
            if (y > 0)
            {
                checkCuadroEn(x, y - 1, linea);
                if (getCuadroEn(x, y - 1).isCompletado())
                {
                    cerroAlgo = true;
                }
            }
            // Cuadro de ABAJO de la línea (si existe) -> Coord esquina sup-izq: (x, y)
            if (y < dimension - 1)
            {
                checkCuadroEn(x, y, linea);
                if (getCuadroEn(x, y).isCompletado())
                {
                    cerroAlgo = true;
                }
            }
        } // CASO 2: Línea Vertical
        else
        {
            // Cuadro de IZQUIERDA de la línea (si existe) -> Coord esquina sup-izq: (x-1, y)
            if (x > 0)
            {
                checkCuadroEn(x - 1, y, linea);
                if (getCuadroEn(x - 1, y).isCompletado())
                {
                    cerroAlgo = true;
                }
            }
            // Cuadro de DERECHA de la línea (si existe) -> Coord esquina sup-izq: (x, y)
            if (x < dimension - 1)
            {
                checkCuadroEn(x, y, linea);
                if (getCuadroEn(x, y).isCompletado())
                {
                    cerroAlgo = true;
                }
            }
        }

        return cerroAlgo;
    }

    // Helper para agregar la línea al cuadro y verificar
    private void checkCuadroEn(int col, int fila, Linea linea) {
        Cuadro c = getCuadroEn(col, fila);
        if (c != null && !c.isCompletado())
        {
            c.agregarLinea(linea);
            c.verificarCompletado();
        }
    }

    // Helper para obtener un cuadro basado en coordenadas de grid
    private Cuadro getCuadroEn(int col, int fila) {
        int index = fila * (dimension - 1) + col;
        if (index >= 0 && index < cuadros.size())
        {
            return cuadros.get(index);
        }
        return null;
    }

    public Object obtenerEstado() {
        return this;
    }

    // Getters
    public int getDimension() {
        return dimension;
    }

    public List<Linea> getLineasDibujadas() {
        return lineasDibujadas;
    }

    public List<Cuadro> getCuadros() {
        return cuadros;
    }
}
