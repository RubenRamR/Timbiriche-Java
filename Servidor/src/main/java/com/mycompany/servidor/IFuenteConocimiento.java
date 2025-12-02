/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.servidor;

/**
 *
 * @author rramirez
 */
public interface IFuenteConocimiento {

    /**
     * Recibe una notificación de que algo pasó en el sistema.
     *
     * @param evento El evento ocurrido.
     */
    void procesarEvento(Evento evento);

    /**
     * Inyecta la referencia al pizarrón para poder consultarlo/escribir.
     *
     * @param bb La instancia del Blackboard.
     */
    void setBlackboard(Blackboard bb);
}
