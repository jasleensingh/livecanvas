package livecanvas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import common.typeutils.BooleanType;
import common.typeutils.ColorType;
import common.typeutils.IntegerType;

public class Canvas implements Constants {
	protected CanvasSettings settings;
	protected final Map<Integer, Tool> toolsMap;
	protected int selectedToolType = -1;
	protected Tool selectedTool;
	protected CanvasContainer canvasContainer;

	public Canvas(int width, int height) {
		settings = new CanvasSettings() {
			public void copyFrom(Settings copy) {
				super.copyFrom(copy);
				canvasContainer.layoutCanvas();
			}
		};
		settings.width = width;
		settings.height = height;
		toolsMap = new HashMap<Integer, Tool>();
	}

	protected void init() {
		addTools();
	}

	protected void addTools() {
	}

	public int getWidth() {
		return settings.width;
	}

	public void setWidth(int width) {
		settings.width = width;
		setSize(width, settings.height);
	}

	public int getHeight() {
		return settings.height;
	}

	public void setHeight(int height) {
		settings.height = height;
		setSize(settings.width, height);
	}

	public void setSize(int width, int height) {
		settings.width = width;
		settings.height = height;
		canvasContainer.layoutCanvas();
	}

	public CanvasSettings getSettings() {
		return settings;
	}

	public CanvasContainer getCanvasContainer() {
		return canvasContainer;
	}

	public void setCanvasContainer(CanvasContainer canvasContainer) {
		this.canvasContainer = canvasContainer;
	}

	public int getSelectedToolType() {
		return selectedToolType;
	}

	public void setSelectedToolType(int selectedToolType) {
		this.selectedToolType = selectedToolType;
		selectedTool = toolsMap.get(selectedToolType);
	}

	public Tool getSelectedTool() {
		return selectedTool;
	}

	public void setSelectedTool(Tool selectedTool) {
		this.selectedTool = selectedTool;
	}

	public void repaint() {
		if (canvasContainer != null) {
			canvasContainer.repaint();
		}
	}

	public void paint(Graphics2D g, int width, int height) {
		selectedTool.paint(g);
	}

	public void pathCreated(Point[] path) {
	}

	public void requestImagePaint(Tool t) {
	}

	public void mouseClicked(MouseEvent e) {
		selectedTool.mouseClicked(e);
	}

	public void mousePressed(MouseEvent e) {
		selectedTool.mousePressed(e);
	}

	public void mouseReleased(MouseEvent e) {
		selectedTool.mouseReleased(e);
	}

	public void mouseDragged(MouseEvent e) {
		selectedTool.mouseDragged(e);
	}

	public void mouseMoved(MouseEvent e) {
		selectedTool.mouseMoved(e);
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
		}
		repaint();
	}

	public static class CanvasSettings extends Settings {
		public static final String CANVAS = "Canvas";

		@IntegerType(name = "Width", category = CANVAS)
		public int width;

		@IntegerType(name = "Height", category = CANVAS)
		public int height;

		@BooleanType(name = "Opaque", category = CANVAS)
		public boolean canvasOpaque = true;

		@ColorType(name = "Background", category = CANVAS)
		public Color canvasBackgroundColor = Color.white;

		@Override
		public CanvasSettings clone() {
			CanvasSettings clone = new CanvasSettings();
			clone.width = width;
			clone.height = height;
			clone.canvasOpaque = canvasOpaque;
			clone.canvasBackgroundColor = canvasBackgroundColor;
			return clone;
		}

		@Override
		public void copyFrom(Settings copy) {
			CanvasSettings s = (CanvasSettings) copy;
			width = s.width;
			height = s.height;
			canvasOpaque = s.canvasOpaque;
			canvasBackgroundColor = s.canvasBackgroundColor;
		}

		@Override
		public String[] getCategories() {
			return new String[] { CANVAS };
		}
	}
}
