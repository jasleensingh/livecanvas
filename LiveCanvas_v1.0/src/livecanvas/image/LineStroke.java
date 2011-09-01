package livecanvas.image;

import java.awt.Stroke;

public class LineStroke {
	public final double angle;
	public final float length;
	public final Stroke stroke;
	public final BlendedColor color;

	public LineStroke(double angle, float length, Stroke stroke,
			BlendedColor color) {
		this.angle = angle;
		this.length = length;
		this.stroke = stroke;
		this.color = color;
	}
}
