package livecanvas.image;

import livecanvas.Progress;

public interface ParticleGenerator<T> {
	// called in first frame
	////////////////////////
	public void preprocess(RenderData data);

	public Particle<T>[] generate(RenderData data, Progress.Indicator progress);
	///////////////////////
	
	// called in subsequent frames, can be used to pass some data to renderer
	///////////////////////
	public void pass(RenderData data);
	///////////////////////
	
	public int state();
}
