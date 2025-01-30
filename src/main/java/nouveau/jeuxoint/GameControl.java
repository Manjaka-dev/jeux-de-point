package nouveau.jeuxoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameControl {

    public static Point setPointCloser(GameState gameState, Point input) {
        List<Point> points = new ArrayList<>();

        // Ajouter tous les points non occupés à la liste
        gameState.getIntersection().forEach((pointt, occupied) -> {
            if (!occupied) {
                points.add(pointt);
            }
        });

        if (points.isEmpty()) {
            System.out.println("Aucun point non occupé disponible");
            return null;
        }

        // Initialiser le point le plus proche et la distance minimale
        Point closestPoint = null;
        double minDistance = Double.MAX_VALUE;

        for (Point point : points) {
            double distance = input.calculDistance(point);
            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = point;
            }
        }

        Player currentPlayer = gameState.getCurrentPlayer();

        if (closestPoint != null && gameState.getIntersection().containsKey(closestPoint) && minDistance <= 10) {
            gameState.setPoint(closestPoint);
            currentPlayer.addToList(closestPoint);
            return closestPoint;
        }
        return null;
    }

    public static void suggestion(GameState gameState) {
        Player currentPlayer = gameState.getCurrentPlayer();
        List<Point> playerPoints = currentPlayer.getPoints();
        int amplitude = 40; // Taille de chaque cellule de la grille

        System.out.println("Début de la suggestion pour le joueur : " + currentPlayer.getName());
        System.out.println("Points du joueur : " + playerPoints);

        // Directions possibles pour les alignements
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        // Créer une liste de HashMap pour les groupes de 5 points alignés
        List<HashMap<Point, Boolean>> alignmentMaps = new ArrayList<>();

        for (Point startPoint : playerPoints) {
            for (int[] dir : directions) {
                HashMap<Point, Boolean> map = new HashMap<>();
                for (int i = 0; i < 5; i++) {
                    Point currentPoint = new Point(startPoint.getX() + i * dir[0] * amplitude, startPoint.getY() + i * dir[1] * amplitude);
                    map.put(currentPoint, gameState.isOccupied(currentPoint));
                }
                alignmentMaps.add(map);
            }
        }

        System.out.println("Cartes d'alignement créées : " + alignmentMaps.size());

        // Trouver le groupe avec le plus de points déjà posés
        HashMap<Point, Boolean> bestGroup = null;
        int maxCount = 0;
        for (HashMap<Point, Boolean> map : alignmentMaps) {
            int count = 0;
            for (Boolean occupied : map.values()) {
                if (occupied) {
                    count++;
                }
            }
            if (count > maxCount) {
                maxCount = count;
                bestGroup = map;
            }
        }

        System.out.println("Meilleur groupe trouvé avec " + maxCount + " points alignés.");

        // Compléter le groupe avec le plus de points déjà posés
        if (bestGroup != null) {
            for (Point point : bestGroup.keySet()) {
                if (!bestGroup.get(point)) {
                    gameState.setPoint(point);
                    currentPlayer.addToList(point);
                    gameState.switchPlayer();
                    System.out.println("Suggestion: Point ajouté : " + point);
                    return;
                }
            }
        } else {
            System.out.println("Aucun groupe trouvé pour la suggestion.");
        }
    }

    public static boolean areNPointsAligned(List<Point> points, int amplitude, int number) {
        // Parcourir tous les points
        for (Point p : points) {
            // Vérifier l'alignement horizontal
            if (checkAlignment(points, p, amplitude, 0, number)) {
                return true;
            }
            // Vérifier l'alignement vertical
            if (checkAlignment(points, p, 0, amplitude, number)) {
                return true;
            }
            // Vérifier l'alignement diagonal (\)
            if (checkAlignment(points, p, amplitude, amplitude, number)) {
                return true;
            }
            // Vérifier l'alignement diagonal (/)
            if (checkAlignment(points, p, amplitude, -amplitude, number)) {
                return true;
            }
        }
        return false; // Aucun alignement trouvé
    }

    public static boolean checkAlignment(List<Point> points, Point start, int dx, int dy, int required) {
        int count = 0;

        for (int i = 0; i < required; i++) {
            Point current = new Point(start.getX() + i * dx, start.getY() + i * dy);
            if (points.contains(current)) {
                count++;
            } else {
                break; // Arrêter si un point manque
            }
        }
        return count == required; // Vérifier si on a trouvé le nombre requis de points alignés
    }

    public static List<Point> getAlignedPoints(List<Point> points, int amplitude, int number) {
        // Parcourir tous les points
        for (Point startPoint : points) {
            // Vérifier l'alignement horizontal
            if (checkAlignment(points, startPoint, amplitude, 0, number)) {
                return collectAlignedPoints(points, startPoint, amplitude, 0, number);
            }
            // Vérifier l'alignement vertical
            if (checkAlignment(points, startPoint, 0, amplitude, number)) {
                return collectAlignedPoints(points, startPoint, 0, amplitude, number);
            }
            // Vérifier l'alignement diagonal (\)
            if (checkAlignment(points, startPoint, amplitude, amplitude, number)) {
                return collectAlignedPoints(points, startPoint, amplitude, amplitude, number);
            }
            // Vérifier l'alignement diagonal (/)
            if (checkAlignment(points, startPoint, amplitude, -amplitude, number)) {
                return collectAlignedPoints(points, startPoint, amplitude, -amplitude, number);
            }
        }
        // Aucun alignement trouvé
        return new ArrayList<>();
    }

    private static List<Point> collectAlignedPoints(List<Point> points, Point startPoint, int dx, int dy, int number) {
        List<Point> alignedPoints = new ArrayList<>();
        int count = 0;

        for (int i = 0; i < number; i++) {
            Point currentPoint = new Point(startPoint.getX() + i * dx, startPoint.getY() + i * dy);
            if (points.contains(currentPoint)) {
                alignedPoints.add(currentPoint);
                count++;
            } else {
                break; // Interruption si un point manque
            }
        }

        // Vérifier que le nombre total correspond bien à `number`
        if (count == number) {
            return alignedPoints;
        } else {
            return new ArrayList<>(); // Retourne une liste vide si pas d'alignement complet
        }
    }

    public static List<List<Point>> groupAlignedPoints(List<Point> points, int amplitude, int number) {
        List<List<Point>> groupedAlignedPoints = new ArrayList<>();
        List<Point> visitedPoints = new ArrayList<>();

        // Parcourir tous les points
        for (Point startPoint : points) {
            if (visitedPoints.contains(startPoint)) {
                continue; // Ignorer les points déjà traités
            }
    
            // Vérifier l'alignement horizontal
            List<Point> horizontalGroup = collectAlignedPoints(points, startPoint, amplitude, 0, number);
            if (!horizontalGroup.isEmpty()) {
                groupedAlignedPoints.add(horizontalGroup);
                visitedPoints.addAll(horizontalGroup);
            }
    
            // Vérifier l'alignement vertical
            List<Point> verticalGroup = collectAlignedPoints(points, startPoint, 0, amplitude, number);
            if (!verticalGroup.isEmpty()) {
                groupedAlignedPoints.add(verticalGroup);
                visitedPoints.addAll(verticalGroup);
            }
    
            // Vérifier l'alignement diagonal (\)
            List<Point> diagonalGroup1 = collectAlignedPoints(points, startPoint, amplitude, amplitude, number);
            if (!diagonalGroup1.isEmpty()) {
                groupedAlignedPoints.add(diagonalGroup1);
                visitedPoints.addAll(diagonalGroup1);
            }
    
            // Vérifier l'alignement diagonal (/)
            List<Point> diagonalGroup2 = collectAlignedPoints(points, startPoint, amplitude, -amplitude, number);
            if (!diagonalGroup2.isEmpty()) {
                groupedAlignedPoints.add(diagonalGroup2);
                visitedPoints.addAll(diagonalGroup2);
            }
        }
    
        return groupedAlignedPoints;
    }
}
