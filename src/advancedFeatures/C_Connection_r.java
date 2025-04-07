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
		System.out.println("C:connection IN  dealing with request from socket " + s);
		try {
			// read request from node
			in = s.getInputStream();
			bin = new BufferedReader(new InputStreamReader(in));

			String ip = bin.readLine();        // Read IP
			String port = bin.readLine();      // Read Port
			String priority = port.equals("6001") ? "high" : "normal"; // Assign priority

			// ✅ Save to buffer using new method
			buffer.saveRequest(ip, port, priority);

			// ✅ Log with priority
			Logger.log("Coordinator received token request from " + ip + ":" + port + " with priority " + priority);

			s.close();
			System.out.println("C:connection OUT    received and recorded request from " + ip + ":" + port + "  (socket closed)");

		} catch (IOException e) {
			System.out.println(e);
			System.exit(1);
		}

		// Show current buffer state
		buffer.show();
	}
}
