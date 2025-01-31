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

    public static List<Map<Point, Boolean>> findLShapedGroup(List<Point> points, int amplitude) {
        List<Map<Point, Boolean>> lShapedGroups = new ArrayList<>();

        // All possible L-shape orientations:
        // 1. ┐ (vertical-right) 2. ┌ (vertical-left)
        // 3. ┘ (vertical-right) 4. └ (vertical-left)
        // 5. ─┐ (horizontal-up) 6. ─┘ (horizontal-down)
        // 7. ┌─ (horizontal-up) 8. └─ (horizontal-down)
        int[][][] orientations = {
                // Vertical patterns (main direction vertical, then horizontal)
                { { 0, 1 }, { 1, 0 } }, // down then right
                { { 0, 1 }, { -1, 0 } }, // down then left
                { { 0, -1 }, { 1, 0 } }, // up then right
                { { 0, -1 }, { -1, 0 } }, // up then left
                // Horizontal patterns (main direction horizontal, then vertical)
                { { 1, 0 }, { 0, -1 } }, // right then up
                { { 1, 0 }, { 0, 1 } }, // right then down
                { { -1, 0 }, { 0, -1 } }, // left then up
                { { -1, 0 }, { 0, 1 } } // left then down
        };

        for (Point startPoint : points) {
            for (int[][] orientation : orientations) {
                int[] mainDir = orientation[0];
                int[] perpDir = orientation[1];

                // Try both patterns: 3+2 and 4+1
                // Pattern 1: 3 main direction + 2 perpendicular
                Map<Point, Boolean> pattern1 = new LinkedHashMap<>();

                // Add main direction points (3 points)
                for (int i = 0; i < 3; i++) {
                    Point p = new Point(
                            startPoint.getX() + i * mainDir[0] * amplitude,
                            startPoint.getY() + i * mainDir[1] * amplitude);
                    pattern1.put(p, points.contains(p));
                }

                // If we have all 3 points in main direction
                int trueCount = (int) pattern1.values().stream().filter(Boolean::booleanValue).count();
                if (trueCount >= 2) { // Changed from == 3 to >= 2 to be more lenient
                    // Get the end point of main direction line
                    Point endPoint = new ArrayList<>(pattern1.keySet()).get(2);

                    // Add perpendicular points (2 points)
                    for (int i = 1; i <= 2; i++) {
                        Point p = new Point(
                                endPoint.getX() + i * perpDir[0] * amplitude,
                                endPoint.getY() + i * perpDir[1] * amplitude);
                        pattern1.put(p, points.contains(p));
                    }

                    // Add to groups if we have at least 4 points
                    trueCount = (int) pattern1.values().stream().filter(Boolean::booleanValue).count();
                    if (trueCount >= 4) {
                        lShapedGroups.add(new LinkedHashMap<>(pattern1));
                        System.out.println("Found pattern (3+2): " + pattern1);
                    }
                }

                // Pattern 2: 4 main direction + 1 perpendicular
                Map<Point, Boolean> pattern2 = new LinkedHashMap<>();

                // Add main direction points (4 points)
                for (int i = 0; i < 4; i++) {
                    Point p = new Point(
                            startPoint.getX() + i * mainDir[0] * amplitude,
                            startPoint.getY() + i * mainDir[1] * amplitude);
                    pattern2.put(p, points.contains(p));
                }

                // If we have at least 3 points in main direction
                trueCount = (int) pattern2.values().stream().filter(Boolean::booleanValue).count();
                if (trueCount >= 3) { // Changed from == 4 to >= 3 to be more lenient
                    // Get the end point of main direction line
                    Point endPoint = new ArrayList<>(pattern2.keySet()).get(3);

                    // Add one perpendicular point
                    Point p = new Point(
                            endPoint.getX() + perpDir[0] * amplitude,
                            endPoint.getY() + perpDir[1] * amplitude);
                    pattern2.put(p, points.contains(p));

                    // Add to groups if we have at least 4 points
                    trueCount = (int) pattern2.values().stream().filter(Boolean::booleanValue).count();
                    if (trueCount >= 4) {
                        lShapedGroups.add(new LinkedHashMap<>(pattern2));
                        System.out.println("Found pattern (4+1): " + pattern2);
                    }
                }
            }
        }

        return lShapedGroups;
    }

    public static void suggestion(GameState gameState) {
        Player currentPlayer = gameState.getCurrentPlayer();
        List<Point> playerPoints = currentPlayer.getPoints();
        int amplitude = 40;
    
        System.out.println("\n=== DÉBUT SUGGESTION ===");
        System.out.println("Joueur actuel: " + currentPlayer.getName());
        System.out.println("Points du joueur: " + playerPoints);
    
        // Get potential L-shaped groups for current player
        List<Map<Point, Boolean>> potentialGroups = findLShapedGroup(playerPoints, amplitude);
        System.out.println("Nombre de groupes potentiels trouvés: " + potentialGroups.size());
    
        // First priority: Complete own L-shape if possible
        for (Map<Point, Boolean> group : potentialGroups) {
            System.out.println("\nAnalyse d'un groupe: " + group);
            long trueCount = group.values().stream().filter(Boolean::booleanValue).count();
            System.out.println("Nombre de points true dans le groupe: " + trueCount);
    
            if (trueCount == 4) {
                Point pointToAdd = findAvailablePoint(group, gameState);
                if (pointToAdd != null) {
                    makeMove(gameState, currentPlayer, pointToAdd);
                    return;
                }
            }
        }
    
        // Second priority: Block opponent's L-shape
        List<Map<Point, Boolean>> opponentPotentialGroups = findLShapedGroup(
                gameState.getNonCurrentPlayer().getPoints(), amplitude);
        
        for (Map<Point, Boolean> group : opponentPotentialGroups) {
            long trueCount = group.values().stream().filter(Boolean::booleanValue).count();
            if (trueCount == 4) {
                Point pointToAdd = findAvailablePoint(group, gameState);
                if (pointToAdd != null) {
                    makeMove(gameState, currentPlayer, pointToAdd);
                    return;
                }
            }
        }
    
        // Last resort: place adjacent to existing point
        if (!playerPoints.isEmpty()) {
            for (Point existingPoint : playerPoints) {
                int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
                for (int[] dir : directions) {
                    Point adjacent = new Point(
                        existingPoint.getX() + dir[0] * amplitude,
                        existingPoint.getY() + dir[1] * amplitude
                    );
                    if (gameState.getIntersection().containsKey(adjacent) &&
                        !gameState.isOccupied(adjacent)) {
                        System.out.println("Placement adjacent au point: " + existingPoint);
                        System.out.println("Point placé: " + adjacent);
                        makeMove(gameState, currentPlayer, adjacent);
                        return;
                    }
                }
            }
        }
    
        System.out.println("Aucune suggestion possible");
        System.out.println("=== FIN SUGGESTION ===\n");
    }
    
    private static Point findAvailablePoint(Map<Point, Boolean> group, GameState gameState) {
        for (Map.Entry<Point, Boolean> entry : group.entrySet()) {
            if (!entry.getValue()) {
                Point pointToAdd = entry.getKey();
                System.out.println("Point trouvé à ajouter: " + pointToAdd);
    
                if (!gameState.isOccupied(pointToAdd) && 
                    !gameState.getCurrentPlayer().getPoints().contains(pointToAdd) &&
                    !gameState.getNonCurrentPlayer().getPoints().contains(pointToAdd)) {
                    System.out.println("=== SUGGESTION TROUVÉE ===");
                    System.out.println("Ajout du point: " + pointToAdd);
                    return pointToAdd;
                } else {
                    System.out.println("Point déjà occupé, on continue la recherche");
                }
            }
        }
        return null;
    }
    
    private static void makeMove(GameState gameState, Player currentPlayer, Point point) {
        gameState.setPoint(point);
        currentPlayer.addToList(point);
        gameState.switchPlayer();
    }
    

}