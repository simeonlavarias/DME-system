package advancedFeatures;

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
		
	    while (true){
		// >>> Print some info on the current buffer content for debugging purposes.
		// >>> please look at the available methods in C_buffer

		System.out.println("C:mutex   Buffer size is "+ buffer.size());
		
		// if the buffer is not empty
			if (buffer.size() >= 2) {
				String[] next = buffer.getHighestPriorityRequest();
				n_host = next[0];
				n_port = Integer.parseInt(next[1]);

				// >>>  **** Granting the token
		    try{
				System.out.println("C:mutex   Granting token to " + n_host + ":" + n_port);
				s = new Socket(n_host, n_port);  // Connect to node to send token
				PrintWriter out = new PrintWriter(s.getOutputStream(), true);
				out.println("TOKEN"); // Send token (just a signal)
				s.close();
			}
		    catch (IOException e) {
				System.out.println(e);
				System.out.println("CRASH Mutex connecting to the node for granting the TOKEN" + e);
		    }
			    
			    
		    //  >>>  **** Getting the token back
		    try{
				Socket returnSocket = ss_back.accept(); // BLOCKING: wait for token return
				System.out.println("C:mutex   Token received back from node.");
				returnSocket.close();
			}
		    catch (IOException e) {
				System.out.println(e);
				System.out.println("CRASH Mutex waiting for the TOKEN back" + e);
		    }
		}// endif	
	    }// endwhile
	}catch (Exception e) {System.out.print(e);}
	
   }
}
