package livecanvas.image;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import livecanvas.BackgroundRef;
import livecanvas.Face;
import livecanvas.Mesh;
import livecanvas.Path;
import livecanvas.Vec3;
import livecanvas.mosaic.Ray;
import livecanvas.mosaic.Ray.Intersection;

import org.poly2tri.Poly2Tri;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.sets.ConstrainedPointSet;
import org.poly2tri.triangulation.sets.PointSet;

public abstract class AbstractUniformParticleGenerator<T> extends
		AbstractParticleGenerator<T> {

	protected abstract int sides();

	@Override
	protected Particle<T>[] generateForPath(Path path, RenderData renderData,
			BackgroundRef bgref) {
		Mesh mesh = path.getMesh();
		List<Particle<T>> particles = new ArrayList<Particle<T>>();
		Point origin = new Point(renderData.canvasSize.width / 2,
				renderData.canvasSize.height / 2);
		BufferedImage srcImage = bgref.toImage();
		int nIterations = 10;
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		int sides = sides();
		List<TriangulationPoint> internal = new ArrayList<TriangulationPoint>(
				Arrays.asList(generateRandomPoints(width, height, sides * sides)));
		PointSet pointSet = null;
		TriangulationPoint[] outlinePointsArray = new TriangulationPoint[sides * 4];
		List<TriangulationPoint> constraints = new ArrayList<TriangulationPoint>();
		int c = 0;
		for (int i = 0; i < sides; i++) {
			outlinePointsArray[c++] = new TPoint(i * (width - 1) / sides, 0);
		}
		for (int i = 0; i < sides; i++) {
			outlinePointsArray[c++] = new TPoint(width - 1, i * (height - 1)
					/ sides);
		}
		for (int i = 0; i < sides; i++) {
			outlinePointsArray[c++] = new TPoint((sides - i) * (width - 1)
					/ sides, height - 1);
		}
		for (int i = 0; i < sides; i++) {
			outlinePointsArray[c++] = new TPoint(0, (sides - i) * (height - 1)
					/ sides);
		}
		for (int i = 0; i < outlinePointsArray.length; i++) {
			constraints.add(outlinePointsArray[i]);
			constraints.add(outlinePointsArray[(i + 1)
					% outlinePointsArray.length]);
		}
		List<TriangulationPoint> outline = Arrays.asList(outlinePointsArray);
		List<TriangulationPoint> points = new ArrayList<TriangulationPoint>();
		Map<TriangulationPoint, List<TriangulationPoint>> point2Edges = new HashMap<TriangulationPoint, List<TriangulationPoint>>();
		while (--nIterations >= 0) {
			points.clear();
			points.addAll(outline);
			points.addAll(internal);
			internal.clear();
			pointSet = new ConstrainedPointSet(points, constraints);
			Poly2Tri.triangulate(pointSet);
			point2Edges.clear();
			for (int i = 0; i < points.size(); i++) {
				point2Edges.put(points.get(i),
						new ArrayList<TriangulationPoint>());
			}
			List<TriangulationPoint> edges;
			for (DelaunayTriangle t : pointSet.getTriangles()) {
				for (int i = 0; i < 3; i++) {
					edges = point2Edges.get(t.points[i]);
					edges.add(t.points[(i + 1) % 3]);
					edges.add(t.points[(i + 2) % 3]);
				}
			}
			for (TriangulationPoint p : pointSet.getPoints()) {
				if (outline.contains(p)) {
					continue;
				}
				double sxsum = 0, sysum = 0, szsum = 0;
				int scount = 0;
				for (TriangulationPoint other : point2Edges.get(p)) {
					sxsum += other.getX();
					sysum += other.getY();
					szsum += other.getZ();
					scount++;
				}
				TPoint np = new TPoint(sxsum / scount, sysum / scount, szsum
						/ scount);
				internal.add(np);
			}
		}
		for (TriangulationPoint p : pointSet.getPoints()) {
			TriangulationPoint[] edges = point2Edges.get(p).toArray(
					new TriangulationPoint[0]);
			final double px = p.getX(), py = p.getY();
			Arrays.sort(edges, new Comparator<TriangulationPoint>() {
				@Override
				public int compare(TriangulationPoint o1, TriangulationPoint o2) {
					return (int) Math.signum(Math.atan2(o1.getY() - py,
							o1.getX() - px)
							- Math.atan2(o2.getY() - py, o2.getX() - px));
				}
			});
			int n = edges.length;
			Polygon poly = new Polygon(new int[n], new int[n], n);
			double[] res = new double[2];
			for (int i = 0; i < n; i++) {
				int i1 = i < n - 1 ? i + 1 : 0;
				circumpoint(px, py, edges[i].getX(), edges[i].getY(),
						edges[i1].getX(), edges[i1].getY(), res);
				poly.xpoints[i] = (int) (res[0]);
				poly.ypoints[i] = (int) (res[1]);
			}
			Rectangle bounds = poly.getBounds();
			int cx = (int) bounds.getCenterX();
			int cy = (int) bounds.getCenterY();
			Face face = findFaceAtPoint(mesh, origin, cx, cy);
			if (face != null) {
				T packet = forPoly(path, renderData, bgref, poly);
				if (packet != null) {
					particles.add(new Particle<T>(origin, mesh, face, cx
							- origin.x, cy - origin.y, packet));
				}
			}
		}
		return particles.toArray(new Particle[0]);
	}

	protected abstract T forPoly(Path path, RenderData renderData,
			BackgroundRef bgref, Polygon poly);

	private static TriangulationPoint[] generateRandomPoints(int width,
			int height, int totalPoints) {
		TriangulationPoint[] points = new TriangulationPoint[totalPoints];
		for (int i = 0; i < totalPoints; i++) {
			points[i] = new TPoint(1 + Math.random() * (width - 2), 1
					+ Math.random() * (height - 2));
		}
		return points;
	}

	private static void circumpoint(double x0, double y0, double x1, double y1,
			double x2, double y2, double[] result) {
		double mx1 = (x1 + x0) / 2;
		double my1 = (y1 + y0) / 2;
		double mx2 = (x2 + x0) / 2;
		double my2 = (y2 + y0) / 2;
		double sx1 = y1 - y0;
		double sy1 = x0 - x1;
		double sx2 = y2 - y0;
		double sy2 = x0 - x2;
		Intersection in = new Ray(mx1, my1, sx1, sy1).intersection(new Ray(mx2,
				my2, sx2, sy2));
		if (in == null) {
			result[0] = (mx1 + mx2) / 2;
			result[1] = (my1 + my2) / 2;
		} else {
			result[0] = in.intersection.x;
			result[1] = in.intersection.y;
		}
	}

	private Polygon face_poly = new Polygon(new int[3], new int[3], 3);

	private Face findFaceAtPoint(Mesh mesh, Point origin, int x, int y) {
		for (Face f : mesh.faces) {
			Vec3 v1 = mesh.vertices[f.v1Index];
			Vec3 v2 = mesh.vertices[f.v2Index];
			Vec3 v3 = mesh.vertices[f.v3Index];
			face_poly.xpoints[0] = (int) (origin.x + v1.x);
			face_poly.xpoints[1] = (int) (origin.x + v2.x);
			face_poly.xpoints[2] = (int) (origin.x + v3.x);
			face_poly.ypoints[0] = (int) (origin.y + v1.y);
			face_poly.ypoints[1] = (int) (origin.y + v2.y);
			face_poly.ypoints[2] = (int) (origin.y + v3.y);
			face_poly.invalidate();
			if (face_poly.contains(x, y)) {
				return f;
			}
		}
		return null;
	}
}
