import java.util.*;

public class PizzaDelivery {
    private static final int NUM_LOCATIONS = 5;
    private static int[][] distances = new int[NUM_LOCATIONS][NUM_LOCATIONS];
    private static boolean[][] obstacles = new boolean[NUM_LOCATIONS][NUM_LOCATIONS];
    private static int currentLocation;

    public static void main(String[] args) {
        System.out.println("Welcome to the Pizza Delivery Game!");
        initializeGame();
    }

    // Initializes the game by generating random data and displaying the initial map and location
    private static void initializeGame() {
        generateRandomData();
        displayMap();
        showCurrentLocation();
        displayMenu();
    }

    // Generates random distances and obstacles between locations
    private static void generateRandomData() {
        Random rand = new Random();
        for (int i = 0; i < NUM_LOCATIONS; i++) {
            for (int j = i + 1; j < NUM_LOCATIONS; j++) {
                int distance = rand.nextInt(91) + 10; // Distance between 10 and 100 meters
                distances[i][j] = distance;
                distances[j][i] = distance;
                // Randomly place obstacles
                obstacles[i][j] = rand.nextBoolean();
                obstacles[j][i] = obstacles[i][j];
            }
        }
        currentLocation = rand.nextInt(NUM_LOCATIONS);
    }

    // Displays the map with distances and obstacles
    private static void displayMap() {
        System.out.println("Map with distances (in meters) and obstacles:");
        for (int i = 0; i < NUM_LOCATIONS; i++) {
            for (int j = 0; j < NUM_LOCATIONS; j++) {
                if (i == j) {
                    System.out.print(" 0 ");
                } else if (obstacles[i][j]) {
                    System.out.print(" X "); // X represents an obstacle
                } else {
                    System.out.print(distances[i][j] + " ");
                }
            }
            System.out.println();
        }
    }

    // Shows the current location of the pizza delivery person
    private static void showCurrentLocation() {
        System.out.println("Your current location is: " + currentLocation);
    }

    // Displays the main menu and handles user input
    private static void displayMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Show Map");
            System.out.println("2. Show Current Location");
            System.out.println("3. Select Destinations");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            int option = scanner.nextInt();

            switch (option) {
                case 1:
                    displayMap();
                    break;
                case 2:
                    showCurrentLocation();
                    break;
                case 3:
                    selectDestinations(scanner);
                    break;
                case 4:
                    System.out.println("Exiting the game. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Allows the user to select multiple destinations for delivery
    private static void selectDestinations(Scanner scanner) {
        System.out.println("Enter the number of destinations you want to visit:");
        int numDestinations = scanner.nextInt();
        List<Integer> destinations = new ArrayList<>();
        for (int i = 0; i < numDestinations; i++) {
            System.out.println("Select destination " + (i + 1) + " (location index 0 to " + (NUM_LOCATIONS - 1) + "): ");
            int destination = scanner.nextInt();
            if (destination < 0 || destination >= NUM_LOCATIONS) {
                System.out.println("Invalid destination. Please try again.");
                i--; // Retry this iteration
            } else {
                destinations.add(destination);
            }
        }
        calculateOptimalRoute(destinations);
    }

    // Calculates the optimal route to visit all selected destinations
    private static void calculateOptimalRoute(List<Integer> destinations) {
        // Initialize variables to store the best route and its distance
        List<Integer> bestRoute = new ArrayList<>();
        int bestDistance = Integer.MAX_VALUE;

        // Generate all permutations of the destinations to find the shortest route
        List<List<Integer>> allPermutations = new ArrayList<>();
        generatePermutations(destinations, 0, allPermutations);

        for (List<Integer> permutation : allPermutations) {
            int distance = 0;
            int previousLocation = currentLocation;

            for (int destination : permutation) {
                if (obstacles[previousLocation][destination]) {
                    distance = Integer.MAX_VALUE;
                    break;
                }
                distance += distances[previousLocation][destination];
                previousLocation = destination;
            }

            if (distance < bestDistance) {
                bestDistance = distance;
                bestRoute = new ArrayList<>(permutation);
            }
        }

        // Display the best route and its distance
        if (bestDistance == Integer.MAX_VALUE) {
            System.out.println("There is no valid path to visit all destinations due to obstacles.");
        } else {
            System.out.println("Optimal Route:");
            System.out.print(currentLocation + " -> ");
            for (int destination : bestRoute) {
                System.out.print(destination + " -> ");
            }
            System.out.println("End");
            System.out.println("Total distance: " + bestDistance + " meters.");
            double timeInHours = bestDistance / 60.0;
            System.out.println("Total time needed: " + timeInHours + " hours.");
        }
    }

    // Helper method to generate all permutations of a list
    private static void generatePermutations(List<Integer> list, int index, List<List<Integer>> result) {
        if (index == list.size() - 1) {
            result.add(new ArrayList<>(list));
            return;
        }

        for (int i = index; i < list.size(); i++) {
            Collections.swap(list, i, index);
            generatePermutations(list, index + 1, result);
            Collections.swap(list, i, index);
        }
    }
}

class Node implements Comparable<Node> {
    int index;
    int distance;

    Node(int index, int distance) {
        this.index = index;
        this.distance = distance;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.distance, other.distance);
    }
}