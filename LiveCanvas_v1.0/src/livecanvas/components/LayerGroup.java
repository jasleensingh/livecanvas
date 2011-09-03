package livecanvas.components;

import java.awt.Graphics2D;

import livecanvas.Path;

public class LayerGroup extends Layer {
	public static enum CombineOp {
		None, Join, Subtract, Intersect
	}

	public final Layer layer1;
	public final Layer layer2;

	public LayerGroup(String name, Layer layer1, Layer layer2, CombineOp op) {
		super(name);
		this.layer1 = layer1;
		this.layer2 = layer2;
		switch (op) {
		default:
		case None:
			break;
		case Join:
			setPath(new Path(layer1.getPath()).join(layer2.getPath(),
					(layer1.getCenterZ() + layer2.getCenterZ()) / 2));
			break;
		case Intersect:
			setPath(new Path(layer1.getPath()).intersect(layer2.getPath(),
					(layer1.getCenterZ() + layer2.getCenterZ()) / 2));
			break;
		case Subtract:
			setPath(new Path(layer1.getPath()).subtract(layer2.getPath(),
					(layer1.getCenterZ() + layer2.getCenterZ()) / 2));
			break;
		}
	}

	public boolean isLeaf() {
		return false;
	}

	@Override
	public void draw(Graphics2D g, int width, int height, boolean drawBBox,
			boolean showMesh, boolean showControlPoints, boolean showBgRef) {
		if (getPath().count > 0) {
			super.draw(g, width, height, drawBBox, showMesh, showControlPoints,
					showBgRef);
		} else {
			layer1.draw(g, width, height, drawBBox, showMesh,
					showControlPoints, showBgRef);
			layer2.draw(g, width, height, drawBBox, showMesh,
					showControlPoints, showBgRef);
		}
	}
}
