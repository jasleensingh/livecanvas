package livecanvas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import livecanvas.animator.AdjustScale;
import livecanvas.animator.RigidTransform;
import livecanvas.animator.Vertex;
import livecanvas.components.Layer;
import livecanvas.mosaic.Ray;
import livecanvas.mosaic.Ray.Intersection;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.sets.ConstrainedPointSet;
import org.poly2tri.triangulation.sets.PointSet;

import livecanvas.thinplate.jama.Matrix;

public abstract class MeshHandler {
	public abstract Mesh generateMesh(int total, Vertex[] vs, int count);

	public abstract void initializeTransform(Layer layer);

	public abstract void updateTransform(Layer layer);

	public abstract void clearTransform();

	protected Mesh generateMeshUniformRandom(int total, Vertex[] vs, int count) {
		int nIterations = 100;
		List<TriangulationPoint> internal = new ArrayList<TriangulationPoint>(
				Arrays.asList(generateRandomPoints(total, vs, count)));
		PointSet pointSet = null;
		TriangulationPoint[] outlinePointsArray = new TriangulationPoint[count];
		List<TriangulationPoint> constraints = new ArrayList<TriangulationPoint>();
		for (int i = 0; i < count; i++) {
			outlinePointsArray[i] = new TPoint(vs[i].x, vs[i].y, vs[i].z);
		}
		for (int i = 0; i < count; i++) {
			constraints.add(outlinePointsArray[i]);
			constraints.add(outlinePointsArray[(i + 1) % count]);
		}
		List<TriangulationPoint> outline = Arrays.asList(outlinePointsArray);
		List<TriangulationPoint> points = new ArrayList<TriangulationPoint>();
		while (--nIterations >= 0) {
			points.clear();
			points.addAll(outline);
			points.addAll(internal);
			internal.clear();
			pointSet = new ConstrainedPointSet(points, constraints);
			Poly2Tri.triangulate(pointSet);
			Map<TriangulationPoint, List<TriangulationPoint>> point2Edges = new HashMap<TriangulationPoint, List<TriangulationPoint>>();
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
				// double sxsum = 0, sysum = 0, szsum = 0;
				// int scount = 0;
				// for (TriangulationPoint other : point2Edges.get(p)) {
				// sxsum += other.getX();
				// sysum += other.getY();
				// szsum += other.getZ();
				// scount++;
				// }
				// TPoint np = new TPoint(sxsum / scount, sysum / scount,
				// szsum
				// / scount);
				TriangulationPoint[] edgesArray = point2Edges.get(p).toArray(
						new TriangulationPoint[0]);
				final double px = p.getX(), py = p.getY();
				Arrays.sort(edgesArray, new Comparator<TriangulationPoint>() {
					@Override
					public int compare(TriangulationPoint o1,
							TriangulationPoint o2) {
						return (int) Math.signum(Math.atan2(o1.getY() - py,
								o1.getX() - px)
								- Math.atan2(o2.getY() - py, o2.getX() - px));
					}
				});
				int n = edgesArray.length;
				double sxsum = 0, sysum = 0;
				double[] res = new double[2];
				for (int i = 0; i < n; i++) {
					int i1 = i < n - 1 ? i + 1 : 0;
					circumpoint(px, py, edgesArray[i].getX(),
							edgesArray[i].getY(), edgesArray[i1].getX(),
							edgesArray[i1].getY(), res);
					sxsum += res[0];
					sysum += res[1];
				}
				TPoint np = new TPoint(sxsum / n, sysum / n, 0);
				internal.add(np);
			}
		}
		return Mesh.toMesh(Mesh.removeOuter2(outline, pointSet.getTriangles())
				.toArray(new DelaunayTriangle[0]), pointSet.getPoints()
				.toArray(new TriangulationPoint[0]), outline.size());
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

	private TriangulationPoint[] generateRandomPoints(int total, Vertex[] vs,
			int count) {
		if (count < 3) {
			throw new IllegalStateException("Insufficient number of points: "
					+ count);
		}
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		for (int i = 0; i < count; i++) {
			points.add(new PolygonPoint(vs[i].x, vs[i].y, vs[i].z));
		}
		Polygon polygon = new Polygon(points);
		Poly2Tri.triangulate(polygon);
		DelaunayTriangle[] triangles = polygon.getTriangles().toArray(
				new DelaunayTriangle[0]);
		Arrays.sort(triangles, new Comparator<DelaunayTriangle>() {
			@Override
			public int compare(DelaunayTriangle t1, DelaunayTriangle t2) {
				return t1.area2() < t2.area2() ? -1 : 1;
			}
		});
		double[] cumulativeArea = new double[triangles.length];
		cumulativeArea[0] = triangles[0].area2();
		for (int i = 1; i < triangles.length; i++) {
			cumulativeArea[i] = cumulativeArea[i - 1] + triangles[i].area2();
		}
		double areaSum = cumulativeArea[triangles.length - 1];
		TriangulationPoint[] generated = new TriangulationPoint[total];
		for (int i = 0; i < total; i++) {
			double guess = Math.random() * areaSum;
			int index = Arrays.binarySearch(cumulativeArea, guess);
			if (index < 0) {
				index = -index - 1;
			}
			DelaunayTriangle tri = triangles[index];
			double x0 = tri.points[0].getX();
			double y0 = tri.points[0].getY();
			double z0 = tri.points[0].getZ();
			double x1 = tri.points[1].getX();
			double y1 = tri.points[1].getY();
			double z1 = tri.points[1].getZ();
			double x2 = tri.points[2].getX();
			double y2 = tri.points[2].getY();
			double z2 = tri.points[2].getZ();
			double xv01 = x1 - x0;
			double yv01 = y1 - y0;
			double zv01 = z1 - z0;
			double xv02 = x2 - x0;
			double yv02 = y2 - y0;
			double zv02 = z2 - z0;
			double u = Math.random(), v = Math.random();
			if (u + v >= 1) {
				u = 1 - u;
				v = 1 - v;
			}
			generated[i] = new TPoint(x0 + u * xv01 + v * xv02, y0 + u * yv01
					+ v * yv02, z0 + u * zv01 + v * zv02);
		}
		return generated;
	}

	public static class Rigid extends MeshHandler {
		private Map<String, RigidTransform> rigidTransformMap = new HashMap<String, RigidTransform>();
		private Map<String, AdjustScale> adjustScaleMap = new HashMap<String, AdjustScale>();

		@Override
		public Mesh generateMesh(int total, Vertex[] vs, int count) {
			return generateMeshUniformRandom(total, vs, count);
		}

		public void initializeTransform(Layer layer) {
			Path path = layer.getPath();
			if (path.isFinalized()) {
				Mesh mesh = path.getMesh();
				List<ControlPoint> controlPoints = mesh.getControlPoints();
				List<Vertex> controlPointVertices = new ArrayList<Vertex>(
						controlPoints.size());
				if (controlPoints.size() > 1) {
					RigidTransform rigidTransform = new RigidTransform(mesh);
					rigidTransformMap.put(layer.getName(), rigidTransform);
					rigidTransform.initErrorMatrix();
					AdjustScale adjustScale = new AdjustScale(mesh);
					adjustScaleMap.put(layer.getName(), adjustScale);
					for (ControlPoint cp : controlPoints) {
						controlPointVertices.add(mesh.vertices[cp.vIndex]);
					}
					rigidTransform.compile(controlPointVertices);
					adjustScale.compile(controlPointVertices);
				}
			}
			for (Layer subLayer : layer.getSubLayers()) {
				initializeTransform(subLayer);
			}
		}

		public void updateTransform(Layer layer) {
			RigidTransform rigidTransform;
			if ((rigidTransform = rigidTransformMap.get(layer.getName())) != null) {
				rigidTransform.update();
				AdjustScale adjustScale = adjustScaleMap.get(layer.getName());
				adjustScale.update();
			}
			for (Layer subLayer : layer.getSubLayers()) {
				updateTransform(subLayer);
			}
		}

		public void clearTransform() {
			rigidTransformMap.clear();
			adjustScaleMap.clear();
		}
	}

	public static class ThinPlate extends MeshHandler {
		private Map<String, Vertex[]> verticesMap = new HashMap<String, Vertex[]>();

		@Override
		public Mesh generateMesh(int total, Vertex[] vs, int count) {
			return generateMeshUniformRandom(total, vs, count);
		}

		public void initializeTransform(Layer layer) {
			Path path = layer.getPath();
			if (path.isFinalized()) {
				Mesh mesh = path.getMesh();
				if (mesh.getControlPoints().size() > 1) {
					Vertex[] srcPoints = new Vertex[mesh.vertices.length];
					for (int i = 0; i < srcPoints.length; i++) {
						srcPoints[i] = new Vertex(mesh.vertices[i]);
					}
					System.err.println("putting: " + layer.getName());
					verticesMap.put(layer.getName(), srcPoints);
				}
			}
			for (Layer subLayer : layer.getSubLayers()) {
				initializeTransform(subLayer);
			}
		}

		public void updateTransform(Layer layer) {
			Vertex[] vertices;
			if ((vertices = verticesMap.get(layer.getName())) != null) {
				Mesh mesh = layer.getPath().getMesh();
				List<ControlPoint> controlPoints = mesh.getControlPoints();
				Vertex[] srcPoints = new Vertex[controlPoints.size()];
				Vertex[] tgtPoints = new Vertex[controlPoints.size()];
				for (int i = 0; i < srcPoints.length; i++) {
					srcPoints[i] = vertices[controlPoints.get(i).vIndex];
					tgtPoints[i] = mesh.vertices[controlPoints.get(i).vIndex];
				}
				double[][] tps = livecanvas.ThinPlate.est_tps(srcPoints,
						tgtPoints);
				OUTER: for (int i = 0; i < mesh.vertices.length; i++) {
					Vertex p = vertices[i];
					for (ControlPoint cp : controlPoints) {
						if (mesh.getControlPointVertex(cp) == p) {
							continue OUTER;
						}
					}
					double[] x = new double[srcPoints.length + 3];
					for (int j = 0; j < srcPoints.length; j++) {
						x[j] = livecanvas.ThinPlate.U(livecanvas.ThinPlate
								.ec_norm(srcPoints[j], p));
					}
					x[srcPoints.length] = 1;
					x[srcPoints.length + 1] = p.x;
					x[srcPoints.length + 2] = p.y;
					mesh.vertices[i].x = Matrix.multiply(tps[0], x);
					mesh.vertices[i].y = Matrix.multiply(tps[1], x);
				}
			}
			for (Layer subLayer : layer.getSubLayers()) {
				updateTransform(subLayer);
			}
		}

		public void clearTransform() {
			verticesMap.clear();
		}
	}

	public static class FeatureBased extends MeshHandler {
		private Map<String, Vertex[]> verticesMap = new HashMap<String, Vertex[]>();

		@Override
		public Mesh generateMesh(int total, Vertex[] vs, int count) {
			return generateMeshUniformRandom(total, vs, count);
		}

		public void initializeTransform(Layer layer) {
			Path path = layer.getPath();
			if (path.isFinalized()) {
				Mesh mesh = path.getMesh();
				if (mesh.getControlPoints().size() > 1) {
					Vertex[] srcPoints = new Vertex[mesh.vertices.length];
					for (int i = 0; i < srcPoints.length; i++) {
						srcPoints[i] = new Vertex(mesh.vertices[i]);
					}
					System.err.println("putting: " + layer.getName());
					verticesMap.put(layer.getName(), srcPoints);
				}
			}
			for (Layer subLayer : layer.getSubLayers()) {
				initializeTransform(subLayer);
			}
		}

		public void updateTransform(Layer layer) {
			Vertex[] vertices;
			if ((vertices = verticesMap.get(layer.getName())) != null) {
				Mesh mesh = layer.getPath().getMesh();
				List<ControlPoint> controlPoints = mesh.getControlPoints();
				Vertex[] srcPoints = new Vertex[controlPoints.size()];
				Vertex[] tgtPoints = new Vertex[controlPoints.size()];
				for (int i = 0; i < srcPoints.length; i++) {
					srcPoints[i] = vertices[controlPoints.get(i).vIndex];
					tgtPoints[i] = mesh.vertices[controlPoints.get(i).vIndex];
				}
				double[][] tps = livecanvas.ThinPlate.est_tps(srcPoints,
						tgtPoints);
				OUTER: for (int i = 0; i < mesh.vertices.length; i++) {
					Vertex p = vertices[i];
					for (ControlPoint cp : controlPoints) {
						if (mesh.getControlPointVertex(cp) == p) {
							continue OUTER;
						}
					}
					double[] x = new double[srcPoints.length + 3];
					for (int j = 0; j < srcPoints.length; j++) {
						x[j] = livecanvas.ThinPlate.U(livecanvas.ThinPlate
								.ec_norm(srcPoints[j], p));
					}
					x[srcPoints.length] = 1;
					x[srcPoints.length + 1] = p.x;
					x[srcPoints.length + 2] = p.y;
					mesh.vertices[i].x = Matrix.multiply(tps[0], x);
					mesh.vertices[i].y = Matrix.multiply(tps[1], x);
				}
			}
			for (Layer subLayer : layer.getSubLayers()) {
				updateTransform(subLayer);
			}
		}

		public void clearTransform() {
			verticesMap.clear();
		}
	}
}
