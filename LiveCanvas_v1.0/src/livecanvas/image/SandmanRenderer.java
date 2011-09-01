package livecanvas.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import livecanvas.Progress;

public class SandmanRenderer extends AbstractRenderer<SandTile> {
	private int[][] textures;
	private int[] textureWidths, textureHeights;

	public SandmanRenderer() {
		try {
			textures = new int[2][];
			textureWidths = new int[2];
			textureHeights = new int[2];
			BufferedImage img;
			img = ImageIO.read(SandmanRenderer.class
					.getResource("res/dark.png"));
			textureWidths[0] = img.getWidth();
			textureHeights[0] = img.getHeight();
			textures[0] = img.getRGB(0, 0, textureWidths[0], textureHeights[0],
					null, 0, textureWidths[0]);
			img = ImageIO.read(SandmanRenderer.class
					.getResource("res/light.png"));
			textureWidths[1] = img.getWidth();
			textureHeights[1] = img.getHeight();
			textures[1] = img.getRGB(0, 0, textureWidths[1], textureHeights[1],
					null, 0, textureWidths[1]);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}

	@Override
	public void render(Graphics2D g, RenderData data,
			Particle<SandTile>[] particles, Progress.Indicator progress) {
		// int n = -1, nn, steps = 10;
		// int height = data.render.getHeight();
		// int width = data.render.getWidth();
		// int i = 0;
		// for (int y = 0; y < height; y++) {
		// for (int x = 0; x < width; x++, i++) {
		// if (progress.isCanceled()) {
		// state(Progress.CANCELED);
		// return;
		// }
		// if ((nn = i * steps / (width * height)) != n) {
		// n = nn;
		// progress.setProgress(
		// String.format("%d%% complete", nn * 10), (double) n
		// / steps);
		// }
		// Particle<SandTile> p = findNearest(particles, x
		// - data.canvasSize.width / 2, y - data.canvasSize.height
		// / 2);
		// Point2D.Double loc = p.getLocation();
		// Color color = calculateBlendedColor(data, p, loc,
		// p.packet.color);
		// // System.err.println(color.getRGB());
		// int type = type(color);
		// int[] tex = textures[type];
		// // int tw = textureWidths[type];
		// // int th = textureHeights[type];
		// // g.setColor(new Color(tex[(y % th) * tw + (x % tw)]));
		// g.setColor(new Color(tex[(int) (Math.random() * tex.length)]));
		// g.fillRect(x, y, 1, 1);
		// }
		// }
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
			Particle<SandTile> p = particles[i];
			Point2D.Double loc = p.getLocation();
			Color color = calculateBlendedColor(data, p, loc, p.packet.color);
			int threshold_gray = (int) (p.packet.threshold * 0xff);
			int type = type(color, threshold_gray);
			int[] tex = textures[type];
			int tw = textureWidths[type];
			int th = textureHeights[type];
			for (int j = 0; j < p.packet.randomx.length; j++) {
				int y = (int) (p.y + p.origin.y + p.packet.randomy[j] + th);
				int x = (int) (p.x + p.origin.x + p.packet.randomx[j] + tw);
				g.setColor(new Color(tex[(y % th) * tw + (x % tw)]));
				g.fillOval((int) loc.x + p.packet.randomx[j] - 1, (int) loc.y
						+ p.packet.randomy[j] - 1, 2, 2);
			}
		}
		state(Progress.DONE);
	}

	private int type(Color color, int threshold_gray) {
		int rgb = color.getRGB();
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = (rgb >> 0) & 0xff;
		int gray = (r + g + b) / 3;
		return gray < threshold_gray ? 0 : 1;
	}

	private Particle<SandTile> findNearest(Particle<SandTile>[] particles,
			int x, int y) {
		int min = Integer.MAX_VALUE;
		Particle<SandTile> nearest = null;
		for (Particle<SandTile> p : particles) {
			int dx = (int) (x - p.x);
			int dy = (int) (y - p.y);
			int dist = dx * dx + dy * dy;
			if (dist < min) {
				min = dist;
				nearest = p;
			}
		}
		return nearest;
	}

	@Override
	public boolean supportsBlending() {
		return true;
	}
}
