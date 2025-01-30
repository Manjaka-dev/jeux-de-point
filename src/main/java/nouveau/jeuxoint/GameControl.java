package nouveau.jeuxoint;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameControl {

    public static Point setPointCloser(GameState gameState, Point input) {
        List<Point> availablePoints = new ArrayList<>();

        // Ajouter tous les points non occupés à la liste
        gameState.getIntersection().forEach((point, occupied) -> {
            if (!occupied) {
                availablePoints.add(point);
            }
        });

        if (availablePoints.isEmpty()) {
            System.out.println("Aucun point non occupé disponible");
            return null;
        }

        // Initialiser le point le plus proche et la distance minimale
        Point closestPoint = null;
        double minDistance = Double.MAX_VALUE;

        for (Point point : availablePoints) {
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

        // Trouver les groupes de points alignés en "L"
        List<Map<Point, Boolean>> lShapedGroups = findLShapedGroup(playerPoints, amplitude);

        System.out.println("Groupes en 'L' trouvés : " + lShapedGroups);

        // Compléter le groupe avec le plus de points déjà posés
        if (!lShapedGroups.isEmpty()) {
            // Trouver le groupe avec le plus de points déjà occupés
            Map<Point, Boolean> bestGroup = lShapedGroups.get(0);
            int maxOccupiedCount = (int) bestGroup.values().stream().filter(Boolean::booleanValue).count();

            for (Map<Point, Boolean> group : lShapedGroups) {
                int occupiedCount = (int) group.values().stream().filter(Boolean::booleanValue).count();
                if (occupiedCount > maxOccupiedCount) {
                    bestGroup = group;
                    maxOccupiedCount = occupiedCount;
                }
            }

            // Ajouter un point disponible à l'emplacement libre dans le groupe
            for (Map.Entry<Point, Boolean> entry : bestGroup.entrySet()) {
                if (!entry.getValue()) { // Si le point n'est pas occupé
                    Point point = entry.getKey();
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

    public static List<Map<Point, Boolean>> findLShapedGroup(List<Point> points, int amplitude) {
        List<Map<Point, Boolean>> lShapedGroups = new ArrayList<>();
        
        // All possible L-shape orientations:
        // 1. ─┐ (right-up)    2. ─┘ (right-down)
        // 3. ┌─ (left-up)     4. └─ (left-down)
        int[][][] orientations = {
            {{1, 0}, {0, -1}},  // right then up
            {{1, 0}, {0, 1}},   // right then down
            {{-1, 0}, {0, -1}}, // left then up
            {{-1, 0}, {0, 1}}   // left then down
        };
    
        for (Point startPoint : points) {
            for (int[][] orientation : orientations) {
                int[] mainDir = orientation[0];  // horizontal direction
                int[] perpDir = orientation[1];  // vertical direction
    
                // Try both patterns: 3+2 and 4+1
                // Pattern 1: 3 horizontal + 2 vertical
                Map<Point, Boolean> pattern1 = new LinkedHashMap<>();
                
                // Add horizontal points (3 points)
                for (int i = 0; i < 3; i++) {
                    Point p = new Point(
                        startPoint.getX() + i * mainDir[0] * amplitude,
                        startPoint.getY() + i * mainDir[1] * amplitude
                    );
                    pattern1.put(p, points.contains(p));
                }
    
                if (pattern1.values().stream().filter(Boolean::booleanValue).count() == 3) {
                    // Get the end point of horizontal line
                    Point endPoint = new ArrayList<>(pattern1.keySet()).get(2);
                    
                    // Add vertical points (2 points)
                    for (int i = 1; i <= 2; i++) {
                        Point p = new Point(
                            endPoint.getX() + perpDir[0] * amplitude,
                            endPoint.getY() + i * perpDir[1] * amplitude
                        );
                        pattern1.put(p, points.contains(p));
                    }
    
                    if (pattern1.values().stream().filter(Boolean::booleanValue).count() == 5) {
                        lShapedGroups.add(new LinkedHashMap<>(pattern1));
                        System.out.println("Found horizontal L pattern (3+2): " + pattern1);
                    }
                }
    
                // Pattern 2: 4 horizontal + 1 vertical
                Map<Point, Boolean> pattern2 = new LinkedHashMap<>();
                
                // Add horizontal points (4 points)
                for (int i = 0; i < 4; i++) {
                    Point p = new Point(
                        startPoint.getX() + i * mainDir[0] * amplitude,
                        startPoint.getY() + i * mainDir[1] * amplitude
                    );
                    pattern2.put(p, points.contains(p));
                }
    
                if (pattern2.values().stream().filter(Boolean::booleanValue).count() == 4) {
                    // Get the end point of horizontal line
                    Point endPoint = new ArrayList<>(pattern2.keySet()).get(3);
                    
                    // Add one vertical point
                    Point p = new Point(
                        endPoint.getX() + perpDir[0] * amplitude,
                        endPoint.getY() + perpDir[1] * amplitude
                    );
                    pattern2.put(p, points.contains(p));
    
                    if (pattern2.values().stream().filter(Boolean::booleanValue).count() == 5) {
                        lShapedGroups.add(new LinkedHashMap<>(pattern2));
                        System.out.println("Found horizontal L pattern (4+1): " + pattern2);
                    }
                }
            }
        }
        
        return lShapedGroups;
    }
    
       
}
