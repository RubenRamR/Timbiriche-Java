/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.modelojuego;

import com.mycompany.dtos.DataDTO;
import com.mycompany.dominio.*;
import com.mycompany.interfacesdispatcher.IDispatcher;
import com.mycompany.protocolo.Protocolo;
import com.mycompany.dominio.Jugador;
import java.util.ArrayList;
import java.util.List;

/**
 * MotorJuego
 * Núcleo de la lógica. Gestiona el estado y las reglas.
 * NO implementa IReceptorExterno directamente.
 */
public class MotorJuego {

    // --- Atributos de Estado ---
    private Tablero tablero;
    private List<Jugador> jugadores;      // Lista ordenada para la rotación de turnos
    private Jugador jugadorLocal;         // Referencia a "quien soy yo" en este cliente
    private Jugador turnoActual;          // Referencia a de quién es el turno
    
    // --- Atributos de Comunicación ---
    private List<IDispatcher> dispatchers;       // Para hablar hacia la RED
    private List<IMotorJuegoListener> listeners; // Para hablar hacia la UI (ModelView)

    /**
     * Constructor
     * @param dimension Dimensiones del tablero (puntos por lado).
     * @param jugadorLocal El jugador que está usando esta instancia.
     * @param todosLosJugadores Lista completa de jugadores en la partida.
     */
    public MotorJuego(int dimension, Jugador jugadorLocal, List<Jugador> todosLosJugadores) {
        this.tablero = new Tablero(dimension);
        this.jugadorLocal = jugadorLocal;
        this.jugadores = todosLosJugadores;
        
        // Regla: Empieza el primer jugador de la lista
        if (todosLosJugadores != null && !todosLosJugadores.isEmpty()) {
            this.turnoActual = todosLosJugadores.get(0);
        }

        this.dispatchers = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    // ==========================================
    //       MÉTODOS DE ENTRADA (Input)
    // ==========================================

    /**
     * 1. Entrada desde la UI Local (El usuario hizo clic).
     */
    public void realizarJugadaLocal(Linea linea) {
        // A. Validar Turno
        if (!jugadorLocal.equals(turnoActual)) {
            System.out.println("Motor: No es tu turno.");
            return; 
        }

        // B. Validar si la línea es geométricamente válida (no duplicada)
        // Usamos check previo para no enviar basura a la red
        if (!tablero.agregarLinea(linea)) { 
            // La línea ya existía, salimos sin hacer nada.
            return; 
        }

        // C. Procesar Lógica Central (Puntos, Turnos, Fin de Juego)
        procesarJugada(linea, jugadorLocal);

        // D. Notificar a la Red (Dispatcher)
        // Solo si la jugada fue válida localmente, avisamos al rival.
        DataDTO dto = new DataDTO(Protocolo.INTENTO_JUGADA);
        dto.setProyectoOrigen(jugadorLocal.getNombre());
        dto.setPayload(serializarLinea(linea));
        notificarDespachadores(dto);
    }

    /**
     * 2. Entrada desde la RED (El rival hizo clic).
     */
    public void procesarJugadaRed(DataDTO datos) {
        // Validamos protocolo
        if (datos.getTipo().equals(Protocolo.INTENTO_JUGADA.toString())) {
            
            // Reconstruir objetos
            Linea lineaRecibida = deserializarLinea(datos.getPayload());
            Jugador jugadorRival = buscarJugadorPorNombre(datos.getProyectoOrigen());

            if (jugadorRival != null && lineaRecibida != null) {
                // Como viene de la red, asumimos que el rival ya validó su turno.
                // Insertamos directamente en nuestro modelo local.
                boolean agregada = tablero.agregarLinea(lineaRecibida);
                
                if (agregada) {
                    procesarJugada(lineaRecibida, jugadorRival);
                }
            }
        }
    }
   

    // ==========================================
    //       NÚCLEO LÓGICO (Core Logic)
    // ==========================================

    /**
     * Método centralizado que aplica las reglas del juego.
     * Se usa tanto para jugadas locales como remotas para mantener consistencia.
     */
    private void procesarJugada(Linea linea, Jugador jugadorQueJugo) {
        
        // 1. Verificar si se cerró algún cuadro (Geometría)
        // El tablero ya tiene la línea (se agregó antes de llamar a este método o dentro).
        // Importante: verificarCuadroCerrado debe ser capaz de detectar cierres
        // basándose en la última línea agregada.
        boolean cerroCuadro = tablero.verificarCuadroCerrado(linea);

        if (cerroCuadro) {
            // REGLA TIMBIRICHE:
            // Si cierras cuadro(s), ganas punto(s) y REPITE turno.
            // Nota: Podrías mejorar 'verificarCuadroCerrado' para que devuelva int
            // por si cierra 2 cuadros a la vez. Aquí asumimos al menos 1 punto.
            jugadorQueJugo.agregarPunto(); 
            
            // Asignar propietario al cuadro (Lógica visual)
            asignarPropietarioCuadrosCerrados(jugadorQueJugo);
            
            // NO cambiamos de turno (this.turnoActual se mantiene)
            
        } else {
            // REGLA ESTÁNDAR:
            // Si no cierras nada, pasa el turno al siguiente.
            avanzarTurno();
        }

        // 2. Verificar si el juego terminó
        if (verificarFinDeJuego()) {
            Jugador ganador = calcularGanador();
            notificarFinDeJuego(ganador);
        } else {
            // 3. Si no terminó, solo actualizamos la vista
            notificarActualizacionEstado();
        }
    }

    private void avanzarTurno() {
        int index = jugadores.indexOf(turnoActual);
        int siguiente = (index + 1) % jugadores.size();
        this.turnoActual = jugadores.get(siguiente);
    }

    private void asignarPropietarioCuadrosCerrados(Jugador jugador) {
        // Recorremos los cuadros para ver cuáles están completos y sin dueño
        // (O idealmente el Tablero nos diría cuáles se acaban de cerrar)
        for (Cuadro c : tablero.getCuadros()) {
            if (c.isCompletado() && c.getPropietario() == null) {
                c.setPropietario(jugador);
                // Si tu lógica permite cerrar 2 cuadros, aquí deberías sumar otro punto
                // si detectas más de uno nuevo.
            }
        }
    }

    private boolean verificarFinDeJuego() {
        for (Cuadro c : tablero.getCuadros()) {
            if (!c.isCompletado()) {
                return false; // Aún hay huecos
            }
        }
        return true; // Todos completos
    }

    private Jugador calcularGanador() {
        Jugador ganador = null;
        int maxPuntos = -1;
        for (Jugador j : jugadores) {
            if (j.getPuntaje() > maxPuntos) {
                maxPuntos = j.getPuntaje();
                ganador = j;
            }
            // Falta manejo de empates, pero esto sirve por ahora
        }
        return ganador;
    }

    // ==========================================
    //    GESTIÓN DE SUBSCRIPTORES (Observer)
    // ==========================================

    public void registrarListener(IMotorJuegoListener listener) {
        listeners.add(listener);
    }

    public void addDispatcher(IDispatcher dispatcher) {
        dispatchers.add(dispatcher);
    }

    private void notificarActualizacionEstado() {
        for (IMotorJuegoListener l : listeners) {
            l.actualizarEstado();
        }
    }

    private void notificarFinDeJuego(Jugador ganador) {
        // Primero actualizamos visualmente la última línea
        notificarActualizacionEstado(); 
        // Luego lanzamos el evento de fin
        for (IMotorJuegoListener l : listeners) {
            l.onJuegoTerminado(ganador);
        }
    }

    private void notificarDespachadores(DataDTO dto) {
        for (IDispatcher d : dispatchers) {
            d.enviar(dto);
        }
    }

    // ==========================================
    //           UTILIDADES / GETTERS
    // ==========================================

    private Jugador buscarJugadorPorNombre(String nombre) {
        for (Jugador j : jugadores) {
            if (j.getNombre().equals(nombre)) return j;
        }
        return null;
    }

    private String serializarLinea(Linea l) {
        return l.getP1().getX() + "," + l.getP1().getY() + "," +
               l.getP2().getX() + "," + l.getP2().getY();
    }

    private Linea deserializarLinea(String s) {
        try {
            String[] parts = s.split(",");
            int x1 = Integer.parseInt(parts[0]);
            int y1 = Integer.parseInt(parts[1]);
            int x2 = Integer.parseInt(parts[2]);
            int y2 = Integer.parseInt(parts[3]);
            return new Linea(new Punto(x1, y1), new Punto(x2, y2));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Tablero getTablero() { return tablero; }
    public Jugador getTurnoActual() { return turnoActual; }
    public Jugador getJugadorLocal() { return jugadorLocal; }
    public List<Jugador> getJugadores() { return jugadores; }
}