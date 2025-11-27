/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.mycompany.clienteprueba;

import com.mycompany.libreriacomun.DTOs.DataDTO;
import com.mycompany.libreriacomun.Interfaces.IReceptorExterno;

/**
 *
 * @author rramirez
 */
public class ClienteLogica implements IReceptorExterno {

    @Override
    public void recibirMensaje(DataDTO datos) {
        if (datos.es(Protocolo.ACTUALIZAR_TABLERO))
        {
            System.out.println("[CLIENTE] Servidor validó: " + datos.getPayload());
        } else
        {
            System.out.println("[CLIENTE] Mensaje inesperado: " + datos.getTipo());
        }
    }
}
