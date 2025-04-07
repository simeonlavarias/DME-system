package addOns;

import java.util.Vector;

public class C_buffer {

	private Vector<Request> buffer;

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
