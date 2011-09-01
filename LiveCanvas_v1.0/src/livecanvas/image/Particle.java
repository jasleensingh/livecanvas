package livecanvas.image;

import java.awt.Point;
import java.awt.geom.Point2D;

import livecanvas.Face;
import livecanvas.Mesh;
import livecanvas.Vec2;
import livecanvas.Vec3;
import livecanvas.animator.Vertex;
import Jama.Matrix;

public class Particle<T> extends Vec2 {
	public final Point origin;
	public final Mesh mesh;
	public final Face face;
	public T packet;
	private Point2D.Double faceCenter;
	// location in local (triangle) coordinate system
	private double[] local;

	public Particle(Point origin, Mesh mesh, Face face, T packet) {
		this(origin, mesh, face, 0, 0, packet);
	}

	public Particle(Point origin, Mesh mesh, Face face, double x, double y,
			T packet) {
		super(x, y);
		this.origin = origin;
		this.mesh = mesh;
		this.face = face;
		this.packet = packet;
		update();
	}

	public void blend() {
		Point2D.Double loc = getLocation();
		x = loc.x - origin.x;
		y = loc.y - origin.y;
		update();
	}

	private void update() {
		Vertex v1 = mesh.vertices[face.v1Index];
		Vertex v2 = mesh.vertices[face.v2Index];
		Vertex v3 = mesh.vertices[face.v3Index];
		Matrix M_inv = new Matrix(new double[][] { { v1.x, v2.x, v3.x },
				{ v1.y, v2.y, v3.y }, { 1, 1, 1 } }).inverse();
		local = livecanvas.Matrix.multiply(M_inv.getArray(), new double[] { x,
				y, 1 });
		faceCenter = new Point2D.Double((v1.x + v2.x + v3.x) / 3,
				(v1.y + v2.y + v3.y) / 3);
	}

	private Point2D.Double faceDisplacement = new Point2D.Double();

	private Point2D.Double getFaceDisplacement() {
		Vec3 v1 = mesh.vertices[face.v1Index];
		Vec3 v2 = mesh.vertices[face.v2Index];
		Vec3 v3 = mesh.vertices[face.v3Index];
		faceDisplacement.setLocation((v1.x + v2.x + v3.x) / 3 - faceCenter.x,
				(v1.y + v2.y + v3.y) / 3 - faceCenter.y);
		return faceDisplacement;
	}

	protected Point2D.Double location = new Point2D.Double();

	public Point2D.Double getLocation2() {
		Point2D.Double fd = getFaceDisplacement();
		location.setLocation(x + fd.x + origin.x, y + fd.y + origin.y);
		return location;
	}

	public Point2D.Double getLocation() {
		Vec3 v1 = mesh.vertices[face.v1Index];
		Vec3 v2 = mesh.vertices[face.v2Index];
		Vec3 v3 = mesh.vertices[face.v3Index];
		double x = v1.x * local[0] + v2.x * local[1] + v3.x * local[2];
		double y = v1.y * local[0] + v2.y * local[1] + v3.y * local[2];
		location.setLocation(x + origin.x, y + origin.y);
		return location;
	}
}