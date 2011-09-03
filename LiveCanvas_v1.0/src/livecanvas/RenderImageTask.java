package livecanvas;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import livecanvas.Progress.Indicator;
import livecanvas.image.Particle;
import livecanvas.image.RenderData;
import livecanvas.image.Style;
import livecanvas.mesheditor.MeshEditor.RenderImageSettings;

public class RenderImageTask implements Progress.Task {
	private Style style;
	private RenderData data;
	private RenderImageSettings renderImageSettings;
	private BufferedImage rendered;

	private RenderImageTask(Style style, RenderData data,
			RenderImageSettings renderImageSettings) {
		this.style = style;
		this.data = data;
		this.renderImageSettings = renderImageSettings;
	}

	@Override
	public String description() {
		return "Rendering Image...";
	}

	@Override
	public final void run(Indicator progress) {
		BufferedImage rendered = new BufferedImage(data.canvasSize.width,
				data.canvasSize.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = rendered.createGraphics();
		g.setPaint(style.renderer.getBackground());
		g.fillRect(0, 0, rendered.getWidth(), rendered.getHeight());
		int state = Progress.NOT_DONE;
		data.render = rendered;
		for (data.pass = 0; state == Progress.NOT_DONE; data.pass++) {
			style.generator.preprocess(data);
			Particle[] particles = style.generator.generate(data, progress);
			state = style.renderer.state();
			if (state == Progress.CANCELED) {
				break;
			}
			style.renderer.render(g, data, particles, progress);
			state = style.renderer.state();
		}
		g.dispose();
		if (renderImageSettings != null
				&& renderImageSettings.writeRenderedFrame) {
			try {
				String name = renderImageSettings.renderedFrameName;
				String format = renderImageSettings.renderedFrameFormat;
				ImageIO.write(rendered, format, new File(
						renderImageSettings.renderedFrameDir + "/" + name + "."
								+ format));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!progress.isCanceled()) {
			this.rendered = rendered;
		}
	}

	public static BufferedImage render(Component parent, Style style,
			RenderData data, RenderImageSettings renderImageSettings) {
		RenderImageTask task = new RenderImageTask(style, data,
				renderImageSettings);
		Progress.Dialog.show(parent, task);
		return task.rendered;
	}
}
