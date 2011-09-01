package livecanvas.image.painterly;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Map;

public class OneStroke {
	private static Map<Integer, Stroke> STROKE_CACHE = new HashMap<Integer, Stroke>();
	private int[] xs, ys;
	private int n;
	private int size10;
	private Color color;

	public OneStroke(int x, int y, float size, Color color) {
		xs = new int[1];
		ys = new int[1];
		add(x, y);
		this.size10 = (int) (size * 10);
		this.color = color;
	}

	public void add(int x, int y) {
		if (n == xs.length) {
			grow();
		}
		xs[n] = x;
		ys[n] = y;
		n++;
	}

	private void grow() {
		int[] nxs = new int[xs.length * 2];
		int[] nys = new int[ys.length * 2];
		System.arraycopy(xs, 0, nxs, 0, n);
		System.arraycopy(ys, 0, nys, 0, n);
		xs = nxs;
		ys = nys;
	}

	public void paint(Graphics2D g) {
		float size = size10 / 10.0f;
		Color c = g.getColor();
		Stroke s = g.getStroke();
		Stroke stroke = STROKE_CACHE.get(size10);
		if (stroke == null) {
			stroke = new BasicStroke(size, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			STROKE_CACHE.put(size10, stroke);
		}
		g.setColor(color);
		g.setStroke(stroke);
		if (n == 1) {
			g.fillOval((int) (xs[0] - size / 2), (int) (ys[0] - size / 2),
					(int) size, (int) size);
		} else {
			Path2D.Float path = new Path2D.Float();
			path.moveTo(xs[0], ys[0]);
			if (n == 2) {
				path.lineTo(xs[1], ys[1]);
			} else if (n == 3) {
				path.quadTo(xs[1], ys[1], xs[2], ys[2]);
			} else {
				for (int i = 3; i < n; i++) {
					path.curveTo(xs[i - 2], ys[i - 2], xs[i - 1], ys[i - 1],
							xs[i], ys[i]);
				}
			}
			g.draw(path);
		}
		g.setColor(c);
		g.setStroke(s);
	}
}
