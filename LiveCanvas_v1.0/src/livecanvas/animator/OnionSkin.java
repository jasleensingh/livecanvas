package livecanvas.animator;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import livecanvas.CanvasMesh;
import livecanvas.components.Keyframe;

public class OnionSkin implements Timeline.Listener {
	private KeyframesContainer container;
	private CanvasMesh canvas;
	private BufferedImage img;
	private boolean showPrevious;
	private boolean showNext;

	public OnionSkin(KeyframesContainer container, CanvasMesh canvas) {
		this.container = container;
		this.canvas = canvas;
	}

	public boolean isShowPrevious() {
		return showPrevious;
	}

	public void setShowPrevious(boolean showPrevious) {
		this.showPrevious = showPrevious;
	}

	public boolean isShowNext() {
		return showNext;
	}

	public void setShowNext(boolean showNext) {
		this.showNext = showNext;
		drawOnionSkin(container.getSelectedKeyframe());
	}

	@Override
	public void timelineChanged(int event) {
	}

	@Override
	public void keyframeChanged(Keyframe keyframe, int event) {
		drawOnionSkin(keyframe);
	}

	private void drawOnionSkin(Keyframe currKeyframe) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		if (img == null || img.getWidth() != width || img.getHeight() != height) {
			img = new BufferedImage(canvas.getWidth(), canvas.getHeight(),
					BufferedImage.TYPE_INT_ARGB);
		}
		Graphics2D g = img.createGraphics();
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(0, 0, width, height);
		if (currKeyframe != null) {
			g.setComposite(c);
			g.translate(width / 2, height / 2);
			Keyframes keyframes = container.getKeyframes();
			int index = keyframes.indexOf(currKeyframe);
			if (showPrevious && index > 0) {
				Keyframe prev = keyframes.get(index - 1);
				container.updateMeshFromKeyframe(prev);
				canvas.paintLayer(g, width, height, canvas.getCurrLayer());
			}
			if (showNext && index < keyframes.size() - 1) {
				Keyframe next = keyframes.get(index + 1);
				container.updateMeshFromKeyframe(next);
				canvas.paintLayer(g, width, height, canvas.getCurrLayer());
			}
		}
		g.dispose();
		container.updateMeshFromKeyframe(currKeyframe);
	}

	public void paint(Graphics g) {
		g.drawImage(img, -canvas.getWidth() / 2, -canvas.getHeight() / 2, null);
	}
}
