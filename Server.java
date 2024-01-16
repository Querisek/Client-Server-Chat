import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JOptionPane;

public class Server {

    private static final int port = 4444;
    private static ArrayList<ClientHandler> clients = new ArrayList<>();

    private static class ClientHandler extends Thread {
        private Socket socket;
        private String username;
        private PrintWriter writer;
        private BufferedReader reader;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                username = reader.readLine();
                System.out.println("New user: " + username);
                writer.println("Hello " + username + ".\n");

                for (ClientHandler client : clients) {
                    if (client != this) {
                        client.writer.println(username + " has joined the chat.");
                    }
                }

                while(true) {
                    String message = reader.readLine();
                    if (message == null) {
                        System.out.println(username + " has left the chat.");
                        writer.close();
                        reader.close();
                        socket.close();
                        clients.remove(this);
                        for (ClientHandler client : clients) {
                            client.writer.println(username + " has left the chat.");
                        }
                        break;
                    } else {
                        for (ClientHandler client : clients) {
                            if (client != this) {
                                client.writer.println(username + ": " + message);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Client error has occurred: " + e);
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Chat works on port: " + port);

        while(true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler client = new ClientHandler(clientSocket);
            clients.add(client);
            client.start();
        }
    }
}