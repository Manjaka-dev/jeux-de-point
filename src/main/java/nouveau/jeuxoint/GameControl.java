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

        // Trouver les groupes de points alignés en "L"
        List<Point> lShapedGroup = findLShapedGroup(playerPoints, amplitude);

        System.out.println("Groupe en 'L' trouvé : " + lShapedGroup);

        // Compléter le groupe avec le plus de points déjà posés
        if (!lShapedGroup.isEmpty()) {
            for (Point point : lShapedGroup) {
                if (!gameState.isOccupied(point)) {
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

    private static List<Point> findLShapedGroup(List<Point> points, int amplitude) {
        List<Point> lShapedPoints = new ArrayList<>();

        // Vérifier les formes en "L" avec 4 points alignés et un point adjacent
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        for (Point startPoint : points) {
            for (int[] dir : directions) {
                List<Point> tempPoints = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    Point currentPoint = new Point(startPoint.getX() + i * dir[0] * amplitude, startPoint.getY() + i * dir[1] * amplitude);
                    if (points.contains(currentPoint)) {
                        tempPoints.add(currentPoint);
                    } else {
                        break;
                    }
                }
                if (tempPoints.size() == 4) {
                    // Ajouter le point adjacent pour former un "L"
                    Point adjacentPoint1 = new Point(tempPoints.get(3).getX() + dir[1] * amplitude, tempPoints.get(3).getY() + dir[0] * amplitude);
                    Point adjacentPoint2 = new Point(tempPoints.get(3).getX() - dir[1] * amplitude, tempPoints.get(3).getY() - dir[0] * amplitude);
                    if (!points.contains(adjacentPoint1)) {
                        lShapedPoints.add(adjacentPoint1);
                    }
                    if (!points.contains(adjacentPoint2)) {
                        lShapedPoints.add(adjacentPoint2);
                    }
                }
            }
        }

        // Vérifier les formes en "L" avec 3 points alignés et deux points adjacents
        for (Point startPoint : points) {
            for (int[] dir : directions) {
                List<Point> tempPoints = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    Point currentPoint = new Point(startPoint.getX() + i * dir[0] * amplitude, startPoint.getY() + i * dir[1] * amplitude);
                    if (points.contains(currentPoint)) {
                        tempPoints.add(currentPoint);
                    } else {
                        break;
                    }
                }
                if (tempPoints.size() == 3) {
                    // Ajouter les deux points adjacents pour former un "L"
                    Point adjacentPoint1 = new Point(tempPoints.get(2).getX() + dir[1] * amplitude, tempPoints.get(2).getY() + dir[0] * amplitude);
                    Point adjacentPoint2 = new Point(tempPoints.get(2).getX() - dir[1] * amplitude, tempPoints.get(2).getY() - dir[0] * amplitude);
                    if (!points.contains(adjacentPoint1)) {
                        lShapedPoints.add(adjacentPoint1);
                    }
                    if (!points.contains(adjacentPoint2)) {
                        lShapedPoints.add(adjacentPoint2);
                    }
                }
            }
        }
        return lShapedPoints;
    }

    public static List<List<Point>> groupAlignedPoints(List<Point> points, int amplitude, int number) {
        List<List<Point>> alignedGroups = new ArrayList<>();

        for (Point p : points) {
            // Vérifier l'alignement horizontal
            List<Point> horizontalGroup = checkAlignment(points, p, amplitude, 0, number);
            if (horizontalGroup.size() == number) {
                alignedGroups.add(horizontalGroup);
            }

            // Vérifier l'alignement vertical
            List<Point> verticalGroup = checkAlignment(points, p, 0, amplitude, number);
            if (verticalGroup.size() == number) {
                alignedGroups.add(verticalGroup);
            }

            // Vérifier l'alignement diagonal (\)
            List<Point> diagonalGroup1 = checkAlignment(points, p, amplitude, amplitude, number);
            if (diagonalGroup1.size() == number) {
                alignedGroups.add(diagonalGroup1);
            }

            // Vérifier l'alignement diagonal (/)
            List<Point> diagonalGroup2 = checkAlignment(points, p, amplitude, -amplitude, number);
            if (diagonalGroup2.size() == number) {
                alignedGroups.add(diagonalGroup2);
            }
        }

        return alignedGroups;
    }

    private static List<Point> checkAlignment(List<Point> points, Point start, int dx, int dy, int required) {
        List<Point> alignedPoints = new ArrayList<>();
        for (int i = 0; i < required; i++) {
            Point current = new Point(start.getX() + i * dx, start.getY() + i * dy);
            if (points.contains(current)) {
                alignedPoints.add(current);
            } else {
                break;
            }
        }
        return alignedPoints;
    }
}
