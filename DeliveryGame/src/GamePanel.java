import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.Timer;

public class GamePanel extends JPanel implements MouseListener, ActionListener {
    private final int TILE_SIZE = 40;
    private final int GRID_WIDTH = 20;
    private final int GRID_HEIGHT = 15;
    private int playerX;
    private int playerY;
    private boolean[][] destinations = new boolean[GRID_WIDTH][GRID_HEIGHT];
    private boolean[][] horizontalRoads = new boolean[GRID_WIDTH][GRID_HEIGHT];
    private boolean[][] verticalRoads = new boolean[GRID_WIDTH][GRID_HEIGHT];
    private boolean[][] buildings = new boolean[GRID_WIDTH][GRID_HEIGHT];
    private java.util.List<Point> path = new ArrayList<>();
    private final String ROAD_LAYOUT_FILE = "src/roads.txt";

    public GamePanel() {
        setPreferredSize(new Dimension(GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE));
        setBackground(Color.WHITE);
        setFocusable(true);
        addMouseListener(this);
        Timer timer = new Timer(100, this);
        timer.start();
        spawnPlayerRandomly();
        loadRoadsFromFile();
    }

    private void spawnPlayerRandomly() {
        Random rand = new Random();
        playerX = rand.nextInt(GRID_WIDTH);
        playerY = rand.nextInt(GRID_HEIGHT);
    }

    private void loadRoadsFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(ROAD_LAYOUT_FILE))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                for (int col = 0; col < line.length(); col++) {
                    char tileType = line.charAt(col);
                    if (tileType == 'H') {
                        horizontalRoads[col][row] = true;
                    } else if (tileType == 'V') {
                        verticalRoads[col][row] = true;
                    } else if (tileType == 'B') {
                        buildings[col][row] = true;
                    }
                }
                row++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isRoad(int x, int y) {
        return horizontalRoads[x][y] || verticalRoads[x][y];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);
        drawRoads(g);
        drawBuildings(g);
        drawDestinations(g);
        drawPlayer(g);
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

    private void drawRoads(Graphics g) {
        g.setColor(Color.GRAY);
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                if (horizontalRoads[i][j]) {
                    g.fillRect(i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
                if (verticalRoads[i][j]) {
                    g.fillRect(i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private void drawBuildings(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                if (buildings[i][j]) {
                    g.fillRect(i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private void drawDestinations(Graphics g) {
        g.setColor(Color.GREEN);
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                if (destinations[i][j]) {
                    g.fillRect(i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private void drawPlayer(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(playerX * TILE_SIZE, playerY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!path.isEmpty()) {
            Point nextStep = path.remove(0);
            playerX = nextStep.x;
            playerY = nextStep.y;
            // Clear the destination if the player reaches it
            if (destinations[playerX][playerY]) {
                destinations[playerX][playerY] = false;
            }
        }
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX() / TILE_SIZE;
        int y = e.getY() / TILE_SIZE;
        if (x < GRID_WIDTH && y < GRID_HEIGHT) {
            // Check if the clicked tile is a road tile
            if (isRoad(x, y)) {
                destinations[x][y] = true;
                calculateShortestPath(new Point(playerX, playerY), new Point(x, y));
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
                    int newDist = distances.get(currentPoint) + 1; // All edges have weight 1
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

        // Check horizontal roads
        if (x > 0 && horizontalRoads[x - 1][y]) neighbors.add(new Point(x - 1, y));
        if (x < GRID_WIDTH - 1 && horizontalRoads[x + 1][y]) neighbors.add(new Point(x + 1, y));
        // Check vertical roads
        if (y > 0 && verticalRoads[x][y - 1]) neighbors.add(new Point(x, y - 1));
        if (y < GRID_HEIGHT - 1 && verticalRoads[x][y + 1]) neighbors.add(new Point(x, y + 1));

        // Check transitions between horizontal and vertical roads
        if (horizontalRoads[x][y] && y > 0 && verticalRoads[x][y - 1]) neighbors.add(new Point(x, y - 1));
        if (horizontalRoads[x][y] && y < GRID_HEIGHT - 1 && verticalRoads[x][y + 1]) neighbors.add(new Point(x, y + 1));
        if (verticalRoads[x][y] && x > 0 && horizontalRoads[x - 1][y]) neighbors.add(new Point(x - 1, y));
        if (verticalRoads[x][y] && x < GRID_WIDTH - 1 && horizontalRoads[x + 1][y]) neighbors.add(new Point(x + 1, y));

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