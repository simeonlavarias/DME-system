package advancedFeatures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class Node{

    private Random ra;
    private Socket	s;
    private PrintWriter pout = null;
    private ServerSocket n_ss;
    private Socket	n_token;
    String	c_host = "127.0.0.1";
    int 	c_request_port = 7000;
    int 	c_return_port = 7001;
    String	n_host = "127.0.0.1";
    String 	n_host_name;
    int     n_port;
    
    public Node(String nam, int por, int sec){
		ra = new Random();
		n_host_name = nam;
		n_port = por;
	
    	System.out.println("Node " +n_host_name+ ":" +n_port+ " of DME is active ....");

    	// NODE sends n_host and n_port  through a socket s to the coordinator
    	// c_host:c_req_port
    	// and immediately opens a server socket through which will receive 
    	// a TOKEN (actually just a synchronization).

		while (true) {
			// 1. Sleep before requesting token
			try {
				int sleepTime = ra.nextInt(sec * 1000);  // Random delay in ms
				System.out.println("[" + n_port + "] Sleeping for " + sleepTime + " ms before requesting token...");
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// 2. Token request and critical section
			try {
				// Send token request
				System.out.println("[" + n_port + "] Sending request to coordinator at " + c_host + ":" + c_request_port);
				s = new Socket(c_host, c_request_port);
				pout = new PrintWriter(s.getOutputStream(), true);
				pout.println(n_host); // Send IP
				pout.println(n_port); // Send Port
				s.close();

				// Wait for token from coordinator
				System.out.println("[" + n_port + "] Waiting for token...");
				n_ss = new ServerSocket(n_port);              // Listen on this node's port
				n_token = n_ss.accept();                      // Blocking call: wait for coordinator
				BufferedReader bin = new BufferedReader(new InputStreamReader(n_token.getInputStream()));
				String msg = bin.readLine();                  // Expecting "TOKEN" from coordinator

				if ("TOKEN".equals(msg)) {
					System.out.println("[" + n_port + "] Token received! Entering critical section...");

					// Critical Section
					int criticalTime = ra.nextInt(3000) + 2000; // 2–5 seconds

					// Log before Thread.sleep(...)
					Logger.log("Node " + n_port + " entered critical section.");

					Thread.sleep(criticalTime);
					System.out.println("[" + n_port + "] Exiting critical section after " + criticalTime + " ms");

					// Close server sockets
					n_token.close();
					n_ss.close();

					// Return the token
					try {
						Socket returnSocket = new Socket(c_host, c_return_port);
						PrintWriter retOut = new PrintWriter(returnSocket.getOutputStream(), true);
						retOut.println("RETURNING TOKEN");
						returnSocket.close();

						// Log after token is returned
						Logger.log("Node " + n_port + " returned token to coordinator.");

						System.out.println("[" + n_port + "] Token returned to coordinator.");
					} catch (IOException e) {
						System.out.println("[" + n_port + "] ERROR returning token: " + e);
					}
				}
			} catch (IOException | InterruptedException e) {
				System.out.println(e);
				System.exit(1);
			}
		}
    }
    
    public static void main (String args[]){
		String n_host_name = ""; 
		int n_port;
		
		// port and millisec (average waiting time) are specific of a node
		if ((args.length < 1) || (args.length > 2)){
		    System.out.print("Usage: Node [port number] [millisecs]");
		    System.exit(1);
		}
		
		// get the IP address and the port number of the node
	 	try{ 
		    InetAddress n_inet_address =  InetAddress.getLocalHost() ;
		    n_host_name = n_inet_address.getHostName();
		    System.out.println ("node hostname is " +n_host_name+":"+n_inet_address);
	    	}
	    	catch (UnknownHostException e){
		    System.out.println(e);
		    System.exit(1);
	    	} 
		
		n_port = Integer.parseInt(args[0]);
		System.out.println ("node port is "+n_port);
	    Node n = new Node(n_host_name, n_port, Integer.parseInt(args[1]));
    }
    
    
}
