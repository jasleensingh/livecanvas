package livecanvas.mosaic;

import livecanvas.Vec2;

public class Ray {
	public final double x;
	public final double y;
	public final double xdirection;
	public final double ydirection;

	public boolean used;

	public Ray(double x, double y, double xdirection, double ydirection) {
		this.x = x;
		this.y = y;
		double norm = Math.sqrt(xdirection * xdirection + ydirection
				* ydirection);
		this.xdirection = xdirection / norm;
		this.ydirection = ydirection / norm;
	}

	public Intersection intersection(Ray ray) {
		// coincident points
		if (FloatCmp.equals(x, ray.x) && FloatCmp.equals(y, ray.y)) {
			return null;
		}
		double det = xdirection * ray.ydirection - ydirection * ray.xdirection;
		// parallel
		if (FloatCmp.zero(det)) {
			return null;
		}
		double r1 = (y * ray.xdirection + ray.x * ray.ydirection - x
				* ray.ydirection - ray.y * ray.xdirection)
				/ det;
		double r2 = (y * xdirection + ray.x * ydirection - x * ydirection - ray.y
				* xdirection)
				/ det;
		return new Intersection(this, ray, new Vec2(x + r1 * xdirection, y + r1
				* ydirection));
	}

	@Override
	public String toString() {
		return String.format("%.2f,%.2f,%.2f,%.2f", x, y, xdirection,
				ydirection);
	}

	public static void main(String[] args) {
		System.err.println(new Ray(0.50, 0.25, -0.71, -0.71)
				.intersection(new Ray(0.38, 0.03, -0.26, -0.97)));
	}

	public static class Intersection implements Comparable<Intersection> {
		public final Ray ray1;
		public final Ray ray2;
		public final Vec2 intersection;
		public final double distance;

		public Intersection(Ray ray1, Ray ray2, Vec2 intersection) {
			this.ray1 = ray1;
			this.ray2 = ray2;
			this.intersection = intersection;
			this.distance = distance(ray1.x, ray1.y, intersection.x,
					intersection.y)
					+ distance(ray2.x, ray2.y, intersection.x, intersection.y);
		}

		@Override
		public int compareTo(Intersection o) {
			return distance < o.distance ? -1 : 1;
		}

		private static final double distance(double x1, double y1, double x2,
				double y2) {
			double dx = x1 - x2;
			double dy = y1 - y2;
			return Math.sqrt(dx * dx + dy * dy);
		}
	}
}
