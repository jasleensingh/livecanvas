package livecanvas.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import livecanvas.animator.Interpolator;
import livecanvas.animator.Keyframes;

public class KeyframeView extends JPanel {
	public static final int KeyframeSize = 80;
	public static final int KeyframeViewBorder = 10;

	public static interface Listener {
		public void onKeyframeSelected(Keyframe kf);

		public void onInterpolatorSelected(Interpolator in);
	}

	private MouseListener ml = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
			Keyframe kf = overKeyframe(e.getX(), e.getY());
			if (kf != null) {
				listener.onKeyframeSelected(kf);
				return;
			}
			Interpolator in = overInterpolator(e.getX(), e.getY());
			if (in != null) {
				listener.onInterpolatorSelected(in);
				return;
			}
		}
	};

	private Keyframes keyframes;
	private KeyframeView.Listener listener;

	public KeyframeView(Keyframes keyframes, KeyframeView.Listener listener) {
		super(null);
		this.keyframes = keyframes;
		this.listener = listener;
		setFocusable(true);
		setBackground(Color.white);
		addMouseListener(ml);
	}

	public Keyframes getKeyframes() {
		return keyframes;
	}

	public void setKeyframes(Keyframes keyframes) {
		this.keyframes = keyframes;
		updateSize();
	}

	private Keyframe overKeyframe(int x, int y) {
		for (Keyframe kf : keyframes) {
			if (kf.getBounds().contains(x, y)) {
				return kf;
			}
		}
		return null;
	}

	private Interpolator overInterpolator(int x, int y) {
		int n = 0;
		for (Keyframe kf : keyframes) {
			if (n++ > 0) {
				Rectangle bounds = kf.getBounds();
				bounds.setBounds(bounds.x - 10, bounds.height / 2 - 25, 10, 50);
				if (bounds.contains(x, y)) {
					return kf.getInterpolator();
				}
			}
		}
		return null;
	}

	public void updateSize() {
		int nkeyframes = keyframes.size();
		setPreferredSize(new Dimension(nkeyframes * KeyframeSize + 10
				* (nkeyframes - 1) + 2 * KeyframeViewBorder, KeyframeSize + 2
				* KeyframeViewBorder));
	}

	private static final Font font = new Font("Sans Serif", Font.BOLD, 12);

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int x = KeyframeViewBorder;
		int y = KeyframeViewBorder;
		int n = 0;
		for (Keyframe kf : keyframes) {
			g.translate(x, y);
			if (kf.isSelected()) {
				g.setColor(Color.lightGray);
				g.fillRect(0, 0, KeyframeSize, KeyframeSize);
			}
			g.setColor(Color.black);
			g.drawRect(0, 0, KeyframeSize, KeyframeSize);
			kf.setBounds(x, y, KeyframeSize, KeyframeSize);
			kf.draw(g, KeyframeSize, KeyframeSize);
			if (n++ > 0) {
				g.setColor(Color.black);
				int tx = -5, ty = KeyframeSize / 2;
				g.fillRect(tx - 5, ty - 25, 10, 50);
				int nIntFrames = kf.getInterpolator().intermediateFramesCount;
				g.setFont(font);
				g.setColor(Color.white);
				g.drawString(nIntFrames + "", tx - 3, ty + 3);
			}
			g.translate(-x, -y);
			x += KeyframeSize + 10;
		}
	}
}