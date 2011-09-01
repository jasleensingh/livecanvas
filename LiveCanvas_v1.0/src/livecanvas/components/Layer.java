package livecanvas.components;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.LinkedList;
import java.util.List;

import livecanvas.BackgroundRef;
import livecanvas.Canvas;
import livecanvas.Constants;
import livecanvas.Path;
import livecanvas.Transform3;
import livecanvas.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import common.typeutils.DoubleType;

public class Layer implements Constants {
	public static enum Visibility {
		VISIBLE, INVISIBLE, GONE
	}

	public static enum Pivot {
		CENTER, TOPLEFT, CUSTOM
	};

	@DoubleType(name = "Center X")
	public double centerX;
	@DoubleType(name = "Center Y")
	public double centerY;
	@DoubleType(name = "Center Z")
	public double centerZ;

	protected String name;
	protected List<Layer> subLayers = new LinkedList<Layer>();
	protected Viewpoint[][] viewpoints = new Viewpoint[ANGLE_DIVISIONS][ANGLE_DIVISIONS];
	protected int currViewpointX, currViewpointY;
	protected Layer parent;
	protected Canvas canvas;
	protected Visibility visibility;
	protected BackgroundRef backgroundRef;
	protected boolean backgroundRefVisible = true;

	public Layer(String name) {
		this.name = name;
		visibility = Visibility.VISIBLE;
		createViewpoints();
		currViewpointX = currViewpointY = 0;
	}

	private void createViewpoints() {
		for (int i = 0; i < ANGLE_DIVISIONS; i++) {
			for (int j = 0; j < ANGLE_DIVISIONS; j++) {
				viewpoints[i][j] = new Viewpoint(i - ANGLE_DIVISIONS2, j
						- ANGLE_DIVISIONS2);
			}
		}
	}

	public Layer clone() {
		Layer clone = new Layer(name + " - Copy");
		Path path = getPath();
		Path pathClone = new Path(path);
		if (path.isFinalized()) {
			pathClone.finalizePath();
		}
		clone.setPath(pathClone);
		return clone;
	}

	public boolean isLeaf() {
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getCenterX() {
		return centerX;
	}

	public void setCenterX(double centerX) {
		moveCenter(centerX, centerY, centerZ);
	}

	public double getCenterY() {
		return centerY;
	}

	public void setCenterY(double centerY) {
		this.centerY = centerY;
		moveCenter(centerX, centerY, centerZ);
	}

	public double getCenterZ() {
		return centerZ;
	}

	public void setCenterZ(double centerZ) {
		this.centerZ = centerZ;
		moveCenter(centerX, centerY, centerZ);
	}

	public void setCenter(double centerX, double centerY, double centerZ) {
		this.centerX = centerX;
		this.centerY = centerY;
		this.centerZ = centerZ;
	}

	public void moveCenter(double centerX, double centerY, double centerZ) {
		translate(centerX - this.centerX, centerY - this.centerY, 0);
	}

	private void updateCenter() {
		Rectangle bounds = getPath().getBounds();
		setCenter(bounds.getCenterX(), bounds.getCenterY(), centerZ);
	}

	private static final int SearchDirection_Left = 0,
			SearchDirection_Right = 1, SearchDirection_Up = 2,
			SearchDirection_Down = 3;
	private static final Point[] SearchStep = { new Point(-1, 0),
			new Point(1, 0), new Point(0, -1), new Point(0, 1) };

	private Viewpoint findNearestViewpoint(int dir) {
		int dx = SearchStep[dir].x;
		int dy = SearchStep[dir].y;
		for (int i = 0; i < ANGLE_DIVISIONS; i++) {
			int vx = (currViewpointX + ANGLE_DIVISIONS2 + dx * i + ANGLE_DIVISIONS)
					% ANGLE_DIVISIONS;
			int vy = (currViewpointY + ANGLE_DIVISIONS2 + dy * i + ANGLE_DIVISIONS)
					% ANGLE_DIVISIONS;
			Viewpoint vp = viewpoints[vx][vy];
			if (!vp.isGenerated()) {
				return vp;
			}
		}
		return null;
	}

	private void generatePath(Viewpoint vp) {
		double pathAngleX = currViewpointX * ANGLE_STEPSIZE;
		Transform3 t;
		Viewpoint left = findNearestViewpoint(SearchDirection_Left);
		Viewpoint right = findNearestViewpoint(SearchDirection_Right);
		Viewpoint up = findNearestViewpoint(SearchDirection_Up);
		Viewpoint down = findNearestViewpoint(SearchDirection_Down);
		Path interpolatedPathX = null;
		Path interpolatedPathY = null;
		if (left != null) {
			double leftPathAngle = left.viewpointX * ANGLE_STEPSIZE;
			double angleDiffLeft = Utils
					.angle0to2PI(pathAngleX - leftPathAngle);
			t = new Transform3().setRotateY(angleDiffLeft);
			Path leftPathRotated = new Path(left.getPath());
			leftPathRotated.applyTransform(t);
			double rightPathAngle = right.viewpointX * ANGLE_STEPSIZE;
			double angleDiffRight = Utils.angle0to2PI(rightPathAngle
					- pathAngleX);
			t = new Transform3().setRotateY(angleDiffRight);
			Path rightPathRotated = new Path(right.getPath());
			rightPathRotated.applyTransform(t);
			double interpolationX = angleDiffLeft
					/ (angleDiffLeft + angleDiffRight);
			interpolatedPathX = new Path(leftPathRotated, rightPathRotated,
					interpolationX);
		}
		if (up != null) {
			double pathAngleY = currViewpointY * ANGLE_STEPSIZE;
			double upPathAngle = up.viewpointY * ANGLE_STEPSIZE;
			double angleDiffUp = Utils.angle0to2PI(pathAngleY - upPathAngle);
			t = new Transform3().setRotateX(angleDiffUp);
			Path upPathRotated = new Path(up.getPath());
			upPathRotated.applyTransform(t);
			double downPathAngle = down.viewpointY * ANGLE_STEPSIZE;
			double angleDiffDown = Utils
					.angle0to2PI(downPathAngle - pathAngleY);
			t = new Transform3().setRotateX(angleDiffDown);
			Path downPathRotated = new Path(down.getPath());
			downPathRotated.applyTransform(t);
			double interpolationY = angleDiffUp / (angleDiffUp + angleDiffDown);
			interpolatedPathY = new Path(upPathRotated, downPathRotated,
					interpolationY);
		}
		Path interpolatedPath;
		if (interpolatedPathX != null && interpolatedPathY != null) {
			interpolatedPath = new Path(interpolatedPathX, interpolatedPathY,
					0.5);
		} else if (interpolatedPathX != null) {
			interpolatedPath = interpolatedPathX;
		} else if (interpolatedPathY != null) {
			interpolatedPath = interpolatedPathY;
		} else {
			throw new RuntimeException("Unexpected: no valid path found");
		}
		vp.setPath(interpolatedPath);
	}

	public Viewpoint[][] getViewpoints() {
		return viewpoints;
	}

	public Viewpoint getCurrentViewpoint() {
		return viewpoints[currViewpointX + ANGLE_DIVISIONS2][currViewpointY
				+ ANGLE_DIVISIONS2];
	}

	public void setCurrViewpoint(int vx, int vy) {
		if (currViewpointX == vx && currViewpointY == vy) {
			return;
		}
		this.currViewpointX = vx;
		this.currViewpointY = vy;
		Viewpoint vp = getCurrentViewpoint();
		if (vp.isGenerated()) {
			generatePath(vp);
		}
	}

	public boolean isDefaultViewpoint() {
		return currViewpointX == 0 && currViewpointY == 0;
	}

	public void translate(double dx, double dy, double dz) {
		// XXX: Change this to use interpolation between different viewpoints
		Path path = getPath();
		path.applyTransform(new Transform3().setTranslate(dx, dy, dz));
		for (Layer subLayer : subLayers) {
			subLayer.translate(dx, dy, dz);
		}
		updateCenter();
	}

	public void rotateZ(double radians, double px, double py, double pz) {
		if (!isDefaultViewpoint()) {
			throw new RuntimeException(
					"Can rotate about Z only in default view");
		}
		// XXX: Change this to use interpolation between different viewpoints
		Path path = getPath();
		path.applyTransform(new Transform3().setRotateZ(radians, px, py, pz));
		for (Layer subLayer : subLayers) {
			subLayer.rotateZ(radians, px, py, pz);
		}
		updateCenter();
	}

	// public void rotateY(double radians, double px, double py, double pz) {
	// // XXX: Change this to use interpolation between different viewpoints
	// centerRotY = Utils.angle_PItoPI(centerRotY + radians);
	// setCurrViewpoint(
	// (int) ((centerRotY + ANGLE_STEPSIZE / 2) / ANGLE_STEPSIZE),
	// currViewpointY);
	// Path path = getPath();
	// path.applyTransform(new Transform3().setRotateY(radians, px, py, pz));
	// for (Layer subLayer : subLayers) {
	// subLayer.rotateY(radians, px, py, pz);
	// }
	// updateCenter();
	// }
	//
	// public void rotateX(double radians, double px, double py, double pz) {
	// // XXX: Change this to use interpolation between different viewpoints
	// centerRotX = Utils.angle_PItoPI(centerRotX + radians);
	// setCurrViewpoint(currViewpointX,
	// (int) ((centerRotX + ANGLE_STEPSIZE / 2) / ANGLE_STEPSIZE));
	// Path path = getPath();
	// path.applyTransform(new Transform3().setRotateX(radians, px, py, pz));
	// for (Layer subLayer : subLayers) {
	// subLayer.rotateX(radians, px, py, pz);
	// }
	// updateCenter();
	// }

	public void scale(double sx, double sy, double sz, double px, double py,
			double pz) {
		// XXX: Change this to use interpolation between different viewpoints
		Path path = getPath();
		path.applyTransform(new Transform3().setScale(sx, sy, sz, px, py, pz));
		for (Layer subLayer : subLayers) {
			subLayer.scale(sx, sy, sz, px, py, pz);
		}
		updateCenter();
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}

	public int indexOf(Layer layer) {
		for (int i = 0; i < subLayers.size(); i++) {
			if (subLayers.get(i) == layer) {
				return i;
			}
		}
		return -1;
	}

	public void addSubLayer(Layer layer) {
		Layer parent;
		if ((parent = layer.getParent()) != null) {
			parent.removeSubLayer(layer);
		}
		subLayers.add(layer);
		layer.setParent(this);
	}

	public void addSubLayer(int index, Layer layer) {
		Layer parent;
		if ((parent = layer.getParent()) != null) {
			parent.removeSubLayer(layer);
		}
		subLayers.add(index, layer);
		layer.setParent(this);
	}

	public void removeSubLayer(Layer layer) {
		layer.setParent(null);
		subLayers.remove(layer);
	}

	public void replaceSubLayer(Layer replace, Layer with) {
		int index = subLayers.indexOf(replace);
		if (index < 0) {
			return;
		}
		Layer parent = replace.getParent();
		removeSubLayer(replace);
		parent.addSubLayer(index, with);
	}

	public List<Layer> getSubLayers() {
		return subLayers;
	}

	public List<Layer> getSubLayersRecursively() {
		List<Layer> list = new LinkedList<Layer>();
		for (Layer subLayer : subLayers) {
			list.add(subLayer);
			list.addAll(subLayer.getSubLayersRecursively());
		}
		return list;
	}

	public Layer getParent() {
		return parent;
	}

	public void setParent(Layer parent) {
		this.parent = parent;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}

	public Path getPath() {
		return getPath(currViewpointX, currViewpointY);
	}

	public Path getPath(int viewpointX, int viewpointY) {
		return viewpoints[viewpointX + 8][viewpointY + 8].getPath();
	}

	public void setPath(Path path) {
		setPath(currViewpointX, currViewpointY, path);
	}

	public void setPath(int viewpointX, int viewpointY, Path path) {
		viewpoints[viewpointX + 8][viewpointY + 8].setPath(path);
	}

	public BackgroundRef getBackgroundRef() {
		return backgroundRef;
	}

	public BackgroundRef findBackgroundRef() {
		if (backgroundRef != null && isBackgroundRefVisible()) {
			return backgroundRef;
		}
		if (parent != null) {
			return parent.findBackgroundRef();
		}
		return null;
	}

	public void setBackgroundRef(BackgroundRef bgref) {
		backgroundRef = bgref;
		if (backgroundRef != null) {
			backgroundRef.setLayer(this);
		}
	}

	public boolean isBackgroundRefVisible() {
		return backgroundRefVisible;
	}

	public void setBackgroundRefVisible(boolean backgroundRefVisible) {
		this.backgroundRefVisible = backgroundRefVisible;
	}

	public Layer getRoot() {
		return parent == null ? this : parent.getRoot();
	}

	private Stroke bboxStroke = new BasicStroke(2.0f);
	private Stroke transformWidgetStroke = new BasicStroke(1.0f);

	public void draw(Graphics2D g, int width, int height, boolean drawBBox,
			boolean showMesh, boolean showBgRef) {
		if (visibility != Visibility.VISIBLE) {
			return;
		}
		Path path = getPath();
		if (showBgRef) {
			BackgroundRef bgref = findBackgroundRef();
			if (bgref != null) {
				Composite c = g.getComposite();
				g.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, 0.5f));
				bgref.draw(g);
				g.setComposite(c);
			}
		}
		if (path != null) {
			path.draw(g, showMesh);
			if (drawBBox) {
				g.setColor(Color.red);
				g.setStroke(bboxStroke);
				Rectangle bounds = path.getBounds();
				g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
				g.setColor(new Color(0, 0, 0, 0x80));
				g.setStroke(transformWidgetStroke);
				int cx = (int) bounds.getCenterX();
				int cy = (int) bounds.getCenterY();
				// for (int i = 0; i < sActionRadius.length; i++) {
				// int r = (int) sActionRadius[i];
				// g.fillOval(cx - r, cy - r, r * 2, r * 2);
				// }
			}
		}
	}

	public JSONObject toJSON() throws JSONException {
		System.err.println("toJSON: " + getName());
		JSONObject json = new JSONObject();
		json.put("name", name);
		if (backgroundRef != null) {
			json.put("backgroundRef", backgroundRef.toJSON());
		}
		JSONArray jsonViewpoints = new JSONArray();
		for (int i = 0; i < viewpoints.length; i++) {
			for (int j = 0; j < viewpoints[i].length; j++) {
				jsonViewpoints.put(viewpoints[i][j].toJSON());
			}
		}
		json.put("viewpoints", jsonViewpoints);
		JSONArray subLayersJSONArray = new JSONArray();
		for (Layer subLayer : subLayers) {
			subLayersJSONArray.put(subLayer.toJSON());
		}
		json.put("subLayers", subLayersJSONArray);
		return json;
	}

	public static Layer fromJSON(JSONObject json) throws JSONException {
		Layer layer = new Layer(json.getString("name"));
		JSONObject bgrefJSON = (JSONObject) json.opt("backgroundRef");
		if (bgrefJSON != null) {
			layer.setBackgroundRef(BackgroundRef.fromJSON(bgrefJSON));
		}
		JSONArray jsonViewpoints = json.getJSONArray("viewpoints");
		for (int i = 0; i < jsonViewpoints.length(); i++) {
			Viewpoint v = Viewpoint.fromJSON(jsonViewpoints.getJSONObject(i));
			layer.viewpoints[v.viewpointX + ANGLE_DIVISIONS2][v.viewpointY
					+ ANGLE_DIVISIONS2] = v;
		}
		JSONArray subLayersJSONArray = json.getJSONArray("subLayers");
		for (int i = 0; i < subLayersJSONArray.length(); i++) {
			layer.addSubLayer(fromJSON(subLayersJSONArray.getJSONObject(i)));
		}
		return layer;
	}
}
