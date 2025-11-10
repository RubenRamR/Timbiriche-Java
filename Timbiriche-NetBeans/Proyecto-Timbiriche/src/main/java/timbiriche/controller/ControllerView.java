package timbiriche.controller;

import timbiriche.modelView.ModelViewModificable;
import timbiriche.back.Linea;
import timbiriche.back.MotorJuego;
import timbiriche.back.ResultadoJugada;

public class ControllerView {

    private final ModelViewModificable modelMod;
    private final MotorJuego motorJuego;

    public ControllerView(ModelViewModificable modeloModificable, MotorJuego motorJuego) {
        this.modelMod = modeloModificable;
        this.motorJuego = motorJuego;
    }

    public void realizarJugada(Linea linea) {
        ResultadoJugada r = motorJuego.procesarJugada(linea);
        modelMod.setEstadoVisual(r);
    }
}
