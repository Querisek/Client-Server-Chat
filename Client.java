import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private String username;
    private PrintWriter writer;
    private BufferedReader reader;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private Socket socket;
    private boolean connected;

    public Client(String username, String serverAddress, int port) {
        super("Chat - " + username);
        this.username = username;

        try {
            socket = new Socket(serverAddress, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(username);
            connected = true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error while connecting to a server: " + e);
            System.exit(1);
        }

       if(connected)
       {
	        chatArea = new JTextArea(20, 40);
	        chatArea.setEditable(false);
	        JScrollPane chatScroll = new JScrollPane(chatArea);
	        inputField = new JTextField(30);
	        sendButton = new JButton("Send");
	        sendButton.addActionListener(this);
	        inputField.addActionListener(this);
	        JPanel inputPanel = new JPanel();
	        inputPanel.add(inputField);
	        inputPanel.add(sendButton);
	        getContentPane().add(chatScroll, BorderLayout.CENTER);
	        getContentPane().add(inputPanel, BorderLayout.SOUTH);
	        pack();
	        setVisible(true);

        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        String message = reader.readLine();
                        if (message == null) {
                            JOptionPane.showMessageDialog(null, "Connection was closed.");
                            connected = false;
                            System.exit(0);
                        }
                        chatArea.append(message + "\n");
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error while reading from a server: " + e);
                    System.exit(1);
                }
            }
        });
        connected = true;
        thread.start();
       }
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                	connected = false;
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                dispose();
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            writer.println(message);
            chatArea.append("You: " + message + "\n");
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        String username = JOptionPane.showInputDialog("Enter your username:");
        if(username == null) {
        	JOptionPane.showMessageDialog(null, "Username has not been entered");
        	System.exit(1);
        }
        Client client = new Client(username, "localhost", 4444);
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}