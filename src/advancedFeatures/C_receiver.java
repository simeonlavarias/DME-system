package advancedFeatures;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class C_receiver extends Thread{
    
    private C_buffer buffer;
    private int 		port;
    private ServerSocket 	s_socket; 
    private Socket		socketFromNode;
    private C_Connection_r connect;
    
    public C_receiver (C_buffer b, int p){
		buffer = b;
		port = p;
    }

	public void run () {
		try {
			s_socket = new ServerSocket(port);  // Create server socket
			System.out.println("C:receiver    Coordinator listening on port " + port);
		} catch (IOException e) {
			System.out.println("Exception creating server socket: " + e);
			return;
		}

		while (true) {
			try {
				socketFromNode = s_socket.accept();  // Accept node connection
				System.out.println("C:receiver    Coordinator has received a request ...");

				connect = new C_Connection_r(socketFromNode, buffer); // Spawn new thread
				connect.start();
			}
			catch (IOException e) {
				System.out.println("Exception when creating a connection: " + e);
			}
		}
	}
}
