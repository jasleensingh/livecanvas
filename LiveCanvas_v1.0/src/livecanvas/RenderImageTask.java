package livecanvas;

import java.awt.Color;
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

public class RenderImageTask implements Progress.Task {
	private Style style;
	private RenderData data;
	private BufferedImage rendered;

	private RenderImageTask(Style style, RenderData data) {
		this.style = style;
		this.data = data;
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
		try {
			ImageIO.write(rendered, "png", new File(
					"C:/Users/Jasleen/Desktop/canvas.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!progress.isCanceled()) {
			this.rendered = rendered;
		}
	}

	public static BufferedImage render(Component parent, Style style,
			RenderData data) {
		RenderImageTask task = new RenderImageTask(style, data);
		Progress.Dialog.show(parent, task);
		return task.rendered;
	}
}
