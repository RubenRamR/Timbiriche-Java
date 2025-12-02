package timbiriche.back;

/** Celda (x,y) superior-izquierda y dueño (quién la completó). */
public class Cuadro {
    private final int x, y;
    private final Jugador dueno;

    public Cuadro(int x, int y, Jugador dueno) {
        this.x = x; this.y = y; this.dueno = dueno;
    }
    public int getX(){ return x; }
    public int getY(){ return y; }
    public Jugador getDueno(){ return dueno; }
}
