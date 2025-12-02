/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.manejadorjuego;


import com.mycompany.ireceptorexterno.IReceptorExterno;
import interfaces.dispatcher.IDispatcher;
import com.mycompany.red.DataDTO;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Serva
 */
public class ManejadorJuego implements IReceptorExterno {
    private final IDispatcher dispatcher;
    private final EstadoJuego estado;

    public ManejadorJuego(IDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.estado = new EstadoJuego();
    }

    @Override
    public void realizarJugada(DataDTO datos) {
        System.out.println("[ManejadorJuego] 🎮 Jugada recibida: " + datos);
        
        // Aplicar lógica de negocio
        estado.procesarJugada(datos);
        
        // Generar respuesta
        DataDTO respuesta = new DataDTO("RESULTADO");
        respuesta.setPayload("Jugada procesada: " + datos.getPayload());
        respuesta.setProyectoOrigen("LOGICA");
        
        // Enviar respuesta si hay destino
        if (datos.getProyectoOrigen() != null) {
            dispatcher.notificarActualizacion(respuesta, "127.0.0.1", 9000);
        }
    }

    @Override
    public void recibirMovimientoRed(DataDTO datos) {
        System.out.println("[ManejadorJuego] 🎯 Movimiento recibido: " + datos);
        
        estado.actualizarMovimiento(datos);
        
        // Notificar a otros jugadores
        DataDTO notificacion = new DataDTO("MOVIMIENTO_ACTUALIZADO");
        notificacion.setPayload("Movimiento procesado");
        notificacion.setProyectoOrigen("LOGICA");
        
        dispatcher.notificarActualizacion(notificacion, "127.0.0.1", 9001);
    }
    
    public EstadoJuego getEstado() {
        return estado;
    }
}