package livecanvas.mosaic;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import livecanvas.mosaic.Ray.Intersection;

import org.poly2tri.Poly2Tri;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.sets.ConstrainedPointSet;
import org.poly2tri.triangulation.sets.PointSet;

public class Mosaic extends JPanel {
	private BufferedImage srcImage, destImage;

	public Mosaic() {
		setFocusable(true);
		setPreferredSize(new Dimension(700, 700));
		setBackground(Color.white);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_SPACE:
					create();
					break;
				case KeyEvent.VK_F5:
					try {
						FileDialog fd = new FileDialog(
								JOptionPane.getFrameForComponent(Mosaic.this),
								"Save as Image");
						fd.setVisible(true);
						String file_str = fd.getFile();
						if (file_str != null) {
							if (file_str.endsWith(".jpg")
									|| file_str.endsWith(".jpeg")) {
								File file = new File(fd.getDirectory() + "/"
										+ file_str);
								ImageIO.write(destImage, "jpg", file);
							} else if (file_str.endsWith(".png")) {
								File file = new File(fd.getDirectory() + "/"
										+ file_str);
								ImageIO.write(destImage, "png", file);
							} else {
								file_str += ".png";
								File file = new File(fd.getDirectory() + "/"
										+ file_str);
								ImageIO.write(destImage, "png", file);
							}
						}
					} catch (Exception ex) {
						String msg = "An error occurred while trying to save.";
						JOptionPane.showMessageDialog(
								JOptionPane.getFrameForComponent(Mosaic.this),
								msg, "Error", JOptionPane.ERROR_MESSAGE);
					}
					break;
				}
				repaint();
			}
		});
		addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent e) {
				FileDialog fd = new FileDialog(
						JOptionPane.getFrameForComponent(Mosaic.this), "Load");
				fd.setVisible(true);
				String file_str = fd.getFile();
				if (file_str == null) {
					return;
				}
				try {
					srcImage = ImageIO.read(new File(fd.getDirectory() + "/"
							+ file_str));
					create();
				} catch (IOException e1) {
					String msg = "An error occurred while trying to load.";
					JOptionPane.showMessageDialog(
							JOptionPane.getFrameForComponent(Mosaic.this), msg,
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				requestFocusInWindow();
				repaint();
			}
		});

		try {
			srcImage = ImageIO
					.read(new File("C:/Users/Jasleen/Desktop/bb.png"));
			create();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int width = getWidth();
		int height = getHeight();
		if (destImage == null) {
			String text = "Click to load image";
			g.drawString(text,
					(width - g.getFontMetrics().stringWidth(text)) / 2,
					height / 2);
		} else {
			int imgWidth = destImage.getWidth();
			int imgHeight = destImage.getHeight();
			double scale = Math.min((double) (width - 40) / imgWidth,
					(double) (height - 40) / imgHeight);
			int scaledWidth = (int) (scale * imgWidth);
			int scaledHeight = (int) (scale * imgHeight);
			g.drawImage(destImage, (width - scaledWidth) / 2,
					(height - scaledHeight) / 2, scaledWidth, scaledHeight,
					null);
		}
		if (hasFocus()) {
			g.setColor(Color.black);
			g.drawRect(0, 0, width - 1, height - 1);
		}
	}

	private void create() {
		if (srcImage == null) {
			return;
		}
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		if (destImage == null || destImage.getWidth() != width
				|| destImage.getHeight() != height) {
			destImage = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
		}
		int nIterations = 10;
		int side = 30;
		List<TriangulationPoint> internal = new ArrayList<TriangulationPoint>(
				Arrays.asList(generateRandomPoints(width, height, side * side)));
		PointSet pointSet = null;
		TriangulationPoint[] outlinePointsArray = new TriangulationPoint[side * 4];
		List<TriangulationPoint> constraints = new ArrayList<TriangulationPoint>();
		int c = 0;
		for (int i = 0; i < side; i++) {
			outlinePointsArray[c++] = new TPoint(i * (width - 1) / side, 0);
		}
		for (int i = 0; i < side; i++) {
			outlinePointsArray[c++] = new TPoint(width - 1, i * (height - 1)
					/ side);
		}
		for (int i = 0; i < side; i++) {
			outlinePointsArray[c++] = new TPoint((side - i) * (width - 1)
					/ side, height - 1);
		}
		for (int i = 0; i < side; i++) {
			outlinePointsArray[c++] = new TPoint(0, (side - i) * (height - 1)
					/ side);
		}
		for (int i = 0; i < outlinePointsArray.length; i++) {
			constraints.add(outlinePointsArray[i]);
			constraints.add(outlinePointsArray[(i + 1)
					% outlinePointsArray.length]);
		}
		List<TriangulationPoint> outline = Arrays.asList(outlinePointsArray);
		List<TriangulationPoint> points = new ArrayList<TriangulationPoint>();
		Map<TriangulationPoint, List<TriangulationPoint>> point2Edges = new HashMap<TriangulationPoint, List<TriangulationPoint>>();
		while (--nIterations >= 0) {
			points.clear();
			points.addAll(outline);
			points.addAll(internal);
			internal.clear();
			pointSet = new ConstrainedPointSet(points, constraints);
			Poly2Tri.triangulate(pointSet);
			point2Edges.clear();
			for (int i = 0; i < points.size(); i++) {
				point2Edges.put(points.get(i),
						new ArrayList<TriangulationPoint>());
			}
			List<TriangulationPoint> edges;
			for (DelaunayTriangle t : pointSet.getTriangles()) {
				for (int i = 0; i < 3; i++) {
					edges = point2Edges.get(t.points[i]);
					edges.add(t.points[(i + 1) % 3]);
					edges.add(t.points[(i + 2) % 3]);
				}
			}
			for (TriangulationPoint p : pointSet.getPoints()) {
				if (outline.contains(p)) {
					continue;
				}
				double sxsum = 0, sysum = 0, szsum = 0;
				int scount = 0;
				for (TriangulationPoint other : point2Edges.get(p)) {
					sxsum += other.getX();
					sysum += other.getY();
					szsum += other.getZ();
					scount++;
				}
				TPoint np = new TPoint(sxsum / scount, sysum / scount, szsum
						/ scount);
				internal.add(np);
			}
		}
		Graphics2D g = destImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.white);
		g.fillRect(0, 0, destImage.getWidth(), destImage.getHeight());
		g.setStroke(new BasicStroke(1.0f));
		for (TriangulationPoint p : pointSet.getPoints()) {
			TriangulationPoint[] edges = point2Edges.get(p).toArray(
					new TriangulationPoint[0]);
			final double px = p.getX(), py = p.getY();
			Arrays.sort(edges, new Comparator<TriangulationPoint>() {
				@Override
				public int compare(TriangulationPoint o1, TriangulationPoint o2) {
					return (int) Math.signum(Math.atan2(o1.getY() - py,
							o1.getX() - px)
							- Math.atan2(o2.getY() - py, o2.getX() - px));
				}
			});
			int n = edges.length;
			Polygon poly = new Polygon(new int[n], new int[n], n);
			double[] res = new double[2];
			for (int i = 0; i < n; i++) {
				int i1 = i < n - 1 ? i + 1 : 0;
				circumpoint(px, py, edges[i].getX(), edges[i].getY(),
						edges[i1].getX(), edges[i1].getY(), res);
				poly.xpoints[i] = (int) (res[0]);
				poly.ypoints[i] = (int) (res[1]);
			}
			fillPoly1(g, poly);
		}
		// for (DelaunayTriangle tri : pointSet.getTriangles()) {
		// Polygon poly = new Polygon(new int[3], new int[3], 3);
		// for (int i = 0; i < tri.points.length; i++) {
		// poly.xpoints[i] = (int) tri.points[i].getX();
		// poly.ypoints[i] = (int) tri.points[i].getY();
		// }
		// fillPoly1(g, poly);
		// }
		g.dispose();
	}

	private static void circumpoint(double x0, double y0, double x1, double y1,
			double x2, double y2, double[] result) {
		double mx1 = (x1 + x0) / 2;
		double my1 = (y1 + y0) / 2;
		double mx2 = (x2 + x0) / 2;
		double my2 = (y2 + y0) / 2;
		double sx1 = y1 - y0;
		double sy1 = x0 - x1;
		double sx2 = y2 - y0;
		double sy2 = x0 - x2;
		Intersection in = new Ray(mx1, my1, sx1, sy1).intersection(new Ray(mx2,
				my2, sx2, sy2));
		if (in == null) {
			result[0] = (mx1 + mx2) / 2;
			result[1] = (my1 + my2) / 2;
		} else {
			result[0] = in.intersection.x;
			result[1] = in.intersection.y;
		}
	}

	private static Stroke s = new BasicStroke(1.5f);

	private void fillPoly1(Graphics2D g, Polygon poly) {
		Rectangle bounds = poly.getBounds();
		int cx = (int) bounds.getCenterX();
		int cy = (int) bounds.getCenterY();
		int rgb = srcImage.getRGB(
				Math.max(0, Math.min(srcImage.getWidth() - 1, cx)),
				Math.max(0, Math.min(srcImage.getHeight() - 1, cy)));
		if ((rgb & 0xff000000) == 0) {
			return;
		}
		Color color = new Color(rgb);
		g.setColor(color);
		// int gray = srcImage.getRGB((int) center.getX(), (int)
		// center.getY()) & 0xff;
		// double sigma = 0.5;
		// gray = (int)(0xff * (1-sigma)) + (int)(gray * sigma);
		// g.setColor(new Color(gray, gray, gray));
		AffineTransform t = g.getTransform();
		g.translate(cx, cy);
		g.rotate(Math.random() * Math.PI);
		g.fillRect(-bounds.width / 3, -bounds.height / 3, 2 * bounds.width / 3,
				bounds.height / 2);
		g.setStroke(s);
		g.setColor(Color.black);
		g.drawRect(-bounds.width / 4, -bounds.height / 4, bounds.width / 2,
				bounds.height / 2);
		g.setTransform(t);
	}

	private void fillPoly4(Graphics2D g, Polygon poly) {
		Rectangle bounds = poly.getBounds();
		g.setColor(new Color(srcImage.getRGB(
				Math.max(
						0,
						Math.min(srcImage.getWidth() - 1,
								(int) bounds.getCenterX())),
				Math.max(
						0,
						Math.min(srcImage.getHeight() - 1,
								(int) bounds.getCenterY())))));
		// int gray = srcImage.getRGB((int) center.getX(), (int)
		// center.getY()) & 0xff;
		// double sigma = 0.5;
		// gray = (int)(0xff * (1-sigma)) + (int)(gray * sigma);
		// g.setColor(new Color(gray, gray, gray));
		g.fillPolygon(poly);
		g.setColor(Color.black);
		g.setStroke(s);
		g.drawPolygon(poly);
	}

	private void fillPoly3(Graphics2D g, Polygon poly, TriangulationPoint center) {
		g.setColor(Color.black);
		// int gray = srcImage.getRGB((int) center.getX(), (int)
		// center.getY()) & 0xff;
		// double sigma = 0.5;
		// gray = (int)(0xff * (1-sigma)) + (int)(gray * sigma);
		// g.setColor(new Color(gray, gray, gray));
		g.drawPolygon(poly);
		// g.setColor(Color.white);
		// g.drawPolygon(poly);
	}

	private void fillPoly2(Graphics2D g, Polygon poly, TriangulationPoint center) {
		g.setStroke(new BasicStroke(1.0f));
		g.setColor(Color.black);
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite
				.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		for (int i = 0; i < 10; i++) {
			int xv01 = poly.xpoints[1] - poly.xpoints[0];
			int yv01 = poly.ypoints[1] - poly.ypoints[0];
			int xv02 = poly.xpoints[2] - poly.xpoints[0];
			int yv02 = poly.ypoints[2] - poly.ypoints[0];
			double u = Math.random(), v = Math.random();
			if (u + v >= 1) {
				u = 1 - u;
				v = 1 - v;
			}
			Point p = new Point((int) (poly.xpoints[0] + u * xv01 + v * xv02),
					(int) (poly.ypoints[0] + u * yv01 + v * yv02));
			double angle = Math.random() * Math.PI;
			double length = 2 + Math.random() * 20;
			int x1 = (int) (p.x + length * Math.cos(angle) / 2);
			int y1 = (int) (p.y - length * Math.sin(angle) / 2);
			int x2 = (int) (p.x - length * Math.cos(angle) / 2);
			int y2 = (int) (p.y + length * Math.sin(angle) / 2);
			// g.setColor(new Color(srcImage.getRGB((int) p.x,
			// (int) p.y)));
			g.drawLine(x1, y1, x2, y2);
		}
		// g.setColor(Color.black);
		// g.drawPolygon(poly);
	}

	private static TriangulationPoint[] generateRandomPoints(int width,
			int height, int totalPoints) {
		TriangulationPoint[] points = new TriangulationPoint[totalPoints];
		for (int i = 0; i < totalPoints; i++) {
			points[i] = new TPoint(1 + Math.random() * (width - 2), 1
					+ Math.random() * (height - 2));
		}
		return points;
	}

	private static void gray_hist(int[] src, int[] dest) {
		int[] hist_cum = new int[256];
		for (int i = 0; i < src.length; i++) {
			int r = (src[i] >> 16) & 0xff;
			int g = (src[i] >> 8) & 0xff;
			int b = (src[i] >> 0) & 0xff;
			int gray = (r + g + b) / 3;
			src[i] = gray;
			hist_cum[gray]++;
		}
		for (int i = 1; i < hist_cum.length; i++) {
			hist_cum[i] += hist_cum[i - 1];
		}
		for (int i = 0; i < src.length; i++) {
			int gray = src[i];
			int max_gray = hist_cum[gray] * 0x100 / src.length;
			int min_gray = gray == 0 ? 0 : hist_cum[gray - 1] * 0x100
					/ src.length;
			gray = (int) (min_gray + Math.random() * (max_gray - min_gray));
			dest[i] = (0xff << 24) | (gray << 16) | (gray << 8) | gray;
		}
	}

	private static final int apply_mask(int[] src, int x, int y, int width,
			int height, double[] mask, double factor, int offset, int maskSize) {
		double sumr = 0, sumg = 0, sumb = 0;
		int m2 = (maskSize - 1) / 2;
		for (int i = 0; i < maskSize; i++) {
			int my = y - m2 + i;
			if (my < 0 || my >= height) {
				continue;
			}
			for (int j = 0; j < maskSize; j++) {
				int mx = x - m2 + j;
				if (mx < 0 || mx >= width) {
					continue;
				}
				int rgb = src[my * width + mx];
				double mult = mask[i * maskSize + j];
				sumr += ((rgb >> 16) & 0xff) * mult;
				sumg += ((rgb >> 8) & 0xff) * mult;
				sumb += (rgb & 0xff) * mult;
			}
		}
		int r = clamp((int) (sumr * factor) + offset);
		int g = clamp((int) (sumg * factor) + offset);
		int b = clamp((int) (sumb * factor) + offset);
		return (0xff << 24) | (r << 16) | (g << 8) | b;
	}

	private static int gray(int rgb) {
		return (((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff)) / 3;
	}

	private static int clamp(int val) {
		return Math.max(0, Math.min(0xff, val));
	}

	private static void blur(int[] src, int[] dest, int width, int height) {
		double[] mask = new double[] { //
		1, 4, 7, 4, 1,//
				4, 16, 26, 16, 4,//
				7, 26, 41, 26, 7,//
				4, 16, 26, 16, 4,//
				1, 4, 7, 4, 1 };
		int offset = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				dest[offset++] = apply_mask(src, x, y, width, height, mask,
						1.0 / 273.0f, 0, 5);
			}
		}
	}

	private static void gradient(int[] src, int[] dest, int width, int height) {
		double[] mask1 = new double[] { -1, 0, 1,//
				-2, 0, 2,//
				-1, 0, 1, };
		double[] mask2 = new double[] { -1, -2, -1,//
				0, 0, 0,//
				1, 2, 1, };
		int offset = 0;
		int min = 256, max = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int gx = apply_mask(src, x, y, width, height, mask1, 1 / 4.0,
						128, 3);
				int gy = apply_mask(src, x, y, width, height, mask2, 1 / 4.0,
						128, 3);
				gx = gray(gx) - 128;
				gy = gray(gy) - 128;
				int gray = 0xff - (int) Math.sqrt(gx * gx + gy * gy) / 2;
				dest[offset++] = gray;
				if (min > gray) {
					min = gray;
				}
				if (max < gray) {
					max = gray;
				}
			}
		}
		int range = max - min;
		for (int i = 0; i < dest.length; i++) {
			int gray = dest[i];
			gray = (gray - min) * 0xff / range;
			dest[i] = (0xff << 24) | (gray << 16) | (gray << 8) | gray;
		}
	}
}
