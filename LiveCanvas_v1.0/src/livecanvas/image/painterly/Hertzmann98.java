package livecanvas.image.painterly;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Hertzmann98 {
	public static BufferedImage diff_img, ref, ref_grad_img;
	private static int[] dest_rgbs, ref_rgbs;
	private static double[] diff, ref_grad_mags;
	private static double[][] ref_grad_dirs;
	private static boolean paintOnce;
	static int __i;

	public static void paint(BufferedImage src, BufferedImage dest,
			StyleParameters styleParameters) {
		if (dest == null || dest.getWidth() != src.getWidth()
				|| dest.getHeight() != src.getHeight()) {
			dest = new BufferedImage(src.getWidth(), src.getHeight(),
					src.getType());
		}
		Graphics2D g = (Graphics2D) dest.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, dest.getWidth(), dest.getHeight());
		double[] brushes = new double[styleParameters.nBrushes];
		double brushSize = styleParameters.minBrushSize;
		for (int i = 0; i < brushes.length; i++) {
			brushes[i] = brushSize;
			brushSize *= styleParameters.brushSizeRatio;
		}
		paintOnce = false;
		for (__i = brushes.length - 1; __i >= 0; __i--) {
			createRef(src, (int) (styleParameters.blurFactor * brushes[__i]));
			System.err.println("painting layer: " + __i);
			paintLayer(dest, ref, styleParameters, brushes[__i]);
			if (__i == 0) {
				g.dispose();
				return;
			}
		}
	}

	private static Color[] GRAYSCALE = new Color[256];
	static {
		for (int i = 0; i < GRAYSCALE.length; i++) {
			GRAYSCALE[i] = new Color(i, i, i);
		}
	}

	private static void createRef(BufferedImage src, int radius) {
		int w = src.getWidth();
		int h = src.getHeight();
		if (ref == null || ref.getWidth() != w || ref.getHeight() != h) {
			ref = new BufferedImage(w, h, src.getType());
			ref_grad_img = new BufferedImage(w, h, src.getType());
			ref_rgbs = new int[w * h];
			ref_grad_mags = new double[w * h];
			ref_grad_dirs = new double[w * h][2];
		}
		new BoxBlurFilter(radius, radius, 5).filter(src, ref);
		ref.getRGB(0, 0, w, h, ref_rgbs, 0, w);
		int[] ref_gray = new int[w * h];
		for (int i = 0; i < ref_gray.length; i++) {
			int r, g, b;
			r = (ref_rgbs[i] >> 16) & 0xff;
			g = (ref_rgbs[i] >> 8) & 0xff;
			b = ref_rgbs[i] & 0xff;
			ref_gray[i] = (r + g + b) / 3;
		}
		int[][] sobel_gx = { { 1, 0, -1 }, { 2, 0, -2 }, { 1, 0, -1 } };
		int[][] sobel_gy = { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } };
		double mxgrad = 1;
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				int offset = y * w + x;
				double gx = (ref_gray[offset - w - 1] * sobel_gx[0][0])
						+ (ref_gray[offset - w] * sobel_gx[0][1])
						+ (ref_gray[offset - w + 1] * sobel_gx[0][2])
						+ (ref_gray[offset - 1] * sobel_gx[1][0])
						+ (ref_gray[offset] * sobel_gx[1][1])
						+ (ref_gray[offset + 1] * sobel_gx[1][2])
						+ (ref_gray[offset + w - 1] * sobel_gx[2][0])
						+ (ref_gray[offset + w] * sobel_gx[2][1])
						+ (ref_gray[offset + w + 1] * sobel_gx[2][2]);
				double gy = (ref_gray[offset - w - 1] * sobel_gy[0][0])
						+ (ref_gray[offset - w] * sobel_gy[0][1])
						+ (ref_gray[offset - w + 1] * sobel_gy[0][2])
						+ (ref_gray[offset - 1] * sobel_gy[1][0])
						+ (ref_gray[offset] * sobel_gy[1][1])
						+ (ref_gray[offset + 1] * sobel_gy[1][2])
						+ (ref_gray[offset + w - 1] * sobel_gy[2][0])
						+ (ref_gray[offset + w] * sobel_gy[2][1])
						+ (ref_gray[offset + w + 1] * sobel_gy[2][2]);
				ref_grad_mags[offset] = Math.sqrt(gx * gx + gy * gy);
				if (ref_grad_mags[offset] > mxgrad) {
					mxgrad = ref_grad_mags[offset];
				}
				ref_grad_dirs[offset][0] = gx / ref_grad_mags[offset];
				ref_grad_dirs[offset][1] = gy / ref_grad_mags[offset];
			}
		}
		Graphics grg = ref_grad_img.getGraphics();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				grg.setColor(GRAYSCALE[(int) (ref_grad_mags[y * w + x] * 255 / mxgrad)]);
				grg.fillRect(x, y, 1, 1);
			}
		}
		grg.dispose();
	}

	private static void paintLayer(BufferedImage dest, BufferedImage ref,
			StyleParameters styleParameters, double brushSize) {
		List<OneStroke> strokes = new LinkedList<OneStroke>();
		createDiff(dest, ref);

		double grid = styleParameters.gridSize * brushSize;
		int igrid = Math.max(2, (int) grid);
		int igrid2 = igrid / 2;

		int w = dest.getWidth();
		int h = dest.getHeight();
		Graphics2D g = dest.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		System.err.println("creating strokes");
		for (int y = igrid2; y < h; y += igrid) {
			for (int x = igrid2; x < w; x += igrid) {
				double error = 0, dmax = -1;
				int dmax_x = -1, dmax_y = -1;
				for (int iy = -igrid2; iy < igrid2; iy++) {
					for (int ix = -igrid2; ix < igrid2; ix++) {
						int _x = x + ix;
						int _y = y + iy;
						if (_x >= w || _y >= h) {
							continue;
						}
						double dpoint = diff[_y * w + _x];
						if (dpoint > dmax) {
							dmax = dpoint;
							dmax_x = _x;
							dmax_y = _y;
						}
						error += dpoint;
					}
				}
				error /= igrid * igrid;
				if (error > styleParameters.approxThreshold) {
					strokes.add(makeSplineStroke(dmax_x, dmax_y, brushSize,
							dest, ref, styleParameters));
				}
			}
		}

		System.err.println("drawing strokes");
		if (__i == 0) {
			double mxdiff = 0;
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					if (diff[y * w + x] > mxdiff) {
						mxdiff = diff[y * w + x];
					}
				}
			}
			int b = 1;
			if (b == 0) {
				for (int y = 0; y < h; y += 1) {
					for (int x = 0; x < w; x += 1) {
						// ((dest_rgb[i] >> 16) & 0xff)
						// g
						// .setColor(grad[(int) (diff[y * igrid + x] * 255 /
						// mxdiff)]);
						// g.fillRect(x, y, 1, 1);
					}
				}
			} else if (b == 1) {
				Graphics gd = diff_img.getGraphics();
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < w; x++) {
						gd.setColor(GRAYSCALE[(int) (diff[y * w + x] * 255 / mxdiff)]);
						gd.fillRect(x, y, 1, 1);
					}
				}
				gd.dispose();
			}
		}
		OneStroke[] strokes_array = strokes.toArray(new OneStroke[0]);
		int[] order = new int[strokes_array.length];
		List<Integer> int_list = new ArrayList<Integer>(order.length);
		for (int i = 0; i < order.length; i++) {
			int_list.add(i);
		}
		for (int i = 0; i < order.length; i++) {
			int o = (int) (int_list.size() * Math.random());
			order[i] = int_list.get(o);
			int_list.remove(o);
		}
		for (int i = 0; i < order.length; i++) {
			strokes_array[order[i]].paint(g);
		}
	}

	private static void createDiff(BufferedImage dest, BufferedImage ref) {
		int w = dest.getWidth();
		int h = dest.getHeight();
		if (dest_rgbs == null || dest_rgbs.length != w * h) {
			dest_rgbs = new int[w * h];
			diff_img = new BufferedImage(w, h, dest.getType());
			diff = new double[w * h];
		}
		dest.getRGB(0, 0, w, h, dest_rgbs, 0, w);
		if (!paintOnce) {
			for (int i = 0; i < w * h; i++) {
				diff[i] = 1;
			}
			paintOnce = true;
			return;
		}
		for (int i = 0; i < w * h; i++) {
			diff[i] = color_diff(dest_rgbs[i], ref_rgbs[i]);
		}
	}

	private static double color_diff(int dest_rgb, int ref_rgb) {
		double dr, dg, db;
		dr = (((dest_rgb >> 16) & 0xff) - ((ref_rgb >> 16) & 0xff)) / 255.0;
		dg = (((dest_rgb >> 8) & 0xff) - ((ref_rgb >> 8) & 0xff)) / 255.0;
		db = ((dest_rgb & 0xff) - (ref_rgb & 0xff)) / 255.0;
		return Math.min(1, Math.sqrt(dr * dr + dg * dg + db * db));
	}

	private static OneStroke makeStroke(int x, int y, double brushSize,
			BufferedImage ref, StyleParameters styleParameters) {
		Color strokeColor = createStrokeColor(ref.getRGB(x, y), styleParameters);
		OneStroke s = new OneStroke(x, y, (float) brushSize, strokeColor);
		return s;
	}

	private static OneStroke makeSplineStroke(int x, int y, double brushSize,
			BufferedImage dest, BufferedImage ref,
			StyleParameters styleParameters) {
		int rgb = ref.getRGB(x, y);
		Color strokeColor = createStrokeColor(rgb, styleParameters);
		OneStroke s = new OneStroke(x, y, (float) brushSize, strokeColor);
		double lastDx, lastDy;
		lastDx = lastDy = 0;

		int w = ref.getWidth();
		int h = ref.getHeight();
		for (int i = 0; i < styleParameters.maxStrokeLength; i++) {
			if (x < 0 || y < 0 || x >= w || y >= h) {
				break;
			}
			if (i > styleParameters.minStrokeLength
					&& (Math.abs(color_diff(rgb, dest.getRGB(x, y))) < Math
							.abs(color_diff(rgb, strokeColor.getRGB())))) {
				break;
			}
			int offset = y * w + x;
			if (ref_grad_mags[offset] == 0) {
				break;
			}

			double gx = ref_grad_dirs[offset][0];
			double gy = ref_grad_dirs[offset][1];
			double dx = -gy;
			double dy = gx;

			if (lastDx * dx + lastDy * dy < 0) {
				dx = -dx;
				dy = -dy;
			}

			dx = styleParameters.curvatureFilter * dx
					+ (1 - styleParameters.curvatureFilter) * lastDx;
			dy = styleParameters.curvatureFilter * dy
					+ (1 - styleParameters.curvatureFilter) * lastDy;
			double norm = Math.sqrt(dx * dx + dy * dy);
			dx /= norm;
			dy /= norm;
			x += brushSize * dx;
			y += brushSize * dy;
			lastDx = dx;
			lastDy = dy;

			s.add(x, y);
		}
		return s;
	}

	private static Color createStrokeColor(int rgb,
			StyleParameters styleParameters) {
		Color strokeColor;
		if (styleParameters.colorJitter > 0) {
			int r = (rgb >> 16) & 0xff;
			int g = (rgb >> 8) & 0xff;
			int b = rgb & 0xff;
			r = Math.min(0xff, Math.max(0, (int) (Math.random() * 50 + r)));
			g = Math.min(0xff, Math.max(0, (int) (Math.random() * 50 + g)));
			b = Math.min(0xff, Math.max(0, (int) (Math.random() * 50 + b)));
			rgb = (r << 16) | (g << 8) | b;
		}
		if (styleParameters.opacity < 1) {
			int alpha = (int) (styleParameters.opacity * 0x100) << 24;
			strokeColor = new Color(alpha | rgb, true);
		} else {
			strokeColor = new Color(rgb);
		}
		return strokeColor;
	}
}
