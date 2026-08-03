package game.ui;

import game.save.GameSnapshot;
import game.save.SaveGame;
import game.save.SaveManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenuUI extends JFrame {

    private JPanel backgroundPanel;
    private final SaveManager saveManager = new SaveManager();

    public MainMenuUI() {
        setTitle("BurgerCity - Főmenü");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(true);

        
        backgroundPanel = new JPanel() {
            private Image backgroundImage;

            {
                try {
                    var backgroundUrl = MainMenuUI.class.getResource("/game/assets/main_menu_bg.png");
                    if (backgroundUrl == null) {
                        throw new IllegalStateException("Resource not found on classpath: /game/assets/main_menu_bg.png");
                    }
                    backgroundImage = new ImageIcon(backgroundUrl).getImage();
                } catch (Exception e) {
                    System.err.println("A háttérkép betöltése nem sikerült: " + e.getMessage());
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    
                    Graphics2D g2d = (Graphics2D) g;
                    GradientPaint gradient = new GradientPaint(
                            0, 0, new Color(34, 193, 195),
                            0, getHeight(), new Color(253, 187, 45)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        backgroundPanel.setLayout(new GridBagLayout());

        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        
        JButton newGameButton = createStyledButton("Új játék indítása");
        JButton loadGameButton = createStyledButton("Mentett játék indítása");

        
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startNewGame();
            }
        });

        loadGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    java.util.List<SaveGame> saves = saveManager.listSaves();
                    if (saves.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                MainMenuUI.this,
                                "Nincs elérhető mentés.",
                                "Betöltés",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        return;
                    }

                    SaveGame selected = (SaveGame) JOptionPane.showInputDialog(
                            MainMenuUI.this,
                            "Válassz mentést:",
                            "Mentett játék indítása",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            saves.toArray(),
                            saves.get(0)
                    );

                    if (selected == null) return;

                    GameSnapshot snapshot = saveManager.loadSnapshot(selected);
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        GameUI gameUI = new GameUI(snapshot);
                        gameUI.setVisible(true);
                    });

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            MainMenuUI.this,
                            "Betöltés sikertelen: " + ex.getMessage(),
                            "Hiba",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });


        
        buttonPanel.add(newGameButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(loadGameButton);

        
        backgroundPanel.add(buttonPanel);

        add(backgroundPanel);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(300, 60));
        button.setMaximumSize(new Dimension(300, 60));
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        
        button.setBackground(new Color(52, 152, 219));

        
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                JButton btn = (JButton) c;
                
                
                Color topColor, bottomColor;
                if (btn.getModel().isPressed()) {
                    topColor = new Color(41, 128, 185);
                    bottomColor = new Color(26, 82, 118);
                } else if (btn.getModel().isRollover()) {
                    topColor = new Color(74, 177, 241);
                    bottomColor = new Color(52, 152, 219);
                } else {
                    topColor = new Color(52, 152, 219);
                    bottomColor = new Color(41, 128, 185);
                }

                
                GradientPaint gradient = new GradientPaint(0, 0, topColor, 0, btn.getHeight(), bottomColor);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, btn.getWidth(), btn.getHeight(), 15, 15);

                
                g2d.setColor(new Color(30, 100, 150));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, btn.getWidth() - 2, btn.getHeight() - 2, 15, 15);

                
                g2d.setColor(btn.getForeground());
                g2d.setFont(btn.getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (btn.getWidth() - fm.stringWidth(btn.getText())) / 2;
                int textY = (btn.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(btn.getText(), textX, textY);

                g2d.dispose();
            }
        });

        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void toggleFullscreen() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        
        if (device.getFullScreenWindow() == this) {
            
            device.setFullScreenWindow(null);
            setResizable(true);
        } else {
            
            setResizable(false);
            dispose();
            setUndecorated(true);
            device.setFullScreenWindow(this);
            setVisible(true);
        }
    }

    private void startNewGame() {
        String gameName = JOptionPane.showInputDialog(
                this,
                "Add meg a játékmenet nevét:",
                "Új játék",
                JOptionPane.PLAIN_MESSAGE
        );

        if (gameName != null && !gameName.trim().isEmpty()) {
            
            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (device.getFullScreenWindow() == this) {
                device.setFullScreenWindow(null);
            }
            
            
            dispose();
            SwingUtilities.invokeLater(() -> {
                GameUI gameUI = new GameUI(gameName);
                gameUI.setVisible(true);
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainMenuUI mainMenu = new MainMenuUI();
            mainMenu.setVisible(true);
        });
    }
}
