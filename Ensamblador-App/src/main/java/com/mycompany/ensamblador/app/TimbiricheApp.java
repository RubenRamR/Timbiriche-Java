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
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import timbiriche.presentacion.ControllerView;
import timbiriche.presentacion.GameView;
import timbiriche.presentacion.ModelView;
import timbiriche.presentacion.RegistroJugadorView;

/**
 * Punto de entrada de la aplicación Cliente.
 */
public class TimbiricheApp {

    // --- Mapeo lógico de avatares (no acoplado a rutas físicas de imágenes) ---
    // Estos valores se guardan en Jugador.rutaAvatar
    // y la Presentación los traducirá a rutas de recursos reales.
    private static final String[] AVATAR_KEYS = {
        "avatar1",
        "avatar2",
        "avatar3",
        "avatar4"
    };

    // --- Datos de conexión ---
    private String ipServidor;
    private final int puertoServidor = 8080;
    private int puertoCliente;

    // --- Componentes de lógica y presentación ---
    private MotorJuego motor;
    private IDispatcher dispatcher;
    private IReceptorExterno receptor;
    private ModelView modelView;
    private ControllerView controller;
    private GameView view;
    private RegistroJugadorView registroView;
    private Jugador yo;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TimbiricheApp app = new TimbiricheApp();
            app.iniciar();
        });
    }

    /**
     * Primer paso de la aplicación: - Pregunta la IP del host - Calcula un
     * puerto local aleatorio - Muestra la ventana de RegistroJugadorView
     */
    public void iniciar() {
        try {
            // 1. OBTENER IP DEL SERVIDOR
            ipServidor = JOptionPane.showInputDialog(
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
            puertoCliente = 9000 + new Random().nextInt(900);

            System.out.println("=== CLIENTE ARRANCANDO ===");
            System.out.println("=== ESCUCHARÁ EN PUERTO: " + puertoCliente + " ===");
            System.out.println("=== SERVIDOR DESTINO: " + ipServidor + ":" + puertoServidor + " ===");

            // 3. MOSTRAR VENTANA DE REGISTRO
            registroView = new RegistroJugadorView();
            registroView.setRegistroJugadorListener(
                    (nickname, avatarId) -> registrarJugador(nickname, avatarId)
            );
            registroView.mostrar();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al iniciar cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Caso de uso REGISTRAR JUGADOR. Es llamado desde RegistroJugadorView
     * cuando el usuario pulsa el botón Registrar.
     *
     * @param nickname Nombre elegido por el jugador.
     * @param avatarId Identificador del avatar seleccionado (0..3).
     */
    public void registrarJugador(String nickname, int avatarId) {
        try {
            // 1. GENERAR COLOR (por ahora seguimos usando un color aleatorio)
            String hexColor = String.format("#%06x", new Random().nextInt(0xffffff + 1));

            // 2. CREAR OBJETO JUGADOR LOCAL
            yo = new Jugador(nickname, hexColor);
            yo.setPuertoEscucha(puertoCliente);

            // NUEVO: asignar ruta del avatar directamente (sin avatarKey)
            String rutaAvatar = "/avatars/avatar" + (avatarId + 1) + ".png";
            yo.setRutaAvatar(rutaAvatar);

            System.out.println("=== CLIENTE: " + nickname + " ===");
            System.out.println("=== AVATAR ID: " + avatarId + " ===");
            System.out.println("=== RUTA AVATAR: " + rutaAvatar + " ===");
            System.out.println("=== ESCUCHANDO EN PUERTO: " + puertoCliente + " ===");
            System.out.println("=== SERVIDOR DESTINO: " + ipServidor + ":" + puertoServidor + " ===");

            // 3. INICIALIZAR COMPONENTES DE JUEGO
            motor = new MotorJuego();
            motor.setJugadorLocal(yo);
            motor.setListaJugadores(new ArrayList<>());

            receptor = new ReceptorExternoImpl(motor);
            dispatcher = FabricaRED.configurarRed(puertoCliente, ipServidor, puertoServidor);

            FabricaRED.establecerReceptor(receptor);
            motor.addDispatcher(dispatcher);

            // 4. LEVANTAR GUI DEL JUEGO
            modelView = new ModelView(motor);
            controller = new ControllerView(modelView);
            view = new GameView(controller, modelView);
            view.setTitle("Timbiriche | Soy: " + nickname + " (Puerto: " + puertoCliente + ")");
            view.setVisible(true);

            // 5. ENVIAR SOLICITUD DE REGISTRO AL SERVIDOR
            DataDTO registroDTO = new DataDTO();
            registroDTO.setTipo("REGISTRO");
            registroDTO.setProyectoOrigen(nickname);
            registroDTO.setPayload(yo); // 'yo' YA incluye puertoEscucha y rutaAvatar

            dispatcher.enviar(registroDTO, ipServidor, puertoServidor);
            System.out.println("[Cliente] Enviando registro de jugador: " + nickname);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al registrar jugador: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
