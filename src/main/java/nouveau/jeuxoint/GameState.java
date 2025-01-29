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
        return intersection.get(point);
    }

    public void clear() {
        intersection.clear();
    }

    public int getTurn() {
        return turn;
    }

    public void switchPlayer() {
        turn = (turn + 1) % players.size();
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
        System.out.println("Point ajouté : " + point + " | Occupied: " + isOccupied(point));
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Player> getPlayers() {
        return players;
    }
    
    @Override
    public String toString() {
        return "GameState [intersection=" + intersection + ", players=" + players + ", turn=" + turn + "]";
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public void setIntersection(HashMap<Point, Boolean> intersection) {
        this.intersection = intersection;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public int getMaxPoint() {
        return maxPoint;
    }

    public void setMaxPoint(int maxPoint) {
        if (maxPoint > 0) {
            this.maxPoint = maxPoint;
        } else {
            throw new IllegalArgumentException("maxPoint must be greater than 0");
        }
    }
}
