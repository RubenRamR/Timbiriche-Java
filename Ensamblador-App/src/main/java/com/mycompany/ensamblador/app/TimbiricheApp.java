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
import timbiriche.presentacion.Vistas.GameView;
import timbiriche.presentacion.ModelView;
import timbiriche.presentacion.Observer;
import timbiriche.presentacion.Vistas.ConfigurarPartidaView;

/**
 * Punto de entrada de la aplicación Cliente.
 */
public class TimbiricheApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
        {
            try
            {
                String ipServidor = (String) JOptionPane.showInputDialog(
                        null,
                        "Ingresa la IP del Host:\n(Deja 'localhost' si eres tú)",
                        "Conectar a Partida",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        "localhost"
                );
                if (ipServidor == null)
                {
                    System.exit(0);
                }

                int puertoCliente = 9000 + new Random().nextInt(900);
                String nombre = "Rugador_" + new Random().nextInt(1000);

                String[] paleta =
                {
                    "#FF0000", "#0000FF", "#008000", "#FFA500", "#800080", "#00FFFF"
                };
                String colorHex = paleta[new Random().nextInt(paleta.length)];

                Jugador yo = new Jugador(nombre, colorHex);
                yo.setPuertoEscucha(puertoCliente);

                Object[] opciones =
                {
                    "Crear Partida", "Unirme"
                };
                int eleccion = JOptionPane.showOptionDialog(null, "Timbiriche", "Menú",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
                boolean esHost = (eleccion == 0);

                if (eleccion == JOptionPane.CLOSED_OPTION)
                {
                    System.exit(0);
                }

                MotorJuego motor = new MotorJuego();
                motor.setJugadorLocal(yo);
                motor.setListaJugadores(new ArrayList<>());

                IDispatcher dispatcher = FabricaRED.configurarRed(puertoCliente, ipServidor, 8080);
                IReceptorExterno receptor = new ReceptorExternoImpl(motor);
                FabricaRED.establecerReceptor(receptor);
                motor.addDispatcher(dispatcher);

                ModelView modelView = new ModelView(motor);
                ControllerView controller = new ControllerView(modelView);

                if (esHost)
                {
                    // --- HOST ---
                    ConfigurarPartidaView configView = new ConfigurarPartidaView(controller, modelView);

                    configView.setModal(true);
                    configView.setVisible(true);

                    if (modelView.getDimension() > 0)
                    {
                        // DTO Hardcodeado 
                        DataDTO unirDTO = new DataDTO();
                        unirDTO.setTipo("UNIRSE_PARTIDA");
                        unirDTO.setProyectoOrigen(nombre);
                        unirDTO.setPayload(yo);

                        dispatcher.enviar(unirDTO);

                        abrirJuego(controller, modelView, nombre);
                    } else
                    {
                        System.exit(0);
                    }

                } else
                {
                    // --- INVITADO ---
                    DataDTO unirDTO = new DataDTO();
                    unirDTO.setTipo("UNIRSE_PARTIDA");
                    unirDTO.setProyectoOrigen(nombre);
                    unirDTO.setPayload(yo);

                    dispatcher.enviar(unirDTO);
                    abrirJuego(controller, modelView, nombre);
                }

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    private static void abrirJuego(ControllerView ctrl, ModelView mv, String titulo) {
        GameView gameView = new GameView(ctrl, mv);
        gameView.setTitle("Timbiriche - " + titulo);
        gameView.setVisible(true);
    }
}
