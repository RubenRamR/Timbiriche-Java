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
public class ModelView implements IModelViewLeible, IMotorJuegoListener {

    private IMotorJuego motor;
    private List<Observer> observadores;

    // Estado Caché para la Vista
    private List<Linea> lineasDibujadas;
    private List<Jugador> jugadores;
    private List<Cuadro> cuadrosRellenos;
    private Jugador turnoActual;
    private boolean juegoTerminado = false;
    private Object tamanoSeleccionado;

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
    // IMPLEMENTACIÓN DE IMotorJuegoListener (Escucha a Lógica)
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

    // =========================================================
    // MÉTODOS DE ACCIÓN (Del Diagrama)
    // =========================================================
    public boolean actualizarJugadaLocal(Linea linea) {
        // Validación rápida antes de enviar al motor (opcional, el motor ya valida)
        if (linea == null)
        {
            return false;
        }
        motor.realizarJugadaLocal(linea);
        return true;
    }

    public void notificarActualizacion(DataDTO estado) {
        // Método legacy del diagrama, útil si recibimos DTO directo
        System.out.println("ModelView recibiendo actualización manual DTO.");
        notificarObservadores();
    }

    public void setTamano(Object tamano) {
        this.tamanoSeleccionado = tamano;
        // Podría implicar reiniciar el tablero en el motor si el juego no ha empezado
    }

    // Método auxiliar privado para sincronizar al inicio
    private void actualizarEstadoDesdeMotor() {
        if (motor.getTablero() != null)
        {
            this.lineasDibujadas = motor.getTablero().lineasDibujadas;
            this.cuadrosRellenos = motor.getTablero().cuadros;
        }
        this.turnoActual = motor.getTurnoActual();
        this.jugadores = motor.getJugadores();
    }

    // =========================================================
    // IMPLEMENTACIÓN DE IModelViewLeible (Getters para UI)
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
        // Filtramos solo los completados para la vista
        List<Cuadro> completados = new ArrayList<>();
        if (cuadrosRellenos != null)
        {
            for (Cuadro c : cuadrosRellenos)
            {
                if (c.isCompletado())
                {
                    completados.add(c);
                }
            }
        }
        return completados;
    }

    // Métodos para convertir datos de Dominio a Visuales (Colores/Avatares)
    @Override
    public String getAvatarJugador(Jugador jugador) {
        return (jugador != null && jugador.rutaAvatar != null) ? jugador.rutaAvatar : "default.png";
    }

    @Override
    public Color getColorJugador(Jugador jugador) {
        if (jugador == null || jugador.color == null)
        {
            return Color.BLACK;
        }
        try
        {
            return Color.decode(jugador.color); // Asume hex string "#RRGGBB"
        } catch (NumberFormatException e)
        {
            return Color.BLACK;
        }
    }

    // =========================================================
    // IMPLEMENTACIÓN DE SUBJET (Observer Pattern)
    // =========================================================
    @Override
    public void agregarObservador(Observer observador) {
        if (!observadores.contains(observador))
        {
            observadores.add(observador);
        }
    }

    @Override
    public void notificarObservadores() {
        for (Observer o : observadores)
        {
            o.actualizar();
        }
    }

    @Override
    public void removerObservador(Observer o) {
        observadores.remove(o);
    }

    @Override
    public int getDimension() {
        if (motor == null || motor.getTablero() == null)
        {
            return 10;
        }
        return motor.getTablero().dimension;
    }

    @Override
    public boolean esJuegoTerminado() {
        return this.juegoTerminado;
    }
}
