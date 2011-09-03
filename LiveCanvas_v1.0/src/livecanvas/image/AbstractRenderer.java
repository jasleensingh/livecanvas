package livecanvas.image;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Point2D;

import common.typeutils.ColorType;

import livecanvas.BackgroundRef;
import livecanvas.components.Layer;

public abstract class AbstractRenderer<T> implements Renderer<T> {
	@ColorType(name = "Background")
	public Color background = Color.white;

	protected int state;

	@Override
	public Paint getBackground() {
		return background;
	}

	protected void state(int state) {
		this.state = state;
	}

	@Override
	public boolean supportsBlending() {
		return false;
	}

	@Override
	public int state() {
		return state;
	}

	protected Color calculateBlendedColor(RenderData data, Particle p,
			Point2D.Double loc, BlendedColor blendedColor) {
		BackgroundRef bgref = blendedColor.bgref;
		Layer layer = bgref.getLayer();
		Color color;
		if (data.currKeyframe == 0) {
			int backingIndex = data.keyframes == null ? 0 : data.keyframes.get(
					data.currKeyframe).getBackgroundRefBackingIndex(layer);
			color = blendedColor.getColor((int) (p.x + p.origin.x),
					(int) (p.y + p.origin.y), backingIndex);
		} else {
			int backingIndex1 = data.keyframes.get(data.currKeyframe - 1)
					.getBackgroundRefBackingIndex(layer);
			int backingIndex2 = data.keyframes.get(data.currKeyframe)
					.getBackgroundRefBackingIndex(layer);
			if (backingIndex1 == backingIndex2) { // don't blend
				color = blendedColor.getColor((int) (p.x + p.origin.x),
						(int) (p.y + p.origin.y), backingIndex1);
			} else {
				color = blendedColor.getColor((int) (p.x + p.origin.x),
						(int) (p.y + p.origin.y), backingIndex1, (int) loc.x,
						(int) loc.y, backingIndex2, data.interpolation);
			}
		}
		return color;
	}
}
