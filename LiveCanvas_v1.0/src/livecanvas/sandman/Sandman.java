package livecanvas.sandman;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Sandman extends JPanel {
	private BufferedImage srcImage, destImage;
	private int[] src, dest;
	private int[] dark, light;

	public Sandman() {
		setFocusable(true);
		setPreferredSize(new Dimension(600, 600));
		setBackground(Color.white);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_SPACE:
					create();
					break;
				case KeyEvent.VK_F5:
					try {
						FileDialog fd = new FileDialog(JOptionPane
								.getFrameForComponent(Sandman.this),
								"Save as Image");
						fd.setVisible(true);
						String file_str = fd.getFile();
						if (file_str != null) {
							if (file_str.endsWith(".jpg")
									|| file_str.endsWith(".jpeg")) {
								File file = new File(fd.getDirectory() + "/"
										+ file_str);
								ImageIO.write(destImage, "jpg", file);
							} else if (file_str.endsWith(".png")) {
								File file = new File(fd.getDirectory() + "/"
										+ file_str);
								ImageIO.write(destImage, "png", file);
							} else {
								file_str += ".png";
								File file = new File(fd.getDirectory() + "/"
										+ file_str);
								ImageIO.write(destImage, "png", file);
							}
						}
					} catch (Exception ex) {
						String msg = "An error occurred while trying to save.";
						JOptionPane.showMessageDialog(JOptionPane
								.getFrameForComponent(Sandman.this), msg,
								"Error", JOptionPane.ERROR_MESSAGE);
					}
					break;
				}
				repaint();
			}
		});
		addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent e) {
				FileDialog fd = new FileDialog(JOptionPane
						.getFrameForComponent(Sandman.this), "Load");
				fd.setVisible(true);
				String file_str = fd.getFile();
				if (file_str == null) {
					return;
				}
				try {
					srcImage = ImageIO.read(new File(fd.getDirectory() + "/"
							+ file_str));
					create();
				} catch (IOException e1) {
					String msg = "An error occurred while trying to load.";
					JOptionPane.showMessageDialog(JOptionPane
							.getFrameForComponent(Sandman.this), msg, "Error",
							JOptionPane.ERROR_MESSAGE);
				}
				requestFocusInWindow();
				repaint();
			}
		});
		try {
			BufferedImage img;
			img = ImageIO.read(Sandman.class.getResource("dark.png"));
			dark = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0,
					img.getWidth());
			img = ImageIO.read(Sandman.class.getResource("light.png"));
			light = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0,
					img.getWidth());
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		// try {
		// srcImage = ImageIO
		// .read(new File("C:/Users/Jasleen/Desktop/me7.jpg"));
		// create();
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int width = getWidth();
		int height = getHeight();
		if (destImage == null) {
			String text = "Click to load image";
			g.drawString(text,
					(width - g.getFontMetrics().stringWidth(text)) / 2,
					height / 2);
		} else {
			int imgWidth = destImage.getWidth();
			int imgHeight = destImage.getHeight();
			double scale = Math.min((double) (width - 40) / imgWidth,
					(double) (height - 40) / imgHeight);
			int scaledWidth = (int) (scale * imgWidth);
			int scaledHeight = (int) (scale * imgHeight);
			g.drawImage(destImage, (width - scaledWidth) / 2,
					(height - scaledHeight) / 2, scaledWidth, scaledHeight,
					null);
		}
		if (hasFocus()) {
			g.setColor(Color.black);
			g.drawRect(0, 0, width - 1, height - 1);
		}
	}

	private static BufferedImage scale(BufferedImage img, double scale) {
		int imgWidth = (int) (img.getWidth() * scale);
		int imgHeight = (int) (img.getHeight() * scale);
		BufferedImage buf = new BufferedImage(imgWidth, imgHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = buf.getGraphics();
		g.drawImage(img, 0, 0, imgWidth, imgHeight, null);
		g.dispose();
		return buf;
	}

	private void create() {
		if (srcImage == null) {
			return;
		}
		BufferedImage result = new BufferedImage(srcImage.getWidth(), srcImage
				.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D rg = result.createGraphics();
		int maxScale = 1;
		for (int scale = maxScale; scale >= 1; scale /= 2) {
			BufferedImage srcImageCopy = scale(srcImage, 1.0 / scale);
			int width = srcImageCopy.getWidth();
			int height = srcImageCopy.getHeight();
			if (destImage == null || destImage.getWidth() != width
					|| destImage.getHeight() != height) {
				destImage = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_ARGB);
				src = new int[width * height];
				dest = new int[width * height];
			}
			srcImageCopy.getRGB(0, 0, width, height, src, 0, width);
			gray_hist(src, dest);
			System.arraycopy(dest, 0, src, 0, dest.length);
			blur(src, dest, width, height, 9);
			System.arraycopy(dest, 0, src, 0, dest.length);
			// threshold(src, dest, dark, light);
			// System.arraycopy(dest, 0, src, 0, dest.length);
			dither(src, dest, width, height, dark, light);
			destImage.setRGB(0, 0, width, height, dest, 0, width);
			rg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					(float) (Math.log(1 + (double) scale / maxScale) / Math
							.log(2))));
			rg.drawImage(destImage, 0, 0, result.getWidth(),
					result.getHeight(), null);
		}
		destImage = result;
		repaint();
	}

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
			int[] dark, int[] light) {
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
			dest[i] = pixels[i] < 0.5 ? dark[(int) (Math.random() * dark.length)]
					: light[(int) (Math.random() * light.length)];
			// dest[i] = pixels[i] > 0.5 ? Color.white.getRGB() : Color.black
			// .getRGB();
		}
	}
}
