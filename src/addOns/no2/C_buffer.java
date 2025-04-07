package addOns.no2;

import java.util.Vector;
import java.util.HashSet;
import java.util.Set;

public class C_buffer {

	private Vector<Request> buffer;

	private Set<Integer> activePorts = new HashSet<>();

	public synchronized void registerNodePort(String port) {
		activePorts.add(Integer.parseInt(port));
	}

	public synchronized Set<Integer> getActivePorts() {
		return new HashSet<>(activePorts); // Return a copy to avoid direct mutation
	}

	private Set<String> registeredPorts = new HashSet<>();

	public synchronized void registerNode(String port) {
		registeredPorts.add(port);
	}

	public synchronized Set<String> getRegisteredPorts() {
		return new HashSet<>(registeredPorts);
	}

	public C_buffer() {
		buffer = new Vector<Request>();
	}

	class Request {
		String ip;
		String port;
		String priority; // "high" or "normal"
		long timestamp;  // For starvation prevention

		public Request(String ip, String port, String priority) {
			this.ip = ip;
			this.port = port;
			this.priority = priority;
			this.timestamp = System.currentTimeMillis();
		}
	}

	public synchronized void saveRequest(String ip, String port, String priority) {
		buffer.add(new Request(ip, port, priority));
		registerNode(port); // Automatically track the node
	}

	public synchronized int size() {
		return buffer.size();
	}

	public synchronized void show() {
		for (Request r : buffer) {
			System.out.println(r.ip + "  " + r.port + "  (" + r.priority + ")");
		}
	}

	public synchronized String[] getHighestPriorityRequest() {
		if (buffer.isEmpty()) return null;

		long now = System.currentTimeMillis();
		int index = 0;

		for (int i = 1; i < buffer.size(); i++) {
			Request current = buffer.get(i);
			Request best = buffer.get(index);

			// Promote "normal" requests if waiting too long
			if (current.priority.equals("normal") && now - current.timestamp > 5000) {
				current.priority = "high"; // Promote due to starvation
			}

			// Pick the higher-priority one
			if (current.priority.equals("high") && best.priority.equals("normal")) {
				index = i;
			}
		}

		Request chosen = buffer.remove(index);
		return new String[]{chosen.ip, chosen.port};
	}
}
