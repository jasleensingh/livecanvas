package livecanvas;

public class Transform3 extends Matrix {
	public static final Transform3 I = new Transform3();

	public Transform3() {
		super(4, 4);
		reset();
	}

	public Transform3(double[][] array) {
		super(array);
	}

	public void set(Transform3 t) {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				A[i][j] = t.A[i][j];
			}
		}
	}

	public void reset() {
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				A[i][j] = i == j ? 1 : 0;
			}
		}
	}

	public Transform3 times(Transform3 t) {
		return new Transform3(super.times(t).getArray());
	}

	public Transform3 setTranslate(double dx, double dy, double dz) {
		A[0][3] = dx;
		A[1][3] = dy;
		A[2][3] = dz;
		return this;
	}

	public Transform3 setRotateX(double radians) {
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);
		A[1][1] = cos;
		A[1][2] = -sin;
		A[2][1] = sin;
		A[2][2] = cos;
		return this;
	}

	public Transform3 setRotateY(double radians) {
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);
		A[0][0] = cos;
		A[0][2] = sin;
		A[2][0] = -sin;
		A[2][2] = cos;
		return this;
	}

	public Transform3 setRotateZ(double radians) {
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);
		A[0][0] = cos;
		A[0][1] = -sin;
		A[1][0] = sin;
		A[1][1] = cos;
		return this;
	}

	public Transform3 setRotateX(double radians, double px, double py, double pz) {
		Transform3 t1 = new Transform3().setTranslate(-px, -py, -pz);
		Transform3 r = new Transform3().setRotateX(radians);
		Transform3 t2 = new Transform3().setTranslate(px, py, pz);
		return new Transform3(t2.times(r).times(t1).getArray());
	}

	public Transform3 setRotateY(double radians, double px, double py, double pz) {
		Transform3 t1 = new Transform3().setTranslate(-px, -py, -pz);
		Transform3 r = new Transform3().setRotateY(radians);
		Transform3 t2 = new Transform3().setTranslate(px, py, pz);
		return new Transform3(t2.times(r).times(t1).getArray());
	}

	public Transform3 setRotateZ(double radians, double px, double py, double pz) {
		Transform3 t1 = new Transform3().setTranslate(-px, -py, -pz);
		Transform3 r = new Transform3().setRotateZ(radians);
		Transform3 t2 = new Transform3().setTranslate(px, py, pz);
		return new Transform3(t2.times(r).times(t1).getArray());
	}

	public Transform3 setScale(double sx, double sy, double sz) {
		A[0][0] = sx;
		A[1][1] = sy;
		A[2][2] = sz;
		return this;
	}

	public Transform3 setScale(double sx, double sy, double sz, double px,
			double py, double pz) {
		Transform3 t1 = new Transform3().setTranslate(-px, -py, pz);
		Transform3 r = new Transform3().setScale(sx, sy, sz);
		Transform3 t2 = new Transform3().setTranslate(px, py, pz);
		return new Transform3(t2.times(r).times(t1).getArray());
	}

	public void preConcat(Transform3 t) {
		set(this.times(t));
	}

	public void postConcat(Transform3 t) {
		set(t.times(this));
	}

	public static double[] apply(Transform3 t, double[] in_d, double[] out_d) {
		double[][] A = t.A;
		out_d[0] = (A[0][0] * in_d[0] + A[0][1] * in_d[1] + A[0][2] * in_d[2] + A[0][3] * in_d[3]);
		out_d[1] = (A[1][0] * in_d[0] + A[1][1] * in_d[1] + A[1][2] * in_d[2] + A[1][3] * in_d[3]);
		out_d[2] = (A[2][0] * in_d[0] + A[2][1] * in_d[1] + A[2][2] * in_d[2] + A[2][3] * in_d[3]);
		return out_d;
	}
}
