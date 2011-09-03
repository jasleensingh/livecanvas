package livecanvas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import livecanvas.components.Layer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import common.typeutils.AutoPanel;
import common.typeutils.EnumType;
import common.typeutils.PropertyFactory;

public abstract class BackgroundRef {
	protected Layer layer;

	protected Point offset = new Point();

	public Layer getLayer() {
		return layer;
	}

	public void setLayer(Layer layer) {
		this.layer = layer;
	}
	
	public Point getOffset() {
		return offset;
	}
	
	public void setOffset(Point offset) {
		this.offset = offset;
	}

	public abstract String getType();

	public abstract boolean load(Component parent, Dimension canvasSize);

	public abstract int getBackingCount();

	protected int backingIndex;

	public int getBackingIndex() {
		return backingIndex >= 0 && backingIndex < getBackingCount() ? backingIndex
				: -1;
	}

	public void setBackingIndex(int index) {
		if (backingIndex >= 0 && backingIndex < getBackingCount()) {
			this.backingIndex = index;
		}
	}

	public abstract Dimension getSize();

	public abstract void draw(Graphics2D g);

	public int getRGB(int x, int y) {
		return getRGB(x, y, backingIndex);
	}

	public abstract int getRGB(int x, int y, int backingIndex);

	public abstract Object getDrawable();

	public abstract BufferedImage toImage();

	protected abstract JSONObject _toJSON() throws JSONException;

	public final JSONObject toJSON() throws JSONException {
		JSONObject json = _toJSON();
		json.put("type", getType());
		json.put("offsetx", offset.x);
		json.put("offsety", offset.y);
		return json;
	}

	protected abstract BackgroundRef _fromJSON(JSONObject json)
			throws JSONException;

	public static BackgroundRef fromJSON(JSONObject json) throws JSONException {
		String type = json.getString("type");
		int offsetx = json.optInt("offsetx");
		int offsety = json.optInt("offsety");
		BackgroundRef bgref;
		if (BGImage.Type.equals(type)) {
			bgref = new BGImage();
		} else if (BGColor.Type.equals(type)) {
			bgref = new BGColor();
		} else {
			return null;
		}
		bgref.offset.setLocation(offsetx, offsety);
		return bgref._fromJSON(json);
	}

	private abstract static class MultiChooser extends JDialog {
		public static class Alignment {
			public static final String LEFT = "Left", CENTER = "Center",
					RIGHT = "Right";
			@EnumType(name = "Horizontal Alignment", allowed = { LEFT, CENTER,
					RIGHT })
			public String horizontal = CENTER;

			public static final String TOP = "Top", MIDDLE = "Middle",
					BOTTOM = "Bottom";
			@EnumType(name = "Vertical Alignment", allowed = { TOP, MIDDLE,
					BOTTOM })
			public String vertical = MIDDLE;

			public Point offset(Dimension outer, Dimension inner) {
				int offsetx, offsety;
				if (LEFT.equals(horizontal)) {
					offsetx = -(outer.width - inner.width) / 2;
				} else if (RIGHT.equals(horizontal)) {
					offsetx = (outer.width - inner.width) / 2;
				} else {
					offsetx = 0;
				}
				if (TOP.equals(horizontal)) {
					offsety = -(outer.height - inner.height) / 2;
				} else if (BOTTOM.equals(horizontal)) {
					offsety = (outer.height - inner.height) / 2;
				} else {
					offsety = 0;
				}
				return new Point(offsetx, offsety);
			}
		}

		private JList contentList;

		private Alignment alignment = new Alignment();

		public MultiChooser(Frame owner, ListCellRenderer lcr) {
			super(owner, "Choose", true);
			JPanel content = new JPanel(new BorderLayout());
			content.setPreferredSize(new Dimension(300, 400));
			JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
			JButton btn;
			btn = new JButton("Add");
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Object obj = add();
					if (obj != null) {
						DefaultListModel model = (DefaultListModel) contentList
								.getModel();
						model.addElement(obj);
					}
				}
			});
			north.add(btn);
			btn = new JButton("Remove");
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					DefaultListModel model = (DefaultListModel) contentList
							.getModel();
					if (model.size() > 0) {
						model.remove(model.size() - 1);
					}
				}
			});
			north.add(btn);
			btn = new JButton("Alignment...");
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(
							MultiChooser.this,
							new AutoPanel(PropertyFactory
									.createProperties(alignment)), "Alignment",
							JOptionPane.PLAIN_MESSAGE);
				}
			});
			north.add(btn);
			content.add(north, BorderLayout.NORTH);
			contentList = new JList(new DefaultListModel());
			contentList.setCellRenderer(lcr);
			content.add(new JScrollPane(contentList));
			JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
			btn = new JButton("Done");
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			south.add(btn);
			btn = new JButton("Cancel");
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					DefaultListModel model = (DefaultListModel) contentList
							.getModel();
					model.clear();
					setVisible(false);
				}
			});
			south.add(btn);
			content.add(south, BorderLayout.SOUTH);
			setContentPane(content);
			pack();
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setLocationRelativeTo(getParent());
		}

		public Object[] get() {
			DefaultListModel model = (DefaultListModel) contentList.getModel();
			return model.toArray();
		}

		public Alignment alignment() {
			return alignment;
		}

		protected abstract Object add();
	}

	public static class BGImage extends BackgroundRef {
		public static final String Type = "image";

		private static class ImageMultiChooser extends MultiChooser {
			public ImageMultiChooser(Frame owner) {
				super(owner, new ListCellRenderer() {
					@Override
					public Component getListCellRendererComponent(JList list,
							Object value, int index, boolean isSelected,
							boolean cellHasFocus) {
						return new JLabel((String) value);
					}
				});
			}

			@Override
			protected Object add() {
				return getImagePath(getParent());
			}

			private String getImagePath(Component parent) {
				FileDialog fd = new FileDialog(
						JOptionPane.getFrameForComponent(parent), "Load Image");
				fd.setVisible(true);
				String file_str = fd.getFile();
				if (file_str == null) {
					return null;
				}
				return fd.getDirectory() + "/" + file_str;
			}
		}

		private BufferedImage[] images;
		private String[] imagePaths;

		@Override
		public String getType() {
			return Type;
		}

		@Override
		public boolean load(Component parent, Dimension canvasSize) {
			MultiChooser mc = new ImageMultiChooser(
					JOptionPane.getFrameForComponent(parent));
			mc.setVisible(true);
			Object[] objs = mc.get();
			if (objs.length <= 0) {
				return false;
			}
			try {
				String[] ips = new String[objs.length];
				for (int i = 0; i < objs.length; i++) {
					ips[i] = (String) objs[i];
				}
				setImagePaths(ips);
			} catch (IOException e) {
				images = null;
				imagePaths = null;
				String msg = "Could not load file!";
				JOptionPane.showMessageDialog(parent, msg, "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			offset = mc.alignment().offset(canvasSize, getSize());
			return true;
		}

		public String[] getImagePaths() {
			return imagePaths;
		}

		public void setImagePaths(String[] ips) throws IOException {
			images = new BufferedImage[ips.length];
			imagePaths = new String[ips.length];
			for (int i = 0; i < ips.length; i++) {
				imagePaths[i] = ips[i];
				images[i] = ImageCache.load(imagePaths[i]);
			}
		}

		@Override
		public int getBackingCount() {
			return images == null ? 0 : images.length;
		}

		@Override
		public Dimension getSize() {
			return images == null ? null : new Dimension(
					images[backingIndex].getWidth(),
					images[backingIndex].getHeight());
		}

		@Override
		public void draw(Graphics2D g) {
			g.drawImage(images[backingIndex], -images[backingIndex].getWidth()
					/ 2 + offset.x, -images[backingIndex].getHeight() / 2
					+ offset.y, null);
		}

		@Override
		public int getRGB(int x, int y, int backingIndex) {
			backingIndex = Math.max(0,
					Math.min(getBackingCount() - 1, backingIndex));
			return images[backingIndex].getRGB(
					Math.max(0,
							Math.min(images[backingIndex].getWidth() - 1, x)),
					Math.max(0,
							Math.min(images[backingIndex].getHeight() - 1, y)));
		}

		@Override
		public Object getDrawable() {
			return images[backingIndex];
		}

		@Override
		public BufferedImage toImage() {
			BufferedImage image = new BufferedImage(getSize().width,
					getSize().height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			g.translate(image.getWidth() / 2, image.getHeight() / 2);
			draw(g);
			g.dispose();
			return image;
		}

		@Override
		public JSONObject _toJSON() throws JSONException {
			JSONObject json = new JSONObject();
			JSONArray imagePathsJSON = new JSONArray();
			for (int i = 0; i < imagePaths.length; i++) {
				imagePathsJSON.put(imagePaths[i]);
			}
			json.put("imagePaths", imagePathsJSON);
			return json;
		}

		@Override
		protected BackgroundRef _fromJSON(JSONObject json) throws JSONException {
			JSONArray imagePathsJSON = json.getJSONArray("imagePaths");
			imagePaths = new String[imagePathsJSON.length()];
			for (int i = 0; i < imagePathsJSON.length(); i++) {
				imagePaths[i] = imagePathsJSON.getString(i);
			}
			int i = 0;
			try {
				images = new BufferedImage[imagePaths.length];
				for (; i < imagePaths.length; i++) {
					System.err.println(imagePaths[i]);
					images[i] = ImageIO.read(new File(imagePaths[i]));
				}
			} catch (IOException e) {
				images = null;
				imagePaths = null;
				e.printStackTrace();
				throw new JSONException("Could not load file at path: "
						+ imagePaths[i]);
			}
			return this;
		}
	}

	public static class BGColor extends BackgroundRef {
		public static final String Type = "color";

		private static class ColorMultiChooser extends MultiChooser {
			public ColorMultiChooser(Frame owner) {
				super(owner, new ListCellRenderer() {
					private JLabel lbl = new JLabel(" ");
					{
						lbl.setOpaque(true);
					}

					@Override
					public Component getListCellRendererComponent(JList list,
							Object value, int index, boolean isSelected,
							boolean cellHasFocus) {
						lbl.setBackground((Color) value);
						return lbl;
					}
				});
			}

			@Override
			protected Object add() {
				Color color = new JColorChooser().showDialog(getParent(),
						"Choose", Color.black);
				if (color == null) {
					return false;
				}
				return color;
			}
		}

		private Color[] colors;

		@Override
		public String getType() {
			return Type;
		}

		@Override
		public boolean load(Component parent, Dimension canvasSize) {
			MultiChooser mc = new ColorMultiChooser(
					JOptionPane.getFrameForComponent(parent));
			mc.setVisible(true);
			Object[] objs = mc.get();
			if (objs.length <= 0) {
				return false;
			}
			Color[] cs = new Color[objs.length];
			for (int i = 0; i < objs.length; i++) {
				cs[i] = (Color) objs[i];
			}
			setColors(cs);
			return true;
		}

		public Color[] getColors() {
			return colors;
		}

		public void setColors(Color[] cs) {
			colors = new Color[cs.length];
			for (int i = 0; i < cs.length; i++) {
				colors[i] = cs[i];
			}
		}

		@Override
		public int getBackingCount() {
			return colors == null ? 0 : colors.length;
		}

		@Override
		public Dimension getSize() {
			return new Dimension(layer.getCanvas().getWidth(), layer
					.getCanvas().getHeight());
		}

		@Override
		public void draw(Graphics2D g) {
			Shape shape = layer.getPath().shape;
			g.setColor(colors[backingIndex]);
			g.fill(shape);
		}

		@Override
		public int getRGB(int x, int y, int backingIndex) {
			backingIndex = Math.max(0,
					Math.min(getBackingCount() - 1, backingIndex));
			// Shape shape = layer.getPath().shape;
			// if (shape.contains(x - getSize().width / 2, y - getSize().height
			// / 2)) {
			return colors[backingIndex].getRGB();
			// } else {
			// return 0;
			// }
		}

		@Override
		public Object getDrawable() {
			return colors[backingIndex];
		}

		@Override
		public BufferedImage toImage() {
			BufferedImage image = new BufferedImage(getSize().width,
					getSize().height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			g.translate(image.getWidth() / 2, image.getHeight() / 2);
			draw(g);
			g.dispose();
			return image;
		}

		@Override
		public JSONObject _toJSON() throws JSONException {
			JSONObject json = new JSONObject();
			JSONArray colorsJSON = new JSONArray();
			for (int i = 0; i < colors.length; i++) {
				colorsJSON.put(colors[i].getRGB());
			}
			json.put("colors", colorsJSON);
			return json;
		}

		@Override
		protected BackgroundRef _fromJSON(JSONObject json) throws JSONException {
			JSONArray colorsJSON = json.getJSONArray("colors");
			colors = new Color[colorsJSON.length()];
			for (int i = 0; i < colorsJSON.length(); i++) {
				colors[i] = new Color(colorsJSON.getInt(i));
			}
			return this;
		}
	}
}
