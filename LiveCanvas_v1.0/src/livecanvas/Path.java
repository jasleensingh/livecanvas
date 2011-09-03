package livecanvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

import livecanvas.animator.Vertex;
import livecanvas.components.LayersView;
import livecanvas.components.LayersView.LayersViewSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Path {
	public Vertex[] vs;
	public Shape shape;
	public int count;
	private boolean finalized;
	private Mesh mesh;

	public Path() {
		vs = new Vertex[32];
	}

	public Path(Path copy) {
		this.count = copy.count;
		vs = new Vertex[copy.count];
		for (int i = 0; i < vs.length; i++) {
			vs[i] = new Vertex(copy.vs[i]);
		}
	}

	public Path(Path p1, Path p2, double interpolation) {
		Vertex[] p1vs, p2vs;
		if (p1.count < p2.count) {
			this.count = p2.count;
			p2vs = p2.vs;
			double dist = 0;
			Vertex last;
			last = p1.vs[0];
			for (int i = 1; i < p1.count; i++) {
				Vertex v = p1.vs[i];
				double dx = v.x - last.x;
				double dy = v.y - last.y;
				double dz = v.z - last.z;
				dist += Math.sqrt(dx * dx + dy * dy + dz * dz);
				last = v;
			}
			p1vs = Utils.subdivide(p1.vs, dist / (p2.count - 1));
		} else if (p1.count > p2.count) {
			this.count = p1.count;
			p1vs = p1.vs;
			double pathLength = 0;
			Vertex last;
			last = p2.vs[0];
			for (int i = 1; i < p2.count; i++) {
				Vertex v = p2.vs[i];
				double dx = v.x - last.x;
				double dy = v.y - last.y;
				double dz = v.z - last.z;
				pathLength += Math.sqrt(dx * dx + dy * dy + dz * dz);
				last = v;
			}
			p2vs = Utils.subdivide(p2.vs, pathLength / (p1.count - 1));
		} else {
			this.count = p1.count;
			p1vs = p1.vs;
			p2vs = p2.vs;
		}
		vs = new Vertex[count];
		for (int i = 0; i < vs.length; i++) {
			vs[i] = new Vertex(p1vs[i], p2vs[i], interpolation);
		}
	}

	public Path join(Path with, double newZ) {
		if (finalized) {
			throw new IllegalStateException("Path already finalized!");
		}
		Area join = new Area(new java.awt.Polygon(Utils.toIntArray(vs, 0),
				Utils.toIntArray(vs, 1), count));
		join.add(new Area(new java.awt.Polygon(Utils.toIntArray(with.vs, 0),
				Utils.toIntArray(with.vs, 1), with.count)));
		Point[] pathPoints = Utils.createPathFromPathIterator(join
				.getPathIterator(null));
		clear();
		for (Point p : pathPoints) {
			add(p.x, p.y, newZ);
		}
		if (with.finalized) {
			finalizePath();
		}
		return this;
	}

	public Path intersect(Path with, double newZ) {
		if (finalized) {
			throw new IllegalStateException("Path already finalized!");
		}
		Area intersect = new Area(new java.awt.Polygon(Utils.toIntArray(vs, 0),
				Utils.toIntArray(vs, 1), count));
		intersect.intersect(new Area(new java.awt.Polygon(Utils.toIntArray(
				with.vs, 0), Utils.toIntArray(with.vs, 1), with.count)));
		Point[] pathPoints = Utils.createPathFromPathIterator(intersect
				.getPathIterator(null));
		clear();
		for (Point p : pathPoints) {
			add(p.x, p.y, newZ);
		}
		if (with.finalized) {
			finalizePath();
		}
		return this;
	}

	public Path subtract(Path with, double newZ) {
		if (finalized) {
			throw new IllegalStateException("Path already finalized!");
		}
		Area subtract = new Area(new java.awt.Polygon(Utils.toIntArray(vs, 0),
				Utils.toIntArray(vs, 1), count));
		subtract.subtract(new Area(new java.awt.Polygon(Utils.toIntArray(
				with.vs, 0), Utils.toIntArray(with.vs, 1), with.count)));
		Point[] pathPoints = Utils.createPathFromPathIterator(subtract
				.getPathIterator(null));
		clear();
		for (Point p : pathPoints) {
			add(p.x, p.y, newZ);
		}
		if (with.finalized) {
			finalizePath();
		}
		return this;
	}

	public boolean isFinalized() {
		return finalized;
	}

	public Mesh finalizePath() {
		Rectangle bbox = getBounds();
		int area = bbox.width * bbox.height;
		int total = (int) (area * LayersViewSettings
				.meshDensity2ParticlesBBoxRatio(LayersView.settings.meshDensity));
		mesh = CanvasMesh.meshHandler.generateMesh(total, vs, count);
		vs = mesh.getPathVertices();
		createShape();
		finalized = true;
		return mesh;
	}

	private void createShape() {
		GeneralPath gp = new GeneralPath();
		gp.moveTo(vs[0].x, vs[0].y);
		for (int i = 1; i < vs.length; i++) {
			gp.lineTo(vs[i].x, vs[i].y);
		}
		gp.closePath();
		shape = gp;
	}

	public void add(double x, double y, double z) {
		if (finalized) {
			throw new IllegalStateException("Path already finalized!");
		}
		if (count >= vs.length) {
			Vertex[] nvs = new Vertex[vs.length * 2];
			System.arraycopy(vs, 0, nvs, 0, vs.length);
			vs = nvs;
		}
		vs[count] = new Vertex(x, y, z, true);
		count++;
		mesh = null;
	}

	public void clear() {
		count = 0;
		mesh = null;
		finalized = false;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++) {
			sb.append((i > 0 ? ";" : "") + vs[i].x + " " + vs[i].y);
		}
		return sb.toString();
	}

	public static Path fromString(String s) {
		String[] tok = s.split(";");
		Path path = new Path();
		for (int i = 0; i < tok.length; i++) {
			String[] n = tok[i].split(" ");
			path.add(Double.parseDouble(n[0]), Double.parseDouble(n[1]),
					Double.parseDouble(n[2]));
		}
		return path;
	}

	private Rectangle bounds = new Rectangle(0, 0, -1, -1);

	public Rectangle getBounds() {
		double minx, miny, maxx, maxy;
		if (count <= 0) {
			bounds.setBounds(0, 0, 0, 0);
		} else {
			minx = maxx = vs[0].x;
			miny = maxy = vs[0].y;
			for (int i = 1; i < count; i++) {
				minx = Math.min(minx, vs[i].x);
				miny = Math.min(miny, vs[i].y);
				maxx = Math.max(maxx, vs[i].x);
				maxy = Math.max(maxy, vs[i].y);
			}
			bounds.setRect(minx, miny, maxx - minx, maxy - miny);
		}
		return bounds;
	}

	private double[] in_d = new double[4];
	private double[] out_d = new double[4];

	public void applyTransform(Transform3 t) {
		if (mesh == null) {
			for (int i = 0; i < count; i++) {
				in_d[0] = vs[i].x;
				in_d[1] = vs[i].y;
				in_d[2] = vs[i].z;
				in_d[3] = 1;
				Transform3.apply(t, in_d, out_d);
				vs[i].x = out_d[0];
				vs[i].y = out_d[1];
				vs[i].z = out_d[2];
			}
		} else {
			mesh.applyTransform(t);
		}
	}

	public Mesh getMesh() {
		if (!isFinalized()) {
			throw new IllegalStateException("Path not finalized yet!");
		}
		return mesh;
	}

	public static Path fromBackgroundRef(BackgroundRef bgref, double density) {
		Dimension size = bgref.getSize();
		int area = size.width * size.height;
		int total = (int) (area * density);
		int nx = (int) (Math.sqrt((double) total / area) * size.width);
		int ny = (int) (Math.sqrt((double) total / area) * size.height);
		// use slightly smaller area of the image to prevent out of bounds
		// errors
		double gridSizeX = (double) (size.width - 4) / nx;
		double gridSizeY = (double) (size.height - 4) / ny;
		Mesh mesh = new Mesh();
		Vertex[] vertices = new Vertex[(nx + 1) * (ny + 1)];
		for (int i = 0; i <= ny; i++) {
			for (int j = 0; j <= nx; j++) {
				vertices[i * (nx + 1) + j] = new Vertex(2 + j * gridSizeX
						- size.width / 2 + bgref.offset.x, 2 + i * gridSizeY - size.height / 2 + bgref.offset.y,
						0, i == 0 || j == 0 || i == ny || j == nx);
			}
		}
		// Reorder boundary vertices so that they form a continuous path
		Path path = new Path();
		path.vs = new Vertex[(nx + ny) * 2];
		path.count = path.vs.length;
		int c = 0;
		Vertex v;
		for (int i = 0; i <= nx; i++) {
			path.vs[c++] = v = vertices[i];
			Utils.ensure(v.onPath);
		}
		for (int i = 1; i <= ny; i++) {
			path.vs[c++] = v = vertices[i * (nx + 1) + nx];
			Utils.ensure(v.onPath);
		}
		for (int i = nx - 1; i >= 0; i--) {
			path.vs[c++] = v = vertices[ny * (nx + 1) + i];
			Utils.ensure(v.onPath);
		}
		for (int i = ny - 1; i > 0; i--) {
			path.vs[c++] = v = vertices[i * (nx + 1)];
			Utils.ensure(v.onPath);
		}
		c = 0;
		mesh.vertices = new Vertex[vertices.length];
		for (int i = 0; i < path.count; i++) {
			mesh.vertices[c++] = path.vs[i];
		}
		for (int i = 0; i < vertices.length; i++) {
			if (!vertices[i].onPath) {
				mesh.vertices[c++] = vertices[i];
			}
		}
		mesh.setVertexIndices();
		for (int i = 0; i < path.count; i++) {
			mesh.addControlPoint(new ControlPoint(path.vs[i].index));
		}
		Face[] faces = new Face[nx * ny * 2];
		for (int i = 0; i < ny; i++) {
			for (int j = 0; j < nx; j++) {
				Vertex v1 = vertices[i * (nx + 1) + j];
				Vertex v2 = vertices[i * (nx + 1) + j + 1];
				Vertex v3 = vertices[(i + 1) * (nx + 1) + j];
				Vertex v4 = vertices[(i + 1) * (nx + 1) + j + 1];
				faces[(i * nx + j) * 2] = new Face(v1.index, v2.index, v3.index);
				faces[(i * nx + j) * 2 + 1] = new Face(v2.index, v3.index,
						v4.index);
			}
		}
		mesh.faces = faces;
		mesh.setFaceIndices();
		path.mesh = mesh;
		path.createShape();
		path.finalized = true;
		return path;
	}

	private Stroke outlineStroke = new BasicStroke(3.0f);

	public void draw(Graphics2D g, boolean showMesh) {
		if (count <= 0) {
			return;
		}
		if (showMesh && isFinalized()) {
			mesh.draw(g);
		}
		g.setStroke(outlineStroke);
		g.setColor(Color.black);
		int x1, y1, x2, y2;
		x1 = (int) vs[0].x;
		y1 = (int) vs[0].y;
		for (int i = 1; i < count; i++) {
			x2 = (int) vs[i].x;
			y2 = (int) vs[i].y;
			g.drawLine(x1, y1, x2, y2);
			x1 = x2;
			y1 = y2;
		}
		x2 = (int) vs[0].x;
		y2 = (int) vs[0].y;
		g.drawLine(x1, y1, x2, y2);
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		if (mesh == null) {
			JSONArray jsonVertices = new JSONArray();
			for (int i = 0; i < count; i++) {
				jsonVertices.put(vs[i].toJSON());
			}
			json.put("vs", jsonVertices);
		} else {
			json.put("mesh", mesh.toJSON());
		}
		json.put("finalized", finalized);
		return json;
	}

	public static Path fromJSON(JSONObject json) throws JSONException {
		Path path = new Path();
		JSONObject meshObj = json.optJSONObject("mesh");
		if (meshObj == null) {
			JSONArray jsonVertices = json.getJSONArray("vs");
			path.count = jsonVertices.length();
			path.vs = new Vertex[path.count];
			for (int i = 0; i < path.count; i++) {
				path.vs[i] = Vertex.fromJSON(jsonVertices.getJSONObject(i));
			}
		} else {
			path.mesh = Mesh.fromJSON(meshObj);
			path.vs = path.mesh.getPathVertices();
			path.count = path.vs.length;
		}
		path.finalized = json.getBoolean("finalized");
		if (path.finalized) {
			path.createShape();
		}
		return path;
	}
}