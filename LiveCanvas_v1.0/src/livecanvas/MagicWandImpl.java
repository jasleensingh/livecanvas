package livecanvas;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class MagicWandImpl {
	public final BufferedImage srcImage;
	public int[] src, dest[], cost, search, used, dir[];
	public Point seed;
	public boolean tracking = false;
	public List<Contour> contours = new LinkedList<Contour>();
	public Contour currContour = new Contour();

	public MagicWandImpl(BufferedImage srcImage) {
		this.srcImage = srcImage;
		create();
	}

	public void mousePressed(int x, int y, boolean shiftDown) {
		seed = new Point(x, y);
		if (currContour.count > 0) {
			contours.add(new Contour(currContour));
			currContour.clear();
		}
		tracking = tracking == false || shiftDown;
		if (tracking) {
			search();
		}
	}

	public void mouseMoved(int x, int y) {
		if (tracking) {
			findContour(x, y);
		}
	}

	private void create() {
		if (srcImage == null) {
			return;
		}
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		src = new int[width * height];
		dest = new int[3][width * height];
		cost = new int[width * height];
		search = new int[width * height];
		used = new int[width * height];
		dir = new int[2][width * height];
		srcImage.getRGB(0, 0, width, height, src, 0, width);
		blur(src, dest[0], width, height);
		System.arraycopy(dest[0], 0, src, 0, dest[0].length);
		laplace(src, dest[0], width, height);
		zerocross(dest[0], dest[1], width, height);
		gradient(src, dest[2], width, height);
		cost(dest[1], dest[2], dest[0], cost, width, height);
	}

	private class ActiveList {
		public class ImagePoint implements Comparable<ImagePoint> {
			public final int x, y;
			public final int dirx, diry;

			public ImagePoint(int x, int y) {
				this(x, y, 0, 0);
			}

			public ImagePoint(int x, int y, int dirx, int diry) {
				this.x = x;
				this.y = y;
				this.dirx = dirx;
				this.diry = diry;
			}

			@Override
			public boolean equals(Object obj) {
				ImagePoint p = (ImagePoint) obj;
				return x == p.x && y == p.y;
			}

			@Override
			public int compareTo(ImagePoint o) {
				return search[y * width + x] - search[o.y * width + o.x];
			}
		}

		private Queue<ImagePoint> queue;
		private int width, height;

		public ActiveList(int width, int height) {
			queue = new PriorityQueue<ImagePoint>();
			this.width = width;
			this.height = height;
		}

		public void add(int x, int y, int dirx, int diry) {
			queue.add(new ImagePoint(x, y, dirx, diry));
		}

		public void remove(int x, int y) {
			queue.remove(new ImagePoint(x, y));
		}

		public ImagePoint removeMin() {
			ImagePoint min = queue.remove();
			return min;
		}

		public boolean isEmpty() {
			return queue.isEmpty();
		}
	}

	private void search() {
		if (seed == null) {
			return;
		}
		int x = seed.x;
		int y = seed.y;
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		Arrays.fill(search, Integer.MAX_VALUE);
		Arrays.fill(used, 0);
		Arrays.fill(dir[0], 0);
		Arrays.fill(dir[1], 0);
		ActiveList activeList = new ActiveList(width, height);
		search[y * width + x] = 0;
		do {
			updateNeighbors(x, y, width, height, activeList);
			// int minIndex = activeList.removeMin();
			// x = activeList.xs[minIndex];
			// y = activeList.ys[minIndex];
			ActiveList.ImagePoint min = activeList.removeMin();
			used[min.y * width + min.x] = 1;
			dir[0][min.y * width + min.x] = min.dirx;
			dir[1][min.y * width + min.x] = min.diry;
			x = min.x;
			y = min.y;
			// try {
			// Thread.sleep(1);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
		} while (!activeList.isEmpty());
		activeList.queue.clear();
		activeList = null;
	}

	private void updateNeighbors(int x, int y, int width, int height,
			ActiveList activeList) {
		int curr = search[y * width + x];
		int offset;
		for (int i = y - 1; i <= y + 1; i++) {
			if (i < 0 || i >= height) {
				continue;
			}
			for (int j = x - 1; j <= x + 1; j++) {
				if (j < 0 || j >= width || used[offset = i * width + j] != 0) {
					continue;
				}
				int nc;
				if (search[offset] > (nc = curr + cost[offset])) {
					if (search[offset] != Integer.MAX_VALUE) {
						activeList.remove(j, i);
					}
					search[offset] = nc;
					activeList.add(j, i, x - j, y - i);
				}
			}
		}
	}

	// Note that points are stored in reverse order (last point of mouse is
	// stored first, and mouse press point is stored last)
	public static class Contour {
		public int[] xs, ys;
		public int count;

		public Contour() {
			xs = new int[32];
			ys = new int[32];
		}

		public Contour(Contour copy) {
			count = copy.count;
			xs = new int[count];
			ys = new int[count];
			System.arraycopy(copy.xs, 0, xs, 0, count);
			System.arraycopy(copy.ys, 0, ys, 0, count);
		}

		public void add(int x, int y) {
			if (count >= xs.length) {
				int[] nxs = new int[xs.length * 2];
				int[] nys = new int[ys.length * 2];
				System.arraycopy(xs, 0, nxs, 0, xs.length);
				System.arraycopy(ys, 0, nys, 0, ys.length);
				xs = nxs;
				ys = nys;
			}
			xs[count] = x;
			ys[count] = y;
			count++;
		}

		public void clear() {
			count = 0;
		}
	}

	private void findContour(int x, int y) {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		if (x >= width || y >= height) {
			return;
		}
		currContour.clear();
		currContour.add(x, y);
		int dirx = dir[0][y * width + x];
		int diry = dir[1][y * width + x];
		while (dirx != 0 || diry != 0) {
			x += dirx;
			y += diry;
			currContour.add(x, y);
			dirx = dir[0][y * width + x];
			diry = dir[1][y * width + x];
		}
	}

	private static final int apply_mask(int[] src, int x, int y, int width,
			int height, double[] mask, double factor, int offset, int maskSize) {
		double sumr = 0, sumg = 0, sumb = 0;
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
				int rgb = src[my * width + mx];
				double mult = mask[i * maskSize + j];
				sumr += ((rgb >> 16) & 0xff) * mult;
				sumg += ((rgb >> 8) & 0xff) * mult;
				sumb += (rgb & 0xff) * mult;
			}
		}
		int r = clamp((int) (sumr * factor) + offset);
		int g = clamp((int) (sumg * factor) + offset);
		int b = clamp((int) (sumb * factor) + offset);
		return (0xff << 24) | (r << 16) | (g << 8) | b;
	}

	private static int gray(int rgb) {
		return (((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff)) / 3;
	}

	private static int clamp(int val) {
		return Math.max(0, Math.min(0xff, val));
	}

	private static void blur(int[] src, int[] dest, int width, int height) {
		double[] mask = new double[] { //
		1, 4, 7, 4, 1,//
				4, 16, 26, 16, 4,//
				7, 26, 41, 26, 7,//
				4, 16, 26, 16, 4,//
				1, 4, 7, 4, 1 };
		int offset = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				dest[offset++] = apply_mask(src, x, y, width, height, mask,
						1.0 / 273.0f, 0, 5);
			}
		}
	}

	private static void laplace(int[] src, int[] dest, int width, int height) {
		double[] mask = new double[] { 0, 0, 1, 0, 0,//
				0, 1, 2, 1, 0,//
				1, 2, -16, 2, 1,//
				0, 1, 2, 1, 0,//
				0, 0, 1, 0, 0 };//
		int offset = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				dest[offset++] = apply_mask(src, x, y, width, height, mask,
						-1 / 8.0, 128, 5);
			}
		}
	}

	private static void zerocross(int[] src, int[] dest, int width, int height) {
		int offset = width + 1;
		for (int y = 1; y < height - 1; y++) {
			for (int x = 1; x < width - 1; x++) {
				int p0 = gray(src[offset]) - 128;
				int pw = gray(src[offset - 1]) - 128;
				int pe = gray(src[offset + 1]) - 128;
				int pn = gray(src[offset - width]) - 128;
				int ps = gray(src[offset + width]) - 128;
				if (p0 * pw < 0 || p0 * pe < 0 || p0 * pn < 0 || p0 * ps < 0) {
					dest[offset] = 0xff000000;
				} else {
					dest[offset] = 0xffffffff;
				}
				offset++;
			}
		}
	}

	private static void gradient(int[] src, int[] dest, int width, int height) {
		double[] mask1 = new double[] { -1, 0, 1,//
				-2, 0, 2,//
				-1, 0, 1, };
		double[] mask2 = new double[] { -1, -2, -1,//
				0, 0, 0,//
				1, 2, 1, };
		int offset = 0;
		int min = 256, max = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int gx = apply_mask(src, x, y, width, height, mask1, 1 / 4.0,
						128, 3);
				int gy = apply_mask(src, x, y, width, height, mask2, 1 / 4.0,
						128, 3);
				gx = gray(gx) - 128;
				gy = gray(gy) - 128;
				int gray = 0xff - (int) Math.sqrt(gx * gx + gy * gy) / 2;
				dest[offset++] = gray;
				if (min > gray) {
					min = gray;
				}
				if (max < gray) {
					max = gray;
				}
			}
		}
		int range = max - min;
		for (int i = 0; i < dest.length; i++) {
			int gray = dest[i];
			gray = (gray - min) * 0xff / range;
			dest[i] = (0xff << 24) | (gray << 16) | (gray << 8) | gray;
		}
	}

	private static void cost(int[] f1, int[] f2, int[] dest, int[] cost,
			int width, int height) {
		int offset = 0;
		double w1 = 0.3;
		double w2 = 1 - w1;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb1 = f1[offset];
				int rgb2 = f2[offset];
				int r = clamp((int) (((rgb1 >> 16) & 0xff) * w1 + ((rgb2 >> 16) & 0xff)
						* w2));
				int g = clamp((int) (((rgb1 >> 8) & 0xff) * w1 + ((rgb2 >> 8) & 0xff)
						* w2));
				int b = clamp((int) ((rgb1 & 0xff) * w1 + (rgb2 & 0xff) * w2));
				cost[offset] = (r + g + b) / 3;
				dest[offset] = (0xff << 24) | (r << 16) | (g << 8) | b;
				offset++;
			}
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
}
