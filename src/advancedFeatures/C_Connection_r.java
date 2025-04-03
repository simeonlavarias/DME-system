package advancedFeatures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
// Reacts to a node request.
// Receives and records the node request in the buffer.
//
public class C_Connection_r extends Thread{
	
    // class variables
    C_buffer buffer;
    Socket 	   s;
    InputStream    in;
    BufferedReader bin;
       	
    public C_Connection_r(Socket s, C_buffer b){
    	this.s = s;
    	this.buffer = b;
    }

	public void run() {
		final int NODE = 0;
		final int PORT = 1;

		String[] request = new String[2];

		System.out.println("C:connection IN  dealing with request from socket " + s);
		try {
			// read request from node
			in = s.getInputStream();
			bin = new BufferedReader(new InputStreamReader(in));

			request[NODE] = bin.readLine();
			request[PORT] = bin.readLine();
			request[2] = request[PORT].equals("6001") ? "high" : "normal";  // Only node 6001 is prioritized
			buffer.saveRequest(request);

			// Log the token request with timestamp
			Logger.log("Coordinator received token request from " + request[NODE] + ":" + request[PORT]);

			s.close();
			System.out.println("C:connection OUT    received and recorded request from " + request[NODE] + ":" + request[PORT] + "  (socket closed)");

		} catch (IOException e) {
			System.out.println(e);
			System.exit(1);
		}

		buffer.show();
	}

}
