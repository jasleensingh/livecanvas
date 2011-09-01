package livecanvas.mosaic;

public class FloatCmp {
	private static final double TOL = 1e-4;

	public static final boolean equals(double a, double b) {
		return Math.abs(a - b) < TOL;
	}

	public static final boolean zero(double a) {
		return Math.abs(a) < TOL;
	}

	public static final boolean lessThan(double a, double b) {
		return a < (b + TOL);
	}

	public static final boolean greaterThan(double a, double b) {
		return a > (b - TOL);
	}

	public static final int compare(double a, double b) {
		return equals(a, b) ? 0 : (lessThan(a, b) ? -1 : 1);
	}
}
