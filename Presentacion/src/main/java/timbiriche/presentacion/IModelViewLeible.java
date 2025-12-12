/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package timbiriche.presentacion;

import com.mycompany.dominio.Cuadro;
import com.mycompany.dominio.Jugador;
import com.mycompany.dominio.Linea;
import com.mycompany.dtos.DataDTO;
import java.awt.Color;
import java.util.List;

/**
 *
 * @author rramirez
 */
public interface IModelViewLeible extends Subjet {

    // Métodos para obtener el estado actual del juego
    List<Linea> getLineasDibujadas();

    List<Jugador> getJugadores();

    Jugador getTurnoActual();

    Jugador getJugadorLocal();

    List<Cuadro> getCuadrosRellenos();

    String getAvatarJugador(Jugador jugador);

    java.awt.Color getColorJugador(Jugador jugador);

    int getDimension();

    boolean esJuegoTerminado();

    // ============================================
    // NUEVO: MÉTODOS PARA LOBBY
    // ============================================
    /**
     * Indica si estamos en lobby o en partida
     */
    boolean isEnLobby();

    /**
     * Indica si el jugador local es el host
     */
    boolean esHost();
}
