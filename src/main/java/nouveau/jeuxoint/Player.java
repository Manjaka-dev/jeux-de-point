package nouveau.jeuxoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private transient Color color;
    private List<Point> points;
    private String colorString; // Pour stocker la couleur sous forme de chaîne

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.points = new ArrayList<>();
        this.colorString = color.toString(); // Initialiser la chaîne de couleur
    }

    public void addToList(Point point) {
        points.add(point);
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        this.colorString = color.toString(); // Mettre à jour la chaîne de couleur
    }

    public List<Point> getPoints() {
        return points;
    }

    // Convert Color to String
    public String getColorAsString() {
        return colorString;
    }

    // Convert String to Color
    public void setColorFromString(String colorString) {
        this.color = Color.valueOf(colorString);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeUTF(color.toString()); // Sauvegarder la couleur sous forme de chaîne
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        colorString = in.readUTF(); // Lire la chaîne de couleur
        color = Color.valueOf(colorString); // Reconstruire l'objet Color
    }
}
