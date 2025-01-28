import javax.swing.*;

public class PizzaDeliveryGame extends JFrame {
    public PizzaDeliveryGame() {
        setTitle("Pizza Delivery Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        pack();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PizzaDeliveryGame game = new PizzaDeliveryGame();
            game.setVisible(true);
        });
    }
}