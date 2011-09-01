package livecanvas;

import livecanvas.animator.Vertex;

public class Edge {
	public Vertex v1;
	public Vertex v2;
	public Face face;
	
	public int index;
	
	public Edge(Vertex v1, Vertex v2) {
		if (v1.compareTo(v2) > 0) {
			Vertex t = v1;
			v1 = v2;
			v2 = t;
		}
		this.v1 = v1;
		this.v2 = v2;
	}

	public Vertex other(Vertex v) {
		if (v1.equals(v)) {
			return v2;
		} else if (v2.equals(v)) {
			return v1;
		}
		return null;
	}

	public double angle() {
		return Math.atan2(v2.y - v1.y, v2.x - v1.x);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Edge)) {
			return false;
		}
		Edge e = (Edge) o;
		return (v1.equals(e.v1) && v2.equals(e.v2))
				|| (v2.equals(e.v1) && v1.equals(e.v2));
	}
}
