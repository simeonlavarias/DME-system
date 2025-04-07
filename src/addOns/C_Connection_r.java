package addOns;

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

			String line = bin.readLine();

			// ✅ Handle shutdown request
			if ("SHUTDOWN".equals(line)) {
				Logger.log("Coordinator received shutdown signal. Initiating graceful shutdown.");
				System.out.println("C: SHUTDOWN signal received from node.");

				// ✅ Final shutdown log
				Logger.log("System shutdown complete.");

				s.close();
				System.exit(0);  // ✅ Graceful coordinator shutdown
				return;
			}

			// ✅ Continue with normal request
			String ip = line;
			String port = bin.readLine();
			String priority = port.equals("6001") ? "high" : "normal"; // Assign priority

			buffer.saveRequest(ip, port, priority);

			Logger.log("Coordinator received token request from " + ip + ":" + port + " with priority " + priority);
			s.close();
			System.out.println("C:connection OUT    received and recorded request from " + ip + ":" + port + "  (socket closed)");
		} catch (IOException e) {
			System.out.println(e);
			System.exit(1);
		}

		buffer.show();
	}
}
