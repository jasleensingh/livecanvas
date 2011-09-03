package livecanvas;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class CanvasContainer extends JPanel {
	private static final Class clazz = CanvasContainer.class;

	public static final int[] ZOOM_PERCENTS = { 1600, 1000, 800, 400, 200, 150,
			100, 75, 50, 25, 10 };

	private static final Paint texture;
	static {
		BufferedImage t = null;
		try {
			t = ImageIO.read(clazz.getResourceAsStream("res/bgtex.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		texture = new TexturePaint(t, new Rectangle2D.Float(0, 0, t.getWidth(),
				t.getHeight()));
	}

	private Canvas canvas;
	private Rectangle canvasBounds = new Rectangle();
	private Point canvasOffset = new Point();
	private int zoomPercent = 100;

	private ComponentListener cl = new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
			layoutCanvas();
		}
	};

	private boolean mousePressed;

	private MouseListener ml = new MouseListener() {
		public void mouseReleased(MouseEvent e) {
			if (mousePressed) {
				e.translatePoint(
						-((int) canvasBounds.getCenterX() + canvasOffset.x),
						-((int) canvasBounds.getCenterY() + canvasOffset.y));
				canvas.mouseReleased(e);
			}
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			if (canvasBounds.contains(x, y)) {
				mousePressed = true;
				e.translatePoint(
						-((int) canvasBounds.getCenterX() + canvasOffset.x),
						-((int) canvasBounds.getCenterY() + canvasOffset.y));
				canvas.mousePressed(e);
			}
		}

		public void mouseClicked(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			if (canvasBounds.contains(x, y)) {
				e.translatePoint(
						-((int) canvasBounds.getCenterX() + canvasOffset.x),
						-((int) canvasBounds.getCenterY() + canvasOffset.y));
				canvas.mouseClicked(e);
			}
		}
	};

	private MouseMotionListener mml = new MouseMotionListener() {
		public void mouseMoved(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			if (canvasBounds.contains(x, y)) {
				e.translatePoint(
						-((int) canvasBounds.getCenterX() + canvasOffset.x),
						-((int) canvasBounds.getCenterY() + canvasOffset.y));
				canvas.mouseMoved(e);
			}
		}

		public void mouseDragged(MouseEvent e) {
			if (mousePressed) {
				e.translatePoint(
						-((int) canvasBounds.getCenterX() + canvasOffset.x),
						-((int) canvasBounds.getCenterY() + canvasOffset.y));
				canvas.mouseDragged(e);
			}
		}
	};

	private MouseWheelListener mwl = new MouseWheelListener() {
		public void mouseWheelMoved(MouseWheelEvent e) {
			int onMask = MouseWheelEvent.CTRL_DOWN_MASK;
			if ((e.getModifiersEx() & onMask) == onMask) {
				int i = 0;
				while (i < ZOOM_PERCENTS.length - 1
						&& !(zoomPercent < ZOOM_PERCENTS[i] && zoomPercent >= ZOOM_PERCENTS[i + 1])) {
					i++;
				}
				int rot = e.getWheelRotation();
				int nz = zoomPercent;
				if (rot < 0) { // zoom in
					if (i > 0) {
						int dpc = (ZOOM_PERCENTS[i] - ZOOM_PERCENTS[i + 1] + 10) / 10;
						nz += dpc;
					}
				} else { // zoom out
					if (i < ZOOM_PERCENTS.length - 1) {
						int dpc = (ZOOM_PERCENTS[i] - ZOOM_PERCENTS[i + 1] + 10) / 10;
						nz -= dpc;
					}
				}
				if (nz <= ZOOM_PERCENTS[0]
						&& nz >= ZOOM_PERCENTS[ZOOM_PERCENTS.length - 1]) {
					setZoomPercent(nz);
				}
			}
		}
	};

	private AWTEventListener ael = new AWTEventListener() {
		public void eventDispatched(AWTEvent event) {
			if (event.getID() == KeyEvent.KEY_PRESSED) {
				KeyEvent e = (KeyEvent) event;
				canvas.keyPressed(e);
				System.err.println(e.getKeyCode() + ", " + KeyEvent.VK_F11);
				if (e.getKeyCode() == KeyEvent.VK_F11) {
					BufferedImage img = new BufferedImage(640, 480,
							BufferedImage.TYPE_INT_RGB);
					Graphics2D g3 = img.createGraphics();
					g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					g3.setColor(Color.white);
					g3.fillRect(0, 0, 640, 480);
					g3.translate(320, 240);
					canvas.paint(g3, 640, 480);
					try {
						ImageIO.write(img, "png", new File(
								"C:/Users/Jasleen/Desktop/thesis/temp/out.png"));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	};

	public CanvasContainer(Canvas canvas) {
		super(null);
		setOpaque(false);
		setFocusable(true);
		this.canvas = canvas;
		canvas.setCanvasContainer(this);
		layoutCanvas();
		canvas.init();
		addComponentListener(cl);
		addMouseListener(ml);
		addMouseMotionListener(mml);
		addMouseWheelListener(mwl);
		Toolkit.getDefaultToolkit().addAWTEventListener(ael,
				AWTEvent.KEY_EVENT_MASK);
	}

	public void layoutCanvas() {
		canvasBounds.width = canvas.getWidth() * zoomPercent / 100;
		canvasBounds.height = canvas.getHeight() * zoomPercent / 100;
		canvasBounds.x = (getWidth() - canvasBounds.width) / 2;
		canvasBounds.y = (getHeight() - canvasBounds.height) / 2;
		repaint();
	}

	public int getZoomPercent() {
		return zoomPercent;
	}

	public void setZoomPercent(int zoomPercent) {
		this.zoomPercent = zoomPercent;
		layoutCanvas();
	}

	public Point getCanvasOffset() {
		return canvasOffset;
	}

	public void setCanvasOffset(int x, int y) {
		canvasOffset.x = x;
		canvasOffset.y = y;
		repaint();
	}

	public void offsetCanvasBy(int dx, int dy) {
		setCanvasOffset(canvasOffset.x + dx, canvasOffset.y + dy);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.gray);
		int x = canvasBounds.x + canvasOffset.x;
		int y = canvasBounds.y + canvasOffset.y;
		int width = canvasBounds.width;
		int height = canvasBounds.height;
		g2.fillRect(x + 3, y + 3, width, height);
		if (!canvas.settings.canvasOpaque) {
			g2.setPaint(texture);
		} else {
			g2.setPaint(canvas.settings.canvasBackgroundColor);
		}
		g2.fillRect(x, y, width, height);
		int cx = x + width / 2;
		int cy = y + height / 2;
		g2.translate(cx, cy);
		canvas.paint(g2, width, height);
		g2.translate(-cx, -cy);
	}
}
