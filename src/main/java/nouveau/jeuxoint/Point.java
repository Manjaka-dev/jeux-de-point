package nouveau.jeuxoint;

import java.io.Serializable;

public class Point implements Serializable {

    private static final long serialVersionUID = 1L;

    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double calculDistance(Point b) {
        if (b == null) {
            return Double.MAX_VALUE;
        }
        return Math.sqrt(Math.pow(b.getX() - this.getX(), 2) + Math.pow(b.getY() - this.getY(), 2));
    }

    @Override
    public boolean equals(Object obj) {
        if (this.x == ((Point) obj).getX() && this.y == ((Point) obj).getY()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Point [x="+x+", y="+y+"]";
    }
}
