/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.ensamblador.app;

/**
 *
 * @author rramirez
 */
import com.mycompany.componentered.FabricaRED;
import com.mycompany.dominio.Jugador;
import com.mycompany.dtos.DataDTO;
import com.mycompany.interfacesdispatcher.IDispatcher;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import com.mycompany.modelojuego.MotorJuego;
import com.mycompany.modelojuego.ReceptorExternoImpl;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import timbiriche.presentacion.ControllerView;
import timbiriche.presentacion.GameView;
import timbiriche.presentacion.LobbyView;
import timbiriche.presentacion.ModelView;

/**
 * Punto de entrada de la aplicación Cliente.
 */
public class TimbiricheApp {

    // Variables globales para el flujo
    private static MotorJuego motor;
    private static ModelView modelView;
    private static ControllerView controller;
    private static LobbyView lobbyView;
    private static GameView gameView;
    private static Jugador jugadorLocal;
    private static IDispatcher dispatcher;
    private static boolean esHost;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // ================================================================
                // FASE 1: CONFIGURACIÓN INICIAL
                // ================================================================

                // 1.1 Preguntarle al usuario si es HOST o CLIENTE
                String[] opciones = {"Crear Partida (Host)", "Unirse a Partida"};
                int opcion = JOptionPane.showOptionDialog(
                        null,
                        "¿Qué deseas hacer?",
                        "Timbiriche - Inicio",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        opciones,
                        opciones[0]
                );

                if (opcion == -1) {
                    System.exit(0);
                }

                esHost = (opcion == 0);

                // 1.2 Obtener IP del servidor
                String ipServidor;
                if (esHost) {
                    ipServidor = "localhost";
                    JOptionPane.showMessageDialog(
                            null,
                            "Serás el Host.\nOtros jugadores deben conectarse a tu IP local.",
                            "Información",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    ipServidor = JOptionPane.showInputDialog(
                            null,
                            "Ingresa la IP del Host:",
                            "Conectar a Partida",
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (ipServidor == null || ipServidor.trim().isEmpty()) {
                        System.exit(0);
                    }
                }

                // 1.3 Configuración del puerto local (dinámico)
                int puertoCliente = 9000 + new Random().nextInt(900);

                // 1.4 Datos del jugador (dinámico)
                String nombre = JOptionPane.showInputDialog(
                        null,
                        "Ingresa tu nombre:",
                        "Jugador_" + new Random().nextInt(1000)
                );
                if (nombre == null || nombre.trim().isEmpty()) {
                    nombre = "Jugador_" + new Random().nextInt(1000);
                }

                String hexColor = String.format("#%06x", new Random().nextInt(0xffffff + 1));
                jugadorLocal = new Jugador(nombre, hexColor);
                jugadorLocal.setPuertoEscucha(puertoCliente);

                System.out.println("=== CLIENTE: " + nombre + " ===");
                System.out.println("=== ROL: " + (esHost ? "HOST" : "CLIENTE") + " ===");
                System.out.println("=== SERVIDOR: " + ipServidor + ":8080 ===");
                System.out.println("=== PUERTO LOCAL: " + puertoCliente + " ===");
                // Crear Motor
                MotorJuego motor = new MotorJuego();
                motor.setJugadorLocal(jugadorLocal);
                motor.setSoyHost(esHost);

                // Crear ModelView
                ModelView modelView = new ModelView(motor);
                ControllerView controller = new ControllerView(modelView);

                // ✅ USAR ReceptorExternoImpl DIRECTAMENTE (ya existe en ModeloJuego)
                IReceptorExterno receptor = new ReceptorExternoImpl(motor);

                // Configurar Red
                IDispatcher dispatcher = FabricaRED.configurarRed(puertoCliente, ipServidor, 8080);
                FabricaRED.establecerReceptor(receptor);  // ← Conexión directa
                motor.addDispatcher(dispatcher);

                // Mostrar Lobby
                LobbyView lobbyView = new LobbyView(controller, modelView);
                lobbyView.setVisible(true);
                GameView gameView = new GameView(controller, modelView);
                gameView.setVisible(false);

                // Enviar registro
                DataDTO registroDTO = new DataDTO();
                registroDTO.setTipo("REGISTRO");
                registroDTO.setProyectoOrigen(nombre);
                registroDTO.setPayload(jugadorLocal);
                dispatcher.enviar(registroDTO, ipServidor, 8080);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
