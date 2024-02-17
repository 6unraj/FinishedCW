import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientServer {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             Scanner serverScanner = new Scanner(socket.getInputStream());
             Scanner scanner = new Scanner(System.in);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.print("Enter your ID: ");
            String clientId = scanner.nextLine();
            writer.println(clientId);

            // Set up shutdown hook to handle Ctrl+C
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Ctrl+C detected. Sending quit message to the server.");
                writer.println("quit");
            }));

            Thread inputThread = new Thread(() -> {
                try {
                    while (true) {
                        String message = scanner.nextLine();
                        if (message.equalsIgnoreCase("/quit")) {
                            // Send the quit message to the server
                            writer.println("quit");
                            // Wait a bit to allow the quit message to be sent before exiting
                            Thread.sleep(100);
                            break;
                        } else if (message.startsWith("/private")) {
                            String[] parts = message.split(" ", 3);
                            if (parts.length == 3) {
                                writer.println(message);
                            } else {
                                System.out.println("Invalid private message format. Use /private recipientId message");
                            }
                        } else {
                            writer.println(message);
                        }
                    }
                } catch (Exception e) {
                    // Handle client exit
                }
            });
            inputThread.start();

            try {
                while (serverScanner.hasNextLine()) {
                    String serverMessage = serverScanner.nextLine();
                    System.out.println("Server: " + serverMessage);
                }
            } catch (Exception e) {
                // Handle client exit
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
