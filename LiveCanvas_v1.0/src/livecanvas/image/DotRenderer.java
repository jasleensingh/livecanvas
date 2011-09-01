package livecanvas.image;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import livecanvas.Progress;

public class DotRenderer extends AbstractRenderer<BlendedColor> {
	@Override
	public void render(Graphics2D g, RenderData data,
			Particle<BlendedColor>[] particles, Progress.Indicator progress) {
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
			Particle<BlendedColor> p = particles[i];
			Point2D.Double loc = p.getLocation();
			g.setColor(calculateBlendedColor(data, p, loc, p.packet));
			g.fillOval((int) loc.x - 10, (int) loc.y - 10, 20, 20);
		}
		state(Progress.DONE);
	}

	@Override
	public boolean supportsBlending() {
		return true;
	}
}
