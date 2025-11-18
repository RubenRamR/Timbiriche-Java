/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.ireceptorexterno;

import com.mycompany.red.DataDTO;


/**
 *
 * @author Serva
 */
public interface IReceptorExterno {
    /**
     * Maneja una jugada recibida desde la red.
     */
    void realizarJugada(DataDTO datos);
    
    /**
     * Recibe un movimiento desde la red y lo procesa.
     */
    void recibirMovimientoRed(DataDTO datos);
}