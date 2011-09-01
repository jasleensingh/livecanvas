package common.typeutils;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.SystemColor;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import common.typeutils.MyWidgets.MyGradientField;



public class EditGradientDialog extends JDialog {

	private static final String OK = "Ok", CANCEL = "Cancel";

	private Gradient gradient, _gradient;

	private EditGradientPanel editGradientPanel;

	private InfoPanel infoPanel;

	@GradientType(name = "Preset", description = "Choice of preset gradients", category = "Edit")
	public Gradient preset = Gradient.GRADIENTS[0];

	public EditGradientDialog(Frame owner, Gradient gradient) {
		super(owner, "Edit Gradient", true);
		this.gradient = gradient;

		initComponents();

		pack();
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	public Gradient getGradient() {
		return _gradient;
	}

	private void initComponents() {
		Container contentPane = getContentPane();
		contentPane.setPreferredSize(new Dimension(600, 400));

		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		GradientProperty gp = (GradientProperty) PropertyFactory
				.createProperty("preset", this);
		MyGradientField gf = new MyGradientField(gp, false);
		Box box = new Box(BoxLayout.X_AXIS);
		box.add(new JLabel("Preset Gradients: "));
		box.add(Box.createHorizontalStrut(5));
		box.add(gf);
		box.add(Box.createHorizontalStrut(5));
		JButton applyGradient = new JButton("Apply Gradient");
		applyGradient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				preset.copyInto(gradient);
				editGradientPanel.updateSliders();
				repaint();
			}
		});
		box.add(applyGradient);
		box.add(Box.createHorizontalStrut(150));
		mainPanel.add(box, BorderLayout.NORTH);

		editGradientPanel = new EditGradientPanel();
		mainPanel.add(editGradientPanel);

		infoPanel = new InfoPanel();
		mainPanel.add(infoPanel, BorderLayout.SOUTH);
		contentPane.add(mainPanel);

		ActionListener a = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				if (cmd.equals(OK)) {
					ok();
					setVisible(false);
				} else if (cmd.equals(CANCEL)) {
					cancel();
					setVisible(false);
				}
			}
		};
		JPanel buttons = new JPanel(new BorderLayout());
		buttons.add(new JSeparator(), BorderLayout.NORTH);
		box = new Box(BoxLayout.X_AXIS);
		box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		box.add(Box.createHorizontalGlue());
		JButton ok = new JButton("Ok");
		ok.setActionCommand(OK);
		ok.addActionListener(a);
		box.add(ok);
		box.add(Box.createHorizontalStrut(10));
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand(CANCEL);
		cancel.addActionListener(a);
		box.add(cancel);
		ok.setPreferredSize(cancel.getPreferredSize());
		buttons.add(box);
		contentPane.add(buttons, BorderLayout.SOUTH);
	}

	private void ok() {
		_gradient = gradient;
	}

	private void cancel() {
	}

	public void setVisible(boolean b) {
		if (b) {
			setLocationRelativeTo(getOwner());
		}
		super.setVisible(b);
	}

	private class EditGradientPanel extends JPanel implements ActionMap {
		private final int PADDING = 20;

		private final int REGION_NONE = 0, REGION_ALPHA = 1, REGION_COLOR = 2,
				REGION_PALETTE = 3;

		private final int PALETTE_WIDTH = 512, PALETTE_HEIGHT = 50;

		private List<ColorSlider> colorSliders = new LinkedList<ColorSlider>();

		private List<AlphaSlider> alphaSliders = new LinkedList<AlphaSlider>();

		private Slider overSlider, selSlider, dragSlider, leftSlider,
				rightSlider;

		private int region = REGION_NONE;

		private double loc;

		private Map<String, Action> actions;

		private JPopupMenu popupMenu;

		// Minimum difference between loc of two sliders
		private double epsilon;

		private MouseListener ml = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (overSlider != null) {
						int n = e.getClickCount();
						if (n == 2) {
							edit();
						}
					} else {
						add();
					}
				}
			}

			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopup(e.getX(), e.getY());
					return;
				}
				setSelected(overSlider);
				if (overSlider != null) {
					if (!overSlider.fixed) {
						dragSlider = overSlider;
						if (dragSlider instanceof ColorSlider) {
							leftSlider = colorSliders.get(dragSlider.index - 1);
							rightSlider = colorSliders
									.get(dragSlider.index + 1);
						} else if (dragSlider instanceof AlphaSlider) {
							leftSlider = alphaSliders.get(dragSlider.index - 1);
							rightSlider = alphaSliders
									.get(dragSlider.index + 1);
						}
						dragSlider.dragOn(true);
					}
					repaint();
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopup(e.getX(), e.getY());
					return;
				}
				if (dragSlider != null) {
					if (region == REGION_NONE) {
						delete();
					} else {
						updateGradient();
					}
					dragSlider.dragOn(false);
					dragSlider = null;
				}
			}

			private void showPopup(int x, int y) {
				EditGradientPanel.this.update();
				popupMenu.show(EditGradientPanel.this, x, y);
			}
		};

		private MouseMotionListener mml = new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				int mx = e.getX(), my = e.getY();
				int x = Math.max(PADDING, (getWidth() - PALETTE_WIDTH) / 2);
				int y = Math.max(PADDING, (getHeight() - PALETTE_HEIGHT) / 2);
				float dx = PALETTE_WIDTH / gradient.colors.length;
				region = getRegion(mx, my, x, y);
				if (region == REGION_NONE) {
					return;
				}
				loc = (double) (mx - x) / PALETTE_WIDTH;
				int i = (int) ((mx - x) / dx);
				infoPanel.setColor(gradient.colors[i]);
				overSlider = null;
				switch (region) {
				case REGION_COLOR:
					for (ColorSlider cs : colorSliders) {
						Rectangle bounds = cs.getBounds(x, y + PALETTE_HEIGHT
								+ 5, dx);
						if (bounds.contains(mx, my)) {
							overSlider = cs;
							break;
						}
					}
					break;
				case REGION_ALPHA:
					for (AlphaSlider as : alphaSliders) {
						Rectangle bounds = as.getBounds(x, y - 25, dx);
						if (bounds.contains(mx, my)) {
							overSlider = as;
							break;
						}
					}
					break;
				}
			}

			public void mouseDragged(MouseEvent e) {
				int mx = e.getX(), my = e.getY();
				int x = Math.max(PADDING, (getWidth() - PALETTE_WIDTH) / 2);
				int y = Math.max(PADDING, (getHeight() - PALETTE_HEIGHT) / 2);
				region = getRegion(mx, my, x, y);
				if (dragSlider != null) {
					if (region == REGION_NONE) {
						dragSlider.visible = false;
					} else {
						dragSlider.visible = true;
						double nloc = (double) (mx - x) / PALETTE_WIDTH;
						if (nloc > leftSlider.loc + epsilon
								&& nloc < rightSlider.loc - epsilon) {
							dragSlider.loc = nloc;
						}
					}
					repaint();
				}
			}

			private int getRegion(int mx, int my, int x, int y) {
				if (mx < x || mx >= x + PALETTE_WIDTH || my < y - 35
						|| my > y + PALETTE_HEIGHT + 35) {
					return REGION_NONE;
				} else if (my > y + PALETTE_HEIGHT) {
					return REGION_COLOR;
				} else if (my < y) {
					return REGION_ALPHA;
				}
				return REGION_PALETTE;
			}
		};

		public EditGradientPanel() {
			super(null);

			actions = new HashMap<String, Action>();
			actions.put("EditAction", new EditAction());
			actions.put("AddAction", new AddAction());
			actions.put("DeleteAction", new DeleteAction());

			MenuFactory mf = new MenuFactory(getClass().getCanonicalName(),
					this);
			popupMenu = mf.createMenu("PopupMenu").getPopupMenu();

			epsilon = 1.0 / gradient.colors.length;

			setBackground(SystemColor.lightGray);
			setBorder(BorderFactory.createLineBorder(Color.black));
			addMouseListener(ml);
			addMouseMotionListener(mml);
			updateSliders();
		}

		public Action getAction(String key) {
			return actions.get(key);
		}

		public Slider getSelected() {
			return selSlider;
		}

		public void setSelected(Slider slider) {
			for (ColorSlider cs : colorSliders) {
				cs.selected = false;
			}
			for (AlphaSlider as : alphaSliders) {
				as.selected = false;
			}
			selSlider = slider;
			if (slider != null) {
				slider.selected = true;
			}
		}

		protected class EditAction extends AbstractAction {
			public void actionPerformed(ActionEvent e) {
				edit();
			}
		};

		private void edit() {
			Slider slider = getSelected();
			if (slider == null) {
				return;
			}
			slider.edit();
			updateGradient();
		}

		protected class AddAction extends AbstractAction {
			public void actionPerformed(ActionEvent e) {
				add();
			}
		};

		private void add() {
			int index;
			switch (region) {
			case REGION_ALPHA:
				index = 0;
				for (AlphaSlider as : alphaSliders) {
					if (Math.abs(as.loc - loc) < epsilon) {
						return;
					}
					if (as.loc > loc) {
						break;
					}
					index++;
				}
				alphaSliders.add(new AlphaSlider(loc, false, index) {
					float tmp;
					{
						AlphaSlider a1 = alphaSliders.get(index - 1), a2 = alphaSliders
								.get(index);
						tmp = (float) (a1.alpha + (loc - a1.loc)
								* (a2.alpha - a1.alpha) / (a2.loc - a1.loc));
					}

					public float getAlpha() {
						return tmp;
					}
				});
				repaint();
				break;
			case REGION_COLOR:
				index = 0;
				for (ColorSlider cs : colorSliders) {
					if (Math.abs(cs.loc - loc) < 0.01) {
						return;
					}
					if (cs.loc > loc) {
						break;
					}
					index++;
				}
				colorSliders.add(new ColorSlider(loc, false, index) {
					Color tmp = gradient.colors[(int) (loc * gradient.colors.length)];

					public Color getColor() {
						return tmp;
					}
				});
				repaint();
				break;
			}
			updateGradient();
			updateSliders();
		}

		protected class DeleteAction extends AbstractAction {
			public void actionPerformed(ActionEvent e) {
				delete();
			}
		};

		private void delete() {
			Slider slider = getSelected();
			if (slider == null) {
				return;
			}
			if (slider instanceof ColorSlider) {
				colorSliders.remove(slider);
			} else if (slider instanceof AlphaSlider) {
				alphaSliders.remove(slider);
			}
			updateGradient();
			updateSliders();
		}

		private void update() {
			// Region not NONE
			boolean rnn = region != REGION_NONE;
			boolean ss = false, ess = false, rac = false;
			if (rnn) {
				// Slider selected
				ss = selSlider != null;
				// end slider selected?
				if (ss) {
					if (selSlider instanceof ColorSlider) {
						ess = selSlider.index <= 0
								|| selSlider.index >= colorSliders.size() - 1;
					} else if (selSlider instanceof AlphaSlider) {
						ess = selSlider.index <= 0
								|| selSlider.index >= alphaSliders.size() - 1;
					}
				}
				// region alpha or color
				rac = region == REGION_ALPHA || region == REGION_COLOR;
			}
			getAction("EditAction").putValue(ABPCListener.PROPERTY_ENABLED, ss);
			getAction("AddAction").putValue(ABPCListener.PROPERTY_ENABLED,
					!ss && rac);
			getAction("DeleteAction").putValue(ABPCListener.PROPERTY_ENABLED,
					ss && !ess);
		}

		private void updateGradient() {
			Comparator<Slider> comp = new Comparator<Slider>() {
				public int compare(Slider s1, Slider s2) {
					return (int) Math.signum(s1.loc - s2.loc);
				}
			};
			ColorSlider[] cs = colorSliders.toArray(new ColorSlider[0]);
			Arrays.sort(cs, comp);
			Color[] keyColors = new Color[cs.length];
			double[] ratioColors = cs.length > 2 ? new double[cs.length - 2]
					: null;
			for (int i = 0; i < cs.length; i++) {
				keyColors[i] = cs[i].getColor();
				if (i > 0 && i < cs.length - 1) {
					ratioColors[i - 1] = cs[i].loc;
				}
			}
			AlphaSlider[] as = alphaSliders.toArray(new AlphaSlider[0]);
			Arrays.sort(as, comp);
			float[] keyAlphas = new float[as.length];
			double[] ratioAlphas = as.length > 2 ? new double[as.length - 2]
					: null;
			for (int i = 0; i < as.length; i++) {
				keyAlphas[i] = as[i].getAlpha();
				if (i > 0 && i < as.length - 1) {
					ratioAlphas[i - 1] = as[i].loc;
				}
			}
			gradient.keyColors = keyColors;
			gradient.ratioColors = ratioColors;
			gradient.colors = Gradient.interpolate(gradient.colors.length,
					keyColors, ratioColors, keyAlphas, ratioAlphas);
			gradient.keyAlphas = keyAlphas;
			gradient.ratioAlphas = ratioAlphas;
			epsilon = 1.0 / gradient.colors.length;
			repaint();
		}

		private void updateSliders() {
			colorSliders.clear();
			alphaSliders.clear();
			boolean useColorRatios = gradient.ratioColors != null, useAlphaRatios = gradient.ratioAlphas != null;
			for (int i = 0; i < gradient.keyColors.length; i++) {
				double loc;
				boolean fixed;
				if (useColorRatios) {
					if (i > 0) {
						if (i < gradient.keyColors.length - 1) {
							fixed = false;
							loc = gradient.ratioColors[i - 1];
						} else {
							fixed = true;
							loc = 1.0;
						}
					} else {
						fixed = true;
						loc = 0.0;
					}
				} else {
					fixed = (i <= 0 || i >= gradient.keyColors.length - 1);
					loc = (double) i / (gradient.keyColors.length - 1);
				}
				colorSliders.add(new ColorSlider(loc, fixed, i));
			}
			for (int i = 0; i < gradient.keyAlphas.length; i++) {
				double loc;
				boolean fixed;
				float alpha = gradient.keyAlphas[i];
				if (useAlphaRatios) {
					if (i > 0) {
						if (i < gradient.keyAlphas.length - 1) {
							fixed = false;
							loc = gradient.ratioAlphas[i - 1];
						} else {
							fixed = true;
							loc = 1.0;
						}
					} else {
						fixed = true;
						loc = 0.0;
					}
				} else {
					fixed = (i <= 0 || i >= gradient.keyAlphas.length - 1);
					loc = (double) i / (gradient.keyAlphas.length - 1);
				}
				alphaSliders.add(new AlphaSlider(loc, fixed, i, alpha));
			}
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2 = (Graphics2D) g;
			int x = Math.max(PADDING, (getWidth() - PALETTE_WIDTH) / 2);
			int y = Math.max(PADDING, (getHeight() - PALETTE_HEIGHT) / 2);
			float dx = PALETTE_WIDTH / gradient.colors.length;
			for (int i = 0; i < gradient.colors.length; i++) {
				g2.setColor(gradient.colors[i]);
				g2.fillRect((int) (x + i * dx), y, (int) dx, PALETTE_HEIGHT);
				g2.setColor(Color.black);
				g2.drawRect((int) (x + i * dx), y, (int) dx, PALETTE_HEIGHT);
			}
			for (ColorSlider cs : colorSliders) {
				cs.paint(g2, x, y + PALETTE_HEIGHT + 5, dx);
			}
			for (AlphaSlider as : alphaSliders) {
				as.paint(g2, x, y - 25, dx);
			}
		}

		private abstract class Slider {
			protected final Stroke NORMAL_STROKE = new BasicStroke(1.0f),
					SELECTION_STROKE = new BasicStroke(3.0f);

			public double loc;

			public final boolean fixed;

			public final int index;

			public boolean visible = true;

			public boolean selected;

			public Slider(double loc, boolean fixed, int index) {
				this.loc = loc;
				this.fixed = fixed;
				this.index = index;
			}

			public Rectangle getBounds(int xoff, int yoff, float dx) {
				return new Rectangle((int) (xoff + loc * (PALETTE_WIDTH - dx)),
						yoff, (int) dx, 20);
			}

			public abstract void paint(Graphics2D g2, int xoff, int yoff,
					float dx);

			public abstract void edit();

			protected boolean dragOn;

			protected double origLoc;

			public void dragOn(boolean b) {
				dragOn = b;
				origLoc = loc;
			}
		}

		private class ColorSlider extends Slider {
			public ColorSlider(double loc, boolean fixed, int index) {
				super(loc, fixed, index);
			}

			public void paint(Graphics2D g2, int xoff, int yoff, float dx) {
				if (!visible) {
					return;
				}
				g2.setColor(getColor());
				Shape shape = createShape(xoff, yoff, dx);
				g2.fill(shape);
				if (selected) {
					g2.setColor(Color.white);
					g2.setStroke(SELECTION_STROKE);
					g2.draw(shape);
				}
				;
				g2.setStroke(NORMAL_STROKE);
				g2.setColor(Color.black);
				g2.draw(shape);
			}

			public Color getColor() {
				return gradient.keyColors[index];
			}

			protected Shape createShape(int xoff, int yoff, float dx) {
				Shape shape;
				Rectangle bounds = getBounds(xoff, yoff, dx);
				if (fixed) {
					shape = new Rectangle2D.Float(bounds.x, bounds.y,
							bounds.width, bounds.height);
				} else {
					GeneralPath path = new GeneralPath();
					float _x = bounds.x + dx / 2, _y = yoff;
					path.moveTo(_x, _y);
					path.lineTo(_x + dx / 2, _y + 10);
					path.lineTo(_x + dx / 2, _y + 20);
					path.lineTo(_x - dx / 2, _y + 20);
					path.lineTo(_x - dx / 2, _y + 10);
					path.closePath();
					shape = path;
				}
				return shape;
			}

			public void edit() {
				Color c = getColor();
				JColorChooser cc = new JColorChooser();
				Color nc = cc.showDialog(EditGradientDialog.this, "Choose", c);
				if (nc != null) {
					gradient.keyColors[index] = nc;
				}
			}
		}

		private class AlphaSlider extends Slider {
			private float alpha;

			private Paint paint;

			public AlphaSlider(double loc, boolean fixed, int index) {
				this(loc, fixed, index, 1.0f);
			}

			public AlphaSlider(double loc, boolean fixed, int index, float alpha) {
				super(loc, fixed, index);
				this.alpha = alpha;
				int s = 4, s2 = s / 2;
				BufferedImage img = new BufferedImage(s, s,
						BufferedImage.TYPE_BYTE_BINARY);
				Graphics g = img.getGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, s2, s2);
				g.setColor(Color.black);
				g.fillRect(s2, 0, s2, s2);
				g.setColor(Color.black);
				g.fillRect(0, s2, s2, s2);
				g.setColor(Color.white);
				g.fillRect(s2, s2, s2, s2);
				paint = new TexturePaint(img, new Rectangle2D.Float(0, 0, s, s));
			}

			public void paint(Graphics2D g2, int xoff, int yoff, float dx) {
				if (!visible) {
					return;
				}
				Shape shape = createShape(xoff, yoff, dx);
				g2.setPaint(paint);
				g2.fill(shape);
				g2.setColor(new Color(0, 0, 0, alpha));
				g2.fill(shape);
				if (selected) {
					g2.setColor(Color.white);
					g2.setStroke(SELECTION_STROKE);
					g2.draw(shape);
				}
				g2.setColor(Color.black);
				g2.setStroke(NORMAL_STROKE);
				g2.draw(shape);
			}

			public float getAlpha() {
				return alpha;
			}

			protected Shape createShape(int xoff, int yoff, float dx) {
				Shape shape;
				Rectangle bounds = getBounds(xoff, yoff, dx);
				if (fixed) {
					shape = new Rectangle2D.Float(bounds.x, bounds.y,
							bounds.width, bounds.height);
				} else {
					GeneralPath path = new GeneralPath();
					float _x = bounds.x + dx / 2, _y = yoff + 20;
					path.moveTo(_x, _y);
					path.lineTo(_x + dx / 2, _y - 10);
					path.lineTo(_x + dx / 2, _y - 20);
					path.lineTo(_x - dx / 2, _y - 20);
					path.lineTo(_x - dx / 2, _y - 10);
					path.closePath();
					shape = path;
				}
				return shape;
			}

			public void edit() {
				new EditAlphaWindow(EditGradientDialog.this).setVisible(true);
			}

			public void dragOn(boolean b) {
			}

			private class EditAlphaWindow extends JDialog {
				private JTextField alphaText;

				private JSlider alphaSlider;

				public EditAlphaWindow(Dialog owner) {
					super(owner, "Edit Alpha", true);
					Container c = getContentPane();
					JPanel main = new JPanel(new BorderLayout(10, 10));
					main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
							10));
					JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEADING));
					pnl.add(new JLabel("Transparency: "));
					alphaText = new JTextField("" + (int) (alpha * 100), 3);
					alphaText.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String str = alphaText.getText();
							boolean set = false;
							if (str != null && (str = str.trim()).length() > 0) {
								try {
									int n = Integer.parseInt(str);
									if (n >= 0 && n <= 100) {
										alphaSlider.setValue(n);
									}
									alpha = n / 100.0f;
									getOwner().repaint();
								} catch (NumberFormatException e1) {
								}
							}
							if (!set) {
								alphaText.setText("" + (int) (alpha * 100));
							}
						}
					});
					pnl.add(alphaText);
					main.add(pnl, BorderLayout.NORTH);
					alphaSlider = new JSlider(0, 100, (int) (alpha * 100));
					alphaSlider.setMajorTickSpacing(25);
					alphaSlider.setMinorTickSpacing(5);
					alphaSlider.setPaintLabels(true);
					alphaSlider.setPaintTicks(true);
					alphaSlider.addChangeListener(new ChangeListener() {
						public void stateChanged(ChangeEvent e) {
							int n = alphaSlider.getValue();
							alphaText.setText("" + n);
							alpha = n / 100.0f;
							getOwner().repaint();
						}
					});
					main.add(alphaSlider);
					c.add(main);
					pack();
					setLocationRelativeTo(getOwner());
					setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				}
			}
		}
	}

	private class InfoPanel extends JPanel {
		private JLabel red, green, blue, alpha;

		private JComboBox method;

		public InfoPanel() {
			super(new GridLayout(1, 3, 10, 10));
			setPreferredSize(new Dimension(300, 120));

			JPanel pnl;
			JPanel colorInfo = new JPanel(new SpringLayout());
			colorInfo.add(new JLabel("Red:"));
			red = new JLabel();
			colorInfo.add(red);
			colorInfo.add(new JLabel("Green:"));
			green = new JLabel();
			colorInfo.add(green);
			colorInfo.add(new JLabel("Blue:"));
			blue = new JLabel();
			colorInfo.add(blue);
			SpringUtilities.makeCompactGrid(colorInfo, 3, 2, 5, 5, 20, 10);
			pnl = new JPanel(new BorderLayout());
			pnl.setBorder(BorderFactory.createTitledBorder("Color"));
			pnl.add(colorInfo, BorderLayout.NORTH);
			add(pnl);

			JPanel transparencyInfo = new JPanel(new SpringLayout());
			transparencyInfo.add(new JLabel("Alpha:"));
			alpha = new JLabel();
			transparencyInfo.add(alpha);
			SpringUtilities.makeCompactGrid(transparencyInfo, 1, 2, 5, 5, 20,
					10);
			pnl = new JPanel(new BorderLayout());
			pnl.setBorder(BorderFactory.createTitledBorder("Transparency"));
			pnl.add(transparencyInfo, BorderLayout.NORTH);
			add(pnl);

			JPanel interpolationInfo = new JPanel(new SpringLayout());
			interpolationInfo.add(new JLabel("Method:"));
			method = new JComboBox(
					new String[] { Gradient.INTERPOLATION_LINEAR });
			interpolationInfo.add(method);
			SpringUtilities.makeCompactGrid(interpolationInfo, 1, 2, 5, 5, 20,
					10);
			pnl = new JPanel(new BorderLayout());
			pnl.setBorder(BorderFactory.createTitledBorder("Interpolation"));
			pnl.add(interpolationInfo, BorderLayout.NORTH);
			add(pnl);
		}

		public void setColor(Color color) {
			red.setText(color == null ? "" : "" + color.getRed());
			green.setText(color == null ? "" : "" + color.getGreen());
			blue.setText(color == null ? "" : "" + color.getBlue());
			alpha.setText(color == null ? "" : "" + color.getAlpha());
		}
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new EditGradientDialog(new JFrame(), Gradient.GRADIENTS[0])
				.setVisible(true);
		System.exit(0);
	}
}
