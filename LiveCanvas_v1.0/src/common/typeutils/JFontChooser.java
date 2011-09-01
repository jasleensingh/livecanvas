package common.typeutils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class JFontChooser extends JDialog implements ActionListener,
		ListSelectionListener {
	public static final int CANCEL_OPTION = 0, APPROVE_OPTION = 1;

	private static String[] fontList;

	private String currentFont;

	private String defaultFont;

	private int fontSize;

	private int option;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JButton btnCancel;

	private JButton btnDefault;

	private JButton btnOk;

	private JCheckBox jCheckBox1;

	private JCheckBox jCheckBox2;

	private JCheckBox jCheckBox3;

	private JCheckBox jCheckBox4;

	private JCheckBox jCheckBox5;

	private JCheckBox jCheckBox6;

	private JCheckBox jCheckBox7;

	private JCheckBox jCheckBox8;

	private JCheckBox jCheckBox9;

	private JList lstFontFamily;

	private JList lstFontSize;

	private JList lstFontStyle;

	private JPanel pnlButtons;

	private JPanel pnlCanvas;

	private JPanel pnlDefault;

	private JPanel pnlEffects;

	private JPanel pnlFont;

	private JPanel pnlFontFamily;

	private JPanel pnlFontSize;

	private JPanel pnlFontStyle;

	private JPanel pnlMain;

	private JPanel pnlOkCancel;

	private JPanel pnlPreview;

	private JScrollPane scpFontFamily;

	private JScrollPane scpFontSize;

	private JScrollPane scpFontStyle;

	private JTextField txfFontFamily;

	private JTextField txfFontSize;

	private JTextField txfFontStyle;

	public JFontChooser(Component comp, String title, String defaultFont) {
		super(comp instanceof Frame ? (Frame) comp : JOptionPane
				.getFrameForComponent(comp));
		setModal(true);
		setTitle(title);
		setAlwaysOnTop(true);

		initComponents();
		initOtherComponents();
		if (isValid(defaultFont)) {
			this.defaultFont = defaultFont;
		} else {
			defaultFont = "Times New Roman, Plain, 12";
		}
		setCurrentFont(defaultFont);
	}

	private void initComponents() {
		GridBagConstraints gridBagConstraints;

		pnlMain = new JPanel();
		pnlFont = new JPanel();
		pnlFontFamily = new JPanel();
		txfFontFamily = new JTextField();
		scpFontFamily = new JScrollPane();
		lstFontFamily = new JList();
		pnlFontStyle = new JPanel();
		txfFontStyle = new JTextField();
		scpFontStyle = new JScrollPane();
		lstFontStyle = new JList();
		pnlFontSize = new JPanel();
		txfFontSize = new JTextField();
		scpFontSize = new JScrollPane();
		lstFontSize = new JList();
		pnlEffects = new JPanel();
		jCheckBox1 = new JCheckBox();
		jCheckBox2 = new JCheckBox();
		jCheckBox3 = new JCheckBox();
		jCheckBox4 = new JCheckBox();
		jCheckBox5 = new JCheckBox();
		jCheckBox6 = new JCheckBox();
		jCheckBox7 = new JCheckBox();
		jCheckBox8 = new JCheckBox();
		jCheckBox9 = new JCheckBox();
		pnlPreview = new JPanel();
		pnlCanvas = new JPanel() {
			public void paint(Graphics g) {
				super.paint(g);
				String font = txfFontFamily.getText();
				if (!font.equals("")) {
					String style = txfFontStyle.getText();
					int fontStyle = Font.PLAIN;
					if (style.equals("Bold")) {
						fontStyle = Font.BOLD;
					} else if (style.equals("Italic")) {
						fontStyle = Font.ITALIC;
					} else if (style.equals("Bold Italic")) {
						fontStyle = Font.BOLD | Font.ITALIC;
					}
					g.setFont(new Font(font, fontStyle, fontSize));
					FontMetrics fm = ((Graphics2D) g).getFontMetrics();
					int dx = fm.stringWidth(font);
					int dy = fm.getHeight();
					g.drawString(font, (int) ((getWidth() - dx) / 2),
							(int) ((getHeight() + 0.7 * dy) / 2));
				}
			}
		};
		pnlButtons = new JPanel();
		pnlDefault = new JPanel();
		btnDefault = new JButton();
		pnlOkCancel = new JPanel();
		btnCancel = new JButton();
		btnOk = new JButton();

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pnlMain.setLayout(new BorderLayout());

		pnlMain.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
		pnlMain.setPreferredSize(new Dimension(400, 400));
		pnlFont.setLayout(new GridBagLayout());

		pnlFontFamily.setLayout(new BorderLayout(2, 2));

		pnlFontFamily.setBorder(new TitledBorder("Font"));
		txfFontFamily.addActionListener(this);
		pnlFontFamily.add(txfFontFamily, BorderLayout.NORTH);

		lstFontFamily.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstFontFamily.addListSelectionListener(this);
		scpFontFamily.setViewportView(lstFontFamily);

		pnlFontFamily.add(scpFontFamily, BorderLayout.CENTER);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridheight = 2;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.33;
		gridBagConstraints.weighty = 0.5;
		gridBagConstraints.insets = new Insets(1, 1, 1, 1);
		pnlFont.add(pnlFontFamily, gridBagConstraints);

		pnlFontStyle.setLayout(new BorderLayout(2, 2));

		pnlFontStyle.setBorder(new TitledBorder("Style"));
		txfFontStyle.addActionListener(this);
		pnlFontStyle.add(txfFontStyle, BorderLayout.NORTH);

		lstFontStyle.setModel(new AbstractListModel() {
			String[] strings = { "Plain", "Bold", "Italic", "Bold Italic" };

			public int getSize() {
				return strings.length;
			}

			public Object getElementAt(int i) {
				return strings[i];
			}
		});
		lstFontStyle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstFontStyle.addListSelectionListener(this);
		scpFontStyle.setViewportView(lstFontStyle);

		pnlFontStyle.add(scpFontStyle, BorderLayout.CENTER);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridheight = 2;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.33;
		gridBagConstraints.weighty = 0.5;
		gridBagConstraints.insets = new Insets(1, 1, 1, 1);
		pnlFont.add(pnlFontStyle, gridBagConstraints);

		pnlFontSize.setLayout(new BorderLayout(2, 2));

		pnlFontSize.setBorder(new TitledBorder("Size"));
		txfFontSize.addActionListener(this);
		pnlFontSize.add(txfFontSize, BorderLayout.NORTH);

		lstFontSize.setModel(new AbstractListModel() {
			String[] strings = { "8", "9", "10", "11", "12", "14", "16", "18",
					"20", "22", "24", "26", "28", "36", "48", "72" };

			public int getSize() {
				return strings.length;
			}

			public Object getElementAt(int i) {
				return strings[i];
			}
		});
		lstFontSize.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstFontSize.addListSelectionListener(this);
		scpFontSize.setViewportView(lstFontSize);

		pnlFontSize.add(scpFontSize, BorderLayout.CENTER);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 0.33;
		gridBagConstraints.weighty = 0.5;
		gridBagConstraints.insets = new Insets(1, 1, 1, 1);
		pnlFont.add(pnlFontSize, gridBagConstraints);

		pnlEffects.setLayout(new GridLayout(3, 3));

		pnlEffects.setBorder(new TitledBorder("Effects"));
		jCheckBox1.setText("Strikethrough");
		jCheckBox1.setEnabled(false);
		jCheckBox1.setMargin(new Insets(0, 0, 0, 0));
		pnlEffects.add(jCheckBox1);

		jCheckBox2.setText("Subscript");
		jCheckBox2.setEnabled(false);
		jCheckBox2.setMargin(new Insets(0, 0, 0, 0));
		pnlEffects.add(jCheckBox2);

		jCheckBox3.setText("Small Caps");
		jCheckBox3.setEnabled(false);
		jCheckBox3.setMargin(new Insets(0, 0, 0, 0));
		pnlEffects.add(jCheckBox3);

		jCheckBox4.setText("Double Strikethrough");
		jCheckBox4.setEnabled(false);
		jCheckBox4.setMargin(new Insets(0, 0, 0, 0));
		pnlEffects.add(jCheckBox4);

		jCheckBox5.setText("Shadow");
		jCheckBox5.setEnabled(false);
		jCheckBox5.setMargin(new Insets(0, 0, 0, 0));
		pnlEffects.add(jCheckBox5);

		jCheckBox6.setText("All Caps");
		jCheckBox6.setEnabled(false);
		jCheckBox6.setMargin(new Insets(0, 0, 0, 0));
		pnlEffects.add(jCheckBox6);

		jCheckBox7.setText("Superscript");
		jCheckBox7.setEnabled(false);
		jCheckBox7.setMargin(new Insets(0, 0, 0, 0));
		pnlEffects.add(jCheckBox7);

		jCheckBox8.setText("Outline");
		jCheckBox8.setEnabled(false);
		jCheckBox8.setMargin(new Insets(0, 0, 0, 0));
		pnlEffects.add(jCheckBox8);

		jCheckBox9.setText("Hidden");
		jCheckBox9.setEnabled(false);
		jCheckBox9.setMargin(new Insets(0, 0, 0, 0));
		pnlEffects.add(jCheckBox9);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.33;
		pnlFont.add(pnlEffects, gridBagConstraints);

		pnlPreview.setLayout(new BorderLayout());

		pnlPreview.setBorder(new CompoundBorder(new TitledBorder("Preview"),
				new EmptyBorder(new Insets(5, 5, 5, 5))));
		pnlCanvas.setBackground(new Color(255, 255, 255));
		pnlCanvas.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlCanvas.setPreferredSize(new Dimension(0, 35));
		pnlPreview.add(pnlCanvas, BorderLayout.CENTER);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.33;
		pnlFont.add(pnlPreview, gridBagConstraints);

		pnlMain.add(pnlFont, BorderLayout.CENTER);

		pnlButtons.setLayout(new BorderLayout());

		pnlDefault.setLayout(new BorderLayout());

		btnDefault.setText("Default...");
		btnDefault.addActionListener(this);
		pnlDefault.add(btnDefault, BorderLayout.CENTER);

		pnlButtons.add(pnlDefault, BorderLayout.WEST);

		pnlOkCancel.setLayout(new BorderLayout(2, 2));

		btnCancel.setText("Cancel");
		btnCancel.addActionListener(this);
		pnlOkCancel.add(btnCancel, BorderLayout.EAST);

		btnOk.setText("OK");
		btnOk.addActionListener(this);
		pnlOkCancel.add(btnOk, BorderLayout.WEST);

		pnlButtons.add(pnlOkCancel, BorderLayout.EAST);

		pnlMain.add(pnlButtons, BorderLayout.SOUTH);

		getContentPane().add(pnlMain, BorderLayout.CENTER);

		pack();
	}

	private void initOtherComponents() {
		lstFontFamily.setModel(new AbstractListModel() {
			public int getSize() {
				return fontList.length;
			}

			public Object getElementAt(int i) {
				return fontList[i];
			}
		});
		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	public int showFontDialog(String c) {
		if (isValid(c)) {
			setCurrentFont(c);
		} else {
			setCurrentFont(defaultFont);
		}
		setLocationRelativeTo(getParent());
		option = CANCEL_OPTION;
		setVisible(true);
		return option;
	}

	public static boolean isValid(String f) {
		if (fontList == null) {
			fontList = GraphicsEnvironment.getLocalGraphicsEnvironment()
					.getAvailableFontFamilyNames();
		}
		String[] e = f.split(",");
		if (e.length != 3) {
			return false;
		}
		int i;
		e[0] = e[0].trim();
		for (i = 0; i < fontList.length; i++) {
			if (fontList[i].equals(e[0])) {
				break;
			}
		}
		if (i >= fontList.length) {
			return false;
		}
		e[1] = e[1].trim();
		if (!e[1].equals("Plain") && !e[1].equals("Bold")
				&& !e[1].equals("Italic") && !e[1].equals("Bold Italic")) {
			return false;
		}
		e[2] = e[2].trim();
		try {
			int n = Integer.parseInt(e[2]);
			if (n < 1) {
				return false;
			}
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public String getCurrentFont() {
		return currentFont;
	}

	public void setCurrentFont(String f) {
		if (isValid(f)) {
			currentFont = f;
			String[] e = currentFont.split(",");
			fontSize = Integer.parseInt(e[2].trim());
			lstFontSize.setSelectedValue("" + fontSize, true);
			lstFontStyle.setSelectedValue(e[1].trim(), true);
			lstFontFamily.setSelectedValue(e[0].trim(), true);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnDefault) {
			setCurrentFont(defaultFont);
		} else if (e.getSource() == btnOk) {
			setCurrentFont(txfFontFamily.getText() + ", "
					+ txfFontStyle.getText() + ", " + txfFontSize.getText());
			option = APPROVE_OPTION;
			setVisible(false);
		} else if (e.getSource() == btnCancel) {
			option = CANCEL_OPTION;
			setVisible(false);
		} else if (e.getSource() == txfFontFamily) {
			int i;
			String font = txfFontFamily.getText();
			ListModel model = lstFontFamily.getModel();
			int length = model.getSize();
			for (i = 0; i < length; i++) {
				if (model.getElementAt(i).equals(font)) {
					break;
				}
			}
			if (i < length) {
				lstFontFamily.setSelectedValue(font, true);
			} else {
				JOptionPane.showMessageDialog(this,
						"Please enter a valid font name", "Invalid Font",
						JOptionPane.WARNING_MESSAGE);
				txfFontFamily
						.setText((String) lstFontFamily.getSelectedValue());
			}
		} else if (e.getSource() == txfFontStyle) {
			int i;
			String style = txfFontStyle.getText();
			ListModel model = lstFontStyle.getModel();
			int length = model.getSize();
			for (i = 0; i < length; i++) {
				if (model.getElementAt(i).equals(style)) {
					break;
				}
			}
			if (i < length) {
				lstFontStyle.setSelectedValue(style, true);
			} else {
				JOptionPane.showMessageDialog(this,
						"Please enter a valid style", "Invalid Font",
						JOptionPane.WARNING_MESSAGE);
				txfFontStyle.setText((String) lstFontStyle.getSelectedValue());
			}
		} else if (e.getSource() == txfFontSize) {
			int i;
			String size = txfFontSize.getText();
			try {
				i = Integer.parseInt(size);
				fontSize = i;
				ListModel model = lstFontSize.getModel();
				int length = model.getSize();
				for (i = 0; i < length; i++) {
					if (model.getElementAt(i).equals(size)) {
						break;
					}
				}
				if (i < length) {
					lstFontSize.setSelectedValue(size, true);
				}
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(this,
						"Please enter a valid size", "Invalid Font",
						JOptionPane.WARNING_MESSAGE);
				txfFontSize.setText("" + fontSize);
			}
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == lstFontFamily) {
			txfFontFamily.setText((String) lstFontFamily.getSelectedValue());
		} else if (e.getSource() == lstFontStyle) {
			txfFontStyle.setText((String) lstFontStyle.getSelectedValue());
		} else if (e.getSource() == lstFontSize) {
			fontSize = Integer
					.parseInt((String) lstFontSize.getSelectedValue());
			txfFontSize.setText("" + fontSize);
		}
		pnlCanvas.repaint();
	}

	public static Font toFont(String f) {
		String[] e = f.split(",");
		e[0] = e[0].trim();
		String fontName = e[0];
		int fontStyle = Font.PLAIN;
		e[1] = e[1].trim();
		if (e[1].equals("Bold")) {
			fontStyle = Font.BOLD;
		} else if (e[1].equals("Italic")) {
			fontStyle = Font.ITALIC;
		} else if (e[1].equals("Bold Italic")) {
			fontStyle = Font.BOLD | Font.ITALIC;
		}
		e[2] = e[2].trim();
		int fontSize = Integer.parseInt(e[2]);
		return new Font(fontName, fontStyle, fontSize);
	}

	public static void main(String args[]) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new JFontChooser(new JFrame(), "Font Chooser", "Verdana, Bold, 14")
				.showFontDialog("Times New Roman, Bold, 12");
	}
}
