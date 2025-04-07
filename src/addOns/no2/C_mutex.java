package addOns.no2;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class C_mutex extends Thread{
    C_buffer buffer;
    Socket   s;
    int      port;

    // ip address and port number of the node requesting the token.
    // They will be fetched from the buffer    
    String n_host;
    int    n_port;
	
    public C_mutex (C_buffer b, int p)	{
		buffer = b;
		port = p;
    }

    public void run(){
	try{ 
	    //  >>>  Listening from the server socket on port 7001
	    // from where the TOKEN will be returned later.
	    ServerSocket ss_back = new ServerSocket(7001);

		while (true) {
			System.out.println("C:mutex   Buffer size is " + buffer.size());

			String[] request = buffer.getHighestPriorityRequest();

			if (request != null) {
				n_host = request[0];
				n_port = Integer.parseInt(request[1]);

				// Grant token
				try {
					System.out.println("C:mutex   Granting token to " + n_host + ":" + n_port);
					s = new Socket(n_host, n_port);
					PrintWriter out = new PrintWriter(s.getOutputStream(), true);
					out.println("TOKEN");
					s.close();
				} catch (IOException e) {
					System.out.println("CRASH Mutex connecting to the node for granting the TOKEN " + e);
				}

				// Wait for token back
				try {
					Socket returnSocket = ss_back.accept();
					Logger.log("Node " + n_port + " returned token to coordinator.");
					System.out.println("C:mutex   Token received back from node.");
					returnSocket.close();
				} catch (IOException e) {
					System.out.println("CRASH Mutex waiting for the TOKEN back " + e);
				}
			}

			try {
				Thread.sleep(100); // avoid tight loop
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}catch (Exception e) {System.out.print(e);}
	
   }
}
