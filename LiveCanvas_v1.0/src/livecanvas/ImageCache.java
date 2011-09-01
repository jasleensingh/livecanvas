package livecanvas;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class ImageCache {
	private static final Map<String, BufferedImage> cache = new HashMap<String, BufferedImage>();

	public static BufferedImage load(String filename) throws IOException {
		BufferedImage image;
		File file = new File(filename);
		image = cache.get(file.getAbsolutePath());
		if (image != null) {
			return image;
		}
		image = ImageIO.read(file);
		cache.put(file.getAbsolutePath(), image);
		return image;
	}
}
