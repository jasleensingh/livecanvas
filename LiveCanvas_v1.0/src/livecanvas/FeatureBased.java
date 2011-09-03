package livecanvas;

public class FeatureBased {
	public static class Line {
		public final Vec3 p1;
		public final Vec3 p2;

		public Line(Vec3 p1, Vec3 p2) {
			this.p1 = p1;
			this.p2 = p2;
		}
	}

	public static Vec3[] transform(Line[] srcLines, Line[] tgtLines,
			Vec3[] tgtPoints, Vec3[] srcPoints, double width, double height) {
		int nLines;
		if ((nLines = srcLines.length) != tgtLines.length) {
			throw new RuntimeException("Line array dimensions do not match!");
		}
		int nPoints;
		if (srcPoints == null) {
			srcPoints = new Vec3[nPoints = tgtPoints.length];
		} else {
			if ((nPoints = srcPoints.length) != tgtPoints.length) {
				throw new RuntimeException(
						"Point array dimensions do not match!");
			}
		}
		Vec3 dsum = new Vec3();
		Vec3 tgtPoint = new Vec3();
		double weightSum;
		Vec3 srcPoint = new Vec3();
		double[] uv = new double[2];
		double a = 0.01 * Math.min(width, height);
		double p = 1;
		double b = 1;
		for (int i = 0; i < nPoints; i++) {
			dsum.set(0, 0, 0);
			weightSum = 0;
			for (int j = 0; j < nLines; j++) {
				Line srcLine = srcLines[j];
				Line tgtLine = tgtLines[j];
				tgtPoint.set(tgtPoints[i]);
				transform(srcLine, tgtLine, tgtPoint, srcPoint, uv);
				Vec3 disp = new Vec3(tgtPoint, srcPoint);
				double u = uv[0];
				double v = uv[1];
				double dist;
				if (u < 0) {
					dist = new Vec3(tgtLine.p1, tgtPoint).length();
				} else if (u > 1) {
					dist = new Vec3(tgtLine.p2, tgtPoint).length();
				} else {
					dist = Math.abs(v);
				}
				double length = new Vec3(tgtLine.p1, tgtLine.p2).length();
				double weight = Math.pow((Math.pow(length, p) / (a + dist)), b);
				dsum.addSelf(disp.multiply(weight));
				weightSum += weight;
			}
			srcPoint.set(tgtPoint.add(dsum.multiply(1 / weightSum)));
			srcPoints[i] = new Vec3(srcPoint);
		}
		return srcPoints;
	}

	public static void transform(Line srcLine, Line tgtLine, Vec3 tgtPoint,
			Vec3 srcPoint, double[] uv) {
		Vec3 tp1 = tgtLine.p1;
		Vec3 tp2 = tgtLine.p2;
		Vec3 tp12 = new Vec3(tp1, tp2);
		tp12.normalizeSelf();
		Vec3 tp1point = new Vec3(tp1, tgtPoint);
		double u = tp1point.dot(tp12);
		double v = tp1point.dot(new Vec3(tp12.y, -tp12.x, 0));
		Vec3 sp1 = srcLine.p1;
		Vec3 sp2 = srcLine.p2;
		Vec3 sp12 = new Vec3(sp1, sp2);
		sp12.normalizeSelf();
		if (srcPoint != null) {
			srcPoint.set(new Vec3(sp1).add(sp12.multiply(u)).add(
					new Vec3(sp12.y, -sp12.x, 0).multiply(v)));
		}
		if (uv != null) {
			uv[0] = u;
			uv[1] = v;
		}
	}
}
