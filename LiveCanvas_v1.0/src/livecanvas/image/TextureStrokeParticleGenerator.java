package livecanvas.image;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import livecanvas.BackgroundRef;
import livecanvas.Face;
import livecanvas.Mesh;
import livecanvas.Path;
import livecanvas.Vec3;
import livecanvas.image.TextureStroke.Texture;

import common.typeutils.DoubleType;
import common.typeutils.FloatType;
import common.typeutils.IntegerType;

public class TextureStrokeParticleGenerator extends
		AbstractParticleGenerator<TextureStroke> {
	@IntegerType(name = "Density", min = 1, max = 100)
	public int density = 10;

	@DoubleType(name = "Angle", min = 0, max = 2 * Math.PI)
	public double angle = Math.PI / 4;

	@FloatType(name = "Angle Jitter", min = 0, max = 1, step = 0.1f)
	public float angleJitter = 0.5f;

	@FloatType(name = "Length", min = 1, max = 100)
	public float length = 10;

	@FloatType(name = "Length Jitter", min = 0, max = 1, step = 0.1f)
	public float lengthJitter = 0.1f;

	@FloatType(name = "Color Jitter", min = 0, max = 1, step = 0.1f)
	public float colorJitter = 0.1f;

	@FloatType(name = "Stroke Size", min = 1, max = 10, step = 0.5f)
	public float strokeSize = 4.5f;

	@Override
	public void preprocess(RenderData data) {
	}

	@Override
	protected Particle<TextureStroke>[] generateForPath(Path path,
			RenderData renderData, BackgroundRef bgref) {
		Mesh mesh = path.getMesh();
		List<Particle<TextureStroke>> particles = new ArrayList<Particle<TextureStroke>>();
		Point origin = new Point(renderData.canvasSize.width / 2,
				renderData.canvasSize.height / 2);
		double cx, cy;
		Texture texture;
		try {
			texture = createTexture(ImageIO
					.read(TextureStrokeParticleGenerator.class
							.getResource("res/texture1.png")));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		for (int i = 0; i < mesh.faces.length; i++) {
			Face face = mesh.faces[i];
			Vec3 v1 = mesh.vertices[face.v1Index];
			Vec3 v2 = mesh.vertices[face.v2Index];
			Vec3 v3 = mesh.vertices[face.v3Index];
			for (int j = 0; j < density; j++) {
				double xv01 = v2.x - v1.x;
				double yv01 = v2.y - v1.y;
				double xv02 = v3.x - v1.x;
				double yv02 = v3.y - v1.y;
				double u = Math.random(), v = Math.random();
				if (u + v >= 1) {
					u = 1 - u;
					v = 1 - v;
				}
				cx = (int) (v1.x + u * xv01 + v * xv02);
				cy = (int) (v1.y + u * yv01 + v * yv02);
				float jitteredLength = (float) (length * (1 + lengthJitter
						* (Math.random() - 0.5)));
				float jitteredAngle = (float) (angle + angleJitter
						* (Math.random() - 0.5) * Math.PI);
				particles.add(new Particle<TextureStroke>(origin, mesh, face,
						cx, cy, new TextureStroke(jitteredAngle,
								jitteredLength, strokeSize, new BlendedColor(
										bgref), texture)));
			}
		}
		return particles.toArray(new Particle[0]);
	}

	private Texture createTexture(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		int[] rgb = img.getRGB(0, 0, width, height, null, 0, width);
		for (int i = 0; i < rgb.length; i++) {
			rgb[i] = rgb[i] & 0xff;
		}
		return new Texture(rgb, width, height);
	}
}
