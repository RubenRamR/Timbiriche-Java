package timbiriche.back;

import java.util.List;

/** Resultado de MotorJuego.procesarJugada(...) */
public class ResultadoJugada {
    private final Linea lineaDibujada;
    private final List<Cuadro> cuadrosCompletados;
    private final Jugador proximoTurno;

    public ResultadoJugada(Linea lineaDibujada, List<Cuadro> cuadrosCompletados, Jugador proximoTurno) {
        this.lineaDibujada = lineaDibujada;
        this.cuadrosCompletados = cuadrosCompletados;
        this.proximoTurno = proximoTurno;
    }
    public Linea getLineaDibujada(){ return lineaDibujada; }
    public List<Cuadro> getCuadrosCompletados(){ return cuadrosCompletados; }
    public Jugador getProximoTurno(){ return proximoTurno; }
}
