/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.interfacesreceptor;

import com.mycompany.dtos.DataDTO;

/**
 *
 * @author rramirez
 * Define el contrato para RECIBIR mensajes desde la red. Lógica de juego
 * implementa esta interfaz para escuchar lo que llega.
 */
public interface IReceptorExterno {

    void recibirMensaje(DataDTO datos);
}
