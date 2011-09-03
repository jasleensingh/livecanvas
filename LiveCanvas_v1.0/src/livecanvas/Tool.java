package livecanvas;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import livecanvas.BackgroundRef.BGImage;
import livecanvas.MagicWandImpl.Contour;

import common.typeutils.ColorType;
import common.typeutils.FloatType;
import common.typeutils.StrokeType;

public abstract class Tool {
	public static interface ToolContext {
		public Color getSelectedColor();
	}

	public final String name;
	protected Canvas canvas;

	public Tool(Canvas canvas, String name) {
		this.canvas = canvas;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean onSelected(ToolContext toolContext) {
		canvas.getCanvasContainer().setCursor(Cursor.getDefaultCursor());
		return true;
	}

	public void onDeselected(ToolContext toolContext) {
	}

	public void onCurrColorChanged(Color currColor) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	protected void repaint() {
		canvas.repaint();
	}

	public abstract void paint(Graphics2D g);

	protected void pathCreated(Point[] path) {
		canvas.pathCreated(path);
	}

	protected void requestImagePaint() {
		canvas.requestImagePaint(this);
	}

	public abstract void imagePaint(Graphics2D g, int width, int height);

	public static class Pencil extends Tool {
		private static final AlphaComposite[] ALPHA = new AlphaComposite[0x100];
		static {
			for (int i = 0; i < 0x100; i++) {
				ALPHA[i] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
						(float) (i + 1) / 0x100);
			}
		}

		private Color pencilColor = Color.black;

		@FloatType(name = "Intensity", min = 0.0f, max = 1.0f, step = 0.05f)
		public float intensity = 1.0f;

		private static final int STORE = 50;
		private int[] pointsx = new int[STORE];
		private int[] pointsy = new int[STORE];
		private int start, end;

		public Pencil(Canvas canvas) {
			super(canvas, "Pencil");
		}

		@Override
		public boolean onSelected(ToolContext toolContext) {
			super.onSelected(toolContext);
			pencilColor = toolContext.getSelectedColor();
			return true;
		}

		@Override
		public void onCurrColorChanged(Color currColor) {
			pencilColor = currColor;
		}

		public void mousePressed(MouseEvent e) {
			start = end = 0;
			add(e.getX(), e.getY());
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			requestImagePaint();
			lines.clear();
		}

		public void mouseDragged(MouseEvent e) {
			add(e.getX(), e.getY());
		}

		private static final int TRAIL = 2000;

		private static class Line {
			public final Color color;
			public final Composite composite;
			public final int x1, y1, x2, y2;

			public Line(Color color, Composite composite, int x1, int y1,
					int x2, int y2) {
				this.color = color;
				this.composite = composite;
				this.x1 = x1;
				this.y1 = y1;
				this.x2 = x2;
				this.y2 = y2;
			}
		}

		private List<Line> lines = new ArrayList<Line>();

		private void add(int x, int y) {
			pointsx[end] = x;
			pointsy[end] = y;
			if ((end = (end + 1) % STORE) == start) {
				start = (start + 1) % STORE;
			}
			for (int i = 0; i < STORE; i++) {
				int n = (i + start) % STORE;
				int dx = pointsx[n] - x;
				int dy = pointsy[n] - y;
				int d = dx * dx + dy * dy;
				if (Math.random() * TRAIL > d) {
					lines.add(new Line(pencilColor, ALPHA[(int) (d * 0x100
							* intensity / TRAIL)], (int) (x + dx * 0.3),
							(int) (y + dy * 0.3),
							(int) (pointsx[n] - dx * 0.3),
							(int) (pointsy[n] - dy * 0.3)));
				}
			}
			repaint();
		}

		private void drawLines(Graphics2D g) {
			Composite c = g.getComposite();
			for (Line l : lines) {
				g.setColor(l.color);
				g.setComposite(l.composite);
				g.drawLine(l.x1, l.y1, l.x2, l.y2);
			}
			g.setComposite(c);
		}

		public void paint(Graphics2D g) {
			drawLines(g);
		}

		public void imagePaint(Graphics2D g, int width, int height) {
			drawLines(g);
		}
	}

	public static class Brush extends Tool {
		@StrokeType(name = "Brush Stroke", description = "Stroke to be used when painting the stroke path")
		public Stroke brushStroke = new BasicStroke(10.0f);
		private Color brushColor = Color.black;
		private Stroke pathStroke = new BasicStroke(1.0f);
		private List<Point> points = new ArrayList<Point>();
		private Point[] pointsArray = new Point[10];
		private boolean tracking;

		private int currX, currY;

		public Brush(Canvas canvas) {
			super(canvas, "Brush");
		}

		@Override
		public boolean onSelected(ToolContext toolContext) {
			super.onSelected(toolContext);
			brushColor = toolContext.getSelectedColor();
			return true;
		}

		@Override
		public void onCurrColorChanged(Color currColor) {
			brushColor = currColor;
		}

		public void mousePressed(MouseEvent e) {
			points.add(new Point(currX = e.getX(), currY = e.getY()));
			tracking = e.isShiftDown();
			repaint();
		}

		public void mouseReleased(MouseEvent e) {
			if (tracking) {
				return;
			}
			pathCreated(Utils.subdivide(points.toArray(new Point[0])));
			requestImagePaint();
			points.clear();
		}

		public void mouseMoved(MouseEvent e) {
			currX = e.getX();
			currY = e.getY();
			repaint();
		}

		public void mouseDragged(MouseEvent e) {
			points.add(new Point(currX = e.getX(), currY = e.getY()));
			repaint();
		}

		private void drawBrush(Graphics2D g) {
			int n = points.size();
			if (pointsArray.length < n) {
				pointsArray = new Point[pointsArray.length * 2];
			}
			points.toArray(pointsArray);
			int x1, y1, x2, y2;
			if (n > 1) {
				x1 = pointsArray[0].x;
				y1 = pointsArray[1].y;
				for (int i = 1; i < n; i++) {
					x2 = pointsArray[i].x;
					y2 = pointsArray[i].y;
					g.drawLine(x1, y1, x2, y2);
					x1 = x2;
					y1 = y2;
				}
				if (tracking) {
					g.drawLine(x1, y1, currX, currY);
				}
			}
		}

		public void paint(Graphics2D g) {
			g.setStroke(pathStroke);
			g.setColor(Color.black);
			drawBrush(g);
		}

		public void imagePaint(Graphics2D g, int width, int height) {
			g.setStroke(brushStroke);
			g.setColor(brushColor);
			drawBrush(g);
		}
	}

	public static class Pen extends Tool {
		private static class CurvePoint implements Cloneable {
			public static final int QUAD = 0, CUBIC = 1;

			public Point p, c1, c2;

			public int type = CUBIC;

			public CurvePoint(int x, int y) {
				p = new Point(x, y);
				c1 = new Point(x, y);
				c2 = new Point(x, y);
			}

			public CurvePoint clone() {
				CurvePoint c = new CurvePoint(p.x, p.y);
				c.c1 = new Point(c1.x, c1.y);
				c.c2 = new Point(c2.x, c2.y);
				c.type = type;
				return c;
			}
		}

		private static abstract class CurveShape {
			public abstract boolean contains(int x, int y);
		}

		private static class CurvePath extends CurveShape {
			public final List<CurvePoint> points = new ArrayList<CurvePoint>();

			public final GeneralPath path = new GeneralPath();

			public CurvePath(CurvePoint[] ps) {
				points.addAll(Arrays.asList(ps));
				createPath(path, points);
			}

			public boolean contains(int x, int y) {
				return path.contains(x, y);
			}
		}

		private static void createPath(GeneralPath path, List<CurvePoint> points) {
			path.reset();
			int len = points.size();
			if (len > 0) {
				CurvePoint p1, p2;
				p1 = points.get(0);
				path.moveTo(p1.p.x, p1.p.y);
				for (int i = 0; i < len; i++) {
					p2 = points.get((i + 1) % len);
					if (p1.type == CurvePoint.CUBIC
							&& p2.type == CurvePoint.CUBIC) {
						path.curveTo(p1.c1.x, p1.c1.y, p2.c2.x, p2.c2.y,
								p2.p.x, p2.p.y);
					} else if (p1.type == CurvePoint.CUBIC) {
						path.quadTo(p1.c1.x, p1.c1.y, p2.p.x, p2.p.y);
					} else if (p2.type == CurvePoint.CUBIC) {
						path.quadTo(p2.c2.x, p2.c2.y, p2.p.x, p2.p.y);
					} else {
						path.lineTo(p2.p.x, p2.p.y);
					}
					p1 = p2;
				}
			}
		}

		@StrokeType(name = "Pen Stroke", description = "Stroke to be used when painting the stroke path")
		public Stroke penStroke = new BasicStroke(1.0f);

		private Color penColor = Color.blue;

		@ColorType(name = "Fill Color", description = "Color to be used when filling inside the path")
		public Color fillColor = Color.white;

		public Pen(Canvas canvas) {
			super(canvas, "Pen");
		}

		@Override
		public boolean onSelected(ToolContext toolContext) {
			super.onSelected(toolContext);
			penColor = toolContext.getSelectedColor();
			return true;
		}

		@Override
		public void onCurrColorChanged(Color currColor) {
			penColor = currColor;
		}

		public void mousePressed(MouseEvent e) {
			int ncp = onPath(e.getX(), e.getY());
			if (ncp == 0) {
				path = new CurvePath(points.toArray(new CurvePoint[0]));
				PathIterator pathIterator = path.path.getPathIterator(null);
				Point[] pathPoints = Utils
						.createPathFromPathIterator(pathIterator);
				pathCreated(pathPoints);
				requestImagePaint();
				path = null;
				points.clear();
			} else if (ncp == -1) {
				points.add(current = new CurvePoint(e.getX(), e.getY()));
			} else {
				int n = --points.get(ncp).type;
				if (n < 0) {
					points.remove(ncp);
				}
			}
			repaint();
		}

		public void mouseReleased(MouseEvent e) {
			current = null;
			repaint();
		}

		public void mouseDragged(MouseEvent e) {
			if (current != null) {
				current.c1.setLocation(e.getX(), e.getY());
				current.c2.setLocation(current.p.x - (e.getX() - current.p.x),
						current.p.y - (e.getY() - current.p.y));
				repaint();
			}
		}

		public void paint(Graphics2D g) {
			g.setColor(Color.black);
			g.setStroke(ls);
			if (points.size() > 0) {
				CurvePoint p1, p2;
				_path.reset();
				p1 = points.get(0);
				drawCtrlPoints(g, p1.p, p1.c1, p1.c2, 4);
				drawPoint(g, p1.p, 8, p1.type == CurvePoint.CUBIC);
				_path.moveTo(p1.p.x, p1.p.y);
				for (int i = 1; i < points.size(); i++) {
					p2 = points.get(i);
					drawCtrlPoints(g, p2.p, p2.c1, p2.c2, 4);
					drawPoint(g, p2.p, 8, p2.type == CurvePoint.CUBIC);
					if (p1.type == CurvePoint.CUBIC
							&& p2.type == CurvePoint.CUBIC) {
						_path.curveTo(p1.c1.x, p1.c1.y, p2.c2.x, p2.c2.y,
								p2.p.x, p2.p.y);
					} else if (p1.type == CurvePoint.CUBIC) {
						_path.quadTo(p1.c1.x, p1.c1.y, p2.p.x, p2.p.y);
					} else if (p2.type == CurvePoint.CUBIC) {
						_path.quadTo(p2.c2.x, p2.c2.y, p2.p.x, p2.p.y);
					} else {
						_path.lineTo(p2.p.x, p2.p.y);
					}
					p1 = p2;
				}
				g.setStroke(ls);
				g.draw(_path);
			}
		}

		public void imagePaint(Graphics2D g, int width, int height) {
			g.setColor(fillColor);
			g.fill(path.path);
			g.setColor(penColor);
			g.setStroke(penStroke);
			g.draw(path.path);
		}

		private List<CurvePoint> points = new ArrayList<CurvePoint>();

		private CurvePath path;

		private CurvePoint current;

		private int onPath(int x, int y) {
			for (int i = 0; i < points.size(); i++) {
				CurvePoint cp = points.get(i);
				if (Math.abs(cp.p.x - x) < 5 && Math.abs(cp.p.y - y) < 5) {
					return i;
				}
			}
			return -1;
		}

		private Stroke ls = new BasicStroke(1.0f), cts = new BasicStroke(1.0f,
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f,
				new float[] { 1.0f, 1.0f }, 0.0f);

		private GeneralPath _path = new GeneralPath();

		private List<CurvePoint> _points = new ArrayList<CurvePoint>();

		private void drawPoint(Graphics2D g2, Point p, int size, boolean filled) {
			Stroke s = g2.getStroke();
			g2.setStroke(ls);
			if (filled) {
				g2.fillRect(p.x - size / 2, p.y - size / 2, size, size);
			} else {
				g2.drawRect(p.x - size / 2, p.y - size / 2, size, size);
			}
			g2.setStroke(s);
		}

		private void drawCtrlPoints(Graphics2D g2, Point p, Point c1, Point c2,
				int size) {
			Stroke s = g2.getStroke();
			g2.setStroke(cts);
			drawPoint(g2, c1, size, true);
			g2.drawLine(c1.x, c1.y, p.x, p.y);
			drawPoint(g2, c2, size, true);
			g2.drawLine(c2.x, c2.y, p.x, p.y);
			g2.setStroke(s);
		}
	}

	public static class Erase extends Tool {
		@StrokeType(name = "Erase Stroke", description = "Stroke to be used when erasing the stroke path")
		public Stroke eraseStroke = new BasicStroke(10.0f);

		private Stroke pathStroke = new BasicStroke(1.0f);

		private List<Point> points = new ArrayList<Point>();

		private Point[] _pointArray = new Point[10];

		public Erase(Canvas canvas) {
			super(canvas, "Erase");
		}

		public void mousePressed(MouseEvent e) {
			points.add(new Point(e.getX(), e.getY()));
			repaint();
		}

		public void mouseReleased(MouseEvent e) {
			requestImagePaint();
			points.clear();
		}

		public void mouseDragged(MouseEvent e) {
			points.add(new Point(e.getX(), e.getY()));
			repaint();
		}

		public void paint(Graphics2D g) {
			g.setStroke(pathStroke);
			g.setColor(Color.black);
			int n = points.size();
			if (_pointArray.length < n) {
				_pointArray = new Point[_pointArray.length * 2];
			}
			points.toArray(_pointArray);
			int x1, y1, x2, y2;
			if (n > 1) {
				x1 = _pointArray[0].x;
				y1 = _pointArray[1].y;
				for (int i = 1; i < n; i++) {
					x2 = _pointArray[i].x;
					y2 = _pointArray[i].y;
					g.drawLine(x1, y1, x2, y2);
					x1 = x2;
					y1 = y2;
				}
			}
		}

		public void imagePaint(Graphics2D g, int width, int height) {
			g.setStroke(eraseStroke);
			Composite c = g.getComposite();
			g.setComposite(AlphaComposite.Clear);
			int n = points.size();
			if (_pointArray.length < n) {
				_pointArray = new Point[_pointArray.length * 2];
			}
			points.toArray(_pointArray);
			int x1, y1, x2, y2;
			if (n > 1) {
				x1 = _pointArray[0].x;
				y1 = _pointArray[0].y;
				for (int i = 1; i < n; i++) {
					x2 = _pointArray[i].x;
					y2 = _pointArray[i].y;
					g.drawLine(x1, y1, x2, y2);
					x1 = x2;
					y1 = y2;
				}
			}
			g.setComposite(c);
		}
	}

	public static class Pointer extends Tool {
		private PointerHandler pointerHandler;

		public Pointer(Canvas canvas, PointerHandler pointerHandler) {
			super(canvas, "Pointer");
			this.pointerHandler = pointerHandler;
		}

		@Override
		public boolean onSelected(ToolContext toolContext) {
			super.onSelected(toolContext);
			return pointerHandler.onSelected();
		}

		@Override
		public void onDeselected(ToolContext toolContext) {
			pointerHandler.onDeselected();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			pointerHandler.mouseClicked(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			pointerHandler.mousePressed(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			pointerHandler.mouseReleased(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			pointerHandler.mouseMoved(e);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			pointerHandler.mouseDragged(e);
		}

		public void paint(Graphics2D g) {
		}

		public void imagePaint(Graphics2D g, int width, int height) {
		}
	}

	public static class Select extends Tool {
		public Select(Canvas canvas) {
			super(canvas, "Select");
		}

		public void paint(Graphics2D g) {
		}

		public void imagePaint(Graphics2D g, int width, int height) {
		}
	}

	public static class PanZoom extends Tool {
		public PanZoom(Canvas canvas) {
			super(canvas, "PanZoom");
		}

		@Override
		public boolean onSelected(ToolContext toolContext) {
			super.onSelected(toolContext);
			canvas.getCanvasContainer().setCursor(
					Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			return true;
		}

		private int currX, currY, lastX, lastY;

		@Override
		public void mousePressed(MouseEvent e) {
			currX = lastX = e.getX();
			currY = lastY = e.getY();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			currX = e.getX();
			currY = e.getY();
			canvas.getCanvasContainer().offsetCanvasBy(currX - lastX,
					currY - lastY);
			currX = lastX;
			currY = lastY;
		}

		public void paint(Graphics2D g) {
		}

		public void imagePaint(Graphics2D g, int width, int height) {
		}
	}

	public static class MagicWand extends Tool {
		private MagicWandImpl impl;

		public MagicWand(Canvas canvas) {
			super(canvas, "MagicWand");
		}

		@Override
		public boolean onSelected(ToolContext toolContext) {
			super.onSelected(toolContext);
			return getImageBgRef() != null;
		}

		private BackgroundRef getImageBgRef() {
			BackgroundRef bgref;
			if ((bgref = ((CanvasMesh) canvas).getCurrLayer()
					.findBackgroundRef()) == null
					|| !bgref.getType().equals(BGImage.Type)) {
				JOptionPane.showMessageDialog(canvas.getCanvasContainer(),
						"Please set a background reference (image) first",
						"Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			BufferedImage bgrefImage = (BufferedImage) bgref.getDrawable();
			if (impl == null || impl.srcImage != bgrefImage) {
				impl = new MagicWandImpl(bgrefImage);
			}
			return bgref;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			impl.mousePressed(e.getX() + canvas.getWidth() / 2, e.getY()
					+ canvas.getHeight() / 2, e.isShiftDown());
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (impl.tracking) {
				return;
			}
			int cx = canvas.getWidth() / 2;
			int cy = canvas.getHeight() / 2;
			List<Point> points = new ArrayList<Point>();
			for (Contour c : impl.contours) {
				// Points are stored in reverse order in contours
				for (int i = c.count - 1; i >= 0; i--) {
					Point p = new Point(c.xs[i] - cx, c.ys[i] - cy);
					points.add(p);
				}
			}
			pathCreated(Utils.subdivide(points.toArray(new Point[0])));
			requestImagePaint();
			impl.contours.clear();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			int cx = canvas.getWidth() / 2;
			int cy = canvas.getHeight() / 2;
			impl.mouseMoved(e.getX() + cx, e.getY() + cy);
			repaint();
		}

		private Stroke stroke = new BasicStroke(2.0f);

		public void paint(Graphics2D g) {
			g.setStroke(stroke);
			g.setColor(Color.green);
			int cx = canvas.getWidth() / 2;
			int cy = canvas.getHeight() / 2;
			for (Contour c : impl.contours) {
				for (int i = 0; i < c.count - 1; i++) {
					g.drawLine(c.xs[i] - cx, c.ys[i] - cy, c.xs[i + 1] - cx,
							c.ys[i + 1] - cy);
				}
			}
			if (impl.currContour != null) {
				g.setColor(Color.red);
				for (int i = 0; i < impl.currContour.count - 1; i++) {
					g.drawLine(impl.currContour.xs[i] - cx,
							impl.currContour.ys[i] - cy,
							impl.currContour.xs[i + 1] - cx,
							impl.currContour.ys[i + 1] - cy);
				}
			}
		}

		public void imagePaint(Graphics2D g, int width, int height) {
		}
	}

	public static class SetControlPoints extends Tool {
		private CanvasMesh canvas;

		public SetControlPoints(CanvasMesh canvas) {
			super(canvas, "SetControlPoints");
			this.canvas = canvas;
		}

		@Override
		public boolean onSelected(ToolContext toolContext) {
			super.onSelected(toolContext);
			if (!canvas.getCurrLayer().getPath().isFinalized()) {
				JOptionPane.showMessageDialog(canvas.getCanvasContainer(),
						"Please create a mesh first", "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
		}

		public void mouseClicked(MouseEvent e) {
			canvas.getCurrLayer().getPath().getMesh()
					.toggleControlPointAt(e.getX(), e.getY());
			repaint();
			canvas.fireMeshEdit(Constants.MESH_SET_CONTROLS);
		}

		public void paint(Graphics2D g) {
		}

		public void imagePaint(Graphics2D g, int width, int height) {
		}
	}
}