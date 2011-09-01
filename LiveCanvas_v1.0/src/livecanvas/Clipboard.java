package livecanvas;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Clipboard {
	private static Map<String, Clipboard> instancesMap = new HashMap<String, Clipboard>();

	private Object[][] clips;

	private int current;

	private List<Listener> listeners = new LinkedList<Listener>();

	public Clipboard() {
		this(10);
	}

	public Clipboard(int size) {
		clips = new Object[size][];
		current = 0;
	}

	public static Clipboard getInstance(Class clazz) {
		return getInstance(clazz.getSimpleName());
	}

	public static Clipboard getInstance(String name) {
		Clipboard instance = instancesMap.get(name);
		if (instance == null) {
			instance = new Clipboard();
			instancesMap.put(name, instance);
		}
		return instance;
	}

	public void addListener(Listener l) {
		listeners.add(l);
	}

	public void removeListener(Listener l) {
		listeners.remove(l);
	}

	protected void notifyAdd(Object[] items) {
		for (Listener l : listeners) {
			l.clipsAdded(this, items);
		}
	}

	public void put(Object[] items) {
		// copy items
		clips[current] = new Object[items.length];
		for (int i = 0; i < items.length; i++) {
			clips[current][i] = items[i];
		}

		int c = current;
		current = (current + 1) % clips.length;

		notifyAdd(clips[c]);
	}

	public Object[] get() {
		return get(0);
	}

	public Object[] get(int offset) {
		offset %= clips.length;
		int t = current - offset;
		if (t > 0) {
			t--;
		} else {
			t += clips.length - 1;
		}
		return clips[t];
	}

	public static interface Listener {
		public void clipsAdded(Clipboard clipboard, Object[] items);
	}
}
