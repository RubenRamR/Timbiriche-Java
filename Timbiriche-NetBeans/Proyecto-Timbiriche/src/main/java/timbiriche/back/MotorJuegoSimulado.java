package timbiriche.back;

import java.util.Collections;

public class MotorJuegoSimulado implements MotorJuego {

    private Jugador turno = Jugador.A;

    @Override
    public ResultadoJugada procesarJugada(Linea linea) {
        // Mock simple: alterna turno y no detecta cuadros aún.
        Jugador proximo = (turno == Jugador.A) ? Jugador.B : Jugador.A;
        ResultadoJugada r = new ResultadoJugada(
            linea,
            Collections.emptyList(),
            proximo
        );
        turno = proximo;
        return r;
    }
}
