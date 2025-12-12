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
import com.mycompany.protocolo.Protocolo;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import timbiriche.presentacion.ControllerView;
import timbiriche.presentacion.GameView;
import timbiriche.presentacion.IModelViewLeible;
import timbiriche.presentacion.IModelViewModificable;
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

                // ================================================================
                // FASE 2: INICIALIZAR ARQUITECTURA (MVC + Motor + Red)
                // ================================================================
                // 2.1 Crear Motor
                motor = new MotorJuego();
                motor.setJugadorLocal(jugadorLocal);
                motor.setSoyHost(esHost);
                motor.setListaJugadores(new ArrayList<>());

                // 2.2 Crear ModelView (Único modelo para Lobby y GameView)
                modelView = new ModelView(motor);

                // 2.3 Crear ControllerView (Único controlador para ambas vistas)
                controller = new ControllerView(modelView);

                // 2.4 Configurar Red
                IReceptorExterno receptor = new ReceptorUnificado();
                dispatcher = FabricaRED.configurarRed(puertoCliente, ipServidor, 8080);
                FabricaRED.establecerReceptor(receptor);
                motor.addDispatcher(dispatcher);

                // ================================================================
                // FASE 3: MOSTRAR LOBBY (Sala de espera)
                // ================================================================
                lobbyView = new LobbyView(controller, modelView);
                lobbyView.setVisible(true);

                // ================================================================
                // FASE 4: ENVIAR REGISTRO AL SERVIDOR
                // ================================================================
                DataDTO registroDTO = new DataDTO();
                registroDTO.setTipo("REGISTRO");
                registroDTO.setProyectoOrigen(nombre);
                registroDTO.setPayload(jugadorLocal);
                dispatcher.enviar(registroDTO, ipServidor, 8080);

                System.out.println("[App] Registro enviado. Esperando respuesta del servidor...");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    // =========================================================================
    // RECEPTOR UNIFICADO: Maneja mensajes de Lobby y Juego
    // =========================================================================
    static class ReceptorUnificado implements IReceptorExterno {

        @Override
        public void recibirMensaje(DataDTO datos) {
            if (datos == null || datos.getTipo() == null) {
                return;
            }

            System.out.println("[App-Receptor] ✉️ Recibido: " + datos.getTipo());

            try {
                Protocolo protocolo = Protocolo.valueOf(datos.getTipo());

                switch (protocolo) {
                    // ========================================================
                    // MENSAJES DE LOBBY
                    // ========================================================

                    case LISTA_JUGADORES:
                        System.out.println("[App-Receptor] 📋 Procesando LISTA_JUGADORES...");
                        procesarListaJugadores(datos);
                        break;

                    case INICIO_PARTIDA:
                        System.out.println("[App-Receptor] 🚀 Procesando INICIO_PARTIDA...");
                        procesarInicioPartida(datos);
                        break;

                    case INICIO_RECHAZADO:
                        System.out.println("[App-Receptor] ❌ Procesando INICIO_RECHAZADO...");
                        procesarRechazoInicio(datos);
                        break;

                    // ========================================================
                    // MENSAJES DE JUEGO
                    // ========================================================
                    case ACTUALIZAR_TABLERO:
                    case CUADRO_CERRADO:
                        System.out.println("[App-Receptor] 🎮 Procesando jugada...");
                        procesarJugada(datos);
                        break;

                    case JUGADA_INVALIDA:
                        System.err.println("[App-Receptor] ⚠️ Jugada inválida rechazada.");
                        break;

                    default:
                        System.out.println("[App-Receptor] ⚠️ Mensaje no manejado: " + protocolo);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("[App-Receptor] ❌ Protocolo desconocido: " + datos.getTipo());
            }
        }

        // =====================================================================
        // PROCESAMIENTO DE LISTA DE JUGADORES
        // =====================================================================
        private void procesarListaJugadores(DataDTO datos) {
            Object payload = datos.getPayload();

            if (payload instanceof List) {
                List<Jugador> jugadores = convertirAJugadores((List<?>) payload);

                System.out.println("[App] 📋 Lista actualizada: " + jugadores.size() + " jugador(es)");
                for (Jugador j : jugadores) {
                    System.out.println("   - " + j.getNombre() + " (" + j.getColor() + ")");
                }

                // Delegar al motor
                motor.actualizarListaDeJugadores(jugadores);
            }
        }

        // =====================================================================
        // PROCESAMIENTO DE INICIO DE PARTIDA
        // =====================================================================
        private void procesarInicioPartida(DataDTO datos) {
            System.out.println("[App] 🎉 ¡PARTIDA INICIADA POR EL SERVIDOR!");

            Object payload = datos.getPayload();
            int dimension = 10; // por defecto

            if (payload instanceof Map) {
                Map<?, ?> config = (Map<?, ?>) payload;
                if (config.containsKey("dimension")) {
                    dimension = ((Number) config.get("dimension")).intValue();
                    System.out.println("[App] 📐 Dimensión del tablero: " + dimension);
                }

                if (config.containsKey("mensaje")) {
                    String mensaje = (String) config.get("mensaje");
                    System.out.println("[App] 💬 Servidor dice: " + mensaje);
                }
            }

            final int dim = dimension;

            System.out.println("[App] 🔄 Notificando al motor sobre inicio...");
            // Notificar al motor (cerrará el lobby vía ModelView)
            motor.recibirInicioPartida(dim);

            System.out.println("[App] 🖥️ Programando apertura de GameView...");
            // Abrir GameView en el hilo de Swing
            SwingUtilities.invokeLater(() -> abrirGameView(dim));
        }

        private void abrirGameView(int dimension) {
            System.out.println("[App] 🎮 Abriendo GameView con tablero " + dimension + "x" + dimension);

            try {
                // Cerrar lobby si aún está abierto
                if (lobbyView != null && lobbyView.isVisible()) {
                    System.out.println("[App] 🚪 Cerrando LobbyView...");
                    lobbyView.dispose();
                    lobbyView = null; // Liberar referencia
                }

                System.out.println("[App] 🏗️ Creando GameView...");
                // Crear y mostrar GameView (usa el mismo ModelView y Controller)
                gameView = new GameView(controller, modelView);
                gameView.setTitle("Timbiriche - " + jugadorLocal.getNombre());

                System.out.println("[App] 👁️ Mostrando GameView...");
                gameView.setVisible(true);

                System.out.println("[App] ✅ GameView iniciada exitosamente.");

            } catch (Exception e) {
                System.err.println("[App] ❌ ERROR abriendo GameView: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // =====================================================================
        // PROCESAMIENTO DE RECHAZO DE INICIO
        // =====================================================================
        private void procesarRechazoInicio(DataDTO datos) {
            String motivo = (String) datos.getPayload();
            System.out.println("[App] ❌ Inicio rechazado: " + motivo);

            // Notificar al motor
            motor.recibirRechazoInicio(motivo);
        }

        // =====================================================================
        // PROCESAMIENTO DE JUGADAS
        // =====================================================================
        private void procesarJugada(DataDTO datos) {
            Object payload = datos.getPayload();

            if (payload instanceof Map) {
                Map<?, ?> mapaLinea = (Map<?, ?>) payload;
                com.mycompany.dominio.Linea linea = convertirMapaALinea(mapaLinea);

                if (linea != null) {
                    String nombreRemitente = datos.getProyectoOrigen();
                    Jugador jugadorRemitente = buscarJugadorPorNombre(nombreRemitente);

                    if (jugadorRemitente == null) {
                        jugadorRemitente = new com.mycompany.dominio.Jugador(
                                nombreRemitente != null ? nombreRemitente : "Desconocido",
                                "#808080"
                        );
                    }

                    motor.realizarJugadaRemota(linea, jugadorRemitente);
                }
            }
        }

        // =====================================================================
        // CONVERSORES (Mapa → Objetos del Dominio)
        // =====================================================================
        private List<Jugador> convertirAJugadores(List<?> lista) {
            List<Jugador> jugadores = new ArrayList<>();

            for (Object item : lista) {
                if (item instanceof Map) {
                    Map<?, ?> mapa = (Map<?, ?>) item;
                    String nombre = (String) mapa.get("nombre");
                    String color = (String) mapa.get("color");

                    if (nombre != null && color != null) {
                        Jugador j = new Jugador(nombre, color);

                        // **CRÍTICO**: Restaurar puerto si existe
                        if (mapa.containsKey("puertoEscucha")) {
                            Object puertoObj = mapa.get("puertoEscucha");
                            if (puertoObj instanceof Number) {
                                j.setPuertoEscucha(((Number) puertoObj).intValue());
                            }
                        }

                        // **NUEVO**: Restaurar estado "listo"
                        if (mapa.containsKey("listo")) {
                            Object listoObj = mapa.get("listo");
                            if (listoObj instanceof Boolean) {
                                j.setListo((Boolean) listoObj);
                                System.out.println("[App-Conversor] " + nombre + " | listo=" + listoObj);
                            }
                        }

                        jugadores.add(j);
                    }
                } else if (item instanceof Jugador) {
                    jugadores.add((Jugador) item);
                }
            }

            return jugadores;
        }

        private com.mycompany.dominio.Linea convertirMapaALinea(Map<?, ?> mapa) {
            try {
                Map<?, ?> p1Map = (Map<?, ?>) mapa.get("p1");
                Map<?, ?> p2Map = (Map<?, ?>) mapa.get("p2");

                int x1 = getInt(p1Map.get("x"));
                int y1 = getInt(p1Map.get("y"));
                int x2 = getInt(p2Map.get("x"));
                int y2 = getInt(p2Map.get("y"));

                return new com.mycompany.dominio.Linea(
                        new com.mycompany.dominio.Punto(x1, y1),
                        new com.mycompany.dominio.Punto(x2, y2)
                );
            } catch (Exception e) {
                System.err.println("[App] Error convirtiendo mapa a línea: " + e.getMessage());
                return null;
            }
        }

        private int getInt(Object obj) {
            if (obj instanceof Number) {
                return ((Number) obj).intValue();
            }
            return 0;
        }

        private Jugador buscarJugadorPorNombre(String nombre) {
            if (nombre == null) {
                return null;
            }

            List<Jugador> jugadores = motor.getJugadores();
            if (jugadores == null) {
                return null;
            }

            for (Jugador j : jugadores) {
                if (j.getNombre().equals(nombre)) {
                    return j;
                }
            }
            return null;
        }
    }
}
