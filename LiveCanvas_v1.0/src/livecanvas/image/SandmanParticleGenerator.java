package livecanvas.image;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import livecanvas.BackgroundRef;
import livecanvas.Path;

import common.typeutils.DoubleType;
import common.typeutils.IntegerType;

public class SandmanParticleGenerator extends
		AbstractUniformParticleGenerator<SandTile> {
	@DoubleType(name = "Threshold", min = 0.0, max = 1.0, step = 0.1)
	public double threshold = 0.25;

	@DoubleType(name = "Density", min = 0.0, max = 2.0, step = 0.1)
	public double density = 0.5;

	@IntegerType(name = "Sides", min = 5, max = 1000, step = 5)
	public int sides = 30;

	@Override
	protected int sides() {
		return sides;
	}

	@Override
	public void preprocess(RenderData data) {
	}

	@Override
	protected SandTile forPoly(Path path, RenderData renderData,
			BackgroundRef bgref, Polygon poly) {
		Rectangle bounds = poly.getBounds();
		int d = Math.max(bounds.width, bounds.height);
		int count = (int) (density * d * d) + 1;
		int[] randomx = new int[count];
		int[] randomy = new int[count];
		for (int i = 0; i < count; i++) {
			randomx[i] = (int) ((Math.random() * 2 - 1) * d);
			randomy[i] = (int) ((Math.random() * 2 - 1) * d);
		}
		return new SandTile(threshold, new BlendedColor(bgref), randomx,
				randomy);
	}
}
