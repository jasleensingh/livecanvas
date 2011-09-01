package livecanvas;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

import livecanvas.animator.Vertex;

public class Utils {
	public static final double EPSILON = 0.0001;

	public static boolean isZero(double n) {
		return Math.abs(n) < EPSILON;
	}

	public static boolean isEqual(double n, double cmp) {
		return Math.abs(n - cmp) < EPSILON;
	}

	public static final double angle_PItoPI(double angle) {
		angle = angle % (2 * Math.PI);
		if (angle >= Math.PI) {
			angle -= 2 * Math.PI;
		} else if (angle < -Math.PI) {
			angle += 2 * Math.PI;
		}
		return angle;
	}

	public static final double angle0to2PI(double angle) {
		angle = angle % (2 * Math.PI);
		if (angle < 0) {
			angle += 2 * Math.PI;
		}
		return angle;
	}

	public static void ensure(boolean b) throws RuntimeException {
		if (!b) {
			throw new RuntimeException("Assert failed!");
		}
	}
	public static String randomAlphaNum(int length) {
		byte[] buf = new byte[length];
		for (int i = 0; i < buf.length; i++) {
			int n = (int) (Math.random() * 62);
			if (n < 10) {
				buf[i] = (byte) (0x30 + n);
			} else if (n < 36) {
				n -= 10;
				buf[i] = (byte) (0x41 + n);
			} else {
				n -= 36;
				buf[i] = (byte) (0x61 + n);
			}
		}
		return new String(buf);
	}

	public static double toPrecision(double val, int scale) {
		return ((double) Math.round((val * scale))) / scale;
	}

	public static int[] toIntArray(Vertex[] array, int dim) {
		int[] array_int = new int[array.length];
		switch (dim) {
		case 0:
			for (int i = 0; i < array.length; i++) {
				array_int[i] = (int) array[i].x;
			}
			break;
		case 1:
			for (int i = 0; i < array.length; i++) {
				array_int[i] = (int) array[i].y;
			}
			break;
		case 2:
			for (int i = 0; i < array.length; i++) {
				array_int[i] = (int) array[i].z;
			}
			break;
		}
		return array_int;
	}

	public static int clamp(int val, int min, int max) {
		if (val < min) {
			return min;
		} else if (val > max) {
			return max;
		}
		return val;
	}

	public static double clamp(double val, double min, double max) {
		if (val < min) {
			return min;
		} else if (val > max) {
			return max;
		}
		return val;
	}

	public static Color doubleToColor(double val, double min, double max) {
		return new Color((int) (0xFFFFFF * (Utils.clamp(val, min, max) / max)));
	}

	public static void deleteOnExit(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				deleteOnExit(f);
			}
		}
		file.deleteOnExit();
	}

	public static void copy(Reader r, Writer w) throws IOException {
		char[] buf = new char[4096];
		int n;
		while ((n = r.read(buf)) > 0) {
			w.write(buf, 0, n);
		}
		w.flush();
	}

	public static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] buf = new byte[4096];
		int n;
		while ((n = in.read(buf)) > 0) {
			out.write(buf, 0, n);
		}
		out.flush();
	}

	public static double distance(double x1, double y1, double x2, double y2) {
		double dx = x1 - x2;
		double dy = y1 - y2;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public static <T> List<T> subtract(List<T> vector0, List<T> vector1) {
		List<T> vector = new LinkedList<T>();
		for (int i = 0; i < vector0.size(); i++) {
			T element = vector0.get(i);
			if (!vector1.contains(element))
				vector.add(element);
		}
		return vector;
	}

	public static String join(String sep, int[] args) {
		return join(sep, args, 0, args.length);
	}

	public static String join(String sep, int[] args, int fromIndex, int toIndex) {
		StringBuilder sb = new StringBuilder();
		for (int i = fromIndex; i < toIndex; i++) {
			sb.append((i > 0 ? sep : "") + args[i]);
		}
		return sb.toString();
	}

	public static String join(String sep, double[] args) {
		return join(sep, args, 0, args.length);
	}

	public static String join(String sep, double[] args, int fromIndex,
			int toIndex) {
		StringBuilder sb = new StringBuilder();
		for (int i = fromIndex; i < toIndex; i++) {
			sb.append((i > 0 ? sep : "") + args[i]);
		}
		return sb.toString();
	}

	public static String join(String sep, Object... args) {
		return join(sep, args, 0, args.length);
	}

	public static String join(String sep, Object[] args, int fromIndex,
			int toIndex) {
		StringBuilder sb = new StringBuilder();
		for (int i = fromIndex; i < toIndex; i++) {
			sb.append((i > 0 ? sep : "") + args[i]);
		}
		return sb.toString();
	}

	public static int[] splitIntArray(String sep, String string) {
		if ((string = string.trim()).length() == 0) {
			return new int[0];
		}
		String[] toks = string.split(sep);
		int c = 0;
		int[] array = new int[toks.length];
		for (String tok : toks) {
			array[c++] = Integer.parseInt(tok);
		}
		return array;
	}

	public static double[] splitDoubleArray(String sep, String string) {
		if ((string = string.trim()).length() == 0) {
			return new double[0];
		}
		String[] toks = string.split(sep);
		int c = 0;
		double[] array = new double[toks.length];
		for (String tok : toks) {
			array[c++] = Double.parseDouble(tok);
		}
		return array;
	}

	public static interface TokenConverter<T> {
		public T convert(String s);
	}

	public static <T> T[] splitArray(String sep, String string, Class clazz,
			TokenConverter<T> converter) {
		String[] toks = string.split(sep);
		int c = 0;
		T[] array = (T[]) Array.newInstance(clazz, toks.length);
		for (String tok : toks) {
			array[c++] = converter.convert(tok);
		}
		return array;
	}

	public static <T> List<T> findComponentsOfType(Container container,
			Class clazz) {
		List<T> list = new LinkedList<T>();
		for (Component c : container.getComponents()) {
			if (clazz.isAssignableFrom(c.getClass())) {
				list.add((T) c);
			}
			if (c instanceof Container) {
				list.addAll((List<T>) findComponentsOfType((Container) c, clazz));
			}
		}
		return list;
	}

	public static Point[] createPathFromPathIterator(PathIterator pathIterator) {
		PathIterator it = new FlatteningPathIterator(pathIterator, 1);
		List<Point> sampledPoints = new ArrayList<Point>();
		float sampled[] = new float[6];
		while (!it.isDone()) {
			switch (it.currentSegment(sampled)) {
			case PathIterator.SEG_MOVETO:
				sampledPoints
						.add(new Point((int) sampled[0], (int) sampled[1]));
				break;
			case PathIterator.SEG_CLOSE:
				sampled[0] = sampledPoints.get(0).x;
				sampled[1] = sampledPoints.get(0).y;
			case PathIterator.SEG_LINETO:
				sampledPoints
						.add(new Point((int) sampled[0], (int) sampled[1]));
				break;
			}
			it.next();
		}
		// remove consecutive points which are too close to each other
		int size = sampledPoints.size();
		for (int i = 0; i < size; i++) {
			Point p1 = sampledPoints.get(i);
			int i2 = (i + 1) % size;
			Point p2 = sampledPoints.get(i2);
			if (p1.equals(p2)) {
				sampledPoints.remove(i2);
				size--;
			}
		}
		if (sampledPoints.get(size - 1).equals(sampledPoints.get(0))) {
			sampledPoints.remove(size - 1);
		}
		Point[] pathPoints = sampledPoints.toArray(new Point[0]);
		return pathPoints;
	}

	public static Point[] subdivide(Point[] points) {
		return subdivide(points, 15);
	}

	public static Point[] subdivide(Point[] points, double step) {
		List<Point> path = new ArrayList<Point>();
		double dist = 0;
		Point last;
		path.add(last = points[0]);
		for (int i = 1; i < points.length; i++) {
			Point p = points[i];
			int dx = p.x - last.x;
			int dy = p.y - last.y;
			dist += Math.sqrt(dx * dx + dy * dy);
			while (dist >= step) {
				Point interp = new Point();
				interp.x = (int) (p.x - dx * (dist - step) / step);
				interp.y = (int) (p.y - dy * (dist - step) / step);
				path.add(interp);
				dist -= step;
			}
			last = p;
		}
		// if (!points[points.length - 1].equals(points[0])) {
		// path.add(points[points.length - 1]);
		// }
		return path.toArray(new Point[0]);
	}

	public static Vertex[] subdivide(Vertex[] vs) {
		return subdivide(vs, 15);
	}

	public static Vertex[] subdivide(Vertex[] vs, double step) {
		List<Vertex> path = new ArrayList<Vertex>();
		double dist = 0;
		Vertex last;
		path.add(last = vs[0]);
		for (int i = 1; i < vs.length; i++) {
			Vertex v = vs[i];
			double dx = v.x - last.x;
			double dy = v.y - last.y;
			double dz = v.z - last.z;
			dist += Math.sqrt(dx * dx + dy * dy + dz * dz);
			while (dist > step) {
				Vertex interp = new Vertex();
				interp.x = v.x - dx * (dist - step) / step;
				interp.y = v.y - dy * (dist - step) / step;
				interp.z = v.z - dz * (dist - step) / step;
				interp.onPath = v.onPath;
				path.add(interp);
				dist -= step;
			}
			last = v;
		}
		path.add(last);
		return path.toArray(new Vertex[0]);
	}

	public static enum ButtonType {
		TEXT_ONLY, ICON_ONLY, TEXT_AND_ICON
	};

	private static class MyButton extends JButton {
		public MyButton(String text, Icon icon, ButtonType type, String desc,
				int action, ActionListener al) {
			super((type == ButtonType.TEXT_ONLY
					|| type == ButtonType.TEXT_AND_ICON ? text : ""),
					(type == ButtonType.ICON_ONLY
							|| type == ButtonType.TEXT_AND_ICON ? icon : null));
			setName(text);
			setToolTipText(desc);
			setFocusable(false);
			setMargin(new Insets(5, 5, 5, 5));
			setActionCommand("" + action);
			addActionListener(al);
		}
	}

	private static class MyToggleButton extends JToggleButton {
		public MyToggleButton(String text, Icon icon, ButtonType type,
				int action, String desc, ButtonGroup bg, ActionListener al) {
			super((type == ButtonType.TEXT_ONLY
					|| type == ButtonType.TEXT_AND_ICON ? text : ""),
					(type == ButtonType.ICON_ONLY
							|| type == ButtonType.TEXT_AND_ICON ? icon : null));
			setName(text);
			setToolTipText(desc);
			setFocusable(false);
			setMargin(new Insets(5, 5, 5, 5));
			setActionCommand("" + action);
			addActionListener(al);
			if (bg != null) {
				bg.add(this);
			}
		}
	}

	public static JButton createToolBarButton(String text, Icon icon,
			ButtonType type, int action, String desc, ActionListener al) {
		return new MyButton(text, icon, type, desc, action, al);
	}

	public static JToggleButton createToolBarToggleButton(String text,
			Icon icon, ButtonType type, int action, String desc,
			ButtonGroup bg, ActionListener al) {
		return new MyToggleButton(text, icon, type, action, desc, bg, al);
	}

	public static JMenuItem createMenuItem(String text, int action,
			int mnemonic, String keystroke, ActionListener al) {
		JMenuItem menuItem = new JMenuItem(text);
		menuItem.setName(text);
		menuItem.setMnemonic(mnemonic);
		menuItem.setActionCommand("" + action);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(keystroke));
		menuItem.addActionListener(al);
		return menuItem;
	}

	public static JCheckBoxMenuItem createCheckBoxMenuItem(String text,
			int action, int mnemonic, String keystroke, ActionListener al) {
		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(text);
		menuItem.setName(text);
		menuItem.setMnemonic(mnemonic);
		menuItem.setActionCommand("" + action);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(keystroke));
		menuItem.addActionListener(al);
		return menuItem;
	}
}
