package common.typeutils;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

public interface MyWidgets {
	public static class MyLabel extends JLabel implements PropertyListener {
		private Property prop;

		public MyLabel(Property prop) {
			this.prop = prop;

			setEnabled(!prop.readonly);
			prop.addListener(this);

			get();
		}

		public void valueChanged(Property prop) {
			get();
		}

		private void get() {
			try {
				setText(prop.get().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class MyComboBox extends JComboBox implements
			PropertyListener, ItemListener {
		private EnumProperty prop;

		public MyComboBox(EnumProperty prop) {
			this.prop = prop;
			setModel(new DefaultComboBoxModel(prop.allowed));

			setEnabled(!prop.readonly);
			prop.addListener(this);
			addItemListener(this);

			get();
		}

		public void valueChanged(Property prop) {
			get();
		}

		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				set();
			}
		}

		private void get() {
			try {
				setSelectedItem((String) prop.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void set() {
			try {
				prop.set((String) getSelectedItem());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class MyTextField extends JTextField implements
			PropertyListener, ActionListener, FocusListener, DocumentListener {
		private StringProperty prop;

		// private DelayThread delayThread = new DelayThread() {
		// public void show() {
		// set();
		// }
		//
		// public void dismiss() {
		// }
		// };

		private boolean changed;

		public MyTextField(StringProperty prop) {
			this.prop = prop;
			addFocusListener(this);
			addActionListener(this);
			prop.addListener(this);
			getDocument().addDocumentListener(this);
			setEnabled(!prop.readonly);

			get();
			// Not sure now if this is a good idea
			// delayThread.start();
		}

		public void valueChanged(Property prop) {
			get();
		}

		public void actionPerformed(ActionEvent e) {
			if (changed) {
				set();
				changed = false;
			}
		}

		public void focusGained(FocusEvent e) {
		}

		public void focusLost(FocusEvent e) {
			if (changed) {
				set();
				changed = false;
			}
		}

		public void changedUpdate(DocumentEvent e) {
			changed = true;
			// delayThread.scheduleShow();
		}

		public void insertUpdate(DocumentEvent e) {
			changed = true;
			// delayThread.scheduleShow();
		}

		public void removeUpdate(DocumentEvent e) {
			changed = true;
			// delayThread.scheduleShow();
		}

		private void get() {
			try {
				setText((String) prop.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void set() {
			try {
				prop.set((String) getText());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class MyPrefixedTextField extends JPanel implements
			PropertyListener, ActionListener, FocusListener, DocumentListener {
		private PrefixedStringProperty prop;

		private JTextField textField = new JTextField();

		private boolean changed;

		public MyPrefixedTextField(PrefixedStringProperty prop) {
			this.prop = prop;

			setLayout(new BorderLayout(10, 10));
			JLabel lbl = new JLabel(prop.prefix);
			add(lbl);
			add(lbl, BorderLayout.WEST);
			add(textField);
			prop.addListener(this);
			textField.addFocusListener(this);
			textField.getDocument().addDocumentListener(this);
			textField.setEnabled(!prop.readonly);

			get();
		}

		public void valueChanged(Property prop) {
			get();
		}

		public void actionPerformed(ActionEvent e) {
			if (changed) {
				set();
				changed = false;
			}
		}

		public void focusGained(FocusEvent e) {
		}

		public void focusLost(FocusEvent e) {
			if (changed) {
				set();
				changed = false;
			}
		}

		public void changedUpdate(DocumentEvent e) {
			changed = true;
		}

		public void insertUpdate(DocumentEvent e) {
			changed = true;
		}

		public void removeUpdate(DocumentEvent e) {
			changed = true;
		}

		private void get() {
			try {
				String s = (String) prop.get();
				if (s.startsWith(prop.prefix)) {
					s = s.substring(prop.prefix.length());
				}
				textField.setText(s);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void set() {
			try {
				prop.set(prop.prefix + (String) textField.getText());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class MyPasswordField extends JPasswordField implements
			PropertyListener, ActionListener, FocusListener, DocumentListener {
		private PasswordProperty prop;

		private boolean changed;

		public MyPasswordField(PasswordProperty prop) {
			this.prop = prop;
			addFocusListener(this);
			addActionListener(this);
			prop.addListener(this);
			getDocument().addDocumentListener(this);
			setEnabled(!prop.readonly);

			get();
		}

		public void valueChanged(Property prop) {
			get();
		}

		public void actionPerformed(ActionEvent e) {
			if (changed) {
				set();
				changed = false;
			}
		}

		public void focusGained(FocusEvent e) {
		}

		public void focusLost(FocusEvent e) {
			if (changed) {
				set();
				changed = false;
			}
		}

		public void changedUpdate(DocumentEvent e) {
			changed = true;
		}

		public void insertUpdate(DocumentEvent e) {
			changed = true;
		}

		public void removeUpdate(DocumentEvent e) {
			changed = true;
		}

		private void get() {
			try {
				setText((String) prop.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void set() {
			try {
				prop.set((String) new String(getPassword()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class MyArrayList extends JPanel implements PropertyListener {
		private StringArrayProperty prop;

		private JList list;

		private DefaultListModel listModel;

		public MyArrayList(StringArrayProperty prop) {
			this.prop = prop;

			setLayout(new BorderLayout());
			prop.addListener(this);
			list = new JList(listModel = new DefaultListModel());
			list.setVisibleRowCount(4);
			list.setEnabled(!prop.readonly);
			add(new JScrollPane(list));

			get();
		}

		public void valueChanged(Property prop) {
			get();
		}

		private void get() {
			try {
				String[] items = (String[]) prop.get();
				listModel.clear();
				for (String item : items) {
					listModel.addElement(item);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void set() {
			try {
				String[] items = new String[listModel.size()];
				listModel.copyInto(items);
				prop.set(items);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class MyTextArea extends JTextArea implements
			PropertyListener, FocusListener, DocumentListener {
		private TextProperty prop;

		private boolean changed;

		public MyTextArea(TextProperty prop) {
			this.prop = prop;

			prop.addListener(this);
			addFocusListener(this);
			getDocument().addDocumentListener(this);
			setEnabled(!prop.readonly);

			get();
		}

		public void valueChanged(Property prop) {
			get();
		}

		public void focusGained(FocusEvent e) {
		}

		public void focusLost(FocusEvent e) {
			if (changed) {
				set();
				changed = false;
			}
		}

		public void changedUpdate(DocumentEvent e) {
			changed = true;
		}

		public void insertUpdate(DocumentEvent e) {
			changed = true;
		}

		public void removeUpdate(DocumentEvent e) {
			changed = true;
		}

		private void get() {
			try {
				setText((String) prop.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void set() {
			try {
				prop.set((String) getText());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class MyCheckBox extends JCheckBox implements
			PropertyListener, ItemListener {
		private BooleanProperty prop;

		public MyCheckBox(BooleanProperty prop) {
			this.prop = prop;
			setOpaque(false);
			prop.addListener(this);
			addItemListener(this);
			setEnabled(!prop.readonly);

			get();
		}

		public void valueChanged(Property prop) {
			get();
		}

		public void itemStateChanged(ItemEvent e) {
			set();
		}

		private void get() {
			try {
				setSelected((Boolean) prop.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void set() {
			try {
				prop.set((Boolean) isSelected());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class MySpinner extends JSpinner implements PropertyListener,
			ChangeListener {
		private enum Type {
			Integer, Double, Float
		};

		private IntegerProperty iprop;

		private DoubleProperty dprop;

		private FloatProperty fprop;

		private Type type;

		public MySpinner(IntegerProperty iprop) {
			this.iprop = iprop;
			this.type = Type.Integer;

			iprop.addListener(this);
			addChangeListener(this);
			setModel(new SpinnerNumberModel(iprop.min, iprop.min, iprop.max,
					iprop.step));
			setEnabled(!iprop.readonly);

			get(this.type);
		}

		public void valueChanged(Property prop) {
			get(this.type);
		}

		public MySpinner(DoubleProperty dprop) {
			this.dprop = dprop;
			this.type = Type.Double;

			dprop.addListener(this);
			addChangeListener(this);
			setModel(new SpinnerNumberModel(dprop.min, dprop.min, dprop.max,
					dprop.step));
			setEnabled(!dprop.readonly);

			get(this.type);
		}

		public MySpinner(FloatProperty fprop) {
			this.fprop = fprop;
			this.type = Type.Float;

			fprop.addListener(this);
			addChangeListener(this);
			setModel(new SpinnerNumberModel((double) fprop.min,
					(double) fprop.min, (double) fprop.max, (double) fprop.step));
			setEnabled(!fprop.readonly);

			get(this.type);
		}

		public void stateChanged(ChangeEvent e) {
			set(this.type);
		}

		private void get(Type type) {
			try {
				switch (type) {
				case Integer:
					setValue((Integer) iprop.get());
					break;
				case Double:
					setValue((Double) dprop.get());
					break;
				case Float:
					float f = (Float) fprop.get();
					setValue((double) f);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void set(Type type) {
			try {
				switch (type) {
				case Integer:
					iprop.set((Integer) getValue());
					break;
				case Double:
					dprop.set((Double) getValue());
					break;
				case Float:
					double d = (Double) getValue();
					fprop.set((float) d);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class MyFontField extends JPanel implements PropertyListener {
		private FontProperty prop;

		private JLabel displayText;

		public MyFontField(FontProperty fp) {
			super(new BorderLayout(2, 2));
			this.prop = fp;

			prop.addListener(this);
			displayText = new JLabel();
			displayText.setPreferredSize(new Dimension(100, 24));
			add(displayText);

			JButton chooseFont = new JButton("Choose");
			chooseFont
					.setToolTipText("Select a font from a font-chooser dialog");
			chooseFont.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					chooseFont();
				}
			});
			add(chooseFont, BorderLayout.EAST);

			get();
		}

		public void chooseFont() {
			String s = displayText.getText();
			JFontChooser fc = new JFontChooser(JOptionPane
					.getFrameForComponent(this), prop.name + " - Choose Font",
					s);
			if (fc.showFontDialog(s) == JFontChooser.APPROVE_OPTION) {
				s = fc.getCurrentFont();
				displayText.setText(s);
				set();
			}
		}

		public void valueChanged(Property prop) {
			get();
		}

		private void get() {
			try {
				Font font = (Font) prop.get();
				displayText.setText(toString(font));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void set() {
			try {
				prop.set(fromString(displayText.getText()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private String toString(Font font) {
			if (font == null) {
				return "Arial, Plain, 12";
			}
			StringBuilder sb = new StringBuilder(font.getFamily() + ",");
			if (font.isPlain()) {
				sb.append(" Plain");
			} else {
				if (font.isBold()) {
					sb.append(" Bold");
				}
				if (font.isItalic()) {
					sb.append(" Italic");
				}
			}
			sb.append(", " + font.getSize());
			return sb.toString();
		}

		private Font fromString(String str) {
			String[] tok = str.split(",");
			if (tok.length != 3) {
				return null;
			}
			int size = Integer.parseInt(tok[2].trim());
			int style = -1;
			tok[1] = tok[1].trim();
			if (tok[1].equals("Plain")) {
				style = Font.PLAIN;
			} else {
				if (tok[1].contains("Bold")) {
					style = Font.BOLD;
				}
				if (tok[1].contains("Italic")) {
					if (style < 0)
						style = Font.ITALIC;
					else
						style |= Font.ITALIC;
				}
			}
			String name = tok[0].trim();
			return new Font(name, style, size);
		}
	}

	public static class MyColorField extends JPanel implements PropertyListener {
		private ColorProperty prop;

		private JLabel displayColor;

		public MyColorField(ColorProperty cp) {
			super(new BorderLayout(2, 2));
			this.prop = cp;

			prop.addListener(this);
			displayColor = new JLabel();
			displayColor.setBorder(BorderFactory.createLineBorder(Color.black));
			displayColor.setOpaque(true);
			JPanel p = new JPanel(new BorderLayout());
			p.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
			p.add(displayColor);
			add(p);

			JButton chooseColor = new JButton("Choose");
			chooseColor
					.setToolTipText("Select a color from a color-chooser dialog");
			chooseColor.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					chooseColor();
				}
			});
			add(chooseColor, BorderLayout.EAST);

			get();
		}

		public void chooseColor() {
			Color c = displayColor.getBackground();
			Color nc = JColorChooser.showDialog(JOptionPane
					.getFrameForComponent(this), prop.name + " - Choose Color",
					c);
			if (nc != null) {
				displayColor.setBackground(nc);
				set();
			}
		}

		public void valueChanged(Property prop) {
			get();
		}

		private void get() {
			try {
				Color color = (Color) prop.get();
				displayColor.setBackground(color);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void set() {
			try {
				prop.set(displayColor.getBackground());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class MyStrokeField extends JPanel implements
			PropertyListener {
		private StrokeProperty prop;

		private StrokeComboBox displayStroke;

		public MyStrokeField(StrokeProperty sp) {
			super(new BorderLayout(2, 2));
			this.prop = sp;

			prop.addListener(this);
			displayStroke = new StrokeComboBox();
			displayStroke.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						set();
					}
				}
			});
			add(displayStroke);

			get();
		}

		public void valueChanged(Property prop) {
			get();
		}

		private void get() {
			try {
				Stroke stroke = (Stroke) prop.get();
				displayStroke.setSelectedItem(stroke);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void set() {
			try {
				prop.set((Stroke) displayStroke.getSelectedItem());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private static class StrokeComboBox extends JComboBox {

			public StrokeComboBox() {
				float[] lineWidths = { 0.25f, 0.5f, 0.75f, 1, 1.5f, 2, 3, 4.5f, 6 };
				ArrayList<Stroke> items = new ArrayList<Stroke>();
				items.add(new BasicStroke(0));
				for (int i = 0; i < StrokeType.STROKES.length; i++) {
					Stroke s = StrokeType.STROKES[i];
					if (s instanceof BasicStroke) {
						BasicStroke bs = (BasicStroke) s;
						for (int j = 0; j < lineWidths.length; j++) {
							items.add(new BasicStroke(lineWidths[j], bs
									.getEndCap(), bs.getLineJoin(), bs
									.getMiterLimit(), bs.getDashArray(), bs
									.getDashPhase()));
						}
					} else {
						items.add(s);
					}
				}
				setModel(new DefaultComboBoxModel(items.toArray(new Stroke[0])));
				setRenderer(new ListCellRenderer() {
					private StrokeLabel strokeLabel = new StrokeLabel(
							StrokeType.STROKES[0]);

					private Color selectionForeground = UIManager
							.getColor("List.selectionForeground"),
							selectionBackground = UIManager
									.getColor("List.selectionBackground"),
							textForeground = UIManager
									.getColor("List.textForeground"),
							textBackground = UIManager
									.getColor("List.textBackground");

					public Component getListCellRendererComponent(JList list,
							Object value, int index, boolean isSelected,
							boolean cellHasFocus) {
						Stroke s = (Stroke) value;
						strokeLabel.stroke = s;
						if (isSelected || cellHasFocus) {
							strokeLabel.setForeground(selectionForeground);
							strokeLabel.setBackground(selectionBackground);
						} else {
							strokeLabel.setForeground(textForeground);
							strokeLabel.setBackground(textBackground);
						}
						return strokeLabel;
					}
				});
			}

			private static class StrokeLabel extends JLabel {

				public Stroke stroke;

				public StrokeLabel(Stroke stroke) {
					this.stroke = stroke;
					setPreferredSize(new Dimension(50, 18));
					setOpaque(true);
				}

				public void paint(Graphics g) {
					super.paint(g);

					Graphics2D g2 = (Graphics2D) g;

					if (stroke != null) {
						g.setColor(getForeground());
						Stroke s = g2.getStroke();
						g2.setStroke(stroke);
						g2.drawLine(5, getHeight() / 2, getWidth() - 10,
								getHeight() / 2);
						g2.setStroke(s);
					}
				}
			}
		}
	}

	public static class MyGradientField extends JPanel implements
			PropertyListener {
		private GradientProperty prop;

		private GradientComboBox displayGradient;

		private boolean customGradientAllowed;

		private ItemListener il = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					Gradient g = (Gradient) e.getItem();
					if (g.equals(Gradient.CUSTOM)) {
						Frame owner = JOptionPane
								.getFrameForComponent(displayGradient);
						EditGradientDialog egd = new EditGradientDialog(owner,
								g);
						egd.setVisible(true);
						Gradient ng = egd.getGradient();
						if (ng != null) {
							g.name = ng.name;
							g.colors = ng.colors;
							g.keyColors = ng.keyColors;
							set();
						} else {
							get();
						}
					} else {
						set();
					}
				}
			}
		};

		public MyGradientField(GradientProperty sp) {
			this(sp, true);
		}

		public MyGradientField(GradientProperty sp,
				boolean customGradientAllowed) {
			super(new BorderLayout(2, 2));
			this.prop = sp;
			setCustomGradientAllowed(customGradientAllowed);

			prop.addListener(this);

			get();
		}

		public boolean isCustomGradientAllowed() {
			return customGradientAllowed;
		}

		public void setCustomGradientAllowed(boolean customGradientAllowed) {
			this.customGradientAllowed = customGradientAllowed;
			if (displayGradient != null) {
				remove(displayGradient);
			}
			List<Gradient> gradientsList = new LinkedList<Gradient>(Arrays
					.asList(Gradient.GRADIENTS));
			if (customGradientAllowed) {
				gradientsList.add(Gradient.CUSTOM);
			}
			displayGradient = new GradientComboBox(gradientsList
					.toArray(new Gradient[0]));
			displayGradient.addItemListener(il);
			add(displayGradient);
			revalidate();
		}

		public void valueChanged(Property prop) {
			get();
		}

		private void get() {
			try {
				Gradient gradient = (Gradient) prop.get();
				displayGradient.setSelectedItem(gradient);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void set() {
			try {
				prop.set((Gradient) displayGradient.getSelectedItem());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private static class GradientComboBox extends JComboBox {

			private Gradient[] gradients;

			public GradientComboBox(Gradient[] gs) {
				this.gradients = gs;
				setModel(new DefaultComboBoxModel(gs));
				setRenderer(new ListCellRenderer() {
					private GradientLabel gradientLabel = new GradientLabel(
							gradients[0]);

					private Color selectionForeground = UIManager
							.getColor("List.selectionForeground"),
							selectionBackground = UIManager
									.getColor("List.selectionBackground"),
							textForeground = UIManager
									.getColor("List.textForeground"),
							textBackground = UIManager
									.getColor("List.textBackground");

					public Component getListCellRendererComponent(JList list,
							Object value, int index, boolean isSelected,
							boolean cellHasFocus) {
						gradientLabel.setGradient((Gradient) value);
						if (isSelected || cellHasFocus) {
							gradientLabel.setForeground(selectionForeground);
							gradientLabel.setBackground(selectionBackground);
						} else {
							gradientLabel.setForeground(textForeground);
							gradientLabel.setBackground(textBackground);
						}
						return gradientLabel;
					}
				});
			}

			private static class GradientLabel extends JLabel {

				private static final int TEXT_WIDTH = 80;

				private Gradient gradient;

				public GradientLabel(Gradient gradient) {
					this.gradient = gradient;
					setText(gradient.name);
					setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
					setOpaque(true);
				}

				public Gradient getGradient() {
					return gradient;
				}

				public void setGradient(Gradient gradient) {
					this.gradient = gradient;
					setText(gradient.name);
				}

				public void paintComponent(Graphics g) {
					super.paintComponent(g);

					if (gradient != null) {
						int xpadding = 3, ypadding = 3;
						int width = getWidth() - 2 * xpadding - TEXT_WIDTH, height = getHeight()
								- 2 * ypadding;
						for (int x = 0; x < width; x++) {
							Color c = gradient.colors[(int) (x
									* gradient.colors.length / width)];
							g.setColor(c);
							g.fillRect(x + TEXT_WIDTH + xpadding, ypadding, 1,
									height);
						}
					}
				}
			}
		}
	}

	public static class MyFileField extends JPanel implements FocusListener,
			ActionListener, DocumentListener, PropertyListener {
		private FileProperty prop;

		private JTextField filePath;

		private boolean changed;

		public MyFileField(FileProperty fp) {
			super(new BorderLayout(2, 2));
			this.prop = fp;

			prop.addListener(this);
			filePath = new JTextField();
			filePath.setOpaque(true);
			filePath.addFocusListener(this);
			filePath.addActionListener(this);
			filePath.getDocument().addDocumentListener(this);
			filePath.setEnabled(!prop.readonly);
			add(filePath);

			JButton browseFiles = new JButton("Browse");
			browseFiles.setToolTipText("Browse for another file");
			browseFiles.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					browseFiles();
				}
			});
			browseFiles.setEnabled(!prop.readonly);
			add(browseFiles, BorderLayout.EAST);

			get();
		}

		public void valueChanged(Property prop) {
			get();
		}

		public void actionPerformed(ActionEvent e) {
			if (changed) {
				set();
				changed = false;
			}
		}

		public void focusGained(FocusEvent e) {
		}

		public void focusLost(FocusEvent e) {
			if (changed) {
				set();
				changed = false;
			}
		}

		public void changedUpdate(DocumentEvent e) {
			changed = true;
		}

		public void insertUpdate(DocumentEvent e) {
			changed = true;
		}

		public void removeUpdate(DocumentEvent e) {
			changed = true;
		}

		public void browseFiles() {
			File f = new File(filePath.getText());
			JFileChooser fc = new JFileChooser(f.getParentFile());
			fc.setAcceptAllFileFilterUsed(true);
			if (prop.filters != null) {
				for (FileFilter ff : prop.filters) {
					fc.addChoosableFileFilter(ff);
				}
			}
			int option = fc.showDialog(JOptionPane.getFrameForComponent(this),
					"Choose");
			if (option == JFileChooser.APPROVE_OPTION) {
				File nf = fc.getSelectedFile();
				filePath.setText(nf.getAbsolutePath());
				set();
			}
		}

		private void get() {
			try {
				File file = (File) prop.get();
				if (file == null && prop.default_ != null) {
					prop.set(prop.default_);
					file = (File) prop.get();
				}
				filePath.setText(file.getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void set() {
			try {
				File file = new File(filePath.getText());
				if (!file.exists()) {
					JOptionPane.showMessageDialog(this, "File does not exist",
							"Warning", JOptionPane.WARNING_MESSAGE);
				} else if (prop.filters != null) {
					boolean accept = false;
					for (FileFilter ff : prop.filters) {
						if (ff.accept(file)) {
							accept = true;
							break;
						}
					}
					if (!accept) {
						JOptionPane.showMessageDialog(this,
								"Does not seem to be valid file", "Warning",
								JOptionPane.WARNING_MESSAGE);
					}
				}
				prop.set(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class MyDirectoryField extends JPanel implements
			FocusListener, ActionListener, DocumentListener, PropertyListener {
		private DirectoryProperty prop;

		private JTextField filePath;

		private boolean changed;

		public MyDirectoryField(DirectoryProperty dp) {
			super(new BorderLayout(2, 2));
			this.prop = dp;

			prop.addListener(this);
			filePath = new JTextField();
			filePath.setOpaque(true);
			filePath.addFocusListener(this);
			filePath.addActionListener(this);
			filePath.getDocument().addDocumentListener(this);
			filePath.setEnabled(!prop.readonly);
			add(filePath);

			JButton browseFiles = new JButton("Browse");
			browseFiles.setToolTipText("Browse for another file");
			browseFiles.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					browseFiles();
				}
			});
			browseFiles.setEnabled(!prop.readonly);
			add(browseFiles, BorderLayout.EAST);

			get();
		}

		public void valueChanged(Property prop) {
			get();
		}

		public void actionPerformed(ActionEvent e) {
			if (changed) {
				set();
				changed = false;
			}
		}

		public void focusGained(FocusEvent e) {
		}

		public void focusLost(FocusEvent e) {
			if (changed) {
				set();
				changed = false;
			}
		}

		public void changedUpdate(DocumentEvent e) {
			changed = true;
		}

		public void insertUpdate(DocumentEvent e) {
			changed = true;
		}

		public void removeUpdate(DocumentEvent e) {
			changed = true;
		}

		public void browseFiles() {
			File f = new File(filePath.getText());
			JFileChooser fc = new JFileChooser(f.getParentFile());
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int option = fc.showDialog(JOptionPane.getFrameForComponent(this),
					"Choose");
			if (option == JFileChooser.APPROVE_OPTION) {
				File nf = fc.getSelectedFile();
				filePath.setText(nf.getAbsolutePath());
				set();
			}
		}

		private void get() {
			try {
				File file = (File) prop.get();
				if (file == null && prop.default_ != null) {
					prop.set(prop.default_);
					file = (File) prop.get();
				}
				filePath.setText(file.getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void set() {
			try {
				File file = new File(filePath.getText());
				if (!file.exists() || !file.isDirectory()) {
					JOptionPane.showMessageDialog(this,
							"Directory does not exist", "Warning",
							JOptionPane.WARNING_MESSAGE);
				}
				prop.set(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class MyDateField extends JTextField implements
			PropertyListener, ActionListener, FocusListener, DocumentListener {
		private DateProperty prop;

		private boolean changed;

		public MyDateField(DateProperty prop) {
			this.prop = prop;
			addFocusListener(this);
			addActionListener(this);
			prop.addListener(this);
			getDocument().addDocumentListener(this);
			setEnabled(!prop.readonly);

			get();
			// Not sure now if this is a good idea
			// delayThread.start();
		}

		public void valueChanged(Property prop) {
			get();
		}

		public void actionPerformed(ActionEvent e) {
			if (changed) {
				set();
				changed = false;
			}
		}

		public void focusGained(FocusEvent e) {
		}

		public void focusLost(FocusEvent e) {
			if (changed) {
				set();
				changed = false;
			}
		}

		public void changedUpdate(DocumentEvent e) {
			changed = true;
		}

		public void insertUpdate(DocumentEvent e) {
			changed = true;
		}

		public void removeUpdate(DocumentEvent e) {
			changed = true;
		}

		private void get() {
			try {
				Date d = (Date) prop.get();
				setText(DateProperty.fromDate(d));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void set() {
			try {
				Date d = DateProperty.toDate((String) getText());
				if (d == null) {
					get();
				} else {
					prop.set(d);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class MyListPanel extends JPanel implements PropertyListener {
		private ListProperty prop;

		public MyListPanel(ListProperty prop) {
			this.prop = prop;

			setLayout(new BorderLayout());
			Class clazz = prop.clazz;
			try {
				add(new AutoPanel(PropertyFactory.createProperties(clazz
						.newInstance())));
			} catch (Exception e) {
				e.printStackTrace();
			}

			setEnabled(!prop.readonly);
			prop.addListener(this);

			get();
		}

		public void valueChanged(Property prop) {
			get();
		}

		private void get() {
		}
	}
}
