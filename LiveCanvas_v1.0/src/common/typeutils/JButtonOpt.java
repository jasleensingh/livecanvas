package common.typeutils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

public class JButtonOpt extends JButtonEx {
	private JPopupMenu popupMenu;

	private ImageIcon dropIcon;

	private boolean over, expand;

	public JButtonOpt() {
		dropIcon = new ImageIcon(getClass().getResource(
				"resources/options_dropdown.png"));
		addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				over = true;
				repaint();
			}

			public void mouseExited(MouseEvent e) {
				over = expand = false;
				repaint();
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				expand = e.getX() > getWidth() - 4 - dropIcon.getIconWidth();
			}
		});
	}

	// public void setIcon(Icon icon) {
	// super.setIcon(icon);
	// updateSize();
	// }
	//	
	// public void setText(String text) {
	// super.setText(text);
	// updateSize();
	// }
	//	
	// protected void updateSize() {
	// Dimension dim = getPreferredSize();
	// dim.width += 20;
	// setPreferredSize(dim);
	// }
	//	
	protected void fireActionPerformed(ActionEvent e) {
		if (!expand) {
			super.fireActionPerformed(e);
		} else {
			popupMenu.show(this, 0, getHeight());
		}
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int x = getWidth() - 8 - dropIcon.getIconWidth();
		int y = (getHeight() - dropIcon.getIconHeight()) / 2;
		if (over && isEnabled()) {
			g.setColor(Color.lightGray);
			g.drawLine(x, 0, x, getHeight());
		}
		dropIcon
				.paintIcon(this, g, getWidth() - 4 - dropIcon.getIconWidth(), y);
	}

	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}

	public void setPopupMenu(JPopupMenu popupMenu) {
		this.popupMenu = popupMenu;
	}
}
