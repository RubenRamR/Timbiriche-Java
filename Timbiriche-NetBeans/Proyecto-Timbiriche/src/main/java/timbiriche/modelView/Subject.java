package timbiriche.modelView;

public interface Subject {

    void agregarObservador(Observer observador);

    void quitarObservador(Observer observador);

    void notificarObservadores();
}
