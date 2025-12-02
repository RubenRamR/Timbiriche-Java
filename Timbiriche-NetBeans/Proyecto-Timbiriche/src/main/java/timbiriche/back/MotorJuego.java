package timbiriche.back;

/** Fachada del Modelo de Juego (puede ser simulado). */
public interface MotorJuego {
    ResultadoJugada procesarJugada(Linea linea);
}
