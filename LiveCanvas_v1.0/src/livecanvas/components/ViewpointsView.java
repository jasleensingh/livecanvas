package livecanvas.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import livecanvas.Constants;

public class ViewpointsView extends JPanel implements Constants {
	public static interface Listener {
		public void viewpointChanged(int vx, int vy);
	}

	private Layer layer;
	private List<Listener> listeners = new LinkedList<Listener>();

	public ViewpointsView(Layer layer) {
		super(new BorderLayout(2, 2));
		this.layer = layer;
		add(new Label("Viewpoints"), BorderLayout.NORTH);
		add(new Select());
	}

	public void layerChanged(Layer layer) {
		this.layer = layer;
		repaint();
	}

	public void addListener(Listener l) {
		listeners.add(l);
	}

	public void removeListener(Listener l) {
		listeners.remove(l);
	}

	protected void notifyLayerSelectionChanged(int vx, int vy) {
		for (Listener l : listeners) {
			l.viewpointChanged(vx, vy);
		}
	}

	private class Select extends JPanel {
		private double dx, dy;
		private MouseListener ml = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				notifyLayerSelectionChanged((int) (e.getX() / dx)
						- ANGLE_DIVISIONS2, (int) (e.getY() / dy)
						- ANGLE_DIVISIONS2);
				repaint();
			}
		};

		public Select() {
			super(null);
			setBackground(Color.white);
			setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			setPreferredSize(new Dimension(200, 200));
			addMouseListener(ml);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			int width = getWidth();
			int height = getHeight();
			dx = (double) width / ANGLE_DIVISIONS;
			dy = (double) height / ANGLE_DIVISIONS;
			int left = (int) (dx / 2);
			int top = (int) (dy / 2);
			g.setColor(Color.red);
			g.drawLine(0, (int) (top + dy * ANGLE_DIVISIONS2), width,
					(int) (top + dy * ANGLE_DIVISIONS2));
			g.setColor(Color.green);
			g.drawLine((int) (left + dx * ANGLE_DIVISIONS2), 0,
					(int) (left + dx * ANGLE_DIVISIONS2), height);
			for (int i = 0; i < ANGLE_DIVISIONS; i++) {
				for (int j = 0; j < ANGLE_DIVISIONS; j++) {
					drawViewpointIcon(g, (int) (left + dx * j), (int) (top + dy
							* i), (int) dx, (int) dy, (i - ANGLE_DIVISIONS2)
							* ANGLE_STEPSIZE, (j - ANGLE_DIVISIONS2)
							* ANGLE_STEPSIZE,
							layer.viewpoints[j][i] == layer
									.getCurrentViewpoint(),
							layer.viewpoints[j][i].getPath().isFinalized());
				}
			}
		}

		private void drawViewpointIcon(Graphics g, int cx, int cy, int width,
				int height, double rotx, double roty, boolean selected,
				boolean finalized) {
			// int iconWidth = (int) Math.abs(width * Math.cos(roty));
			// int iconHeight = (int) Math.abs(height * Math.cos(rotx));
			// g.drawOval(cx - iconWidth / 2, cy - iconHeight / 2, iconWidth,
			// iconHeight);
			if (selected) {
				g.setColor(Color.yellow);
				g.fillRect(cx - width / 2, cy - width / 2, width, height);
			}
			if (finalized) {
				g.setColor(Color.red);
				g.fillOval(cx - 5, cy - 5, 10, 10);
			} else {
				g.setColor(Color.black);
				g.fillRect(cx - 2, cy - 2, 4, 4);
			}
		}
	}
	//
	// public static void main(String[] args) throws Exception {
	// JDialog d = new JDialog((JFrame) null, "ViewpointsView Test", true);
	// d.setContentPane(new ViewpointsView(new Layer("Test")));
	// d.pack();
	// d.setLocationRelativeTo(null);
	// d.setVisible(true);
	// System.exit(0);
	// }
}
