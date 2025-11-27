/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.mycompany.servidorprueba;

/**
 *
 * @author rramirez
 */
public enum Protocolo {
    // --- FLUJO REALIZAR JUGADA ---

    // 1. Cliente -> Servidor: "Quiero poner una línea aquí"
    INTENTO_JUGADA,
    // 2. Servidor -> Cliente: "Esa línea no se puede poner" (Error)
    JUGADA_INVALIDA,
    // 3. Servidor -> Todos: "Se puso una línea, actualicen sus tableros"
    ACTUALIZAR_TABLERO,
    // 4. Servidor -> Todos: "Se cerró un cuadro, punto para X"
    CUADRO_CERRADO,
    // 5. Servidor -> Todos: "Cambio de turno"
    CAMBIO_TURNO
}
