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

public class ReceptorExternoImpl implements IReceptorExterno {

    private IMotorJuego motorJuego;

    public ReceptorExternoImpl(IMotorJuego motorJuego) {
        this.motorJuego = motorJuego;
    }

    @Override
    public void recibirMensaje(DataDTO datos) {
        if (datos == null || datos.getTipo() == null)
        {
            return;
        }

        try
        {
            Protocolo protocolo = Protocolo.valueOf(datos.getTipo());

            switch (protocolo)
            {
                case ACTUALIZAR_TABLERO:
                    System.out.println("Receptor: Recibido ACTUALIZAR_TABLERO.");
                    procesarJugadaRemota(datos);
                    break;

                case CUADRO_CERRADO:
                    System.out.println("Receptor: Recibido CUADRO_CERRADO.");
                    procesarJugadaRemota(datos);
                    break;

                case LISTA_JUGADORES:
                    System.out.println("Receptor: Recibida lista de jugadores.");
                    procesarListaJugadores(datos.getPayload());
                    break;

                case JUGADA_INVALIDA:
                    System.err.println("SERVIDOR: La jugada fue rechazada.");
                    break;

                default:
                    break;
            }
        } catch (IllegalArgumentException e)
        {
            System.err.println("Receptor Error: Protocolo desconocido: " + datos.getTipo());
        }
    }

    private void procesarJugadaRemota(DataDTO datos) {
        Object payload = datos.getPayload();
        Linea linea = null;

        if (payload instanceof Linea)
        {
            linea = (Linea) payload;
        }
        else if (payload instanceof Map)
        {
            linea = convertirMapaALinea((Map<String, Object>) payload);
        }

        if (linea != null)
        {
            String nombreRemitente = datos.getProyectoOrigen();

            Jugador jugadorR = buscarJugadorPorNombre(nombreRemitente);

            if (jugadorR == null)
            {
                jugadorR = new Jugador(nombreRemitente != null ? nombreRemitente : "Desconocido", "#808080");
            }

            motorJuego.realizarJugadaRemota(linea, jugadorR);
        }
    }

    private void procesarListaJugadores(Object payload) {
        List<Jugador> listaLimpia = new ArrayList<>();

        if (payload instanceof List)
        {
            List<?> listaCruda = (List<?>) payload;

            for (Object item : listaCruda)
            {
                if (item instanceof Map)
                {
                    Jugador j = convertirMapaAJugador((Map<String, Object>) item);
                    if (j != null)
                    {
                        listaLimpia.add(j);
                    }
                }
                else if (item instanceof Jugador)
                {
                    listaLimpia.add((Jugador) item);
                }
            }

            motorJuego.actualizarListaDeJugadores(listaLimpia);
        }
    }

    private Linea convertirMapaALinea(Map<String, Object> mapa) {
        try
        {
            Map<String, Object> p1Map = (Map<String, Object>) mapa.get("p1");
            Map<String, Object> p2Map = (Map<String, Object>) mapa.get("p2");

            int x1 = getInt(p1Map.get("x"));
            int y1 = getInt(p1Map.get("y"));
            int x2 = getInt(p2Map.get("x"));
            int y2 = getInt(p2Map.get("y"));

            return new Linea(new Punto(x1, y1), new Punto(x2, y2));
        } catch (Exception e)
        {
            System.err.println("Receptor: Error convirtiendo Mapa a Linea -> " + e.getMessage());
            return null;
        }
    }

    private Jugador convertirMapaAJugador(Map<String, Object> mapa) {
        try
        {
            String nombre = (String) mapa.get("nombre");
            String color = (String) mapa.get("color");
            String rutaAvatar = (String) mapa.get("rutaAvatar");

            Jugador j = new Jugador(nombre, color);

            if (rutaAvatar != null)
            {
                j.setRutaAvatar(rutaAvatar);
            }

            Object puntajeObj = mapa.get("puntaje");
            if (puntajeObj instanceof Number)
            {
                int puntos = ((Number) puntajeObj).intValue();
                if (puntos > 0)
                {
                    j.sumarPuntos(puntos);
                }
            }

            Object puertoEscuchaObj = mapa.get("puertoEscucha");
            if (puertoEscuchaObj instanceof Number)
            {
                j.setPuertoEscucha(((Number) puertoEscuchaObj).intValue());
            }

            return j;
        } catch (Exception e)
        {
            System.err.println("Receptor: Error convirtiendo Mapa a Jugador -> " + e.getMessage());
            return null;
        }
    }

    private int getInt(Object obj) {
        if (obj instanceof Number)
        {
            return ((Number) obj).intValue();
        }
        return 0;
    }

    private Jugador buscarJugadorPorNombre(String nombre) {
        if (nombre == null)
        {
            return null;
        }
        List<Jugador> jugadores = motorJuego.getJugadores();
        if (jugadores == null)
        {
            return null;
        }

        for (Jugador j : jugadores)
        {
            if (j.getNombre().equals(nombre))
            {
                return j;
            }
        }
        return null;
    }
}
