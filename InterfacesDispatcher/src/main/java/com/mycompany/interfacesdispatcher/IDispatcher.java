/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.interfacesdispatcher;

import com.mycompany.dtos.DataDTO;

/**
 *
 * @author rramirez ENVIAR mensajes a la red.
 *
 */
public interface IDispatcher {

    /**
     * Versión para el CLIENTE: Envía al servidor predeterminado.
     * No requiere saber IP ni puerto.
     */
    void enviar(DataDTO datos);

    /**
     * Versión para el SERVIDOR: Envía a un cliente específico.
     */
    void enviar(DataDTO datos, String ip, int puerto);
}
