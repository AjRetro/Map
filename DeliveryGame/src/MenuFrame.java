import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class MenuFrame extends JFrame {
    private Image cursorImage;

    private void loadSprites() {
    try {
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
    public MenuFrame() {
        loadSprites();
        setTitle("Crust Cross");
        setSize(800, 600); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

      
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        panel.setBackground(new Color(255, 228, 196)); 

       
        JLabel titleLabel = new JLabel("Crust Cross");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(139, 69, 19)); 
        panel.add(titleLabel);

        
        panel.add(Box.createRigidArea(new Dimension(0, 50)));

        // Add Play button
        JButton playButton = new JButton("Play");
        playButton.setFont(new Font("Arial", Font.BOLD, 50  ));
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.setBackground(new Color(34, 139, 34)); // Green background color
        playButton.setForeground(Color.WHITE); // White text color
        playButton.setFocusPainted(false);
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the game frame
                PizzaDeliveryGame game = new PizzaDeliveryGame();
                game.setVisible(true);
                // Close the menu frame
                dispose();
            }
        });
        panel.add(playButton);

        // Add some space
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Add Close button
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 24));
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.setBackground(new Color(178, 34, 34)); // Red background color
        closeButton.setForeground(Color.WHITE); // White text color
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Close the application
                System.exit(0);
            }
        });
        panel.add(closeButton);

        // Add the panel to the frame
        add(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MenuFrame menu = new MenuFrame();
            menu.setVisible(true);
        });
    }
}