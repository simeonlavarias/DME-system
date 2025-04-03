package advancedFeatures;

import java.util.Vector;

public class C_buffer {

	private Vector<String[]> buffer;

	public C_buffer() {
		buffer = new Vector<String[]>();
	}

	public synchronized int size() {
		return buffer.size();
	}

	public synchronized void saveRequest(String[] request) {
		buffer.add(request);  // [0]=IP, [1]=Port, [2]=Priority ("high"/"normal")
	}

	public synchronized String[] getHighestPriorityRequest() {
		if (buffer.isEmpty()) return null;

		int index = 0;
		for (int i = 1; i < buffer.size(); i++) {
			String[] current = buffer.get(i);
			String[] best = buffer.get(index);
			if (current[2].equals("high") && best[2].equals("normal")) {
				index = i; // current has higher priority
			}
		}
		return buffer.remove(index); // Remove and return highest priority
	}

	public synchronized void show() {
		for (String[] entry : buffer) {
			System.out.println(entry[0] + "  " + entry[1] + "  (" + entry[2] + ")");
		}
	}
}