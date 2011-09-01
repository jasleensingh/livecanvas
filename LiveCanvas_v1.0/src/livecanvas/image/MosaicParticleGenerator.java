package livecanvas.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;

import livecanvas.BackgroundRef;
import livecanvas.Path;

import common.typeutils.ColorType;
import common.typeutils.IntegerType;
import common.typeutils.StrokeType;

public class MosaicParticleGenerator extends
		AbstractUniformParticleGenerator<MosaicTile> {
	@ColorType(name = "Tile Outline Color")
	public Color tileOutlineColor = Color.black;

	@StrokeType(name = "Tile Outline Stroke")
	public Stroke tileOutlineStroke = new BasicStroke(1.0f);

	@IntegerType(name = "Sides", min = 5, max = 1000, step = 5)
	public int sides = 30;

	@Override
	protected int sides() {
		return sides;
	}

	@Override
	public void preprocess(RenderData data) {
	}

	protected MosaicTile forPoly(Path path, RenderData renderData,
			BackgroundRef bgref, Polygon poly) {
		Rectangle bounds = poly.getBounds();
		int cx = (int) bounds.getCenterX();
		int cy = (int) bounds.getCenterY();
		return createTile(bgref, cx, cy, bounds.width, bounds.height);
	}

	private MosaicTile createTile(BackgroundRef bgref, int cx, int cy,
			int width, int height) {
		int rgb = bgref.getRGB(
				Math.max(0, Math.min(bgref.getSize().width - 1, cx)),
				Math.max(0, Math.min(bgref.getSize().height - 1, cy)));
		if ((rgb & 0xff000000) == 0) {
			return null;
		}
		double angle = Math.random() * Math.PI;
		Rectangle drawRect = new Rectangle(-width / 4, -height / 4, width / 2,
				height / 2);
		Rectangle fillRect = new Rectangle(-width / 3, -height / 3,
				2 * width / 3, 3 * height / 4);
		Color drawColor = tileOutlineColor;
		BlendedColor fillColor = new BlendedColor(bgref);
		Stroke drawStroke = tileOutlineStroke;
		return new MosaicTile(angle, drawRect, fillRect, drawColor, fillColor,
				drawStroke);
	}
}
