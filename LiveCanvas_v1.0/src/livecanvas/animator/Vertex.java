package livecanvas.animator;

import livecanvas.Utils;
import livecanvas.Vec2;
import livecanvas.Vec3;

import org.json.JSONException;
import org.json.JSONObject;

public class Vertex extends Vec3 implements Comparable<Vertex> {
	// private List<Edge> edges = new LinkedList<Edge>();

	public int index;

	public boolean onPath;

	public Vertex() {
	}

	private void init(double x, double y, double z, boolean onPath) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.onPath = onPath;
	}

	public Vertex(double x, double y, double z, boolean onPath) {
		init(x, y, z, onPath);
	}

	public Vertex(Vertex copy) {
		init(copy.x, copy.y, copy.z, copy.onPath);
	}

	public Vertex(Vertex v1, Vertex v2, double interpolation) {
		if (v1.onPath != v2.onPath) {
			throw new IllegalArgumentException(
					"v1.onPath != v2.onPath (incompatible vertex types)");
		}
		double x = (1 - interpolation) * v1.x + interpolation * v2.x;
		double y = (1 - interpolation) * v1.y + interpolation * v2.y;
		double z = (1 - interpolation) * v1.z + interpolation * v2.z;
		init(x, y, z, v1.onPath);
	}

	// public void addEdge(Edge edge) {
	// edges.add(edge);
	// }
	//
	// public void removeEdge(Edge edge) {
	// edges.remove(edge);
	// }
	//
	// public Edge[] getEdges() {
	// return edges.toArray(new Edge[0]);
	// }
	//
	// public boolean containsEdge(Edge edge) {
	// for (Edge e : edges) {
	// if (e.equals(edge)) {
	// return true;
	// }
	// }
	// return false;
	// }

	public boolean equals(Object v) {
		if (v == null || !(v instanceof Vertex)) {
			return false;
		}
		return compareTo((Vertex) v) == 0;
	}

	public int compareTo(Vertex v) {
		int n;
		return (n = FloatCmp.compare(y, v.y)) != 0 ? n : FloatCmp.compare(x,
				v.x);
	}

	public double distance(Vertex v) {
		return Utils.distance(x, y, v.x, v.y);
	}

	@Override
	public String toString() {
		return String.format("%.2f,%.2f", x, y);
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("index", index);
		json.put("onPath", onPath);
		return json;
	}

	public static Vertex fromJSON(JSONObject json) throws JSONException {
		Vec3 vec = Vec3.fromJSON(json);
		Vertex v = new Vertex();
		v.x = vec.x;
		v.y = vec.y;
		v.z = vec.z;
		v.index = json.getInt("index");
		v.onPath = json.getBoolean("onPath");
		return v;
	}
}
