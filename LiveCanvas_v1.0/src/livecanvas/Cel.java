package livecanvas;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

public class Cel {
	public int index;

	private int width, height;

	public BufferedImage buf;

	private static int[] scratch_rgb;

	private static BufferedImage scratch;

	private static Graphics2D sg;

	public BufferedImage backgroundRef;

	public Cel(int index, int width, int height) {
		this.index = index;
		this.width = width;
		this.height = height;
		buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		init();
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
		buf = copyBuf(width, height);
	}

	private BufferedImage copyBuf(int width, int height) {
		BufferedImage newBuf = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = newBuf.getGraphics();
		g.drawImage(buf, (width - buf.getWidth()) / 2,
				(height - buf.getHeight()) / 2, null);
		g.dispose();
		init();
		return newBuf;
	}

	private void init() {
		if (scratch == null || scratch.getWidth() != width
				|| scratch.getHeight() != height) {
			scratch = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
			scratch_rgb = new int[width * height];
			sg = scratch.createGraphics();
		}
	}

	private void clearScratch() {
		AlphaComposite c = (AlphaComposite) sg.getComposite();
		sg.setComposite(AlphaComposite.Clear);
		sg.fillRect(0, 0, width, height);
		sg.setComposite(c);
	}

	private void clearBuf() {
		Graphics2D g = buf.createGraphics();
		AlphaComposite c = (AlphaComposite) g.getComposite();
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(0, 0, width, height);
		g.setComposite(c);
		g.dispose();
	}

	private void scratchBufToArray() {
		scratch.getRGB(0, 0, width, height, scratch_rgb, 0, width);
	}

	private void arrayToScratchBuf() {
		scratch.setRGB(0, 0, width, height, scratch_rgb, 0, width);
	}

	private void copyBufToScratch() {
		clearScratch();
		sg.drawImage(buf, 0, 0, null);
	}

	private void copyBufArrayToScratchArray(int[] buf_rgb) {
		System.arraycopy(buf_rgb, 0, scratch_rgb, 0, width * height);
	}

	private void copyScratchArrayToBufArray(int[] buf_rgb) {
		System.arraycopy(scratch_rgb, 0, buf_rgb, 0, width * height);
	}

	private void copyScratchToBuf() {
		clearBuf();
		Graphics2D g = buf.createGraphics();
		g.drawImage(scratch, 0, 0, null);
		g.dispose();
	}

	public void release() {
		if (buf == null) {
			return;
		}
	}

	public void retrieve() {
		if (buf != null) {
			return;
		}
	}

	private int cc_n, cc_x, cc_y;

	private void clear_fill(int[] rgb, int x, int y) {
		List<Point> pts = new LinkedList<Point>();
		pts.add(new Point(x, y));
		while (!pts.isEmpty()) {
			Point pt = pts.remove(0);
			x = pt.x;
			y = pt.y;
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					if (i == 0 && j == 0) {
						continue;
					}
					int _x = x + j;
					int _y = y + i;
					if (_x < 0 || _x >= width || _y < 0 || _y >= height) {
						continue;
					}
					int p = _y * width + _x;
					if ((rgb[p] & 0xFF000000) != 0 && (rgb[p] & 0xFFFFFF) == 0) {
						rgb[p] = 0;
						cc_n += 1;
						cc_x += _x;
						cc_y += _y;
						pts.add(new Point(_x, _y));
					}
				}
			}
		}
	}
}
