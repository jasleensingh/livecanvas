package livecanvas.image;

public class SandTile {
	public final double threshold;
	public final BlendedColor color;
	public final int[] randomx, randomy;
	
	public SandTile(double threshold, BlendedColor color, int[] randomx,
			int[] randomy) {
		this.threshold = threshold;
		this.color = color;
		this.randomx = randomx;
		this.randomy = randomy;
	}
}
