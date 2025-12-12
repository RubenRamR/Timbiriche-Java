/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.mycompany.protocolo;

/**
 *
 * @author rramirez
 */
public enum Protocolo {
    // --- 1. GESTIÓN DE SALA (CONEXIÓN) ---
    /**
     * Cliente -> Servidor: "Quiero entrar con el nombre X"
     */
    SOLICITUD_LOGIN,
    /**
     * Servidor -> Cliente: "Bienvenido, espera a que empiece"
     */
    LOGIN_ACEPTADO,
    /**
     * Servidor -> Cliente: "Sala llena o nombre repetido"
     */
    LOGIN_RECHAZADO,
    /**
     * Cliente (Host) -> Servidor: "Iniciar la partida ahora"
     */
    SOLICITUD_INICIO_PARTIDA,
    /**
     * Servidor -> Todos: "Ya estamos todos, comienza el juego"
     */
    INICIO_PARTIDA,
    
    INICIO_RECHAZADO,
    // --- 2. FLUJO DEL JUEGO (TIMBIRICHE) ---
    /**
     * Cliente -> Servidor: "Quiero poner una línea en estas coordenadas"
     */
    INTENTO_JUGADA,
    /**
     * Servidor -> Cliente: "Esa línea ya estaba ocupada / No es tu turno"
     */
    JUGADA_INVALIDA,
    /**
     * Servidor -> Todos: "La jugada es válida, DIBUJEN la línea"
     */
    ACTUALIZAR_TABLERO,
    /**
     * Servidor -> Todos: "Alguien cerró cuadro, ASIGNEN punto y relleno"
     */
    CUADRO_CERRADO,
    /**
     * Servidor -> Todos: "Terminó el turno actual, le toca a X"
     */
    CAMBIO_TURNO,
    /**
     * Servidor -> Todos: "Se llenó el tablero, ganó X"
     */
    FIN_PARTIDA,
    // --- 3. INFRAESTRUCTURA INTERNA (BLACKBOARD) ---
    /**
     * * Experto -> Control: "Necesito que envíes este DTO por la red". (Este
     * es vital para romper el ciclo de dependencia en el servidor)
     */
    SOLICITUD_ENVIO,
    LISTA_JUGADORES,
    REGISTRO
}
