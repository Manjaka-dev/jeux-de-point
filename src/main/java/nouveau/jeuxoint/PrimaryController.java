package nouveau.jeuxoint;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class PrimaryController {

    @FXML
    private Canvas gameCanvas;

    @FXML
    private Button suggestionButton;

    private GameState gameState;

    private double cellSize = 40;

    @FXML
    private void initialize() {
        gameState = new GameState();
        initializePlayers();
        drawGrid();
        gameCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleCanvasClick);
        updateSuggestionButtonColor();
    }

    private void initializePlayers() {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Player 1", Color.RED));
        players.add(new Player("Player 2", Color.BLUE));
        gameState.setPlayers(players);
    }

    private void drawGrid() {
        if (gameState != null) {
            GraphicsContext gc = gameCanvas.getGraphicsContext2D();
            gc.setStroke(Color.GRAY);
            double width = gameCanvas.getWidth();
            double height = gameCanvas.getHeight(); // Taille de chaque cellule de la grille

            for (double x = 0; x <= width; x += cellSize) {
                gc.strokeLine(x, 0, x, height);
            }

            for (double y = 0; y <= height; y += cellSize) {
                gc.strokeLine(0, y, width, y);
            }

            for (double x = 0; x <= width; x += cellSize) {
                for (double y = 0; y <= height; y += cellSize) {
                    gameState.addIntersection(new Point((int) x, (int) y));
                }
            }
        } else {
            throw new IllegalStateException("gameState is null");
        }
    }

    private void handleCanvasClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        Point inputPoint = new Point((int) x, (int) y);
        Point closestPoint = GameControl.setPointCloser(gameState, inputPoint);
        if (closestPoint != null) {
            Player currentPlayer = gameState.getCurrentPlayer();
            if (currentPlayer.getPoints().contains(closestPoint)) {
                drawPoint(closestPoint, currentPlayer.getColor());
                gameState.switchPlayer();
                updateSuggestionButtonColor();
            }
        }
        // Redessiner tous les points du joueur actuel
        drawAllPoints();
        drawAlignedLines(); // Assurez-vous que cette ligne est présente pour dessiner les lignes alignées
    }

    private void drawPoint(Point point, Color color) {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.setFill(color);
        double ovalSize = cellSize / 3; // Réduire la taille de l'ovale
        gc.fillOval(point.getX() - ovalSize / 2, point.getY() - ovalSize / 2, ovalSize, ovalSize);
    }

    private void drawAllPoints() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        drawGrid();
        for (Player player : gameState.getPlayers()) {
            for (Point point : player.getPoints()) {
                drawPoint(point, player.getColor());
            }
        }
    }

    private void drawAlignedLines() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.setLineWidth(2);
    
        for (Player player : gameState.getPlayers()) {
            List<Point> playerPoints = player.getPoints();
            List<Map<Point, Boolean>> lShapedGroups = GameControl.findLShapedGroup(playerPoints, ((int) cellSize));
    
            for (Map<Point, Boolean> map : lShapedGroups) {
                boolean allTrue = map.values().stream().filter(Boolean::booleanValue).count() == 5;
                if (allTrue) {
                    System.out.println("Groupe en 'L' trouvé avec 5 points : " + map);
                    List<Point> points = new ArrayList<>(map.keySet());
    
                    // Dessiner les lignes pour connecter les points en "L"
                    for (int i = 0; i < points.size(); i++) {
                        Point p1 = points.get(i);
                        if (!map.get(p1)) continue; // Ignorer les points en false
    
                        for (int j = i + 1; j < points.size(); j++) {
                            Point p2 = points.get(j);
                            if (!map.get(p2)) continue; // Ignorer les points en false
    
                            // Vérifier si les deux points sont alignés ou adjacents
                            boolean aligned = (p1.getX() == p2.getX() && Math.abs(p1.getY() - p2.getY()) == cellSize) ||
                                             (p1.getY() == p2.getY() && Math.abs(p1.getX() - p2.getX()) == cellSize);
                            boolean adjacent = (Math.abs(p1.getX() - p2.getX()) == cellSize && Math.abs(p1.getY() - p2.getY()) == 0) ||
                                               (Math.abs(p1.getY() - p2.getY()) == cellSize && Math.abs(p1.getX() - p2.getX()) == 0);
    
                            if (aligned || adjacent) {
                                gc.setStroke(player.getColor());
                                gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                                System.out.println("Ligne dessinée entre " + p1 + " et " + p2);
                            }
                        }
                    }
                }
            }
        }
    }
    

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    @FXML
    private void handleSuggestion() {
        // Logique pour le bouton Suggestion
        System.out.println("Bouton Suggestion cliqué");
        GameControl.suggestion(gameState);

        // Redessiner la grille et les points après la suggestion
        drawGrid();
        for (Player player : gameState.getPlayers()) {
            for (Point point : player.getPoints()) {
                drawPoint(point, player.getColor());
            }
        }
        drawAlignedLines();
        updateSuggestionButtonColor();
    }

    @FXML
    private void handleSave() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("gamestate.ser"))) {
            out.writeObject(gameState);
            System.out.println("Game state saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoad() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("gamestate.ser"))) {
            gameState = (GameState) in.readObject();
            // Redessiner la grille et les points après le chargement
            drawGrid();
            for (Player player : gameState.getPlayers()) {
                for (Point point : player.getPoints()) {
                    drawPoint(point, player.getColor());
                }
            }
            // Relier les points alignés après le chargement
            drawAlignedLines();
            updateSuggestionButtonColor();
            System.out.println("Game state loaded.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateSuggestionButtonColor() {
        Color currentColor = gameState.getCurrentPlayer().getColor();
        String colorString = String.format("#%02X%02X%02X",
                (int) (currentColor.getRed() * 255),
                (int) (currentColor.getGreen() * 255),
                (int) (currentColor.getBlue() * 255));
        suggestionButton.setStyle("-fx-background-color: " + colorString + ";");
    }
}