package livecanvas.image;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import livecanvas.BackgroundRef;
import livecanvas.Path;
import livecanvas.Progress.Indicator;
import livecanvas.components.LayersView.LayersViewSettings;

public class ApplyNPRFilter {

	public static void main(String[] args) throws Exception {
		for (File in : new File(
				"C:/Users/Jasleen/Desktop/potus-scaled/thinplate-morphing")
				.listFiles()) {
			if (!in.getName().endsWith(".png")) {
				continue;
			}
			BufferedImage img = ImageIO.read(in);
			File outDir = new File(in.getParent() + "/output");
			if (!outDir.exists()) {
				outDir.mkdir();
			}
			BackgroundRef.BGImage bgref = new BackgroundRef.BGImage();
			bgref.setImagePaths(new String[] { in.getPath() });
			Path path = Path
					.fromBackgroundRef(
							bgref,
							LayersViewSettings
									.meshDensity2ParticlesBBoxRatio(LayersViewSettings.DENSITY_MEDIUM));
			LineStrokeParticleGenerator gen = new LineStrokeParticleGenerator();
			LineStrokeRenderer ren = new LineStrokeRenderer();
			RenderData renderData = new RenderData();
			renderData.canvasSize = new Dimension(img.getWidth(),
					img.getHeight());
			Particle<LineStroke>[] particles = gen.generateForPath(path,
					renderData, bgref);
			BufferedImage out = new BufferedImage(img.getWidth(),
					img.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = out.createGraphics();
			g.setColor(Color.white);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
			// g.translate(renderData.canvasSize.width/2,
			// renderData.canvasSize.height/2);
			ren.render(g, renderData, particles, Indicator.CONSOLE);
			g.dispose();
			ImageIO.write(out, "png", new File(outDir + "/" + in.getName()));
		}
	}
}
