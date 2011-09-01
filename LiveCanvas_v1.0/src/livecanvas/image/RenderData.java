package livecanvas.image;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import livecanvas.animator.Keyframes;
import livecanvas.components.Layer;

public class RenderData {
	public Dimension canvasSize;

	public int pass;

	public BufferedImage render;

	public Layer layer;

	public Keyframes keyframes;

	public int currKeyframe;
	
	public float interpolation;
	
	public Object packet;
}
