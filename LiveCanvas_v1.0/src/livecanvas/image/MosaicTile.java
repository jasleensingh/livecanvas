package livecanvas.image;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Stroke;

public class MosaicTile {
	public double angle;
	public Rectangle drawRect, fillRect;
	public Color drawColor;
	public BlendedColor fillColor;
	public Stroke drawStroke;

	public MosaicTile(double angle, Rectangle drawRect, Rectangle fillRect,
			Color drawColor, BlendedColor fillColor, Stroke drawStroke) {
		this.angle = angle;
		this.drawRect = drawRect;
		this.fillRect = fillRect;
		this.drawColor = drawColor;
		this.fillColor = fillColor;
		this.drawStroke = drawStroke;
	}
}
