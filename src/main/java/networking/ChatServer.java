package networking;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    private static final Set<ClientHandler> clients =
            Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Chat server started on port 5000");

        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket);
            clients.add(handler);
            new Thread(handler).start();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;

        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }

        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    broadcast(msg);
                }
            } catch (IOException ignored) {
            } finally {
                clients.remove(this);
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        void broadcast(String msg) {
            synchronized (clients) {
                for (ClientHandler c : clients) {
                    c.out.println(msg);
                }
            }
        }
    }
}
