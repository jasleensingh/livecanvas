package livecanvas.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import livecanvas.Progress;

public class MosaicRenderer extends AbstractRenderer<MosaicTile> {
	private static Map<Integer, Stroke> STROKE_CACHE = new HashMap<Integer, Stroke>();

	@Override
	public Paint getBackground() {
		return new Color(0x22221A);
	}

	@Override
	public void render(Graphics2D g, RenderData data,
			Particle<MosaicTile>[] particles, Progress.Indicator progress) {
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
			Particle<MosaicTile> p = particles[i];
			Point2D.Double loc = p.getLocation();
			MosaicTile tile = p.packet;
			g.setColor(calculateBlendedColor(data, p, loc, tile.fillColor));
			AffineTransform t = g.getTransform();
			g.translate(loc.x, loc.y);
			g.rotate(tile.angle);
			g.fillRect(tile.fillRect.x, tile.fillRect.y, tile.fillRect.width,
					tile.fillRect.height);
			if (!tile.fillColor.equals(getBackground())) {
				g.setStroke(tile.drawStroke);
				g.setColor(tile.drawColor);
				g.drawRect(tile.drawRect.x, tile.drawRect.y,
						tile.drawRect.width, tile.drawRect.height);
			}
			g.setTransform(t);
		}
	}

	@Override
	public boolean supportsBlending() {
		return true;
	}
}
