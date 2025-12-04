/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.modelojuego;

import com.mycompany.dominio.Linea;
import com.mycompany.dominio.Punto;
import com.mycompany.dtos.DataDTO;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import com.mycompany.protocolo.Protocolo;

/**
 *
 * @author rramirez
 */
public class ReceptorExternoImpl implements IReceptorExterno {

    // Referencia al núcleo de la lógica
    private MotorJuego motorJuego;

    /**
     * Constructor
     *
     * @param motorJuego Referencia al motor para poder inyectarle las jugadas
     * que lleguen.
     */
    public ReceptorExternoImpl(MotorJuego motorJuego) {
        this.motorJuego = motorJuego;
    }

    /**
     * Recibe un mensaje genérico (DTO) desde la infraestructura de red. Analiza
     * el tipo de mensaje (Protocolo) y delega a métodos privados.
     *
     * * @param datos El objeto de transferencia recibido.
     */
    @Override
    public void recibirMensaje(DataDTO datos) {
        // 1. Validaciones de seguridad
        if (datos == null || datos.getTipo() == null) {
            System.err.println("ReceptorExterno: Recibido mensaje nulo o sin tipo.");
            return;
        }

        String tipoMensaje = datos.getTipo();

        // -------------------------------------------------------------
        // CASO 1: Confirmación del Servidor (Viene de la RED)
        // El servidor nos dice "Esta jugada es válida, dibújenla todos".
        // -------------------------------------------------------------
        if (tipoMensaje.equals(Protocolo.ACTUALIZAR_TABLERO.toString())) {
            if (motorJuego != null) {
                // Pasamos el DTO directo, ya que 'procesarJugadaRed' sabe deserializarlo
                motorJuego.procesarJugadaRed(datos);
            }
        } 

        // -------------------------------------------------------------
        // CASO 2: Solicitud Local (Viene del ControllerView / UI)
        // El usuario hizo clic. El controlador empaquetó un INTENTO_JUGADA.
        // Aquí lo desempaquetamos y se lo pasamos al motor como "Acción Local".
        // -------------------------------------------------------------
        else if (tipoMensaje.equals(Protocolo.INTENTO_JUGADA.toString())) {
            if (motorJuego != null) {
                // Deserializamos el payload (String "x1,y1,x2,y2") a Objeto Linea
                Linea linea = deserializarLinea(datos.getPayload());
                
                if (linea != null) {
                    // Llamamos al flujo local del motor
                    motorJuego.realizarJugadaLocal(linea);
                } else {
                    System.err.println("ReceptorExterno: Error al deserializar jugada local.");
                }
            }
        }
        
        // -------------------------------------------------------------
        // CASO 3: Inicio de Partida (Opcional pero recomendado)
        // -------------------------------------------------------------
        else if (tipoMensaje.equals(Protocolo.INICIO_PARTIDA.toString())) {
             System.out.println("ReceptorExterno: Iniciando partida...");
             // Aquí podrías notificar a la vista para quitar la pantalla de espera
        }

        // Otros casos (Login, Errores, etc.)
        else {
            System.out.println("ReceptorExterno: Tipo no procesado -> " + tipoMensaje);
        }
    }

    /**
     * Método auxiliar para convertir el String del payload nuevamente a un objeto Linea.
     * Formato esperado: "x1,y1,x2,y2"
     */
    private Linea deserializarLinea(String payload) {
        if (payload == null || payload.isEmpty()) return null;
        
        try {
            String[] partes = payload.split(",");
            if (partes.length < 4) return null;

            int x1 = Integer.parseInt(partes[0]);
            int y1 = Integer.parseInt(partes[1]);
            int x2 = Integer.parseInt(partes[2]);
            int y2 = Integer.parseInt(partes[3]);

            // Asumiendo que tienes los constructores correctos en Punto y Linea
            return new Linea(new Punto(x1, y1), new Punto(x2, y2));
            
        } catch (NumberFormatException e) {
            System.err.println("Error formato numérico en payload: " + payload);
            return null;
        }
    }

    /**
     * Método privado (según tu diagrama) que concreta la acción de jugar.
     * Delega la responsabilidad al método del motor diseñado para la RED.
     */
    private void realizarJugada(DataDTO datos) {
        if (motorJuego != null)
        {
            // Asegúrate de que este método sea 'public' en MotorJuego
            motorJuego.procesarJugadaRed(datos);
        }
    }
}
