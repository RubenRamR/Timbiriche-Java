/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.modelojuego;

import com.mycompany.dominio.Jugador;
import com.mycompany.dominio.Linea;
import com.mycompany.dominio.Punto;
import com.mycompany.dtos.DataDTO;
import com.mycompany.imotorjuego.IMotorJuego;
import com.mycompany.interfacesreceptor.IReceptorExterno;
import com.mycompany.protocolo.Protocolo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author rramirez
 */
public class ReceptorExternoImpl implements IReceptorExterno {

    private IMotorJuego motorJuego;

    public ReceptorExternoImpl(IMotorJuego motorJuego) {
        this.motorJuego = motorJuego;
    }

    @Override
    public void recibirMensaje(DataDTO datos) {
        if (datos == null || datos.getTipo() == null) {
            return;
        }

        try {
            Protocolo protocolo = Protocolo.valueOf(datos.getTipo());

            switch (protocolo) {
                case ACTUALIZAR_TABLERO:
                case CUADRO_CERRADO:
                    procesarJugadaRemota(datos);
                    break;

                case LISTA_JUGADORES:
                    procesarListaJugadores(datos.getPayload());
                    break;

                // ✅ AGREGAR ESTE CASO (actualmente solo está en ReceptorUnificado)
                case INICIO_PARTIDA:
                    procesarInicioPartida(datos);
                    break;

                case INICIO_RECHAZADO:
                    procesarRechazoInicio(datos);
                    break;

                case JUGADA_INVALIDA:
                    System.err.println("Jugada inválida rechazada.");
                    break;

                default:
                    System.out.println("Mensaje no manejado: " + protocolo);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Protocolo desconocido: " + datos.getTipo());
        }
    }

    private void procesarJugadaRemota(DataDTO datos) {
        Object payload = datos.getPayload();
        Linea linea = null;

        // Opción A: Ya es un objeto Linea (Local o si usaras ObjectStream)
        if (payload instanceof Linea) {
            linea = (Linea) payload;
        } // Opción B: Es un MAPA (Viene del Servidor JSON desacoplado)
        else if (payload instanceof Map) {
            linea = convertirMapaALinea((Map<String, Object>) payload);
        }

        if (linea != null) {
            String nombreRemitente = datos.getProyectoOrigen();

            Jugador jugadorR = buscarJugadorPorNombre(nombreRemitente);

            if (jugadorR == null) {
                // Jugador temporal si no existe en la lista local
                jugadorR = new Jugador(nombreRemitente != null ? nombreRemitente : "Desconocido", "#808080");
            }

            motorJuego.realizarJugadaRemota(linea, jugadorR);
        }
    }

    private void procesarListaJugadores(Object payload) {
        List<Jugador> listaLimpia = new ArrayList<>();

        if (payload instanceof List) {
            List<?> listaCruda = (List<?>) payload;

            for (Object item : listaCruda) {
                if (item instanceof Map) {
                    // Convertir cada Mapa a Jugador manualmente
                    Jugador j = convertirMapaAJugador((Map<String, Object>) item);
                    if (j != null) {
                        listaLimpia.add(j);
                    }
                } else if (item instanceof Jugador) {
                    listaLimpia.add((Jugador) item);
                }
            }
            motorJuego.actualizarListaDeJugadores(listaLimpia);
        }
    }

    // -------------------------------------------------------------------------
    // MAPPERS
    // -------------------------------------------------------------------------
    private Linea convertirMapaALinea(Map<String, Object> mapa) {
        try {
            // Extraer sub-mapas de los puntos
            Map<String, Object> p1Map = (Map<String, Object>) mapa.get("p1");
            Map<String, Object> p2Map = (Map<String, Object>) mapa.get("p2");

            // Usar helper getInt para evitar errores de cast (Long vs Integer)
            int x1 = getInt(p1Map.get("x"));
            int y1 = getInt(p1Map.get("y"));
            int x2 = getInt(p2Map.get("x"));
            int y2 = getInt(p2Map.get("y"));

            return new Linea(new Punto(x1, y1), new Punto(x2, y2));
        } catch (Exception e) {
            System.err.println("Receptor: Error convirtiendo Mapa a Linea -> " + e.getMessage());
            return null;
        }
    }

    private Jugador convertirMapaAJugador(Map<String, Object> mapa) {
        try {
            String nombre = (String) mapa.get("nombre");
            String color = (String) mapa.get("color");

            Jugador j = new Jugador(nombre, color);

            // **NUEVO**: Restaurar estado "listo"
            if (mapa.containsKey("listo")) {
                Object listoObj = mapa.get("listo");
                if (listoObj instanceof Boolean) {
                    j.setListo((Boolean) listoObj);
                }
            }

            // Restaurar puerto si existe
            if (mapa.containsKey("puertoEscucha")) {
                Object puertoObj = mapa.get("puertoEscucha");
                if (puertoObj instanceof Number) {
                    j.setPuertoEscucha(((Number) puertoObj).intValue());
                }
            }

            return j;
        } catch (Exception e) {
            System.err.println("[Receptor] Error convirtiendo mapa a jugador: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper vital: Convierte cualquier Number (Integer, Long, Double) a int.
     * Jackson a veces devuelve Long para números pequeños, esto lo soluciona.
     */
    private int getInt(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        return 0;
    }

    private Jugador buscarJugadorPorNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        List<Jugador> jugadores = motorJuego.getJugadores();
        if (jugadores == null) {
            return null;
        }

        for (Jugador j : jugadores) {
            if (j.getNombre().equals(nombre)) {
                return j;
            }
        }
        return null;
    }

    private void procesarInicioPartida(DataDTO datos) {
        Object payload = datos.getPayload();
        int dimension = 10; // por defecto

        if (payload instanceof Map) {
            Map<?, ?> config = (Map<?, ?>) payload;
            if (config.containsKey("dimension")) {
                dimension = ((Number) config.get("dimension")).intValue();
            }
        }

        motorJuego.recibirInicioPartida(dimension);
    }

    private void procesarRechazoInicio(DataDTO datos) {
        String motivo = (String) datos.getPayload();
        motorJuego.recibirRechazoInicio(motivo);
    }
}
