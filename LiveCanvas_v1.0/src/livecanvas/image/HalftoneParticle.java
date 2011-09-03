package livecanvas.image;

public class HalftoneParticle {
	public final double threshold;
	public final BlendedColor color;

	public HalftoneParticle(double threshold, BlendedColor color) {
		this.threshold = threshold;
		this.color = color;
	}
}
