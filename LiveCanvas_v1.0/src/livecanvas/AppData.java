package livecanvas;

import java.io.File;

public class AppData {

	private static File base_dir = new File(System.getProperty("user.home")
			+ "/.LiveCanvas");
	static {
		if (!base_dir.exists()) {
			if (!base_dir.mkdir()) {
				System.err
						.println("Unable to create AppData base directory, app may not function properly");
			}
		}
	}

	public static class Store {
		public final String name;

		private File store_dir;

		public Store(String name) {
			this.name = name;
			store_dir = new File(base_dir + "/" + name);
			if (!store_dir.exists()) {
				if (!store_dir.mkdirs()) {
					System.err.println("Error creating store directory: "
							+ store_dir);
				}
			}
		}

		public File getFile(String rel_path) {
			File file = new File(base_dir + "/" + name + "/" + rel_path);
			if (!file.getParentFile().exists()) {
				if (!file.getParentFile().mkdirs()) {
					System.err.println("Error creating store data directory: "
							+ file.getParentFile());
				}
			}
			return file;
		}

		public File[] getFiles(String rel_path) {
			File file = new File(base_dir + "/" + name + "/" + rel_path);
			return file.listFiles();
		}

		public File getDir(String rel_path) {
			File file = new File(base_dir + "/" + name + "/" + rel_path);
			if (!file.exists()) {
				if (!file.mkdirs()) {
					System.err.println("Error creating store data directory: "
							+ file);
				}
			}
			return file;
		}
	}
}
