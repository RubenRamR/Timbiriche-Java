/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.ensamblador.app;

/**
 *
 * @author rramirez
 */
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mycompany.componentered.FabricaRED;
import com.mycompany.dominio.Jugador;
import com.mycompany.dominio.Linea;
import com.mycompany.dominio.Punto;
import com.mycompany.dtos.DataDTO;
import com.mycompany.interfacesdispatcher.IDispatcher;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import com.mycompany.modelojuego.MotorJuego;
import com.mycompany.modelojuego.ReceptorExternoImpl;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import timbiriche.presentacion.ControllerView;
import timbiriche.presentacion.Vistas.GameView;
import timbiriche.presentacion.IModelViewLeible;
import timbiriche.presentacion.IModelViewModificable;
import timbiriche.presentacion.ModelView;

/**
 * Punto de entrada de la aplicación Cliente.
 */
public class TimbiricheApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. OBTENER IP DEL SERVIDOR
                String ipServidor = JOptionPane.showInputDialog(
                        null, 
                        "Ingresa la IP del Host:\n(Si tú eres el Host, escribe 'localhost')", 
                        "Conectar a Partida", 
                        JOptionPane.QUESTION_MESSAGE
                );

                if (ipServidor == null || ipServidor.trim().isEmpty()) {
                    System.exit(0);
                }

                // 2. CONFIGURACIÓN DEL PUERTO LOCAL (DINÁMICO)
                // Generamos un puerto aleatorio para escuchar respuestas
                int puertoCliente = 9000 + new Random().nextInt(900); 

                // 3. DATOS DEL JUGADOR
                String nombre = "Jugador_" + new Random().nextInt(1000);
                String hexColor = String.format("#%06x", new Random().nextInt(0xffffff + 1));
                
                Jugador yo = new Jugador(nombre, hexColor);
                
                yo.setPuertoEscucha(puertoCliente); 
                
                System.out.println("=== CLIENTE: " + nombre + " ===");
                System.out.println("=== ESCUCHANDO EN PUERTO: " + puertoCliente + " ===");
                System.out.println("=== SERVIDOR DESTINO: " + ipServidor + ":8080 ===");

                // 4. INICIALIZAR COMPONENTES
                MotorJuego motor = new MotorJuego();
                motor.setJugadorLocal(yo);
                motor.setListaJugadores(new ArrayList<>()); 

                IReceptorExterno receptor = new ReceptorExternoImpl(motor);
                
                // Configurar red escuchando en puertoCliente
                IDispatcher dispatcher = FabricaRED.configurarRed(puertoCliente, ipServidor, 8080);

                FabricaRED.establecerReceptor(receptor);
                motor.addDispatcher(dispatcher);

                // 5. LEVANTAR GUI
                ModelView modelView = new ModelView(motor); 
                ControllerView controller = new ControllerView(modelView);
                GameView view = new GameView(controller, modelView);       
                view.setTitle("Timbiriche - " + nombre + " (Puerto: " + puertoCliente + ")");
                view.setVisible(true);

                // 6. SOLICITUD DE REGISTRO
                DataDTO registroDTO = new DataDTO();
                registroDTO.setTipo("REGISTRO");
                registroDTO.setProyectoOrigen(nombre);
                
                // Enviamos el objeto 'yo' que YA INCLUYE el puertoEscucha adentro
                registroDTO.setPayload(yo); 

                // Enviamos al servidor (puerto fijo 8080)
                dispatcher.enviar(registroDTO, ipServidor, 8080);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error al conectar: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}