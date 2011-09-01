package livecanvas.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import livecanvas.Progress;

public class PainterlyRenderer extends AbstractRenderer<CurvedStroke> {
	private static Map<Integer, Stroke> STROKE_CACHE = new HashMap<Integer, Stroke>();

	@Override
	public Paint getBackground() {
		return Color.black;
	}

	@Override
	public void render(Graphics2D g, RenderData data,
			Particle<CurvedStroke>[] particles, Progress.Indicator progress) {
		Color c = g.getColor();
		Stroke s = g.getStroke();
		for (int i = 0; i < particles.length; i++) {
			Particle<CurvedStroke> p = particles[i];
			Point2D.Double loc = p.getLocation();
			loc = new Point2D.Double(loc.x - data.canvasSize.width / 2, loc.y
					- data.canvasSize.height / 2);
			CurvedStroke cs = p.packet;
			float size = cs.size10 / 10.0f;
			Stroke stroke = STROKE_CACHE.get(cs.size10);
			if (stroke == null) {
				stroke = new BasicStroke(size, BasicStroke.CAP_ROUND,
						BasicStroke.JOIN_ROUND);
				STROKE_CACHE.put(cs.size10, stroke);
			}
			g.setColor(cs.color);
			g.setStroke(stroke);
			if (cs.count == 1) {
				g.fillOval((int) (loc.x + cs.xs[0] - size / 2), (int) (loc.y
						+ cs.ys[0] - size / 2), (int) size, (int) size);
			} else {
				Path2D.Float path = new Path2D.Float();
				path.moveTo(loc.x + cs.xs[0], loc.y + cs.ys[0]);
				if (cs.count == 2) {
					path.lineTo(loc.x + cs.xs[1], loc.y + cs.ys[1]);
				} else if (cs.count == 3) {
					path.quadTo(loc.x + cs.xs[1], loc.y + cs.ys[1], loc.x
							+ cs.xs[2], loc.y + cs.ys[2]);
				} else {
					for (int j = 3; j < cs.count; j++) {
						path.curveTo(loc.x + cs.xs[j - 2],
								loc.y + cs.ys[j - 2], loc.x + cs.xs[j - 1],
								loc.y + cs.ys[j - 1], loc.x + cs.xs[j], loc.y
										+ cs.ys[j]);
					}
				}
				g.draw(path);
			}
		}
		g.setColor(c);
		g.setStroke(s);
		state((Boolean) data.packet ? Progress.DONE : Progress.NOT_DONE);
	}
}
