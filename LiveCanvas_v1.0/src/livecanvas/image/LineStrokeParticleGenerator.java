package livecanvas.image;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import livecanvas.BackgroundRef;
import livecanvas.Face;
import livecanvas.Mesh;
import livecanvas.Path;
import livecanvas.Vec3;

import common.typeutils.DoubleType;
import common.typeutils.FloatType;
import common.typeutils.IntegerType;

public class LineStrokeParticleGenerator extends
		AbstractParticleGenerator<LineStroke> {
	@IntegerType(name = "Density", min = 1, max = 100)
	public int density = 40;

	@DoubleType(name = "Angle", min = 0, max = 2 * Math.PI)
	public double angle = Math.PI / 4;

	@FloatType(name = "Angle Jitter", min = 0, max = 1)
	public float angleJitter = 0.5f;

	@FloatType(name = "Length", min = 1, max = 100)
	public float length = 10;

	@FloatType(name = "Length Jitter", min = 0, max = 1)
	public float lengthJitter = 0.1f;

	@FloatType(name = "Color Jitter", min = 0, max = 1)
	public float colorJitter = 0.1f;

	@FloatType(name = "Stroke Size", min = 0.1f, max = 100.0f, step = 0.1f)
	public float strokeSize = 1.0f;

	@Override
	public void preprocess(RenderData data) {
	}

	@Override
	protected Particle<LineStroke>[] generateForPath(Path path,
			RenderData renderData, BackgroundRef bgref) {
		Mesh mesh = path.getMesh();
		List<Particle<LineStroke>> particles = new ArrayList<Particle<LineStroke>>();
		Point origin = new Point(renderData.canvasSize.width / 2,
				renderData.canvasSize.height / 2);
		double cx, cy;
		BlendedColor color;
		Stroke stroke = new BasicStroke(strokeSize);
		for (int i = 0; i < mesh.faces.length; i++) {
			Face face = mesh.faces[i];
			Vec3 v1 = mesh.vertices[face.v1Index];
			Vec3 v2 = mesh.vertices[face.v2Index];
			Vec3 v3 = mesh.vertices[face.v3Index];
			for (int j = 0; j < density; j++) {
				double xv01 = v2.x - v1.x;
				double yv01 = v2.y - v1.y;
				double xv02 = v3.x - v1.x;
				double yv02 = v3.y - v1.y;
				double u = Math.random(), v = Math.random();
				if (u + v >= 1) {
					u = 1 - u;
					v = 1 - v;
				}
				cx = (int) (v1.x + u * xv01 + v * xv02);
				cy = (int) (v1.y + u * yv01 + v * yv02);
				float jitteredLength = (float) (length * (1 + lengthJitter
						* (Math.random() - 0.5)));
				float jitteredAngle = (float) (angle + angleJitter
						* (Math.random() - 0.5) * Math.PI);
				color = new BlendedColor(bgref);
				particles.add(new Particle<LineStroke>(origin, mesh, face, cx,
						cy, new LineStroke(jitteredAngle, jitteredLength,
								stroke, color)));
			}
		}
		return particles.toArray(new Particle[0]);
	}
}
