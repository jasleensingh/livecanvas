package livecanvas.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import livecanvas.Progress;
import livecanvas.image.TextureStroke.Texture;

public class TextureStrokeRenderer extends AbstractRenderer<TextureStroke> {
	@Override
	public void render(Graphics2D g, RenderData data,
			Particle<TextureStroke>[] particles, Progress.Indicator progress) {
		AffineTransform t = g.getTransform();
		int n = -1, nn, steps = 10;
		for (int i = 0; i < particles.length; i++) {
			if (progress.isCanceled()) {
				state(Progress.CANCELED);
				return;
			}
			if ((nn = i * steps / particles.length) != n) {
				n = nn;
				progress.setProgress(String.format("%d%% complete", nn * 10),
						(double) n / steps);
			}
			Particle<TextureStroke> p = particles[i];
			Point2D.Double loc = p.getLocation();
			g.translate(loc.x, loc.y);
			TextureStroke ts = p.packet;
			Color c = calculateBlendedColor(data, p, loc, ts.color);
			if (c.getAlpha() <= 0) {
				continue;
			}
			g.rotate(ts.angle);
			g.scale((double) ts.length / ts.texture.width, (double) ts.size
					/ ts.texture.height);
			BufferedImage texture = filter(c, ts.texture);
			g.drawImage(texture, -texture.getWidth() / 2,
					-texture.getHeight() / 2, null);
			g.setTransform(t);
		}
		state(Progress.DONE);
	}

	private BufferedImage filtered;
	private int[] rgbArray;

	private BufferedImage filter(Color color, Texture texture) {
		int width = texture.width;
		int height = texture.height;
		if (filtered == null || filtered.getWidth() != width
				|| filtered.getHeight() != height) {
			filtered = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
			rgbArray = filtered.getRGB(0, 0, width, height, null, 0, width);
		}
		int rgb = color.getRGB() & 0x00ffffff;
		for (int i = 0; i < texture.gray.length; i++) {
			int alpha = 0xff - (texture.gray[i] & 0xff);
			rgbArray[i] = (alpha << 24) | rgb;
		}
		filtered.setRGB(0, 0, width, height, rgbArray, 0, width);
		return filtered;
	}

	@Override
	public boolean supportsBlending() {
		return true;
	}
}
