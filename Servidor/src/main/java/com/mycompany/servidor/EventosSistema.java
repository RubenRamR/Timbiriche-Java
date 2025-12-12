/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.servidor;

/**
 *
 * @author rramirez
 */
public class EventosSistema {

    // Evento interno del sistema para indicar que se debe enviar un DTO
    public static final String SOLICITUD_ENVIO = "SYS_SOLICITUD_ENVIO";

     // Evento interno para manejar nuevas sesiones (no lo usamos aún directamente)
    public static final String NUEVA_SESION = "SYS_NUEVA_SESION";

    // Usado cuando un cliente quiere registrarse en la partida
    public static final String REGISTRO = "REGISTRO";

    // Usado cuando el servidor envía a los clientes la lista actual de jugadores
    public static final String LISTA_JUGADORES = "LISTA_JUGADORES";
}
