package addOns.no2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ShutdownListener extends Thread {
    private int port;

    public ShutdownListener(int nodePort) {
        this.port = nodePort + 100; // Dedicated shutdown port
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[ShutdownListener] Listening for shutdown on port " + port);
            Socket socket = serverSocket.accept();

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg = in.readLine();
            if ("SHUTDOWN".equals(msg)) {
                System.out.println("[ShutdownListener] Shutdown message received. Exiting node.");
                Logger.log("Node on port " + (port - 100) + " received shutdown broadcast. Exiting.");
                System.exit(0);
            }

            socket.close();
        } catch (Exception e) {
            System.out.println("[ShutdownListener] Error: " + e.getMessage());
        }
    }
}
