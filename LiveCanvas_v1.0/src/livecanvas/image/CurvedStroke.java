package livecanvas.image;

import java.awt.Color;

import livecanvas.Face;

public class CurvedStroke {
	public final int sx, sy; // starting point of the curve
	public Face face;
	// always relative to sx, sy
	// so to get real values, do xs[i] + sx, ys[i] + sy
	public int[] xs, ys;
	public int count;
	public int size10;
	public Color color;

	public CurvedStroke(Face face, int x, int y, float size, Color color) {
		this.face = face;
		sx = x;
		sy = y;
		xs = new int[1];
		ys = new int[1];
		add(x, y);
		this.size10 = (int) (size * 10);
		this.color = color;
	}

	public void add(int x, int y) {
		if (count == xs.length) {
			grow();
		}
		xs[count] = x - sx;
		ys[count] = y - sy;
		count++;
	}

	private void grow() {
		int[] nxs = new int[xs.length * 2];
		int[] nys = new int[ys.length * 2];
		System.arraycopy(xs, 0, nxs, 0, count);
		System.arraycopy(ys, 0, nys, 0, count);
		xs = nxs;
		ys = nys;
	}
}
