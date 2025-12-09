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
import timbiriche.presentacion.GameView;
import timbiriche.presentacion.ModelView;

/**
 * Punto de entrada de la aplicación Cliente.
 */
public class TimbiricheApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
        {
            try
            {
                
                String nombre = "Jugador_" + new Random().nextInt(1000);

                String hexColor = String.format("#%06x", new Random().nextInt(0xffffff + 1));

                String ipServidor = JOptionPane.showInputDialog("IP del Servidor:", "127.0.0.1");
                if (ipServidor == null || ipServidor.trim().isEmpty())
                {
                    return;
                }

                // 2. CREAR JUGADOR LOCAL
                Jugador yo = new Jugador(nombre, hexColor);
                System.out.println("=== INICIANDO: " + nombre + " (" + hexColor + ") ===");

                // 3. MOTOR (Inicia sin rivales, esperando la lista del server)
                MotorJuego motor = new MotorJuego();
                motor.setJugadorLocal(yo);
                motor.setListaJugadores(new ArrayList<>()); // Lista vacía al inicio

                // 4. CONEXIÓN RED
                IReceptorExterno receptor = new ReceptorExternoImpl(motor);
                FabricaRED fabricaRed = new FabricaRED();

                // Puerto local 9000 (Estándar)
                IDispatcher dispatcher = fabricaRed.configurarRed(9000, ipServidor, 8080);

                fabricaRed.establecerReceptor(receptor);
                motor.addDispatcher(dispatcher);

                // 5. GUI
                ModelView modelView = new ModelView(motor);
                ControllerView controller = new ControllerView(receptor, yo);
                GameView view = new GameView(controller, modelView);
                view.setVisible(true);

                // =========================================================
                // 6. REGISTRO AUTOMÁTICO
                // =========================================================
                DataDTO registroDTO = new DataDTO();
                registroDTO.setTipo("REGISTRO");
                registroDTO.setProyectoOrigen(nombre);
                String jsonJugador = "{"
                        + "\"nombre\":\"" + yo.getNombre() + "\","
                        + "\"color\":\"" + yo.getColor() + "\","
                        + "\"puntaje\":0"
                        + "}";

                registroDTO.setPayload(jsonJugador);
                // Enviamos solicitud
                dispatcher.enviar(registroDTO, ipServidor, 8080);

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }
}
