package livecanvas.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import livecanvas.Progress;
import livecanvas.components.Layer;

import common.typeutils.BooleanType;
import common.typeutils.IntegerType;

public class MeshRenderer extends AbstractRenderer<Object> {
	@BooleanType(name = "Show Particles", description = "Whether particles should be drawn or not")
	public boolean showParticles = true;

	@IntegerType(name = "Particle Radius", description = "Radius of circle shape used to draw each particle")
	public int particleRadius = 2;

	@BooleanType(name = "Show Mesh", description = "Whether mesh should be drawn or not")
	public boolean showMesh = true;

	@BooleanType(name = "Show Control Points", description = "Whether control points should be drawn or not")
	public boolean showControlPoints = true;

	public void render(Graphics2D g, RenderData data,
			Particle<Object>[] particles, Progress.Indicator progress) {
		int width = data.render.getWidth();
		int height = data.render.getHeight();
		g.translate(width / 2, height / 2);
		drawAll(g, data.layer, width, height);
		g.translate(-width / 2, -height / 2);
		if (showParticles) {
			int n = -1, nn, steps = 10;
			for (int i = 0; i < particles.length; i++) {
				if (progress.isCanceled()) {
					state(Progress.CANCELED);
					return;
				}
				if ((nn = i * steps / particles.length) != n) {
					n = nn;
					progress.setProgress(
							String.format("%d%% complete", nn * 10), (double) n
									/ steps);
				}
				Particle<Object> p = particles[i];
				Point2D.Double loc = p.getLocation();
				int cx = (int) loc.x;
				int cy = (int) loc.y;
				g.setColor(Color.black);
				g.fillOval(cx - particleRadius, cy - particleRadius,
						particleRadius * 2, particleRadius * 2);
			}
		}
		state(Progress.DONE);
	}

	private void drawAll(Graphics2D g, Layer layer, int width, int height) {
		layer.draw(g, width, height, false, showMesh, showControlPoints, false);
		for (Layer subLayer : layer.getSubLayers()) {
			drawAll(g, subLayer, width, height);
		}
	}

	@Override
	public boolean supportsBlending() {
		return true;
	}
}
