package livecanvas.image;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import livecanvas.BackgroundRef;
import livecanvas.Face;
import livecanvas.Mesh;
import livecanvas.Path;
import livecanvas.Vec3;

import common.typeutils.IntegerType;

public class MeshParticleGenerator extends AbstractParticleGenerator<Object> {
	@IntegerType(name = "Density", min = 1, max = 100)
	public int density = 40;

	@Override
	public void preprocess(RenderData data) {
	}

	@Override
	protected Particle<Object>[] generateForPath(Path path,
			RenderData renderData, BackgroundRef bgref) {
		Mesh mesh = path.getMesh();
		List<Particle<Object>> particles = new ArrayList<Particle<Object>>();
		Point origin = new Point(renderData.canvasSize.width / 2,
				renderData.canvasSize.height / 2);
		double cx, cy;
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
				particles.add(new Particle<Object>(origin, mesh, face, cx, cy,
						null));
			}
		}
		return particles.toArray(new Particle[0]);
	}
}
