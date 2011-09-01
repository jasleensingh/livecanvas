package livecanvas.components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import common.typeutils.AutoPanel;
import common.typeutils.Property;
import common.typeutils.PropertyFactory;
import common.typeutils.PropertyListener;

import livecanvas.animator.Interpolator;

public class InterpolationEditor extends JDialog {
	private static Interpolator[] TEMPLATES;
	static {
		List<Interpolator> list = new LinkedList<Interpolator>();
		list.add(new Interpolator(0, 0, 1, 1));
		list.add(new Interpolator(0, 1, 0, 1));
		list.add(new Interpolator(0, 1, 1, 1));
		list.add(new Interpolator(0, 1, 1, 0));
		list.add(new Interpolator(1, 0, 0, 1));
		list.add(new Interpolator(1, 0, 1, 1));
		list.add(new Interpolator(1, 0, 1, 0));
		TEMPLATES = list.toArray(new Interpolator[0]);
	}

	public InterpolationEditor(Component parent, Interpolator interpolator) {
		super(JOptionPane.getFrameForComponent(parent), "Interpolation Editor",
				true);
		setContentPane(new InterpolatorEditorView(interpolator));
		pack();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(getParent());
	}

	private class InterpolatorEditorView extends JPanel {
		private GraphView graphView;

		public InterpolatorEditorView(Interpolator interpolator) {
			super(new BorderLayout());
			JPanel center = new JPanel(new BorderLayout(10, 10));
			center.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			Property[] props = PropertyFactory.createProperties(interpolator);
			props[0].addListener(new PropertyListener() {
				@Override
				public void valueChanged(Property prop) {
					repaint();
				}
			});
			center.add(new AutoPanel(props), BorderLayout.NORTH);
			JPanel graphViewContainer = new JPanel(new GridBagLayout());
			graphView = new GraphView(interpolator, 400);
			graphView.setBorder(BorderFactory
					.createBevelBorder(BevelBorder.LOWERED));
			graphViewContainer.add(graphView);
			center.add(graphViewContainer);
			add(center);
			JPanel east = new JPanel(new BorderLayout());
			east.setPreferredSize(new Dimension(220, 10));
			east.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(10, 0, 10, 10),
					BorderFactory.createTitledBorder("Templates")));
			JPanel scpView = new JPanel(new BorderLayout());
			Box templates = new Box(BoxLayout.Y_AXIS);
			templates
					.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			ActionListener templateSelected = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					TemplateView tv = (TemplateView) e.getSource();
					Interpolator in = graphView.getInterpolator();
					in.control1.setLocation(tv.interpolator.control1);
					in.control2.setLocation(tv.interpolator.control2);
					repaint();
				}
			};
			for (int i = 0; i < TEMPLATES.length; i++) {
				if (i > 0) {
					templates.add(Box.createVerticalStrut(10));
				}
				templates.add(new TemplateView(TEMPLATES[i], templateSelected));
			}
			scpView.add(templates, BorderLayout.NORTH);
			JScrollPane scp = new JScrollPane(scpView);
			scp.setBorder(null);
			east.add(scp);
			add(east, BorderLayout.EAST);
			JPanel south = new JPanel(new BorderLayout());
			south.add(new JSeparator(), BorderLayout.NORTH);
			JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JButton done = new JButton("Done");
			done.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					InterpolationEditor.this.dispose();
				}
			});
			buttons.add(done);
			south.add(buttons);
			add(south, BorderLayout.SOUTH);
		}

		private class TemplateView extends JButton {
			public final Interpolator interpolator;

			public TemplateView(Interpolator interpolator, ActionListener al) {
				this.interpolator = interpolator;
				setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
				setContentAreaFilled(false);
				setFocusable(false);
				addActionListener(al);
				int size = 150;
				BufferedImage img = new BufferedImage(size, size,
						BufferedImage.TYPE_INT_RGB);
				Graphics g = img.createGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, size, size);
				GraphView gv = new GraphView(interpolator, size);
				gv.draw(g, size, size);
				g.dispose();
				setIcon(new ImageIcon(img));
			}
		}

		private class GraphView extends JPanel {
			private final int ControlPointSize;
			private Interpolator interpolator;
			private Color gridColor = new Color(0xffcccccc);
			private Color emgridColor = new Color(0xff777777);
			private Stroke graphStroke, controlPointLineStroke;
			private Point2D.Float currControlPoint;
			private int left, top, right, bottom;
			private Point loc = new Point();

			public GraphView(Interpolator in, int size) {
				super(null);
				this.interpolator = in;
				ControlPointSize = size / 30;
				graphStroke = new BasicStroke(size / 100.0f);
				controlPointLineStroke = new BasicStroke(1.0f);
				setBackground(Color.white);
				setPreferredSize(new Dimension(size, size));
				addMouseListener(new MouseAdapter() {
					private Rectangle rect = new Rectangle();

					@Override
					public void mousePressed(MouseEvent e) {
						pointOnGraph(interpolator.control1.x,
								interpolator.control1.y, loc);
						rect.setBounds(loc.x - ControlPointSize / 2, loc.y
								- ControlPointSize / 2, ControlPointSize,
								ControlPointSize);
						if (rect.contains(e.getX(), e.getY())) {
							currControlPoint = interpolator.control1;
							return;
						}
						pointOnGraph(interpolator.control2.x,
								interpolator.control2.y, loc);
						rect.setBounds(loc.x - ControlPointSize / 2, loc.y
								- ControlPointSize / 2, ControlPointSize,
								ControlPointSize);
						if (rect.contains(e.getX(), e.getY())) {
							currControlPoint = interpolator.control2;
							return;
						}
						currControlPoint = null;
					}

					@Override
					public void mouseReleased(MouseEvent e) {
						currControlPoint = null;
					}
				});
				addMouseMotionListener(new MouseMotionAdapter() {
					@Override
					public void mouseDragged(MouseEvent e) {
						if (currControlPoint != null) {
							float x = Math.max(
									0,
									Math.min(1, (float) (e.getX() - left)
											/ (right - left)));
							float y = Math.max(
									0,
									Math.min(1, (float) (bottom - e.getY())
											/ (bottom - top)));
							currControlPoint.setLocation(x, y);
							repaint();
						}
					}
				});
			}

			private void pointOnGraph(float fx, float fy, Point point) {
				point.x = (int) (left + fx * (right - left));
				point.y = (int) (bottom - fy * (bottom - top));
			}

			public Interpolator getInterpolator() {
				return interpolator;
			}

			public void setInterpolator(Interpolator interpolator) {
				this.interpolator = interpolator;
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				draw(g, getWidth(), getHeight());
			}

			public void draw(Graphics g, int width, int height) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				int margin = 3;
				int divisions = 30;
				int steps = divisions + margin * 2;
				int size = Math.min(width, height);
				for (int i = 1; i < steps; i++) {
					g2.setColor((i - margin) % 5 == 0 ? emgridColor : gridColor);
					int x = i * size / steps;
					g.drawLine(x, 0, x, size);
				}
				for (int i = 1; i < steps; i++) {
					g2.setColor((i - margin) % 5 == 0 ? emgridColor : gridColor);
					int y = i * size / steps;
					g.drawLine(0, y, size, y);
				}
				g.setColor(Color.black);
				left = top = 3 * size / steps;
				right = bottom = 33 * size / steps;
				g.drawLine(0, left, size, left);
				g.drawLine(0, right, size, right);
				g.drawLine(top, 0, top, size);
				g.drawLine(bottom, 0, bottom, size);
				int x1, y1, x2, y2;
				float dx = 1.0f / divisions;
				int rangex = right - left;
				int rangey = bottom - top;
				g2.setColor(Color.red);
				g2.setStroke(controlPointLineStroke);
				pointOnGraph(interpolator.control1.x, interpolator.control1.y,
						loc);
				g2.drawLine(left, bottom, loc.x, loc.y);
				g2.fillOval(loc.x - ControlPointSize / 2, loc.y
						- ControlPointSize / 2, ControlPointSize,
						ControlPointSize);
				pointOnGraph(interpolator.control2.x, interpolator.control2.y,
						loc);
				g2.drawLine(right, top, loc.x, loc.y);
				g2.fillOval(loc.x - ControlPointSize / 2, loc.y
						- ControlPointSize / 2, ControlPointSize,
						ControlPointSize);
				x1 = left;
				y1 = bottom;
				g2.setColor(Color.black);
				g2.setStroke(graphStroke);
				for (float x = dx; x <= 1.01f; x += dx) {
					x2 = left + (int) (x * rangex);
					y2 = bottom - (int) (interpolator.findYForX(x) * rangey);
					g.drawLine(x1, y1, x2, y2);
					x1 = x2;
					y1 = y2;
				}
				if (interpolator.intermediateFramesCount > 0) {
					for (int i = 1; i <= interpolator.intermediateFramesCount; i++) {
						float x = (float) i
								/ (interpolator.intermediateFramesCount + 1);
						x1 = x2 = left + (int) (x * rangex);
						y1 = bottom
								- (int) (interpolator.findYForX(x) * rangey)
								- 10;
						y2 = y1 + 20;
						g.drawLine(x1, y1, x2, y2);
					}
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new InterpolationEditor(null, new Interpolator()).setVisible(true);
		System.exit(0);
	}
}
