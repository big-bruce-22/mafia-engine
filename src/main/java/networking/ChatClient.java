package networking;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Server IP: ");
        String serverIp = scanner.nextLine();

        System.out.print("Your name: ");
        String name = scanner.nextLine();

        Socket socket = new Socket(serverIp, 443);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );
        PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true
        );

        // Receiver
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println(msg);
                }
            } catch (IOException ignored) {}
        }).start();

        // Sender
        while (true) {
            String message = scanner.nextLine();
            out.println(name + ": " + message);
        }
    }
}
