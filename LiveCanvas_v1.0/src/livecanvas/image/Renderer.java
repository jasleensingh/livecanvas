package livecanvas.image;

import java.awt.Graphics2D;
import java.awt.Paint;

import livecanvas.Progress;

public interface Renderer<T> {
	public Paint getBackground();

	public void render(Graphics2D g, RenderData data, Particle<T>[] particles, Progress.Indicator progress);

	public boolean supportsBlending();
	
	public int state();
}
