/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package timbiriche.presentacion;

import com.mycompany.dominio.Cuadro;
import com.mycompany.dominio.Jugador;
import com.mycompany.dominio.Linea;
import com.mycompany.dominio.Tablero;
import com.mycompany.dtos.DataDTO;
import com.mycompany.imotorjuego.IMotorJuego;
import com.mycompany.imotorjuego.IMotorJuegoListener;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author rramirez
 */
public class ModelView implements IModelViewLeible, IModelViewModificable, IMotorJuegoListener {

    private IMotorJuego motor;
    private List<Observer> observadores;

    private List<Linea> lineasDibujadas;
    private List<Jugador> jugadores;
    private List<Cuadro> cuadrosRellenos;
    private Jugador turnoActual;
    private boolean juegoTerminado = false;
    private Object tamanoSeleccionado;
    // Para comunicar rechazo al LobbyView
    private String ultimoMensajeRechazo = null;

    public ModelView(IMotorJuego motor) {
        this.motor = motor;
        this.observadores = new ArrayList<>();

        // Inicializar listas para evitar NullPointer en la UI antes de conectar
        this.lineasDibujadas = new ArrayList<>();
        this.jugadores = new ArrayList<>();
        this.cuadrosRellenos = new ArrayList<>();

        // Suscribirse al motor para recibir actualizaciones de la red/lógica
        this.motor.registrarListener(this);

        // Carga inicial
        actualizarEstadoDesdeMotor();
    }

    // =========================================================
    // IMPLEMENTACIÓN DE IMotorJuegoListener
    // =========================================================
    @Override
    public void onJuegoActualizado(Tablero tablero, Jugador turnoActual) {
        // Actualizamos el caché local con los datos frescos del dominio
        this.lineasDibujadas = tablero.lineasDibujadas;
        this.cuadrosRellenos = tablero.cuadros; // La UI filtrará los isCompletado()
        this.turnoActual = turnoActual;
        this.jugadores = motor.getJugadores(); // Actualizamos lista de jugadores

        // Avisamos a la Vista que se repinte
        notificarObservadores();
    }

    @Override
    public void onJuegoTerminado(Jugador ganador) {
        this.juegoTerminado = true;
        System.out.println("MODELVIEW: Recibido fin de juego. Ganador: " + (ganador != null ? ganador.getNombre() : "Empate"));
        notificarObservadores();
    }

    @Override
    public void onError(String mensaje) {
        System.err.println("[ModelView Error]: " + mensaje);
        // Aquí podríamos disparar un observer especial para mostrar dialogs
    }

    @Override
    public void actualizarJugadaLocal(Linea linea) {
        if (linea == null) {
            return;
        }
        motor.realizarJugadaLocal(linea);
    }

    @Override
    public void actualizarEstadoDesdeMotor() {
        // Lógica de sincronización (igual a la que tenías)
        if (motor.getTablero() != null) {
            this.lineasDibujadas = motor.getTablero().lineasDibujadas;
            this.cuadrosRellenos = motor.getTablero().cuadros;
        }
        this.turnoActual = motor.getTurnoActual();
        this.jugadores = motor.getJugadores();

        notificarObservadores();
    }

    public void notificarActualizacion(DataDTO estado) {
        // Método legacy del diagrama, útil si recibimos DTO directo
        System.out.println("ModelView recibiendo actualización manual DTO.");
        notificarObservadores();
    }

    public void setTamano(Object tamano) {
        this.tamanoSeleccionado = tamano;
    }

    // =========================================================
    // IMPLEMENTACIÓN DE IModelViewLeible
    // =========================================================
    @Override
    public List<Linea> getLineasDibujadas() {
        return lineasDibujadas != null ? lineasDibujadas : Collections.emptyList();
    }

    @Override
    public List<Jugador> getJugadores() {
        return jugadores != null ? jugadores : Collections.emptyList();
    }

    @Override
    public Jugador getTurnoActual() {
        return turnoActual;
    }

    @Override
    public Jugador getJugadorLocal() {
        return motor.getJugadorLocal();
    }

    @Override
    public List<Cuadro> getCuadrosRellenos() {
        List<Cuadro> completados = new ArrayList<>();
        if (cuadrosRellenos != null) {
            for (Cuadro c : cuadrosRellenos) {
                if (c.isCompletado()) {
                    completados.add(c);
                }
            }
        }
        return completados;
    }

    @Override
    public String getAvatarJugador(Jugador jugador) {
        return (jugador != null && jugador.rutaAvatar != null) ? jugador.rutaAvatar : "default.png";
    }

    @Override
    public Color getColorJugador(Jugador jugador) {
        if (jugador == null || jugador.color == null) {
            return Color.BLACK;
        }
        try {
            return Color.decode(jugador.color); // Asume hex string "#RRGGBB"
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }
    }

    // =========================================================
    // IMPLEMENTACIÓN DE SUBJET (Observer Pattern)
    // =========================================================
    @Override
    public void agregarObservador(Observer observador) {
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    @Override
    public void notificarObservadores() {
        for (Observer o : observadores) {
            o.actualizar();
        }
    }

    @Override
    public void removerObservador(Observer o) {
        observadores.remove(o);
    }

    @Override
    public int getDimension() {
        if (motor == null || motor.getTablero() == null) {
            return 10;
        }
        return motor.getTablero().dimension;
    }

    @Override
    public boolean esJuegoTerminado() {
        return this.juegoTerminado;
    }

    @Override
    public boolean isEnLobby() {
        return motor.isEnLobby();
    }

    @Override
    public boolean esHost() {
        return motor.isSoyHost();
    }

    @Override
    public void solicitarInicioPartida(int dimension) {
        motor.solicitarInicioPartida(dimension);
    }

    @Override
    public void onListaJugadoresActualizada(List<Jugador> jugadores) {
        System.out.println("[ModelView] Lista actualizada: " + jugadores.size() + " jugadores");
        this.jugadores = jugadores;
        notificarObservadores(); // Actualiza LobbyView
    }

    @Override
    public void onPartidaIniciada(int dimension) {
        System.out.println("[ModelView] 🎉 onPartidaIniciada() llamado - Dimensión: " + dimension);
        System.out.println("[ModelView] 📢 Notificando a " + observadores.size() + " observadores...");

        for (Observer o : observadores) {
            System.out.println("[ModelView]    └─> Notificando a: " + o.getClass().getSimpleName());
        }

        notificarObservadores();
        System.out.println("[ModelView] ✅ Notificación completada");
    }

    @Override
    public void onInicioRechazado(String motivo) {
        System.err.println("[ModelView] Inicio rechazado: " + motivo);
        notificarObservadores(); // LobbyView mostrará el error
    }

    /**
     * Permite al LobbyView consultar si hubo un rechazo
     *
     * @return Mensaje de rechazo o null
     */
    public String consumirMensajeRechazo() {
        String msg = this.ultimoMensajeRechazo;
        this.ultimoMensajeRechazo = null; // Limpiar después de leer
        return msg;
    }
}
