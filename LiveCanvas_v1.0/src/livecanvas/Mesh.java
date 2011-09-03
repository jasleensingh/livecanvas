package livecanvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import livecanvas.animator.Vertex;
import livecanvas.components.LayersView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

public class Mesh {
	public Vertex[] vertices;
	public Face[] faces;
	private List<ControlPoint> controlPoints = new ArrayList<ControlPoint>();

	public Mesh() {
	}

	public Mesh(Vertex[] vertices, Face[] faces) {
		this.vertices = vertices;
		this.faces = faces;
	}

	public void setVertexIndices() {
		for (int i = 0; i < vertices.length; i++) {
			vertices[i].index = i;
		}
	}

	public void setFaceIndices() {
		for (int i = 0; i < faces.length; i++) {
			faces[i].index = i;
		}
	}

	public void toggleControlPointAt(int x, int y) {
		Vertex p = findNearestMeshVertex(x, y);
		if (p == null) {
			return;
		}
		ControlPoint cp = new ControlPoint(p.index);
		if (controlPoints.contains(cp)) {
			removeControlPoint(cp);
		} else {
			addControlPoint(cp);
		}
	}

	public void addControlPoint(ControlPoint cp) {
		if (controlPoints.contains(cp)) {
			return;
		}
		controlPoints.add(cp);
	}

	public void removeControlPoint(ControlPoint cp) {
		controlPoints.remove(cp);
	}

	public Vertex findNearestMeshVertex(int x, int y) {
		Vertex min = null;
		double minDist = Double.MAX_VALUE;
		double d;
		for (Vertex p : vertices) {
			if ((d = Utils.distance(p.x, p.y, x, y)) < minDist) {
				minDist = d;
				min = p;
			}
		}
		return min;
	}

	public List<ControlPoint> getControlPoints() {
		return controlPoints;
	}

	public int getControlPointsCount() {
		return controlPoints.size();
	}

	public Vertex getControlPointVertex(ControlPoint cp) {
		return vertices[cp.vIndex];
	}

	public Vertex getControlPointVertex(int cpIndex) {
		return vertices[controlPoints.get(cpIndex).vIndex];
	}

	public Vertex[] getPathVertices() {
		List<Vertex> pathVertexList = new ArrayList<Vertex>();
		for (Vertex v : vertices) {
			if (v.onPath) {
				pathVertexList.add(v);
			}
		}
		return pathVertexList.toArray(new Vertex[0]);
	}

	private Rectangle2D.Double _bounds = new Rectangle2D.Double();

	public Rectangle2D.Double getBounds() {
		double minx, miny, maxx, maxy;
		minx = maxx = vertices[0].x;
		miny = maxy = vertices[0].y;
		for (int i = 1; i < vertices.length; i++) {
			minx = Math.min(minx, vertices[i].x);
			miny = Math.min(miny, vertices[i].y);
			maxx = Math.max(maxx, vertices[i].x);
			maxy = Math.max(maxy, vertices[i].y);
		}
		_bounds.setRect(minx, miny, maxx - minx, maxy - miny);
		return _bounds;
	}

	private double[] in_d = new double[4];
	private double[] out_d = new double[4];

	public void applyTransform(Transform3 t) {
		for (int i = 0; i < vertices.length; i++) {
			in_d[0] = vertices[i].x;
			in_d[1] = vertices[i].y;
			in_d[2] = vertices[i].z;
			in_d[3] = 1;
			Transform3.apply(t, in_d, out_d);
			vertices[i].x = out_d[0];
			vertices[i].y = out_d[1];
			vertices[i].z = out_d[2];
		}
	}

	private Stroke meshStroke = new BasicStroke(1.0f);

	public void draw(Graphics2D g) {
		g.setStroke(meshStroke);
		g.setColor(LayersView.settings.meshColor);
		Vertex[] vs = new Vertex[3];
		int x1, y1, x2, y2;
		for (Face face : faces) {
			vs[0] = vertices[face.v1Index];
			vs[1] = vertices[face.v2Index];
			vs[2] = vertices[face.v3Index];
			for (int i = 0; i < 3; i++) {
				Vertex v1 = vs[i];
				Vertex v2 = vs[(i + 1) % 3];
				x1 = (int) v1.x;
				y1 = (int) v1.y;
				x2 = (int) v2.x;
				y2 = (int) v2.y;
				g.drawLine(x1, y1, x2, y2);
			}
		}
		g.setColor(Color.red);
		for (ControlPoint cp : getControlPoints()) {
			Vertex v = vertices[cp.vIndex];
			x1 = (int) v.x;
			y1 = (int) v.y;
			g.fillOval(x1 - 6, y1 - 6, 12, 12);
		}
		CanvasMesh.meshHandler.onDrawMesh(this, g);
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		JSONArray jsonVertices = new JSONArray();
		for (Vertex v : vertices) {
			jsonVertices.put(v.toJSON());
		}
		json.put("vertices", jsonVertices);
		JSONArray jsonFaces = new JSONArray();
		for (Face f : faces) {
			jsonFaces.put(f.toJSON());
		}
		json.put("faces", jsonFaces);
		JSONArray controlPointsArray = new JSONArray();
		for (ControlPoint cp : controlPoints) {
			controlPointsArray.put(cp.toJSON());
		}
		json.put("controlPoints", controlPointsArray);
		return json;
	}

	public static Mesh fromJSON(JSONObject json) throws JSONException {
		Mesh mesh = new Mesh();
		JSONArray jsonVertices = json.getJSONArray("vertices");
		mesh.vertices = new Vertex[jsonVertices.length()];
		for (int i = 0; i < mesh.vertices.length; i++) {
			mesh.vertices[i] = Vertex.fromJSON(jsonVertices.getJSONObject(i));
		}
		JSONArray jsonFaces = json.getJSONArray("faces");
		mesh.faces = new Face[jsonFaces.length()];
		for (int i = 0; i < mesh.faces.length; i++) {
			mesh.faces[i] = Face.fromJSON(jsonFaces.getJSONObject(i));
		}
		JSONArray controlPointsArray = json.optJSONArray("controlPoints");
		if (controlPointsArray != null) {
			for (int i = 0; i < controlPointsArray.length(); i++) {
				mesh.controlPoints.add(ControlPoint.fromJSON(controlPointsArray
						.getJSONObject(i)));
			}
		}
		return mesh;
	}

	// doesn't work for some cases e.g bird beak type shape
	public static List<DelaunayTriangle> removeOuter(
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
					for (int i = 0; i < 3; i++) {
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
			if (possiblyOutside) { // definitely outside
				triangles.remove(tri);
			}
		}
		return triangles;
	}

	public static List<DelaunayTriangle> removeOuter2(
			List<TriangulationPoint> outline, List<DelaunayTriangle> triangles) {
		int npoints = outline.size();
		int[] xpoints = new int[npoints];
		int[] ypoints = new int[npoints];
		for (int i = 0; i < npoints; i++) {
			TriangulationPoint p = outline.get(i);
			xpoints[i] = (int) p.getX();
			ypoints[i] = (int) p.getY();
		}
		Polygon polygon = new Polygon(xpoints, ypoints, npoints);
		DelaunayTriangle[] array = triangles.toArray(new DelaunayTriangle[0]);
		for (DelaunayTriangle tri : array) {
			double cx = 0, cy = 0;
			for (TriangulationPoint p : tri.points) {
				cx += p.getX();
				cy += p.getY();
			}
			cx /= tri.points.length;
			cy /= tri.points.length;
			if (!polygon.contains(cx, cy)) {
				triangles.remove(tri);
			}
		}
		return triangles;
	}

	public static Mesh toMesh(DelaunayTriangle[] triangles,
			TriangulationPoint[] points, int nPathPoints) {
		Map<TriangulationPoint, Vertex> point2Vertex = new HashMap<TriangulationPoint, Vertex>();
		Vertex[] meshVertices = new Vertex[points.length];
		for (int i = 0; i < points.length; i++) {
			Vertex vertex = new Vertex(points[i].getX(), points[i].getY(),
					points[i].getZ(), i < nPathPoints);
			point2Vertex.put(points[i], meshVertices[i] = vertex);
		}
		Mesh mesh = new Mesh();
		mesh.vertices = meshVertices;
		mesh.setVertexIndices();
		mesh.faces = new Face[triangles.length];
		// Edge[] edges = new Edge[faces.length * 3];
		for (int i = 0; i < triangles.length; i++) {
			DelaunayTriangle t = triangles[i];
			Vertex v1 = point2Vertex.get(t.points[0]);
			Vertex v2 = point2Vertex.get(t.points[1]);
			Vertex v3 = point2Vertex.get(t.points[2]);
			// Edge e12 = new Edge(v1, v2);
			// Edge e23 = new Edge(v2, v3);
			// Edge e31 = new Edge(v3, v1);
			// v1.addEdge(e31);
			// v1.addEdge(e12);
			// v2.addEdge(e12);
			// v2.addEdge(e23);
			// v3.addEdge(e23);
			// v3.addEdge(e31);
			mesh.faces[i] = new Face(v1.index, v2.index, v3.index);
		}
		mesh.setFaceIndices();
		return mesh;
	}

	public static Mesh toMesh(int total, BufferedImage image) {
		Mesh mesh = new Mesh();
		int imgWidth = image.getWidth();
		int imgHeight = image.getHeight();
		int area = imgWidth * imgHeight;
		int nx = (int) (Math.sqrt((double) total / area) * imgWidth);
		int ny = (int) (Math.sqrt((double) total / area) * imgHeight);
		double gridSizeX = imgWidth / nx;
		double gridSizeY = imgHeight / ny;
		Vertex[] vertices = new Vertex[(nx + 1) * (ny + 1)];
		for (int i = 0; i <= ny; i++) {
			for (int j = 0; j <= nx; j++) {
				vertices[i * (ny + 1) + j] = new Vertex(j * gridSizeX, i
						* gridSizeY, 0, false);
			}
		}
		mesh.vertices = vertices;
		mesh.setVertexIndices();
		Face[] faces = new Face[nx * ny * 2];
		for (int i = 0; i < ny; i++) {
			for (int j = 0; j < nx; j++) {
				Vertex v1 = vertices[i * (ny + 1) + j];
				Vertex v2 = vertices[i * (ny + 1) + j + 1];
				Vertex v3 = vertices[(i + 1) * (ny + 1) + j];
				Vertex v4 = vertices[(i + 1) * (ny + 1) + j + 1];
				faces[(i * ny + j) * 2] = new Face(v1.index, v2.index, v3.index);
				faces[(i * ny + j) * 2 + 1] = new Face(v2.index, v3.index,
						v4.index);
			}
		}
		mesh.faces = faces;
		mesh.setFaceIndices();
		return mesh;
	}
}