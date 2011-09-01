package livecanvas;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import livecanvas.Tool.Pointer.PointerHandler;
import livecanvas.animator.OnionSkin;
import livecanvas.components.Layer;

public class CanvasMesh extends Canvas {
	public static interface Listener {
		public void meshEdited(int mode, Layer layer);
	}

	public static MeshHandler MeshHandler = new MeshHandler.Rigid();

	private Layer currLayer;
	private List<Listener> listeners;
	private boolean showMesh;
	private boolean seeThrough;
	private OnionSkin onionSkin;
	private PointerHandler pointerHandler;

	public CanvasMesh(int width, int height, PointerHandler pointerHandler) {
		super(width, height);
		this.listeners = new LinkedList<Listener>();
		this.pointerHandler = pointerHandler;
		showMesh = true;
		seeThrough = true;
	}

	protected void addTools() {
		toolsMap.put(TOOLS_BRUSH, new Tool.Brush(this) {
			@Override
			public boolean onSelected(ToolContext toolContext) {
				if (!super.onSelected(toolContext)) {
					return false;
				}
				return canDrawMesh();
			}
		});
		toolsMap.put(TOOLS_PEN, new Tool.Pen(this) {
			@Override
			public boolean onSelected(ToolContext toolContext) {
				if (!super.onSelected(toolContext)) {
					return false;
				}
				return canDrawMesh();
			}
		});
		toolsMap.put(TOOLS_MAGICWAND, new Tool.MagicWand(this) {
			@Override
			public boolean onSelected(ToolContext toolContext) {
				if (!super.onSelected(toolContext)) {
					return false;
				}
				return canDrawMesh();
			}
		});
		toolsMap.put(TOOLS_POINTER, new Tool.Pointer(this, pointerHandler));
		toolsMap.put(TOOLS_PANZOOM, new Tool.PanZoom(this));
		toolsMap.put(TOOLS_SETCONTROLPOINTS, new Tool.SetControlPoints(this));
	}

	private boolean canDrawMesh() {
		if (currLayer.getName().equals("ROOT")) {
			JOptionPane.showMessageDialog(canvasContainer,
					"Cannot draw mesh on root layer!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (!currLayer.isLeaf()) {
			JOptionPane.showMessageDialog(canvasContainer,
					"Cannot draw mesh on a layer group!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	@Override
	public void paint(Graphics2D g, int width, int height) {
		super.paint(g, width, height);
		if (onionSkin != null) {
			onionSkin.paint(g);
		}
		paintLayer(g, width, height, currLayer);
	}

	public void paintLayer(Graphics2D g, int width, int height, Layer layer) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		if (seeThrough) {
			drawAll(g, layer.getRoot(), layer);
		} else {
			layer.draw(g, width, height,
					getSelectedToolType() == TOOLS_POINTER, showMesh, true);
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	private static AlphaComposite sLighten = AlphaComposite.getInstance(
			AlphaComposite.SRC_OVER, 0.25f);

	private void drawAll(Graphics2D g, Layer layer, Layer curr) {
		if (layer == curr) {
			layer.draw(g, getWidth(), getHeight(),
					getSelectedToolType() == TOOLS_POINTER, showMesh, true);
		} else {
			Composite c = g.getComposite();
			g.setComposite(sLighten);
			layer.draw(g, getWidth(), getHeight(), false, showMesh, false);
			g.setComposite(c);
		}
		for (Layer subLayer : layer.getSubLayers()) {
			drawAll(g, subLayer, curr);
		}
	}

	public void pathCreated(Point[] pathPoints) {
		Path path = currLayer.getPath();
		double z = currLayer.getParent().getCenterZ() + 100;
		path.clear();
		for (Point p : pathPoints) {
			path.add(p.x, p.y, z);
		}
		Rectangle bounds = path.getBounds();
		currLayer.setCenter(bounds.getCenterX(), bounds.getCenterY(), z);
		path.finalizePath();
		fireMeshEdit(MESH_DRAW);
	}

	public void requestImagePaint(Tool t) {
		repaint();
	}

	public void initializeTransform() {
		MeshHandler.initializeTransform(currLayer.getRoot());
	}

	public void updateTransform() {
		MeshHandler.updateTransform(currLayer.getRoot());
	}

	public void addListener(Listener l) {
		listeners.add(l);
	}

	public void removeListener(Listener l) {
		listeners.remove(l);
	}

	public void fireMeshEdit(int mode) {
		for (Listener l : listeners) {
			l.meshEdited(mode, currLayer);
		}
	}

	public boolean canSave() {
		return true;
	}

	public void clear() {
		MeshHandler.clearTransform();
	}

	public Layer getCurrLayer() {
		return currLayer;
	}

	public void setCurrLayer(Layer currLayer) {
		this.currLayer = currLayer;
	}

	public boolean isShowMesh() {
		return showMesh;
	}

	public void setShowMesh(boolean showMesh) {
		this.showMesh = showMesh;
		repaint();
	}

	public boolean isSeeThrough() {
		return seeThrough;
	}

	public void setSeeThrough(boolean seeThrough) {
		this.seeThrough = seeThrough;
		repaint();
	}

	public OnionSkin getOnionSkin() {
		return onionSkin;
	}

	public void setOnionSkin(OnionSkin onionSkin) {
		this.onionSkin = onionSkin;
	}
}
