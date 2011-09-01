package livecanvas.image;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

import livecanvas.BackgroundRef;
import livecanvas.Face;
import livecanvas.Mesh;
import livecanvas.Path;
import livecanvas.Vec3;

import common.typeutils.DoubleType;

public class DitheredParticleGenerator extends AbstractParticleGenerator<Color> {
	@DoubleType(name = "Threshold", min = 0.0, max = 1.0, step = 0.1)
	public double threshold = 0.25;

	private static final int apply_mask(int[] src, int x, int y, int width,
			int height, double[] mask, int maskSize) {
		double sum = 0;
		int m2 = (maskSize - 1) / 2;
		for (int i = 0; i < maskSize; i++) {
			int my = y - m2 + i;
			if (my < 0 || my >= height) {
				continue;
			}
			for (int j = 0; j < maskSize; j++) {
				int mx = x - m2 + j;
				if (mx < 0 || mx >= width) {
					continue;
				}
				sum += (src[my * width + mx] & 0xff) * mask[i * maskSize + j];
			}
		}
		int gray = (int) (sum / (maskSize * maskSize));
		return (0xff << 24) | (gray << 16) | (gray << 8) | gray;
	}

	private static void blur(int[] src, int[] dest, int width, int height,
			int blurMaskSize) {
		double[] mask = new double[blurMaskSize * blurMaskSize];
		for (int i = 0; i < mask.length; i++) {
			mask[i] = 1.0;
		}
		int offset = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				dest[offset++] = apply_mask(src, x, y, width, height, mask,
						blurMaskSize);
			}
		}
	}

	private static void threshold(int[] src, int[] dest, int[] dark, int[] light) {
		for (int i = 0; i < dest.length; i++) {
			dest[i] = (src[i] & 0xff) < 0x80 ? dark[(int) (Math.random() * dark.length)]
					: light[(int) (Math.random() * light.length)];
		}
	}

	private static void gray_hist(int[] src, int[] dest) {
		int[] hist_cum = new int[256];
		for (int i = 0; i < src.length; i++) {
			int r = (src[i] >> 16) & 0xff;
			int g = (src[i] >> 8) & 0xff;
			int b = (src[i] >> 0) & 0xff;
			int gray = (r + g + b) / 3;
			src[i] = gray;
			hist_cum[gray]++;
		}
		for (int i = 1; i < hist_cum.length; i++) {
			hist_cum[i] += hist_cum[i - 1];
		}
		for (int i = 0; i < src.length; i++) {
			int gray = src[i];
			int max_gray = hist_cum[gray] * 0x100 / src.length;
			int min_gray = gray == 0 ? 0 : hist_cum[gray - 1] * 0x100
					/ src.length;
			gray = (int) (min_gray + Math.random() * (max_gray - min_gray));
			dest[i] = (0xff << 24) | (gray << 16) | (gray << 8) | gray;
		}
	}

	private static void dither(int[] src, int[] dest, int width, int height,
			int[] dark, int[] light, double threshold) {
		double[] pixels = new double[src.length];
		for (int i = 0; i < src.length; i++) {
			pixels[i] = ((double) (src[i] & 0xff)) / 0xff;
		}
		int offset = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double p = pixels[offset];
				pixels[offset] = p > 0.5 ? 1 : 0;
				double error = p - pixels[offset];
				if (x < width - 1) {
					pixels[offset + 1] += error * 7 / 16;
				}
				if (y < height - 1) {
					if (x > 0) {
						pixels[offset + width - 1] += error * 3 / 16;
					}
					pixels[offset + width] += error * 5 / 16;
					if (x < width - 1) {
						pixels[offset + width + 1] += error * 1 / 16;
					}
				}
				offset++;
			}
		}
		for (int i = 0; i < dest.length; i++) {
			dest[i] = pixels[i] < threshold ? dark[(int) (Math.random() * dark.length)]
					: light[(int) (Math.random() * light.length)];
		}
	}

	private int[] dark, light;

	@Override
	public void preprocess(RenderData data) {
		// try {
		// BufferedImage img;
		// img = ImageIO.read(Sandman.class.getResource("dark.png"));
		// dark = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0,
		// img.getWidth());
		// img = ImageIO.read(Sandman.class.getResource("light.png"));
		// light = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0,
		// img.getWidth());
		// } catch (IOException e2) {
		// e2.printStackTrace();
		// }
		// int[] src = new int[data.rgbArray.length];
		// int[] dest = new int[data.rgbArray.length];
		// System.arraycopy(data.rgbArray, 0, src, 0, data.rgbArray.length);
		// gray_hist(src, dest);
		// System.arraycopy(dest, 0, src, 0, dest.length);
		// blur(src, dest, data.imgSize.width, data.imgSize.height, 9);
		// System.arraycopy(dest, 0, src, 0, dest.length);
		// dither(src, dest, data.imgSize.width, data.imgSize.height, dark,
		// light);
	}

	@Override
	protected Particle<Color>[] generateForPath(Path path,
			RenderData renderData, BackgroundRef bgref) {
		Mesh mesh = path.getMesh();
		List<Particle<Color>> particles = new ArrayList<Particle<Color>>();
		Polygon polygon = new Polygon(new int[3], new int[3], 3);
		Point origin = new Point(renderData.canvasSize.width / 2,
				renderData.canvasSize.height / 2);
		for (int i = 0; i < mesh.faces.length; i++) {
			Face face = mesh.faces[i];
			Vec3 v1 = mesh.vertices[face.v1Index];
			Vec3 v2 = mesh.vertices[face.v2Index];
			Vec3 v3 = mesh.vertices[face.v3Index];
			polygon.invalidate();
			polygon.xpoints[0] = (int) v1.x;
			polygon.ypoints[0] = (int) v1.y;
			polygon.xpoints[1] = (int) v2.x;
			polygon.ypoints[1] = (int) v2.y;
			polygon.xpoints[2] = (int) v3.x;
			polygon.ypoints[2] = (int) v3.y;
			int minx = (int) Math.min(Math.min(v1.x, v2.x), v3.x);
			int miny = (int) Math.min(Math.min(v1.y, v2.y), v3.y);
			int maxx = (int) Math.max(Math.max(v1.x, v2.x), v3.x);
			int maxy = (int) Math.max(Math.max(v1.y, v2.y), v3.y);
			double trgray = threshold * 0xff;
			for (int y = miny; y <= maxy; y++) {
				for (int x = minx; x <= maxx; x++) {
					if (polygon.contains(x, y)) {
						if ((bgref.getRGB((x + origin.x), (y + origin.y)) & 0xff) < trgray) {
							particles.add(new Particle<Color>(origin, mesh,
									face, x, y, Color.black));
						}
					}
				}
			}
		}
		return particles.toArray(new Particle[0]);
	}
}
