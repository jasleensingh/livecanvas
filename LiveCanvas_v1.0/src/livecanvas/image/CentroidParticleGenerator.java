package livecanvas.image;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import livecanvas.BackgroundRef;
import livecanvas.Face;
import livecanvas.Mesh;
import livecanvas.Path;
import livecanvas.Vec3;

import common.typeutils.BooleanType;

public class CentroidParticleGenerator extends
		AbstractParticleGenerator<BlendedColor> {
	@BooleanType(name = "Include Corners")
	public boolean includeCorners = true;

	@Override
	public void preprocess(RenderData data) {
	}

	@Override
	protected Particle<BlendedColor>[] generateForPath(Path path,
			RenderData renderData, BackgroundRef bgrefData) {
		Mesh mesh = path.getMesh();
		List<Particle<BlendedColor>> particles = new ArrayList<Particle<BlendedColor>>();
		Point origin = new Point(renderData.canvasSize.width / 2,
				renderData.canvasSize.height / 2);
		double cx, cy;
		for (int i = 0; i < mesh.faces.length; i++) {
			Face face = mesh.faces[i];
			Vec3 v1 = mesh.vertices[face.v1Index];
			Vec3 v2 = mesh.vertices[face.v2Index];
			Vec3 v3 = mesh.vertices[face.v3Index];
			if (includeCorners) {
				cx = v1.x;
				cy = v1.y;
				particles.add(new Particle<BlendedColor>(origin, mesh, face,
						cx, cy, new BlendedColor(bgrefData)));
				cx = v2.x;
				cy = v2.y;
				particles.add(new Particle<BlendedColor>(origin, mesh, face,
						cx, cy, new BlendedColor(bgrefData)));
				cx = v3.x;
				cy = v3.y;
				particles.add(new Particle<BlendedColor>(origin, mesh, face,
						cx, cy, new BlendedColor(bgrefData)));
			}
			cx = (v1.x + v2.x + v3.x) / 3;
			cy = (v1.y + v2.y + v3.y) / 3;
			particles.add(new Particle<BlendedColor>(origin, mesh, face, cx,
					cy, new BlendedColor(bgrefData)));
		}
		return particles.toArray(new Particle[0]);
	}
}
