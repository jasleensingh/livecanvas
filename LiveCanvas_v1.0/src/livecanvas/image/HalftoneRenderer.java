package livecanvas.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

import common.typeutils.IntegerType;

import livecanvas.Progress;

public class HalftoneRenderer extends AbstractRenderer<HalftoneParticle> {
	@IntegerType(name = "Max Size")
	public int maxSize = 10;

	@Override
	public void render(Graphics2D g, RenderData data,
			Particle<HalftoneParticle>[] particles, Progress.Indicator progress) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
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
			Particle<HalftoneParticle> p = particles[i];
			Point2D.Double loc = p.getLocation();
			Color color = calculateBlendedColor(data, p, loc, p.packet.color);
			int threshold_gray = (int) (p.packet.threshold * 0xff);
			int gray = gray(color);
			if (gray < threshold_gray) {
				g.setColor(Color.black);
				int size = maxSize * (threshold_gray - gray) / threshold_gray;
				g.fillOval((int) loc.x - size / 2, (int) loc.y - size / 2,
						size, size);
			}
		}
		state(Progress.DONE);
	}

	private int gray(Color color) {
		int rgb = color.getRGB();
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = (rgb >> 0) & 0xff;
		int gray = (r + g + b) / 3;
		return gray;
	}

	@Override
	public boolean supportsBlending() {
		return true;
	}
}
