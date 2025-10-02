package timbiriche.modelView;

import timbiriche.back.ResultadoJugada;
import timbiriche.back.TamanoTablero;

public interface ModelViewModificable {
    void setEstadoVisual(ResultadoJugada datos);
    void setTamano(TamanoTablero tamano);
}
