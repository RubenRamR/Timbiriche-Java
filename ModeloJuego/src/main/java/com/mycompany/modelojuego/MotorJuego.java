/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.modelojuego;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mycompany.imotorjuego.IMotorJuego;
import java.util.Random;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.dtos.DataDTO;
import com.mycompany.dominio.*;
import com.mycompany.interfacesdispatcher.IDispatcher;
import com.mycompany.dominio.Jugador;
import com.mycompany.protocolo.Protocolo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MotorJuego implements IMotorJuego {

    // --- ESTADO DEL JUEGO (MODELO) ---
    private Tablero tablero;
    private Jugador jugadorLocal;
    private Jugador turnoActual;
    private List<Jugador> listaJugadores;

    // --- INFRAESTRUCTURA Y OBSERVADORES ---
    private List<IMotorJuegoListener> listeners;
    private List<IDispatcher> dispatchers;
    private ObjectMapper jsonMapper;

    /**
     * Constructor. Inicializa el tablero y las listas.
     */
    public MotorJuego() {
        this.tablero = new Tablero(10); 
        this.listeners = new ArrayList<>();
        this.dispatchers = new ArrayList<>();
        this.listaJugadores = new ArrayList<>();
        this.jsonMapper = new ObjectMapper();
    }

    // ==========================================
    // MÉTODOS DE CONFIGURACIÓN (SETUP)
    // ==========================================

    /**
     * Define quién es el usuario en esta máquina.
     * Vital para validar si puedes jugar o no.
     */
    public void setJugadorLocal(Jugador jugador) {
        this.jugadorLocal = jugador;
        // Si la lista está vacía, nos agregamos (para pruebas unitarias o modo solo)
        if (listaJugadores.isEmpty()) {
            listaJugadores.add(jugador);
        }
    }

    /**
     * Configura la lista de jugadores de la partida.
     * Usado por TimbiricheApp para inyectar los jugadores fijos.
     */
    public void setListaJugadores(List<Jugador> jugadores) {
        this.listaJugadores = new ArrayList<>(jugadores);
        // Regla: El primer jugador de la lista comienza
        if (!this.listaJugadores.isEmpty()) {
            this.turnoActual = this.listaJugadores.get(0);
        }
        // Notificamos para que la UI se pinte inicializada
        notificarCambios();
    }

    public void addDispatcher(IDispatcher dispatcher) {
        this.dispatchers.add(dispatcher);
    }

    // ==========================================
    // IMPLEMENTACIÓN DE IMotorJuego (ENTRADA UI)
    // ==========================================

    @Override
    public void registrarListener(IMotorJuegoListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void realizarJugadaLocal(Linea linea) {
        // 1. Validar reglas locales
        if (!validarJugada(linea)) {
            return;
        }

        try {
            String jsonLinea = jsonMapper.writeValueAsString(linea);
            
            DataDTO dto = new DataDTO(Protocolo.INTENTO_JUGADA);
            dto.setPayload(jsonLinea);
            
            // Asignar origen
            if (jugadorLocal != null) {
                dto.setProyectoOrigen(jugadorLocal.getNombre());
            } else {
                dto.setProyectoOrigen("Anonimo");
            }

            System.out.println("Motor: Enviando jugada a la red..."); // DEBUG
            
            // ESTO ES LO QUE ENVÍA AL SERVIDOR
            notificarDespachadores(dto);
            
        } catch (Exception e) {
            System.err.println("Motor Error: " + e.getMessage());
        }
    }

    // ==========================================
    // LÓGICA DE JUEGO (CORE)
    // ==========================================

    /**
     * Ejecuta una jugada que ha sido validada/recibida desde la red (o servidor local).
     * Esta es la "verdad" del juego.
     * * @param linea La línea a dibujar.
     * @param jugadorRemitente El jugador que hizo la línea.
     */
    public void realizarJugadaRemota(Linea linea, Jugador jugadorRemitente) {
        // 1. Agregar línea al tablero lógico
        boolean agregada = tablero.agregarLinea(linea);

        if (agregada) {
            // 2. Verificar si se cerraron cuadros con esta línea
            int cuadrosCerrados = contarYAsignarCuadrosCerrados(linea, jugadorRemitente);

            if (cuadrosCerrados > 0) {
                // --- REGLA TIMBIRICHE: Cierra cuadro -> Gana puntos y REPITE turno ---
                if (jugadorRemitente != null) {
                    jugadorRemitente.sumarPuntos(cuadrosCerrados);
                    // El turno NO cambia (this.turnoActual sigue siendo jugadorRemitente)
                }
            } else {
                // --- REGLA TIMBIRICHE: No cierra cuadro -> CAMBIA turno ---
                avanzarTurno();
            }

            // 3. Verificar Fin de Juego (Opcional, si tablero lleno)
            // if (tablero.estaLleno()) { ... }

            // 4. Actualizar a todos los observadores (UI)
            notificarCambios();
        }
    }

    /**
     * Valida si la jugada local es legal antes de enviarla.
     */
    private boolean validarJugada(Linea linea) {
        if (jugadorLocal == null || turnoActual == null) {
            onError("La partida no ha iniciado o falta configuración.");
            return false;
        }
        
        // Regla: Turno
        if (!jugadorLocal.getNombre().equals(turnoActual.getNombre())) {
            onError("No es tu turno. Espera a: " + turnoActual.getNombre());
            return false;
        }
        
        // Regla: Disponibilidad
        if (tablero.existeLinea(linea)) {
            onError("Esa línea ya está ocupada.");
            return false;
        }
        
        return true;
    }

    /**
     * Usa la lógica del Dominio (Tablero/Cuadro) para verificar cierres.
     * Asigna el propietario a los cuadros completados en este turno.
     */
    private int contarYAsignarCuadrosCerrados(Linea linea, Jugador jugador) {
        int conteo = 0;
        
        // El método del tablero devuelve true si AL MENOS UN cuadro se cerró
        // También actualiza internamente el estado de los cuadros (completado=true)
        boolean huboCierre = tablero.verificarCuadroCerrado(linea);
        
        if (huboCierre) {
            // Buscamos cuáles cuadros están completos pero SIN dueño (los recién cerrados)
            for (Cuadro c : tablero.getCuadros()) {
                if (c.isCompletado() && c.getPropietario() == null) {
                    c.setPropietario(jugador);
                    conteo++;
                }
            }
        }
        return conteo;
    }

    private void avanzarTurno() {
        if (listaJugadores.isEmpty()) return;

        int indexActual = listaJugadores.indexOf(turnoActual);
        if (indexActual == -1) {
            // Si por error no está, reiniciamos al primero
            turnoActual = listaJugadores.get(0);
        } else {
            // Rotación circular: (0 -> 1 -> 2 -> 0)
            int siguienteIndex = (indexActual + 1) % listaJugadores.size();
            turnoActual = listaJugadores.get(siguienteIndex);
        }
    }

    // ==========================================
    // MÉTODOS DE SOPORTE (Getters y Notificaciones)
    // ==========================================

    private void notificarCambios() {
        for (IMotorJuegoListener l : listeners) {
            l.onJuegoActualizado(this.tablero, this.turnoActual);
        }
    }

    private void notificarDespachadores(DataDTO datos) {
        for (IDispatcher d : dispatchers) {
            d.enviar(datos);
        }
    }

    private void onError(String msg) {
        for (IMotorJuegoListener l : listeners) {
            l.onError(msg);
        }
    }

    // --- Implementación de Getters de IMotorJuego ---

    @Override
    public Tablero getTablero() {
        return tablero;
    }

    @Override
    public Jugador getTurnoActual() {
        return turnoActual;
    }

    @Override
    public Jugador getJugadorLocal() {
        return jugadorLocal;
    }

    @Override
    public List<Jugador> getJugadores() {
        return listaJugadores;
    }
}