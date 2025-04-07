package addOns.no2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class Node{
	private int requestCount = 0; // Track how many times this node requested the token
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

		new ShutdownListener(n_port).start();

		while (true) {
			// 1. Sleep before requesting token
			try {
				int sleepTime = ra.nextInt(sec * 1000);  // Random delay in ms
				System.out.println("[" + n_port + "] Sleeping for " + sleepTime + " ms before requesting token...");
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// 2. Token request with retry logic
			while (true) {
				try {
					System.out.println("[" + n_port + "] Sending request to coordinator at " + c_host + ":" + c_request_port);
					s = new Socket(c_host, c_request_port);
					pout = new PrintWriter(s.getOutputStream(), true);
					pout.println(n_host); // Send IP
					pout.println(n_port); // Send Port
					s.close();
					break; // ✅ Successfully sent request — break retry loop
				} catch (IOException e) {
					System.out.println("[" + n_port + "] Coordinator is down or unreachable. Retrying in 2 seconds...");
					try {
						Thread.sleep(2000); // Wait before retrying
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}

// ✅ Now proceed to wait for token as usual
			try {
				System.out.println("[" + n_port + "] Waiting for token...");
				n_ss = new ServerSocket(n_port);
				n_token = n_ss.accept();
				BufferedReader bin = new BufferedReader(new InputStreamReader(n_token.getInputStream()));
				String msg = bin.readLine();

				if ("SHUTDOWN".equals(msg)) {
					System.out.println("[" + n_port + "] Shutdown signal received. Terminating node.");
					Logger.log("Node " + n_port + " received shutdown signal. Shutting down.");
					n_token.close();
					n_ss.close();
					System.exit(0);  // ✅ Node shuts down gracefully
				} else if ("TOKEN".equals(msg)) {
					System.out.println("[" + n_port + "] Token received! Entering critical section...");

					int criticalTime = ra.nextInt(3000) + 2000;
					Logger.log("Node " + n_port + " entered critical section.");
					Thread.sleep(criticalTime);
					System.out.println("[" + n_port + "] Exiting critical section after " + criticalTime + " ms");

					n_token.close();
					n_ss.close();

					try {
						Socket returnSocket = new Socket(c_host, c_return_port);
						PrintWriter retOut = new PrintWriter(returnSocket.getOutputStream(), true);
						retOut.println("RETURNING TOKEN");
						returnSocket.close();
						Logger.log("Node " + n_port + " returned token to coordinator.");
						System.out.println("[" + n_port + "] Token returned to coordinator.");

						requestCount++;
						if (n_port == 6001 && requestCount == 5) {
							try {
								Socket shutdownSocket = new Socket(c_host, c_request_port);
								PrintWriter out = new PrintWriter(shutdownSocket.getOutputStream(), true);
								out.println("SHUTDOWN");
								shutdownSocket.close();
								System.out.println("[" + n_port + "] Shutdown request sent to coordinator.");
								// Wait for coordinator shutdown broadcast
								try {
									System.out.println("[" + n_port + "] Waiting for SHUTDOWN confirmation from coordinator...");
									ServerSocket shutdownServer = new ServerSocket(n_port);
									Socket shutdownSignal = shutdownServer.accept();
									BufferedReader shutdownReader = new BufferedReader(new InputStreamReader(shutdownSignal.getInputStream()));
									String shutdownMsg = shutdownReader.readLine();
									if ("SHUTDOWN".equals(shutdownMsg)) {
										System.out.println("[" + n_port + "] Final shutdown signal received from coordinator. Exiting.");
										Logger.log("Node " + n_port + " shutting down after initiating shutdown.");
									}
									shutdownSignal.close();
									shutdownServer.close();
								} catch (IOException e) {
									System.out.println("[" + n_port + "] ERROR waiting for final SHUTDOWN: " + e);
								}

								System.exit(0);
							} catch (IOException e) {
								System.out.println("[" + n_port + "] ERROR sending shutdown request: " + e);
							}
						}
					} catch (IOException e) {
						System.out.println("[" + n_port + "] ERROR returning token: " + e);
					}
				}
			} catch (IOException | InterruptedException e) {
				System.out.println("[" + n_port + "] ERROR during token reception: " + e);
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
