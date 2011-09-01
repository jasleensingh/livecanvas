package livecanvas.sketchcreator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class ColorBar extends JPanel {
	public static interface Listener {
		public void selectedColorChanged(Color newColor);
	}

	private class Swatch extends JButton {
		public final Color color;

		public Swatch(Color color) {
			this.color = color;
			setPreferredSize(new Dimension(15, 15));
			setContentAreaFilled(false);
			setBorder(BorderFactory.createLineBorder(Color.black));
			setBackground(color);
			setFocusable(false);
			addActionListener(swatchSelectionListener);
		}

		@Override
		protected void paintComponent(Graphics g) {
			g.setColor(color);
			g.fillRect(0, 0, getWidth(), getHeight());
			super.paintComponent(g);
		}
	}

	private ActionListener swatchSelectionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			selectedColor = ((Swatch) e.getSource()).color;
			selectedColorDisplay.setBackground(selectedColor);
			notifyListener();
		}
	};
	private Color selectedColor = Color.black;
	private Listener listener;
	private JPanel selectedColorDisplay;

	public ColorBar() {
		super(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		selectedColorDisplay = new JPanel();
		selectedColorDisplay.setBackground(selectedColor);
		selectedColorDisplay.setBorder(BorderFactory.createLineBorder(Color.black));
		selectedColorDisplay.setPreferredSize(new Dimension(30, 30));
		add(selectedColorDisplay, BorderLayout.NORTH);
		Color[] colors = createColors();
		JPanel swatchesPanel = new JPanel(new GridLayout(colors.length / 12, 12,
				2, 2));
		for (int i = 0; i < colors.length; i++) {
			swatchesPanel.add(new Swatch(colors[i]));
		}
		add(swatchesPanel);
	}

	public Color getSelectedColor() {
		return selectedColor;
	}

	public void setSelectedColor(Color selectedColor) {
		this.selectedColor = selectedColor;
		repaint();
	}

	public Listener getListener() {
		return listener;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	protected void notifyListener() {
		if (listener == null) {
			return;
		}
		listener.selectedColorChanged(selectedColor);
	}

	private Color[] createColors() {
		List<Color> colorList = new ArrayList<Color>();
		for (int gr = 0; gr <= 0xff; gr += 0x33) {
			Color c = new Color(gr, gr, gr);
			colorList.add(c);
		}
		for (int i = 1; i <= 6; i++) {
			Color c = new Color((i & 0x1) != 0 ? 0xff : 0x00,
					(i & 0x2) != 0 ? 0xff : 0x00, (i & 0x4) != 0 ? 0xff : 0x00);
			colorList.add(c);
		}
		for (int r = 0; r <= 0xff; r += 0x33) {
			for (int g = 0; g <= 0xff; g += 0x33) {
				for (int b = 0; b <= 0xff; b += 0x33) {
					Color c = new Color(r, g, b);
					colorList.add(c);
				}
			}
		}
		return colorList.toArray(new Color[0]);
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JDialog d = new JDialog((JFrame) null, "ColorBar Test", true);
		d.setContentPane(new ColorBar());
		d.pack();
		d.setLocationRelativeTo(null);
		d.setVisible(true);
		System.exit(0);
	}
}
