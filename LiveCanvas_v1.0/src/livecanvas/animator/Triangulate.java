package livecanvas.animator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.sets.ConstrainedPointSet;
import org.poly2tri.triangulation.sets.PointSet;

public class Triangulate extends JPanel {

	private List<DelaunayTriangle> triangles;

	public Triangulate() {
		super(null);
		setPreferredSize(new Dimension(600, 550));
		setBackground(Color.white);
		createPolygon();
	}

	private void createPolygon() {
		int width = getPreferredSize().width;
		int height = getPreferredSize().height;
		int cx = width / 2;
		int cy = height / 2;
		int radius = Math.min(width, height) / 3;
		int count = 10;
		int[] xs = new int[count];
		int[] ys = new int[count];
		for (int i = 0; i < count; i++) {
			double angle = i * 2 * Math.PI / count;
			int x = cx + (int) (radius * Math.cos(angle));
			int y = cy + (int) (radius * Math.sin(angle));
			xs[i] = x;
			ys[i] = y;
		}
		triangles = generateMesh(100, xs, ys, count);
	}

	private List<DelaunayTriangle> generateMesh(int total, int[] xs, int[] ys,
			int count) {
		int nIterations = 10;
		List<TriangulationPoint> internal = new ArrayList<TriangulationPoint>(
				Arrays.asList(generateRandomPoints(total, xs, ys, count)));
		PointSet pointSet = null;
		TriangulationPoint[] outlinePointsArray = new TriangulationPoint[count];
		for (int i = 0; i < count; i++) {
			outlinePointsArray[i] = new TPoint(xs[i], ys[i]);
		}
		List<TriangulationPoint> constraints = new ArrayList<TriangulationPoint>();
		for (int i = 0; i < count; i++) {
			outlinePointsArray[i] = new TPoint(xs[i], ys[i]);
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
			for (DelaunayTriangle t : removeOuter(outline,
					pointSet.getTriangles())) {
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
				double sxsum = 0, sysum = 0;
				int scount = 0;
				for (TriangulationPoint other : point2Edges.get(p)) {
					sxsum += other.getX();
					sysum += other.getY();
					scount++;
				}
				TPoint np = new TPoint(sxsum / scount, sysum / scount, 0);
				internal.add(np);
			}
		}
		return removeOuter(outline, pointSet.getTriangles());
	}

	public TriangulationPoint[] generateRandomPoints(int total, int[] xs,
			int[] ys, int count) {
		if (count < 3) {
			throw new IllegalStateException("Insufficient number of points: "
					+ count);
		}
		List<PolygonPoint> points = new ArrayList<PolygonPoint>();
		for (int i = 0; i < count; i++) {
			points.add(new PolygonPoint(xs[i], ys[i]));
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
			// System.err.println(areaSum + "," + guess);
			int index = Arrays.binarySearch(cumulativeArea, guess);
			if (index < 0) {
				index = -index - 1;
			}
			// System.err.println(index);
			DelaunayTriangle tri = triangles[index];
			double x0 = tri.points[0].getX();
			double y0 = tri.points[0].getY();
			double x1 = tri.points[1].getX();
			double y1 = tri.points[1].getY();
			double x2 = tri.points[2].getX();
			double y2 = tri.points[2].getY();
			double xv01 = x1 - x0;
			double yv01 = y1 - y0;
			double xv02 = x2 - x0;
			double yv02 = y2 - y0;
			double u = Math.random(), v = Math.random();
			if (u + v >= 1) {
				u = 1 - u;
				v = 1 - v;
			}
			generated[i] = new TPoint(x0 + u * xv01 + v * xv02, y0 + u * yv01
					+ v * yv02);
		}
		return generated;
	}

	private List<DelaunayTriangle> removeOuter(
			List<TriangulationPoint> outline, List<DelaunayTriangle> triangles) {
		DelaunayTriangle[] array = triangles.toArray(new DelaunayTriangle[0]);
		for (DelaunayTriangle tri : array) {
			boolean possiblyOutside = true;
			for (TriangulationPoint p : tri.points) {
				if (!outline.contains(p)) {
					possiblyOutside = false;
					break;
				}
			}
			if (possiblyOutside) {
				// check if all 3 points belong to at least one other face
				boolean[] found = new boolean[3];
				for (DelaunayTriangle tri2 : array) {
					if (tri2 == tri) {
						continue;
					}
					for (int i = 0; i < tri.points.length; i++) {
						TriangulationPoint p = tri.points[i];
						for (TriangulationPoint p2 : tri2.points) {
							if (p.equals(p2)) {
								found[i] = true;
							}
						}
					}
				}
				for (int i = 0; i < 3; i++) {
					if (!found[i]) {
						possiblyOutside = false;
					}
				}
			}
			if (possiblyOutside) { // definitly outside
				triangles.remove(tri);
			}
		}
		return triangles;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (triangles == null) {
			return;
		}
		g.setColor(Color.black);
		int x1, y1, x2, y2;
		for (int i = 0; i < triangles.size(); i++) {
			DelaunayTriangle tri = triangles.get(i);
			x1 = (int) tri.points[0].getX();
			y1 = (int) tri.points[0].getY();
			for (int j = 1; j <= tri.points.length; j++) {
				x2 = (int) tri.points[j % tri.points.length].getX();
				y2 = (int) tri.points[j % tri.points.length].getY();
				g.drawLine(x1, y1, x2, y2);
				x1 = x2;
				y1 = y2;
			}
		}
	}

	public static void main(String[] args) {
		final JDialog d = new JDialog((JFrame) null, "Triangulate", true);
		d.getContentPane().add(new Triangulate());
		d.pack();
		d.setLocationRelativeTo(null);
		d.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					d.setVisible(false);
				}
			}
		});
		d.setVisible(true);
		System.exit(0);
	}
}
