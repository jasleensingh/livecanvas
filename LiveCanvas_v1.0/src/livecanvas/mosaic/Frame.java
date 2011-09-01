package livecanvas.mosaic;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class Frame {
	public static void main(String[] args) {
		final JDialog d = new JDialog((JFrame) null, "Mosaic", true);
		d.getContentPane().add(new Mosaic());
		d.pack();
		d.setLocationRelativeTo(null);
		d.setVisible(true);
		d.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					d.setVisible(false);
				}
			}
		});
		System.exit(0);
	}
}
