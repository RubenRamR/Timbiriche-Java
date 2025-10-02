package timbiriche.back;

import java.util.Objects;

/** Segmento entre puntos adyacentes ortogonales (x1,y1)-(x2,y2). */
public class Linea {
    private final int x1, y1, x2, y2;

    public Linea(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2), dy = Math.abs(y1 - y2);
        if (!((dx == 1 && dy == 0) || (dx == 0 && dy == 1)))
            throw new IllegalArgumentException("La línea no conecta puntos adyacentes ortogonales.");
        this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
    }
    public int getX1(){ return x1; } public int getY1(){ return y1; }
    public int getX2(){ return x2; } public int getY2(){ return y2; }

    @Override public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof Linea)) return false;
        Linea l=(Linea)o;
        return (x1==l.x1 && y1==l.y1 && x2==l.x2 && y2==l.y2) ||
               (x1==l.x2 && y1==l.y2 && x2==l.x1 && y2==l.y1);
    }
    @Override public int hashCode(){
        int ax = Math.min(x1, x2), ay = Math.min(y1, y2);
        int bx = Math.max(x1, x2), by = Math.max(y1, y2);
        return Objects.hash(ax, ay, bx, by);
    }
}
