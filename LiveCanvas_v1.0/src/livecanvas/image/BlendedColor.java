package livecanvas.image;

import java.awt.Color;

import livecanvas.BackgroundRef;

public class BlendedColor {
	public Color getColor(int x, int y, int backingIndex1) {
		return new Color(bgref.getRGB(x, y, backingIndex1), true);
	}

	public Color getColor(int x1, int y1, int backingIndex1, int x2, int y2,
			int backingIndex2, double blendFactor) {
		int rgb1 = bgref.getRGB(x1, y1, backingIndex1);
		int rgb2 = bgref.getRGB(x2, y2, backingIndex2);
		double blendFactorInv = 1 - blendFactor;
		int a = (int) (((rgb1 >> 24) & 0xff) * blendFactorInv + ((rgb2 >> 24) & 0xff)
				* blendFactor);
		int r = (int) (((rgb1 >> 16) & 0xff) * blendFactorInv + ((rgb2 >> 16) & 0xff)
				* blendFactor);
		int g = (int) (((rgb1 >> 8) & 0xff) * blendFactorInv + ((rgb2 >> 8) & 0xff)
				* blendFactor);
		int b = (int) ((rgb1 & 0xff) * blendFactorInv + (rgb2 & 0xff)
				* blendFactor);
		return new Color(r, g, b, a);
	}

	public final BackgroundRef bgref;

	public BlendedColor(BackgroundRef bgref) {
		this.bgref = bgref;
	}
}
