package livecanvas.image;

import java.awt.Color;

public class TextureStroke {
	public static class Texture {
		public final int[] gray;
		public final int width, height;

		public Texture(int[] gray, int width, int height) {
			this.gray = gray;
			this.width = width;
			this.height = height;
			if (gray.length != width * height) {
				throw new IllegalArgumentException("Incompatible arguments!");
			}
		}
	}

	public final double angle;
	public final float length;
	public final float size;
	public final BlendedColor color;
	public final Texture texture;

	public TextureStroke(double angle, float length, float size, BlendedColor color,
			Texture texture) {
		this.angle = angle;
		this.length = length;
		this.size = size;
		this.color = color;
		this.texture = texture;
	}
}
