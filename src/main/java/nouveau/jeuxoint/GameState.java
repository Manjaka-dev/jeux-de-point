package nouveau.jeuxoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    HashMap<Point, Boolean> intersection;
    List<Player> players;
    int turn;
    int maxPoint;

    public GameState() {
        intersection = new HashMap<>();
        players = new ArrayList<>(); // Initialisation de la liste des joueurs
    }

    public void addIntersection(Point point) {
        intersection.put(point, false);
    }

    public void removeIntersection(Point point) {
        intersection.remove(point);
    }

    public boolean isOccupied(Point point) {
        Boolean occupied = intersection.get(point);
        return occupied != null && occupied;
    }

    public void clear() {
        intersection.clear();
    }

    public int getTurn() {
        return turn;
    }

    public void switchPlayer() {
        turn = (turn + 1) % players.size();
        System.out.println("Changement de joueur : " + getCurrentPlayer().getName());
    }

    public HashMap<Point, Boolean> getIntersection() {
        return intersection;
    }

    public Player getCurrentPlayer() {
        if (players == null || players.isEmpty()) {
            throw new IllegalStateException("Players list is null or empty");
        }
        return players.get(turn);
    }

    public void setPoint(Point point) {
        intersection.put(point, true);
        System.out.println("Point occupé par " + getCurrentPlayer().getName()+" : "+point);
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
