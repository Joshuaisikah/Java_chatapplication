package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;

public class ChatClient {
    private String hostname;
    private int port;
    private String userName;

    private JPanel messagePanel; // Panel for individual messages
    private JTextArea chatHistory; // For logging chat history
    private JTextField textField;
    private JButton sendButton;
    private PrintWriter writer;

    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

  public void execute() {
    JFrame frame = new JFrame("Chat Application");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(600, 400);
    frame.getContentPane().setBackground(Color.GREEN);
    ((JComponent) frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(72, 72, 72, 72));
chatHistory = new JTextArea("------------------------------------------------------welcome to the chat room\n");
chatHistory.setFont(new Font("Arial", Font.PLAIN, 20));

chatHistory.setEditable(false);
JScrollPane historyScrollPane = new JScrollPane(chatHistory);
historyScrollPane.getViewport().setBackground(Color.WHITE); // Ensure the chat history background is white

messagePanel = new JPanel();
messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
messagePanel.setBackground(Color.WHITE); // Set the message panel background to white
JScrollPane messageScrollPane = new JScrollPane(messagePanel);
messageScrollPane.getViewport().setBackground(new Color(0, 100, 0)); // Ensure the viewport background is white

textField = new JTextField(80);
textField.setFont(new Font("Arial", Font.PLAIN, 20));
textField.setPreferredSize(new Dimension(100, 40));
sendButton = new JButton("Send");
sendButton.setPreferredSize(new Dimension(100, 40));

// Adjusting the layout for the text field and send button
JPanel southPanel = new JPanel(new BorderLayout()); // Use BorderLayout for better control
southPanel.setBackground(Color.BLACK); // Set the south panel background to black
JPanel inputPanel = new JPanel(); // This panel will contain the text field and send button
inputPanel.setLayout(new FlowLayout()); // Use FlowLayout for the input components
inputPanel.setBackground(Color.BLACK); // Set the input panel background to black
inputPanel.add(textField);
inputPanel.add(sendButton);

// Create a panel that will act as a spacer to effectively raise the inputPanel
JPanel spacerPanel = new JPanel();
spacerPanel.setLayout(new BoxLayout(spacerPanel, BoxLayout.Y_AXIS));
Component verticalStrut = Box.createRigidArea(new Dimension(10, 0)); // Spacer to raise the inputPanel
spacerPanel.add(verticalStrut);
spacerPanel.add(inputPanel);

southPanel.add(spacerPanel, BorderLayout.NORTH); // Add the spacerPanel to the north to raise the input components

// Add components to the frame
frame.getContentPane().add(BorderLayout.NORTH, historyScrollPane);
frame.getContentPane().add(BorderLayout.CENTER, messageScrollPane);
frame.getContentPane().add(BorderLayout.SOUTH, southPanel);

frame.setVisible(true);
    
    
    // Additional code remains unchanged


        sendButton.addActionListener(e -> {
            String message = textField.getText();
            if (!message.isEmpty()) {
                displayMessage(userName + ": " + message, true); // true indicates message is sent by the user
                writer.println(userName + ": " + message);
                textField.setText("");
            }
        });

        try {
            Socket socket = new Socket(hostname, port);
            displayMessage("Connected to the chat server", true);

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            userName = JOptionPane.showInputDialog(frame, "Enter your name:");
            writer.println(userName);

            new Thread(() -> {
                try {
                    String response;
                    while ((response = reader.readLine()) != null) {
                        String finalResponse = response;
                        SwingUtilities.invokeLater(() -> {
                            boolean isSentByUser = finalResponse.startsWith(userName + ":");
                            displayMessage(finalResponse, isSentByUser);
                        });
                    }
                } catch (IOException ex) {
                    displayMessage("Error reading from server: " + ex.getMessage(), true);
                }
            }).start();

        } catch (UnknownHostException ex) {
            displayMessage("Server not found: " + ex.getMessage(), true);
        } catch (IOException ex) {
            displayMessage("I/O Error: " + ex.getMessage(), true);
        }
    }

    private void displayMessage(String message, boolean isSentByUser) {
    JPanel messageCard = new JPanel(new BorderLayout());
    messageCard.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    JLabel messageLabel = new JLabel("<html><body style='width: 200px'>" + message + "</body></html>");
    
    if (isSentByUser) {
        messageCard.add(messageLabel, BorderLayout.EAST);
    } else {
        messageCard.add(messageLabel, BorderLayout.WEST);
    }
    
    messagePanel.add(messageCard);
    messagePanel.revalidate();
    messagePanel.repaint();
    // Scroll to the bottom to ensure the latest message is visible
    JScrollBar verticalBar = ((JScrollPane) messagePanel.getParent().getParent()).getVerticalScrollBar();
    verticalBar.setValue(verticalBar.getMaximum());
}
    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        ChatClient client = new ChatClient(hostname, port);
        client.execute();
    }

    public String getUserName() {
        return this.userName;
    }

    public JTextField getTextField() {
        return this.textField;
    }

    public JButton getSendButton() {
        return this.sendButton;
    }
}