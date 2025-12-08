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
                // ===========================================================
                // 1. CONFIGURACIÓN DE CONEXIÓN (UI)
                // ===========================================================
                
                // A. Pedir Nombre
                String nombre = JOptionPane.showInputDialog(null, "Escribe tu nombre:", "Login", JOptionPane.QUESTION_MESSAGE);
                if (nombre == null || nombre.trim().isEmpty()) {
                    nombre = "Jugador_" + new Random().nextInt(1000);
                }
                
                // B. Pedir IP del Servidor
                // Si eres el Host, deja localhost. Si es tu amigo, debe poner TU IP (ej. 192.168.1.50)
                String ipServidor = JOptionPane.showInputDialog(null, "IP del Servidor (Host):", "127.0.0.1");
                if (ipServidor == null || ipServidor.trim().isEmpty()) {
                    ipServidor = "127.0.0.1";
                }

                // C. Configuración de Puertos
                int puertoServidor = 8080;
                int puertoLocal = 9000; // ESTÁNDAR: Todos escuchan en el 9000 en su propia PC

                System.out.println("=== INICIANDO TIMBIRICHE ===");
                System.out.println("Soy: " + nombre);
                System.out.println("Conectando a: " + ipServidor);

                // ===========================================================
                // 2. CONSTRUCCIÓN DEL NÚCLEO
                // ===========================================================
                
                // Colores aleatorios para diferenciar
                String colorHex = String.format("#%06x", new Random().nextInt(0xffffff + 1));
                Jugador local = new Jugador(nombre, colorHex);
                
                // Motor
                MotorJuego motor = new MotorJuego();
                motor.setJugadorLocal(local);

                // IMPORTANTE: Para que el juego inicie sin lobby, agregamos al local
                // y a un "Rival_Fantasma" para que la lógica de turnos no se rompa
                // hasta que el servidor sincronice la lista real.
                List<Jugador> listaInicial = new ArrayList<>();
                listaInicial.add(local);
                // listaInicial.add(new Jugador("Esperando...", "#CCCCCC")); 
                motor.setListaJugadores(listaInicial);

                // ===========================================================
                // 3. CONEXIÓN DE RED
                // ===========================================================
                IReceptorExterno receptor = new ReceptorExternoImpl(motor);
                FabricaRED fabricaRed = new FabricaRED();
                
                // Aquí se levanta el Socket en el puerto 9000 de ESTA laptop
                IDispatcher dispatcher = fabricaRed.configurarRed(puertoLocal, ipServidor, puertoServidor);
                
                fabricaRed.establecerReceptor(receptor);
                motor.addDispatcher(dispatcher);

                // ===========================================================
                // 4. PRESENTACIÓN
                // ===========================================================
                ModelView modelView = new ModelView(motor);
                ControllerView controller = new ControllerView(receptor, local);
                GameView view = new GameView(controller, modelView);
                
                view.setVisible(true);
                
                // Opcional: Enviar un saludo al servidor para registrar la IP
                // motor.solicitarIngreso(nombre, colorHex); 

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al iniciar: " + e.getMessage());
                System.exit(1);
            }
        });
    }
}