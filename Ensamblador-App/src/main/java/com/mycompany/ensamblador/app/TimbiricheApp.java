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
       SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("=== INICIANDO CLIENTE TIMBIRICHE ===");

                // -----------------------------------------------------------
                // 1. CONFIGURACIÓN DE USUARIO Y RED (Hardcoded para Pruebas)
                // -----------------------------------------------------------
                // Definimos al jugador de esta máquina
                Jugador local = new Jugador("Jugador_Local", "#0000FF"); // Azul
                
                // Simulamos un rival para que la lógica de turnos funcione
                Jugador remoto = new Jugador("Rival_Remoto", "#FF0000"); // Rojo

                String ipServidor = "127.0.0.1";
                int puertoServidor = 8080; // Puerto donde escucha tu Servidor
                int puertoCliente = 9000;     // 0 = Automático

                // -----------------------------------------------------------
                // 2. CAPA LÓGICA (Core)
                // -----------------------------------------------------------
                MotorJuego motor = new MotorJuego();

                // CONFIGURACIÓN ESTADO INICIAL (Saltando Login)
                motor.setJugadorLocal(local);
                
                List<Jugador> listaInicial = new ArrayList<>();
                listaInicial.add(local);
//                Comentarear linea de abajo para probar en remoto
//                listaInicial.add(remoto);
                motor.setListaJugadores(listaInicial); // Esto activa el turno del primer jugador

                // -----------------------------------------------------------
                // 3. CAPA DE INFRAESTRUCTURA (Red)
                // -----------------------------------------------------------
                // A. Entrada (Receptor): Recibe mensajes y se los da al Motor
                IReceptorExterno receptor = new ReceptorExternoImpl(motor);

                // B. Salida (Dispatcher): Motor envía mensajes a la red
                FabricaRED fabricaRed = new FabricaRED();
                
                // Configurar sockets y obtener el objeto para enviar
                IDispatcher dispatcher = fabricaRed.configurarRed(puertoCliente, ipServidor, puertoServidor);
                
                // Conectar la entrada de red al receptor que creamos
                fabricaRed.establecerReceptor(receptor);

                // Conectar la salida del motor al dispatcher
                motor.addDispatcher(dispatcher);

                // -----------------------------------------------------------
                // 4. CAPA DE PRESENTACIÓN (MVC)
                // -----------------------------------------------------------
                
                // A. ViewModel (ModelView): Escucha cambios en el Motor
                ModelView modelView = new ModelView(motor);

                // B. Controller (ControllerView): Recibe inputs de la Vista
                // Inyectamos 'receptor' para enviar datos y 'local' para firmarlos
                ControllerView controller = new ControllerView(receptor, local);

                // C. View (GameView): Dibuja y detecta clics
                GameView view = new GameView(controller, modelView);

                // -----------------------------------------------------------
                // 5. ARRANQUE
                // -----------------------------------------------------------
                view.setVisible(true);
                System.out.println("-> Cliente iniciado correctamente.");
                System.out.println("-> Turno actual: " + motor.getTurnoActual().getNombre());

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error fatal al iniciar: " + e.getMessage());
                System.exit(1);
            }
        });
    }
}
