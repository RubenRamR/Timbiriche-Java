/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.mycompany.ensamblador.app.PruebaLocal;

import com.mycompany.componentered.FabricaRED;
import com.mycompany.dominio.Jugador;
import com.mycompany.interfacesdispatcher.IDispatcher;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import com.mycompany.modelojuego.MotorJuego;
import com.mycompany.modelojuego.ReceptorExternoImpl;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import timbiriche.presentacion.ControllerView;
import timbiriche.presentacion.Vistas.GameView;
import timbiriche.presentacion.IModelViewLeible;
import timbiriche.presentacion.IModelViewModificable;
import timbiriche.presentacion.ModelView;

/**
 *
 * @author rramirez
 */
public class ClienteMorado {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Jugador azul = new Jugador("Azul", "#0000FF"); 
                Jugador rojo = new Jugador("Rojo", "#FF0000");
                Jugador morado = new Jugador("Morado", "#800080");

                MotorJuego motor = new MotorJuego();
                motor.configurarTablero(3); // <--- TABLERO PEQUEÑO
                motor.setJugadorLocal(morado);

                List<Jugador> lista = new ArrayList<>();
                lista.add(azul);
                lista.add(rojo);
                lista.add(morado);
                motor.setListaJugadores(lista);

                // Red (Puerto 9002)
                IReceptorExterno receptor = new ReceptorExternoImpl(motor);
                FabricaRED fabrica = new FabricaRED();
                IDispatcher dispatcher = fabrica.configurarRed(9002, "127.0.0.1", 8080);
                fabrica.establecerReceptor(receptor);
                motor.addDispatcher(dispatcher);

                IModelViewModificable modelViewModificable = new ModelView(motor);
                IModelViewLeible modelViewLeible = new ModelView(motor);
                ControllerView controller = new ControllerView(modelViewModificable);
                GameView view = new GameView(controller, modelViewLeible);
                
                view.setLocation(1000, 0); // Desplazar ventana
                view.setTitle("Cliente 3: MORADO (9002)");
                view.setVisible(true);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }
}