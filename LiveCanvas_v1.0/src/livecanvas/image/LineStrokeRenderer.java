package livecanvas.image;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import livecanvas.Progress;

public class LineStrokeRenderer extends AbstractRenderer<LineStroke> {
	@Override
	public void render(Graphics2D g, RenderData data,
			Particle<LineStroke>[] particles, Progress.Indicator progress) {
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
			Particle<LineStroke> p = particles[i];
			g.setStroke(p.packet.stroke);
			Point2D.Double loc = p.getLocation();
			int x1, y1, x2, y2;
			x1 = (int) (loc.x + p.packet.length * Math.cos(p.packet.angle));
			y1 = (int) (loc.y + p.packet.length * Math.sin(p.packet.angle));
			x2 = (int) (loc.x - p.packet.length * Math.cos(p.packet.angle));
			y2 = (int) (loc.y - p.packet.length * Math.sin(p.packet.angle));
			g.setColor(calculateBlendedColor(data, p, loc, p.packet.color));
			g.drawLine(x1, y1, x2, y2);
		}
		state(Progress.DONE);
	}

	@Override
	public boolean supportsBlending() {
		return true;
	}
}
