import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.Timer;

public class GamePanel extends JPanel implements MouseListener, ActionListener {
    private final int TILE_SIZE = 40;
    private final int GRID_WIDTH = 20;
    private final int GRID_HEIGHT = 15;
    private int playerX;
    private int playerY;
    private boolean[] destinations = new boolean[GRID_WIDTH * GRID_HEIGHT];
    private boolean[] roads = new boolean[GRID_WIDTH * GRID_HEIGHT];
    private boolean[] buildings = new boolean[GRID_WIDTH * GRID_HEIGHT];
    private boolean[] emptyTiles = new boolean[GRID_WIDTH * GRID_HEIGHT];
    private int[] roadLengths = new int[GRID_WIDTH * GRID_HEIGHT];
    private java.util.List<Point> path = new ArrayList<>();
    private final String ROAD_LAYOUT_FILE = "src/roads.txt";
    private final int PLAYER_SPEED = 1; // 1 minute per meter
    private final int VISUAL_SPEED = 50; // visual speed multiplier
    private int remainingDistance = 0;
    private Point lastBuilding = null;
    private int totalDistance = 0;
    private Point highlightedBuilding = null;
    private int score = 0;
    private Image playerSpriteUp;
    private Image playerSpriteDown;
    private Image playerSpriteLeft;
    private Image playerSpriteRight;
    private Image playerSprite; // Current sprite
    private String playerDirection = "down"; // Default direction
    private Image roadSprite;
    private Image buildingSprite;
    private Image emptyTileSprite;
    private Image indicatorSprite;
    private boolean gameOver = false;
    private boolean showDeliveryMessage = false;
    private boolean showTravelMessage = false;
    private String deliveryMessage = "";
    private String travelMessage = "";
    private String TravelMessage2 = "";
    private String DeliverMessage2 = "";
    private Point obstacle = null;
    private Image obstacleSprite;
    private Image scoreIcon;
    private Image cursorImage;
    private int[] buildingTypes = new int[GRID_WIDTH * GRID_HEIGHT];
    private Image[] buildingSprites = new Image[4]; // Array to hold different building sprites


    public GamePanel() {
        setPreferredSize(new Dimension(GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE));
        setBackground(Color.WHITE);
        setFocusable(true);
        addMouseListener(this);
        Timer timer = new Timer(100, this); // Update every 100 milliseconds
        timer.start();
        loadRoadsFromFile();
        generateRandomRoadLengths();
        spawnPlayerRandomly();
        highlightRandomBuilding();
        loadSprites();
    }


    private void loadSprites() {
        try {
            playerSpriteUp = ImageIO.read(new File("src/up.png"));
            playerSpriteDown = ImageIO.read(new File("src/down.png"));
            playerSpriteLeft = ImageIO.read(new File("src/left.png"));
            playerSpriteRight = ImageIO.read(new File("src/right.png"));
            playerSprite = playerSpriteDown; // Default sprite
            roadSprite = ImageIO.read(new File("src/road.png"));
            buildingSprites[0] = ImageIO.read(new File("src/building1.png")); // Load skyscraper sprite
            buildingSprites[1] = ImageIO.read(new File("src/building2.png")); // Load house sprite
            buildingSprites[2] = ImageIO.read(new File("src/building3.png")); // Load office sprite
            buildingSprites[3] = ImageIO.read(new File("src/building2.png")); // Load shop sprite
            emptyTileSprite = ImageIO.read(new File("src/empty.png"));
            indicatorSprite = ImageIO.read(new File("src/indicator.png")); // Load the indicator sprite
            obstacleSprite = ImageIO.read(new File("src/obstacle.png")); // Load the obstacle sprite
            scoreIcon = ImageIO.read(new File("src/score_icon.png")); // Load the score icon
            cursorImage = ImageIO.read(new File("src/cursor.png")); // Load the cursor image
            setCustomCursor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setCustomCursor() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Cursor cursor = toolkit.createCustomCursor(cursorImage, new Point(0, 0), "Custom Cursor");
        setCursor(cursor);
    }

    private void placeObstacle() {
        if (score > 3) {
            Random rand = new Random();
            while (true) {
                int x = rand.nextInt(GRID_WIDTH);
                int y = rand.nextInt(GRID_HEIGHT);
                if (roads[toIndex(x, y)] && !isBuilding(x, y) && !(x == playerX && y == playerY)) {
                    obstacle = new Point(x, y);
                    break;
                }
            }
        } else {
            obstacle = null;
        }
    }

    private void spawnPlayerRandomly() {
        Random rand = new Random();
        java.util.List<Point> buildingPositions = new ArrayList<>();
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                if (isBuilding(i, j)) {
                    buildingPositions.add(new Point(i, j));
                }
            }
        }
        if (!buildingPositions.isEmpty()) {
            Point spawnPoint = buildingPositions.get(rand.nextInt(buildingPositions.size()));
            playerX = spawnPoint.x;
            playerY = spawnPoint.y;
            lastBuilding = new Point(playerX, playerY);
        }
    }

    private void highlightRandomBuilding() {
        Random rand = new Random();
        java.util.List<Point> buildingPositions = new ArrayList<>();
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                if (isBuilding(i, j)) {
                    buildingPositions.add(new Point(i, j));
                }
            }
        }
        if (!buildingPositions.isEmpty()) {
            highlightedBuilding = buildingPositions.get(rand.nextInt(buildingPositions.size()));
        }
        placeObstacle(); // Place the obstacle after highlighting a random building
    }

    private void loadRoadsFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(ROAD_LAYOUT_FILE))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                for (int col = 0; col < line.length(); col++) {
                    char tileType = line.charAt(col);
                    int index = toIndex(col, row);
                    if (tileType == 'R') {
                        roads[index] = true;
                    } else if (tileType >= '0' && tileType <= '3') {
                        buildings[index] = true;
                        buildingTypes[index] = tileType - '0'; // Store the building type
                    } else if (tileType == '.') {
                        emptyTiles[index] = true;
                    }
                }
                row++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateRandomRoadLengths() {
        Random rand = new Random();
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                int index = toIndex(i, j);
                if (roads[index] && hasAdjacentBuilding(i, j)) {
                    roadLengths[index] = 10 + rand.nextInt(11); // Random length between 10 and 100 meters
                } else if (roads[index]) {
                    roadLengths[index] = 10; // Default length for other roads
                }
            }
        }
    }

    private boolean hasAdjacentBuilding(int x, int y) {
        return (x > 0 && isBuilding(x - 1, y)) ||
               (x < GRID_WIDTH - 1 && isBuilding(x + 1, y)) ||
               (y > 0 && isBuilding(x, y - 1)) ||
               (y < GRID_HEIGHT - 1 && isBuilding(x, y + 1));
    }

    private boolean isBuilding(int x, int y) {
        return buildings[toIndex(x, y)];
    }

    private int toIndex(int x, int y) {
        return y * GRID_WIDTH + x;
    }

    @Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    drawGrid(g);
    drawEmptyTiles(g);
    drawRoads(g);
    drawBuildings(g);
    drawPlayer(g);
    drawHighlightedBuilding(g);
    drawScore(g);
    if (obstacle != null) {
        g.drawImage(obstacleSprite, obstacle.x * TILE_SIZE, obstacle.y * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
    }
    if (gameOver) {
        drawGameOver(g);
    }
    if (showDeliveryMessage) {
        drawDeliveryMessage(g);
    }
    if (showTravelMessage) {
        drawTravelMessage(g);
    }
    
}
    private void drawGrid(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= GRID_WIDTH; i++) {
            g.drawLine(i * TILE_SIZE, 0, i * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);
        }
        for (int i = 0; i <= GRID_HEIGHT; i++) {
            g.drawLine(0, i * TILE_SIZE, GRID_WIDTH * TILE_SIZE, i * TILE_SIZE);
        }
    }

    private void drawEmptyTiles(Graphics g) {
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                if (emptyTiles[toIndex(i, j)]) {
                    g.drawImage(emptyTileSprite, i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
                }
            }
        }
    }

    private void drawRoads(Graphics g) {
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                if (roads[toIndex(i, j)]) {
                    g.drawImage(roadSprite, i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
                }
            }
        }
    }

    private void drawBuildings(Graphics g) {
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                int index = toIndex(i, j);
                if (buildings[index]) {
                    int buildingType = buildingTypes[index];
                    g.drawImage(buildingSprites[buildingType], i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
                }
            }
        }
    }

    private void drawPlayer(Graphics g) {
        if (playerSprite != null) {
            g.drawImage(playerSprite, playerX * TILE_SIZE, playerY * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
        } else {
            g.setColor(Color.RED);
            g.fillRect(playerX * TILE_SIZE, playerY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawHighlightedBuilding(Graphics g) {
        if (highlightedBuilding != null) {
            int offsetX = 5; // Adjust the x position
            int offsetY = -20; // Adjust the y position
            g.drawImage(indicatorSprite, highlightedBuilding.x * TILE_SIZE + offsetX, highlightedBuilding.y * TILE_SIZE + offsetY, TILE_SIZE, TILE_SIZE, this);
        }
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString(": " + score, 50, 32);
        if (scoreIcon != null) {
            g.drawImage(scoreIcon, 10, 0, 40, 40, this); // Adjust the position and size as needed
        }
    }
    private void drawGameOver(Graphics g) {
        int score1 = score+1;
        g.setColor(new Color(255, 255, 204)); // Light yellow background color
        g.fillRect(110, 140, 570, 160); // Background rectangle
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Congrats! You have a score of " + score1, 150, 200);
        g.drawString(" and total travel time of " + totalDistance + " minutes.", 150, 240);
    }

    private void drawDeliveryMessage(Graphics g) {
        g.setColor(new Color(255, 255, 204)); // Light yellow background color
        g.fillRect(40, 180, 700, 160); // Background rectangle
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString(deliveryMessage, 100, 200);
        g.drawString(DeliverMessage2, 135, 240);
        g.drawString("Would you like to continue the game?", 190, 280);
        g.drawString("Yes", 310, 320);
        g.drawString("No", 410, 320);
    }

    private void drawTravelMessage(Graphics g) {
        g.setColor(new Color(255, 255, 204)); // Light yellow background color
        g.fillRect(40, 180, 700, 160); // Background rectangle
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString(travelMessage, 130, 220);    
        g.drawString(TravelMessage2, 190, 260);        
        g.drawString("Click to continue...", 280,320);
    }

   @Override
public void actionPerformed(ActionEvent e) {
    if (!path.isEmpty() && !gameOver && !showDeliveryMessage && !showTravelMessage) {
        if (remainingDistance <= 0) {
            Point nextStep = path.remove(0);
            // Determine the direction of movement
            if (nextStep.x > playerX) {
                playerDirection = "right";
                playerSprite = playerSpriteRight;
            } else if (nextStep.x < playerX) {
                playerDirection = "left";
                playerSprite = playerSpriteLeft;
            } else if (nextStep.y > playerY) {
                playerDirection = "down";
                playerSprite = playerSpriteDown;
            } else if (nextStep.y < playerY) {
                playerDirection = "up";
                playerSprite = playerSpriteUp;
            }
            playerX = nextStep.x;
            playerY = nextStep.y;
            remainingDistance = roadLengths[toIndex(playerX, playerY)];
            totalDistance += remainingDistance;
            // Clear the destination if the player reaches it
            if (destinations[toIndex(playerX, playerY)]) {
                destinations[toIndex(playerX, playerY)] = false;
                // Check if the player reached the destination building
                if (isBuilding(playerX, playerY)) {
                    if (lastBuilding != null) {
                        int time = totalDistance; // Time in minutes since 1 meter = 1 minute
                        int hours = time / 60;
                        int minutes = time % 60;
                        if (highlightedBuilding != null && playerX == highlightedBuilding.x && playerY == highlightedBuilding.y) {
                            deliveryMessage = "You delivered a Pizza with a time of " + hours + " hour/s and " + minutes + " minutes. ";
                            DeliverMessage2 = "From building (" + lastBuilding.x + ", " + lastBuilding.y + ") to building (" + playerX + ", " + playerY + ") with " + totalDistance + " meters.";
                            showDeliveryMessage = true;
                            totalDistance = 0; // Reset total distance for the next trip
                            lastBuilding = new Point(playerX, playerY); // Update last building
                            repaint();
                            return;
                        } else {
                            travelMessage = "Traveled from building at (" + lastBuilding.x + ", " + lastBuilding.y + ") to building at (" + playerX + ", " + playerY + ") ";
                            TravelMessage2 = "with " + totalDistance + " meters in " + hours + " hour/s and " + minutes + " mins.";
                            showTravelMessage = true;
                            totalDistance = 0; // Reset total distance for the next trip
                            lastBuilding = new Point(playerX, playerY); // Update last building
                            repaint();
                            return;
                        }
                    }
                }
            }
        } else {
            remainingDistance -= PLAYER_SPEED * VISUAL_SPEED * 0.1; // Adjust for 100ms timer and visual speed
        }
    }
    repaint();
}

    @Override
    public void mouseClicked(MouseEvent e) {
        if (showDeliveryMessage) {
            int x = e.getX();
            int y = e.getY();
            if (x >= 300 && x <= 350 && y >= 300 && y <= 340) {
                // Yes button clicked
                score++;
                highlightRandomBuilding();
                showDeliveryMessage = false;
            } else if (x >= 390 && x <= 430 && y >= 300 && y <= 340) {
                // No button clicked
                gameOver = true;
                showDeliveryMessage = false;
            }
            repaint();
        } else if (showTravelMessage) {
            showTravelMessage = false;
            repaint();
        } else {
            int x = e.getX() / TILE_SIZE;
            int y = e.getY() / TILE_SIZE;
            if (x < GRID_WIDTH && y < GRID_HEIGHT) {
                // Check if the clicked tile is a building
                if (isBuilding(x, y)) {
                    destinations[toIndex(x, y)] = true;
                    calculateShortestPath(new Point(playerX, playerY), new Point(x, y));
                }
            }
        }
    }

    private void calculateShortestPath(Point start, Point end) {
        // Implement Dijkstra's algorithm to find the shortest path
        PriorityQueue<Node> queue = new PriorityQueue<>();
        Map<Point, Integer> distances = new HashMap<>();
        Map<Point, Point> previous = new HashMap<>();
        Set<Point> visited = new HashSet<>();

        distances.put(start, 0);
        queue.add(new Node(start, 0));

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            Point currentPoint = current.point;

            if (visited.contains(currentPoint)) continue;
            visited.add(currentPoint);

            if (currentPoint.equals(end)) break;

            for (Point neighbor : getNeighbors(currentPoint)) {
                if (!visited.contains(neighbor)) {
                    int newDist = distances.get(currentPoint) + roadLengths[toIndex(neighbor.x, neighbor.y)];
                    if (newDist < distances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                        distances.put(neighbor, newDist);
                        previous.put(neighbor, currentPoint);
                        queue.add(new Node(neighbor, newDist));
                    }
                }
            }
        }

        // Reconstruct the path
        path.clear();
        for (Point at = end; at != null; at = previous.get(at)) {
            path.add(0, at);
        }
    }

    private java.util.List<Point> getNeighbors(Point point) {
        java.util.List<Point> neighbors = new ArrayList<>();
        int x = point.x;
        int y = point.y;
    
        // Check roads and buildings
        if (x > 0 && (roads[toIndex(x - 1, y)] || buildings[toIndex(x - 1, y)]) && (obstacle == null || !obstacle.equals(new Point(x - 1, y)))) neighbors.add(new Point(x - 1, y));
        if (x < GRID_WIDTH - 1 && (roads[toIndex(x + 1, y)] || buildings[toIndex(x + 1, y)]) && (obstacle == null || !obstacle.equals(new Point(x + 1, y)))) neighbors.add(new Point(x + 1, y));
        if (y > 0 && (roads[toIndex(x, y - 1)] || buildings[toIndex(x, y - 1)]) && (obstacle == null || !obstacle.equals(new Point(x, y - 1)))) neighbors.add(new Point(x, y - 1));
        if (y < GRID_HEIGHT - 1 && (roads[toIndex(x, y + 1)] || buildings[toIndex(x, y + 1)]) && (obstacle == null || !obstacle.equals(new Point(x, y + 1)))) neighbors.add(new Point(x, y + 1));
    
        return neighbors;
    }

    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    // Node class to use in the priority queue for Dijkstra's algorithm
    private static class Node implements Comparable<Node> {
        Point point;
        int distance;

        Node(Point point, int distance) {
            this.point = point;
            this.distance = distance;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.distance, other.distance);
        }
    }
}