package livecanvas;

import livecanvas.thinplate.jama.Matrix;

public class ThinPlate {
	public static double[][] est_tps(Vec3[] srcPoints, Vec3[] tgtPoints) {
		int nparams = 3 + srcPoints.length;
		double[][] params = new double[2][];
		double[][] M_data = new double[nparams][nparams];
		for (int i = 0; i < srcPoints.length; i++) {
			for (int j = 0; j < srcPoints.length; j++) {
				M_data[i][j] = U(ec_norm(srcPoints[i], srcPoints[j]));
			}
		}
		for (int i = 0; i < srcPoints.length; i++) {
			double[] p = { 1, srcPoints[i].x, srcPoints[i].y };
			for (int j = srcPoints.length; j < srcPoints.length + 3; j++) {
				M_data[i][j] = M_data[j][i] = p[j - srcPoints.length];
			}
		}
		Matrix M = new Matrix(M_data);
		Matrix M_inv = M.inverse();
		// Matrix M_inv = M.transpose().times(M).inverse().times(M.transpose());
		double[] b = new double[nparams];
		for (int i = 0; i < tgtPoints.length; i++) {
			b[i] = tgtPoints[i].x;
		}
		params[0] = Matrix.multiply(M_inv.getArray(), b);
		for (int i = 0; i < tgtPoints.length; i++) {
			b[i] = tgtPoints[i].y;
		}
		params[1] = Matrix.multiply(M_inv.getArray(), b);
		return params;
	}

	public static final double ec_norm(Vec3 p1, Vec3 p2) {
		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public static final double U(double r) {
		return r <= 0.0001 ? 0 : r * r * Math.log(r);
	}
}
