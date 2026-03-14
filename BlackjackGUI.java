import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

class Card {
    String suit;
    String rank;
    int value;
    Card(String suit, String rank, int value) {
        this.suit = suit;
        this.rank = rank;
        this.value = value;
    }
    public String toString() {
        return rank + " of " + suit;
    }
    public String getImageFileName() {
        String r = rank.toLowerCase();
        if (r.equals("j")) r = "j";
        if (r.equals("q")) r = "q";
        if (r.equals("k")) r = "k";
        if (r.equals("a")) r = "a";
        return r + "_of_" + suit.toLowerCase() + ".png";
    }
}

class Deck {
    private List<Card> cards = new ArrayList<>();
    private static final String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
    private static final String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    private static final Map<String, Integer> values = new HashMap<>();
    static {
        values.put("2", 2);
        values.put("3", 3);
        values.put("4", 4);
        values.put("5", 5);
        values.put("6", 6);
        values.put("7", 7);
        values.put("8", 8);
        values.put("9", 9);
        values.put("10", 10);
        values.put("J", 10);
        values.put("Q", 10);
        values.put("K", 10);
        values.put("A", 11);
    }
    Deck() {
        for (String suit : suits) {
            for (String rank : ranks) {
                cards.add(new Card(suit, rank, values.get(rank)));
            }
        }
        java.util.Collections.shuffle(cards);
    }
    Card deal() {
        return cards.remove(cards.size() - 1);
    }
}

class Hand {
    List<Card> cards = new ArrayList<>();
    void addCard(Card card) {
        cards.add(card);
    }
    int getValue() {
        int value = 0;
        int aces = 0;
        for (Card card : cards) {
            value += card.value;
            if (card.rank.equals("A")) aces++;
        }
        while (value > 21 && aces > 0) {
            value -= 10;
            aces--;
        }
        return value;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Card card : cards) sb.append(card.toString()).append(", ");
        return sb.toString();
    }
}

public class BlackjackGUI extends JFrame {
    private Deck deck;
    private Hand playerHand;
    private Hand dealerHand;
    private JPanel playerPanel, dealerPanel;
    private JLabel statusLabel;
    private JButton hitButton, standButton, restartButton;
    private JPanel bettingPanel;
    private JLabel balanceLabel, betLabel;
    private JButton bet10Button, bet50Button, bet100Button, splitButton, doubleDownButton;
    private int playerBalance = 1000;
    private int currentBet = 0;
    private boolean bettingOpen = true;
    private static final int CARD_WIDTH = 100;
    private static final int CARD_HEIGHT = 150;

    public BlackjackGUI() {
        setTitle("Blackjack");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Center panels for cards
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        dealerPanel = new JPanel();
        playerPanel = new JPanel();
        Color tableGreen = new Color(0, 102, 0);
        dealerPanel.setBackground(tableGreen);
        playerPanel.setBackground(tableGreen);
        centerPanel.setBackground(tableGreen);
        centerPanel.add(dealerPanel); // Dealer on top
        centerPanel.add(playerPanel); // Player on bottom
        add(centerPanel, BorderLayout.CENTER);

        // Status and balance
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        statusLabel = new JLabel();
        balanceLabel = new JLabel();
        betLabel = new JLabel();
        topPanel.add(statusLabel);
        topPanel.add(balanceLabel);
        add(topPanel, BorderLayout.NORTH);

        // Betting panel
        bettingPanel = new JPanel();
        bettingPanel.setBackground(tableGreen);
        bet10Button = new JButton("Bet $10");
        bet50Button = new JButton("Bet $50");
        bet100Button = new JButton("Bet $100");
        bettingPanel.add(new JLabel("Place your bet: "));
        bettingPanel.add(bet10Button);
        bettingPanel.add(bet50Button);
        bettingPanel.add(bet100Button);
        bettingPanel.add(betLabel);
        add(bettingPanel, BorderLayout.WEST);

        // Action buttons
        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");
        restartButton = new JButton("Restart");
        splitButton = new JButton("Split");
        doubleDownButton = new JButton("Double Down");
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(tableGreen);
        buttonPanel.add(hitButton);
        buttonPanel.add(standButton);
        buttonPanel.add(splitButton);
        buttonPanel.add(doubleDownButton);
        buttonPanel.add(restartButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Listeners
        hitButton.addActionListener(e -> hit());
        standButton.addActionListener(e -> stand());
        restartButton.addActionListener(e -> startGame());
        bet10Button.addActionListener(e -> placeBet(10));
        bet50Button.addActionListener(e -> placeBet(50));
        bet100Button.addActionListener(e -> placeBet(100));
        splitButton.addActionListener(e -> split());
        doubleDownButton.addActionListener(e -> doubleDown());

        startGame();
    }

    private void startGame() {
        deck = new Deck();
        playerHand = new Hand();
        dealerHand = new Hand();
        statusLabel.setText("");
        currentBet = 0;
        bettingOpen = true;
        updateBalanceLabel();
        updateBetLabel();
        enableBetting(true);
        updateUI(true);
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        splitButton.setEnabled(false);
        doubleDownButton.setEnabled(false);
    }

    private void updateUI(boolean hideDealer) {
        playerPanel.removeAll();
        dealerPanel.removeAll();
        playerPanel.add(new JLabel("Player Hand (" + playerHand.getValue() + "):"));
        for (Card card : playerHand.cards) {
            playerPanel.add(new JLabel(getCardIcon(card)));
        }
        dealerPanel.add(new JLabel("Dealer Hand:" + (hideDealer ? " ?" : " " + dealerHand.getValue())));
        for (int i = 0; i < dealerHand.cards.size(); i++) {
            if (i == 0 || !hideDealer) {
                dealerPanel.add(new JLabel(getCardIcon(dealerHand.cards.get(i))));
            } else {
                dealerPanel.add(new JLabel(getBackIcon()));
            }
        }
        playerPanel.revalidate();
        playerPanel.repaint();
        dealerPanel.revalidate();
        dealerPanel.repaint();
    }

    private void updateBalanceLabel() {
        balanceLabel.setText("Balance: $" + playerBalance);
    }

    private void updateBetLabel() {
        betLabel.setText("Current Bet: $" + currentBet);
    }

    private void enableBetting(boolean enable) {
        bet10Button.setEnabled(enable);
        bet50Button.setEnabled(enable);
        bet100Button.setEnabled(enable);
        bettingOpen = enable;
    }

    private void placeBet(int amount) {
        if (!bettingOpen) return;
        if (playerBalance < amount) {
            statusLabel.setText("Not enough balance to bet $" + amount);
            return;
        }
        currentBet = amount;
        playerBalance -= amount;
        updateBalanceLabel();
        updateBetLabel();
        enableBetting(false);
        // Deal cards and enable actions
        playerHand = new Hand();
        dealerHand = new Hand();
        playerHand.addCard(deck.deal());
        playerHand.addCard(deck.deal());
        dealerHand.addCard(deck.deal());
        dealerHand.addCard(deck.deal());
        updateUI(true);
        hitButton.setEnabled(true);
        standButton.setEnabled(true);
        // Enable split if two cards of same rank
        splitButton.setEnabled(playerHand.cards.size() == 2 && playerHand.cards.get(0).rank.equals(playerHand.cards.get(1).rank));
        doubleDownButton.setEnabled(true);
        statusLabel.setText("");
    }

    private ImageIcon getCardIcon(Card card) {
        String path = "cards/" + card.getImageFileName();
        File file = new File(path);
        if (!file.exists()) {
            generateCardImage(card, path);
        }
        try {
            BufferedImage img = ImageIO.read(new File(path));
            return new ImageIcon(img.getScaledInstance(CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH));
        } catch (IOException e) {
            return new ImageIcon();
        }
    }

    private ImageIcon getBackIcon() {
        String path = "cards/back.png";
        File file = new File(path);
        if (!file.exists()) {
            generateBackImage(path);
        }
        try {
            BufferedImage img = ImageIO.read(new File(path));
            return new ImageIcon(img.getScaledInstance(CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH));
        } catch (IOException e) {
            return new ImageIcon();
        }
    }

    private void generateCardImage(Card card, String path) {
        BufferedImage img = new BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, CARD_WIDTH - 1, CARD_HEIGHT - 1);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String rank = card.rank;
        String suit = card.suit;
        String suitSymbol = getSuitSymbol(suit);
        g.drawString(rank, 10, 30);
        g.drawString(suitSymbol, 10, 60);
        g.drawString(rank, CARD_WIDTH - 30, CARD_HEIGHT - 10);
        g.drawString(suitSymbol, CARD_WIDTH - 30, CARD_HEIGHT - 40);
        g.setFont(new Font("Arial", Font.PLAIN, 60));
        g.drawString(suitSymbol, CARD_WIDTH / 2 - 20, CARD_HEIGHT / 2 + 20);
        g.dispose();
        try {
            ImageIO.write(img, "png", new File(path));
        } catch (IOException e) {
            // ignore
        }
    }

    private void generateBackImage(String path) {
        BufferedImage img = new BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT);
        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("BJ", CARD_WIDTH / 2 - 30, CARD_HEIGHT / 2 + 15);
        g.dispose();
        try {
            ImageIO.write(img, "png", new File(path));
        } catch (IOException e) {
            // ignore
        }
    }

    private String getSuitSymbol(String suit) {
        switch (suit) {
            case "Hearts": return "♥";
            case "Diamonds": return "♦";
            case "Clubs": return "♣";
            case "Spades": return "♠";
            default: return "?";
        }
    }

    private void hit() {
        playerHand.addCard(deck.deal());
        updateUI(true);
        if (playerHand.getValue() > 21) {
            statusLabel.setText("Player busts! Dealer wins.");
            hitButton.setEnabled(false);
            standButton.setEnabled(false);
            splitButton.setEnabled(false);
            doubleDownButton.setEnabled(false);
            updateUI(false);
        }
    }

    private void stand() {
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        splitButton.setEnabled(false);
        doubleDownButton.setEnabled(false);
        while (dealerHand.getValue() < 17) {
            dealerHand.addCard(deck.deal());
        }
        updateUI(false);
        int playerValue = playerHand.getValue();
        int dealerValue = dealerHand.getValue();
        if (dealerValue > 21) {
            statusLabel.setText("Dealer busts! Player wins.");
            playerBalance += currentBet * 2;
        } else if (dealerValue > playerValue) {
            statusLabel.setText("Dealer wins.");
        } else if (dealerValue < playerValue) {
            statusLabel.setText("Player wins!");
            playerBalance += currentBet * 2;
        } else {
            statusLabel.setText("Push (Tie).");
            playerBalance += currentBet;
        }
        updateBalanceLabel();
    }

    private void split() {
        // Placeholder for split logic
        statusLabel.setText("Split not yet implemented.");
    }

    private void doubleDown() {
        if (playerBalance < currentBet) {
            statusLabel.setText("Not enough balance to double down.");
            return;
        }
        playerBalance -= currentBet;
        currentBet *= 2;
        updateBalanceLabel();
        updateBetLabel();
        hit();
        if (playerHand.getValue() <= 21) {
            stand();
        }
        doubleDownButton.setEnabled(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BlackjackGUI().setVisible(true));
    }
}
