package addOns.no2;

import java.net.InetAddress;

public class Coordinator {
	public static void main(String[] args) {
		int port = 7000;

		try {
			InetAddress c_addr = InetAddress.getLocalHost();
			String c_name = c_addr.getHostName();
			System.out.println("Coordinator address is " + c_addr);
			System.out.println("Coordinator host name is " + c_name + "\n\n");
		} catch (Exception e) {
			System.err.println(e);
			System.err.println("Error in coordinator");
		}

		// allows defining port at launch time
		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		}

		// 💡 Create shared buffer
		C_buffer buffer = new C_buffer();

		// 🧵 Start coordinator threads
		C_receiver receiver = new C_receiver(buffer, port);   // Listens for token requests
		C_mutex mutex = new C_mutex(buffer, port);            // Grants and waits for token

		receiver.start();
		mutex.start();

		System.out.println("Coordinator running on port " + port + "...");
	}

}
