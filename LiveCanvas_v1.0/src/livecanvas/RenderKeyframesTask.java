package livecanvas;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import livecanvas.Progress.Indicator;
import livecanvas.animator.Interpolator;
import livecanvas.animator.Keyframes;
import livecanvas.animator.KeyframesContainer;
import livecanvas.components.Keyframe;
import livecanvas.components.Layer;
import livecanvas.image.Particle;
import livecanvas.image.RenderData;
import livecanvas.image.Style;

public class RenderKeyframesTask implements Progress.Task {
	private KeyframesContainer container;
	private Keyframes keyframes;
	private Style style;
	private RenderData data;
	private BufferedImage[] renderedFrames;
	private String desc;

	private RenderKeyframesTask(KeyframesContainer container,
			Keyframes keyframes, Style style, RenderData data) {
		this.container = container;
		this.keyframes = keyframes;
		this.style = style;
		this.data = data;
	}

	@Override
	public String description() {
		return desc;
	}

	@Override
	public final void run(Indicator progress) {
		List<BufferedImage> renderedFramesList = new LinkedList<BufferedImage>();
		desc = "Generating particles...";
		List<Particle[]> particlesInPass = new LinkedList<Particle[]>();
		for (int i = 0; i < keyframes.size(); i++) {
			if (progress.isCanceled()) {
				break;
			}
			data.currKeyframe = i;
			Keyframe kf = keyframes.get(i);
			Interpolator in = kf.getInterpolator();
			for (int j = 0; j <= in.intermediateFramesCount; j++) {
				desc = "Rendering Frame " + (renderedFramesList.size() + 1) + "...";
				if (j == in.intermediateFramesCount) {
					data.interpolation = 1.0f;
					container.updateMeshFromKeyframe(kf);
				} else {
					float x = (float) (j + 1)
							/ (in.intermediateFramesCount + 1);
					float interpolation = in.findYForX(x);
					data.interpolation = interpolation;
					container.updateMeshFromKeyframes(keyframes.get(i - 1), kf,
							interpolation);
				}
				BufferedImage rendered = new BufferedImage(
						data.canvasSize.width, data.canvasSize.height,
						BufferedImage.TYPE_INT_RGB);
				Graphics2D g = rendered.createGraphics();
				g.setPaint(style.renderer.getBackground());
				g.fillRect(0, 0, data.canvasSize.width, data.canvasSize.height);
				int state = Progress.NOT_DONE;
				data.render = rendered;
				for (data.pass = 0; state == Progress.NOT_DONE; data.pass++) {
					Particle[] particles;
					if (data.pass < particlesInPass.size()) {
						particles = particlesInPass.get(data.pass);
						style.generator.pass(data);
					} else {
						style.generator.preprocess(data);
						particles = style.generator.generate(data, progress);
						particlesInPass.add(data.pass, particles);
					}
					state = style.renderer.state();
					if (state == Progress.CANCELED) {
						break;
					}
					style.renderer.render(g, data, particles, progress);
					state = style.renderer.state();
				}
				g.dispose();
				// try {
				// ImageIO.write(rendered, "png", new File(
				// "C:/Users/Jasleen/Desktop/animframes/"
				// + renderedFramesList.size() + ".png"));
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				renderedFramesList.add(rendered);

				// Update particle positions if blending
				// KINDA HACKY
				if (i > 0 && j == in.intermediateFramesCount) {
					for (Layer l : data.layer.getSubLayersRecursively()) {
						int backingIndex1 = keyframes.get(i - 1)
								.getBackgroundRefBackingIndex(l);
						int backingIndex2 = kf.getBackgroundRefBackingIndex(l);
						if (backingIndex1 != backingIndex2) {
							System.err.println("blending " + l.getName() + "!!!!!!!!!!!");
							Mesh mesh = l.getPath().getMesh();
							for (Particle[] ps : particlesInPass) {
								for (Particle p : ps) {
									if (p.mesh == mesh) {
										p.blend();
									}
								}
							}
						}
					}
				}
			}
		}
		if (!progress.isCanceled()) {
			this.renderedFrames = renderedFramesList
					.toArray(new BufferedImage[0]);
		}
	}

	public static BufferedImage[] render(Component parent,
			KeyframesContainer container, Keyframes keyframes, Style style,
			RenderData data) {
		RenderKeyframesTask task = new RenderKeyframesTask(container,
				keyframes, style, data);
		Progress.Dialog.show(parent, task);
		return task.renderedFrames;
	}
}
