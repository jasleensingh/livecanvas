package livecanvas.image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import livecanvas.BackgroundRef;
import livecanvas.Face;
import livecanvas.Mesh;
import livecanvas.Path;
import livecanvas.Vec3;
import livecanvas.image.painterly.BoxBlurFilter;

import common.typeutils.DoubleType;
import common.typeutils.IntegerType;

public class PainterlyParticleGenerator extends
		AbstractParticleGenerator<CurvedStroke> {
	@DoubleType(name = "Approximation Threshold", min = 0.0, max = 1.0, step = 0.001)
	public double approxThreshold = 0.01;

	@DoubleType(name = "Min Brush Size")
	public double minBrushSize = 4;

	@DoubleType(name = "Brush Size Ratio")
	public double brushSizeRatio = 2;

	@IntegerType(name = "Num Brushes")
	public int nBrushes = 3;

	@DoubleType(name = "Curvature Filter", min = 0, max = 1, step = 0.1)
	public double curvatureFilter = 0.5;

	@DoubleType(name = "Blur Factor", min = 0, max = 10, step = 0.1)
	public double blurFactor = 1;

	@IntegerType(name = "Min Stroke Length", min = 1, max = 100)
	public int minStrokeLength = 16;

	@IntegerType(name = "Max Stroke Length", min = 1, max = 100)
	public int maxStrokeLength = 64;

	@DoubleType(name = "Grid Size", min = 0.1, max = 10, step = 0.1)
	public double gridSize = 1;

	@DoubleType(name = "Opacity", min = 0.1, max = 1, step = 0.05)
	public double opacity = .5;

	@DoubleType(name = "Color Jitter", min = 0, max = 1, step = 0.1)
	public double colorJitter = 0.5;

	private double[] brushes;

	@Override
	public void preprocess(RenderData data) {
		System.err.println("pass: " + data.pass);
		if (data.pass == 0) {
			brushes = new double[nBrushes];
			double brushSize = minBrushSize;
			for (int i = 0; i < brushes.length; i++) {
				brushes[i] = brushSize;
				brushSize *= brushSizeRatio;
			}
		}
	}

	@Override
	protected Particle<CurvedStroke>[] generateForPath(Path path,
			RenderData renderData, BackgroundRef bgref) {
		Mesh mesh = path.getMesh();
		paintOnce = false;
		List<Particle<CurvedStroke>> particles = new ArrayList<Particle<CurvedStroke>>();
		Point origin = new Point(renderData.canvasSize.width / 2,
				renderData.canvasSize.height / 2);
		int brushIndex = brushes.length - 1 - renderData.pass;
		createRef(bgref.toImage(), (int) (blurFactor * brushes[brushIndex]));
		System.err.println("painting layer: " + brushIndex);
		CurvedStroke[] strokes_array = paintLayer(renderData.render, ref,
				brushes[brushIndex], origin, mesh);
		int[] order = new int[strokes_array.length];
		List<Integer> int_list = new ArrayList<Integer>(order.length);
		for (int j = 0; j < order.length; j++) {
			int_list.add(j);
		}
		for (int j = 0; j < order.length; j++) {
			int o = (int) (int_list.size() * Math.random());
			order[j] = int_list.get(o);
			int_list.remove(o);
		}
		for (int j = 0; j < order.length; j++) {
			CurvedStroke cs = strokes_array[order[j]];
			particles
					.add(new Particle(origin, mesh, cs.face, cs.sx, cs.sy, cs));
		}
		renderData.packet = renderData.pass == nBrushes - 1; // last pass
		return particles.toArray(new Particle[0]);
	}

	@Override
	public void pass(RenderData renderData) {
		renderData.packet = renderData.pass == nBrushes - 1; // last pass
	}

	private Polygon face_poly = new Polygon(new int[3], new int[3], 3);

	private Face findFaceAtPoint(Mesh mesh, Point origin, int x, int y) {
		for (Face f : mesh.faces) {
			Vec3 v1 = mesh.vertices[f.v1Index];
			Vec3 v2 = mesh.vertices[f.v2Index];
			Vec3 v3 = mesh.vertices[f.v3Index];
			face_poly.xpoints[0] = (int) (origin.x + v1.x);
			face_poly.xpoints[1] = (int) (origin.x + v2.x);
			face_poly.xpoints[2] = (int) (origin.x + v3.x);
			face_poly.ypoints[0] = (int) (origin.y + v1.y);
			face_poly.ypoints[1] = (int) (origin.y + v2.y);
			face_poly.ypoints[2] = (int) (origin.y + v3.y);
			face_poly.invalidate();
			if (face_poly.contains(x, y)) {
				return f;
			}
		}
		return null;
	}

	private static BufferedImage ref, ref_grad_img;
	private static int[] dest_rgbs, ref_rgbs;
	private static double[] diff, ref_grad_mags;
	private static double[][] ref_grad_dirs;
	private static boolean paintOnce;

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

	private CurvedStroke[] paintLayer(BufferedImage dest, BufferedImage ref,
			double brushSize, Point origin, Mesh mesh) {
		List<CurvedStroke> strokes = new LinkedList<CurvedStroke>();
		createDiff(dest, ref);

		double grid = gridSize * brushSize;
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
				Face dface = null;
				double error = 0, dmax = -1;
				int dmax_x = -1, dmax_y = -1;
				for (int iy = -igrid2; iy < igrid2; iy++) {
					for (int ix = -igrid2; ix < igrid2; ix++) {
						int _x = x + ix;
						int _y = y + iy;
						Face f;
						if ((f = findFaceAtPoint(mesh, origin, _x, _y)) == null) {
							continue;
						}
						double dpoint = diff[_y * w + _x];
						if (dpoint > dmax) {
							dface = f;
							dmax = dpoint;
							dmax_x = _x;
							dmax_y = _y;
						}
						error += dpoint;
					}
				}
				error /= igrid * igrid;
				if (error > approxThreshold) {
					CurvedStroke cs = makeSplineStroke(dface, dmax_x, dmax_y,
							brushSize, dest, ref);
					strokes.add(cs);
				}
			}
		}
		return strokes.toArray(new CurvedStroke[0]);
	}

	private static void createDiff(BufferedImage dest, BufferedImage ref) {
		int w = dest.getWidth();
		int h = dest.getHeight();
		if (dest_rgbs == null || dest_rgbs.length != w * h) {
			dest_rgbs = new int[w * h];
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

	public static double color_diff(int rgb1, int rgb2) {
		double dr, dg, db;
		dr = (((rgb1 >> 16) & 0xff) - ((rgb2 >> 16) & 0xff)) / 255.0;
		dg = (((rgb1 >> 8) & 0xff) - ((rgb2 >> 8) & 0xff)) / 255.0;
		db = ((rgb1 & 0xff) - (rgb2 & 0xff)) / 255.0;
		return Math.min(1, Math.sqrt(dr * dr + dg * dg + db * db));
	}

	private CurvedStroke makeStroke(Face face, int x, int y, double brushSize,
			BufferedImage ref) {
		Color strokeColor = createStrokeColor(ref.getRGB(x, y));
		CurvedStroke s = new CurvedStroke(face, x, y, (float) brushSize,
				strokeColor);
		return s;
	}

	private CurvedStroke makeSplineStroke(Face face, int x, int y,
			double brushSize, BufferedImage dest, BufferedImage ref) {
		int rgb = ref.getRGB(x, y);
		Color strokeColor = createStrokeColor(rgb);
		CurvedStroke s = new CurvedStroke(face, x, y, (float) brushSize,
				strokeColor);
		double lastDx, lastDy;
		lastDx = lastDy = 0;

		int w = ref.getWidth();
		int h = ref.getHeight();
		for (int i = 0; i < maxStrokeLength; i++) {
			if (x < 0 || y < 0 || x >= w || y >= h) {
				break;
			}
			if (i > minStrokeLength
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
			dx = curvatureFilter * dx + (1 - curvatureFilter) * lastDx;
			dy = curvatureFilter * dy + (1 - curvatureFilter) * lastDy;
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

	private Color createStrokeColor(int rgb) {
		Color strokeColor;
		if (colorJitter > 0) {
			int r = (rgb >> 16) & 0xff;
			int g = (rgb >> 8) & 0xff;
			int b = rgb & 0xff;
			r = Math.min(0xff, Math.max(0, (int) (Math.random() * 50 + r)));
			g = Math.min(0xff, Math.max(0, (int) (Math.random() * 50 + g)));
			b = Math.min(0xff, Math.max(0, (int) (Math.random() * 50 + b)));
			rgb = (r << 16) | (g << 8) | b;
		}
		if (opacity < 1) {
			int alpha = (int) (opacity * 0x100) << 24;
			strokeColor = new Color(alpha | rgb, true);
		} else {
			strokeColor = new Color(rgb);
		}
		return strokeColor;
	}
}
