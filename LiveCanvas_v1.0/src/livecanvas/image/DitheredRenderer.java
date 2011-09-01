package livecanvas.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import livecanvas.Progress;

public class DitheredRenderer extends AbstractRenderer<Color> {
	@Override
	public void render(Graphics2D g, RenderData data,
			Particle<Color>[] particles, Progress.Indicator progress) {
		// Polygon polygon = new Polygon(new int[3], new int[3], 3);
		int n = -1, nn, steps = 10;
		for (int i = 0; i < particles.length; i++) {
			if (progress.isCanceled()) {
				state(Progress.CANCELED);
				return;
			}
			if ((nn = i * steps / particles.length) != n) {
				n = nn;
				progress.setProgress(String.format("%d%% complete", nn * 10),
						(double) n / steps);
			}
			Particle<Color> p = particles[i];
			g.setColor(p.packet);
			Point2D.Double loc = p.getLocation();
			g.fillRect((int) loc.x - 1, (int) loc.y - 1, 1, 1);
			// g.setColor(p.packet);
			// Vertex v1 = p.mesh.vertices[p.face.v1Index];
			// Vertex v2 = p.mesh.vertices[p.face.v2Index];
			// Vertex v3 = p.mesh.vertices[p.face.v3Index];
			// polygon.xpoints[0] = (int) v1.x + p.origin.x;
			// polygon.ypoints[0] = (int) v1.y + p.origin.y;
			// polygon.xpoints[1] = (int) v2.x + p.origin.x;
			// polygon.ypoints[1] = (int) v2.y + p.origin.y;
			// polygon.xpoints[2] = (int) v3.x + p.origin.x;
			// polygon.ypoints[2] = (int) v3.y + p.origin.y;
			// g.drawPolygon(polygon);
		}
		state(Progress.DONE);
	}
}
