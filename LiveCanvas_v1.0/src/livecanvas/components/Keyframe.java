package livecanvas.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import livecanvas.BackgroundRef;
import livecanvas.ControlPoint;
import livecanvas.Mesh;
import livecanvas.Path;
import livecanvas.Vec3;
import livecanvas.animator.Interpolator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Keyframe {
	private int frameNumber;
	private Interpolator interpolator = new Interpolator();

	private static class LayerInfo {
		public Vec3[] controlPointVertexLocations;
		public int backingIndex = -1;

		public LayerInfo() {
		}

		public LayerInfo(Vec3[] controlPointVertexLocations, int backingIndex) {
			this.controlPointVertexLocations = controlPointVertexLocations;
			this.backingIndex = backingIndex;
		}

		public JSONObject toJSON() throws JSONException {
			JSONObject json = new JSONObject();
			JSONArray jsonControlPointVertexLocations = new JSONArray();
			for (Vec3 v : controlPointVertexLocations) {
				jsonControlPointVertexLocations.put(v.toJSON());
			}
			json.put("controlPointVertexLocations",
					jsonControlPointVertexLocations);
			json.put("backingIndex", backingIndex);
			return json;
		}

		public static LayerInfo fromJSON(JSONObject json) throws JSONException {
			JSONArray jsonControlPointVertexLocations = json
					.getJSONArray("controlPointVertexLocations");
			Vec3[] vs = new Vec3[jsonControlPointVertexLocations.length()];
			for (int j = 0; j < vs.length; j++) {
				vs[j] = Vec3.fromJSON(jsonControlPointVertexLocations
						.getJSONObject(j));
			}
			int backingIndex = json.getInt("backingIndex");
			LayerInfo layerInfo = new LayerInfo(vs, backingIndex);
			return layerInfo;
		}
	}

	private Map<String, LayerInfo> layerInfoMap;

	private static final int THUMB_SIZE = 80;
	private BufferedImage thumbnail;
	public static Keyframe selectedKeyframe;

	public Keyframe() {
		this(-1);
	}

	public Keyframe(int frameNumber) {
		this.frameNumber = frameNumber;
		thumbnail = new BufferedImage(THUMB_SIZE, THUMB_SIZE,
				BufferedImage.TYPE_INT_ARGB);
		layerInfoMap = new HashMap<String, LayerInfo>();
	}

	public int getFrameNumber() {
		return frameNumber;
	}

	public void setFrameNumber(int frameNumber) {
		this.frameNumber = frameNumber;
	}

	public Interpolator getInterpolator() {
		return interpolator;
	}

	public void setInterpolator(Interpolator interpolator) {
		this.interpolator = interpolator;
	}

	public boolean isSelected() {
		return selectedKeyframe == this;
	}

	public void setSelected(boolean selected) {
		if (selected && !isSelected()) {
			selectedKeyframe = this;
		} else if (!selected && isSelected()) {
			selectedKeyframe = null;
		}
	}

	private String generateKey(String layerName, int vx, int vy) {
		return layerName + ";" + vx + "," + vy;
	}

	public Vec3[] getControlPointVertexLocations(Layer layer) {
		int vx = layer.getCurrentViewpoint().viewpointX
				+ Layer.ANGLE_DIVISIONS2;
		int vy = layer.getCurrentViewpoint().viewpointY
				+ Layer.ANGLE_DIVISIONS2;
		return getControlPointVertexLocations(layer, vx, vy);
	}

	public Vec3[] getControlPointVertexLocations(Layer layer, int vx, int vy) {
		String key = generateKey(layer.getName(), vx, vy);
		LayerInfo layerInfo = layerInfoMap.get(key);
		return layerInfo == null ? null : layerInfo.controlPointVertexLocations;
	}

	public void setControlPointVertexLocations(Layer layer, int vx, int vy,
			Vec3[] cpv) {
		String key = generateKey(layer.getName(), vx, vy);
		LayerInfo layerInfo = layerInfoMap.get(key);
		if (layerInfo == null) {
			layerInfo = new LayerInfo();
			layerInfoMap.put(key, layerInfo);
		}
		layerInfo.controlPointVertexLocations = cpv;
	}

	public int getBackgroundRefBackingIndex(Layer layer) {
		int vx = layer.getCurrentViewpoint().viewpointX
				+ Layer.ANGLE_DIVISIONS2;
		int vy = layer.getCurrentViewpoint().viewpointY
				+ Layer.ANGLE_DIVISIONS2;
		return getBackgroundRefBackingIndex(layer, vx, vy);
	}

	public int getBackgroundRefBackingIndex(Layer layer, int vx, int vy) {
		String key = generateKey(layer.getName(), vx, vy);
		LayerInfo layerInfo = layerInfoMap.get(key);
		return layerInfo == null ? 0 : layerInfo.backingIndex;
	}

	public void setBackgroundRefBackingIndex(Layer layer, int vx, int vy,
			int index) {
		String key = generateKey(layer.getName(), vx, vy);
		LayerInfo layerInfo = layerInfoMap.get(key);
		if (layerInfo == null) {
			layerInfo = new LayerInfo();
			layerInfoMap.put(key, layerInfo);
		}
		layerInfo.backingIndex = index;
	}

	public void meshEdited(Layer layer) {
		saveLayerInfo(layer.getRoot());
		drawThumbnail(layer.getRoot());
	}

	private void saveLayerInfo(Layer layer) {
		Viewpoint[][] viewpoints = layer.getViewpoints();
		for (int vx = 0; vx < viewpoints.length; vx++) {
			for (int vy = 0; vy < viewpoints[vx].length; vy++) {
				Path path = viewpoints[vx][vy].getPath();
				if (path.isFinalized()) {
					BackgroundRef bgref = layer.getBackgroundRef();
					if (bgref != null) {
						setBackgroundRefBackingIndex(layer, vx, vy,
								bgref.getBackingIndex());
					}
					Mesh mesh = path.getMesh();
					List<ControlPoint> cps = mesh.getControlPoints();
					Vec3[] controlPointVertexLocations = getControlPointVertexLocations(
							layer, vx, vy);
					if (controlPointVertexLocations == null) {
						controlPointVertexLocations = new Vec3[cps.size()];
						for (int i = 0; i < controlPointVertexLocations.length; i++) {
							controlPointVertexLocations[i] = new Vec3();
						}
						setControlPointVertexLocations(layer, vx, vy,
								controlPointVertexLocations);
					}
					for (int i = 0; i < cps.size(); i++) {
						controlPointVertexLocations[i].set(mesh
								.getControlPointVertex(cps.get(i)));
					}
				}
			}
		}
		for (Layer subLayer : layer.getSubLayers()) {
			saveLayerInfo(subLayer);
		}
	}

	private Rectangle getBoundsAll(Layer layer) {
		Rectangle bounds = layer.getPath().getBounds();
		for (Layer subLayer : layer.getSubLayers()) {
			bounds.add(getBoundsAll(subLayer));
		}
		return bounds;
	}

	private void drawAll(Graphics2D g, Layer layer) {
		layer.draw(g, 0, 0, false, false, false);
		for (Layer subLayer : layer.getSubLayers()) {
			drawAll(g, subLayer);
		}
	}

	private void drawThumbnail(Layer rootLayer) {
		Rectangle bounds = getBoundsAll(rootLayer);
		Graphics2D g = thumbnail.createGraphics();
		Composite c = g.getComposite();
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(0, 0, THUMB_SIZE, THUMB_SIZE);
		g.setComposite(c);
		double scale = Math.min((double) (THUMB_SIZE - 10) / bounds.width,
				(double) (THUMB_SIZE - 10) / bounds.height);
		g.translate(THUMB_SIZE / 2, THUMB_SIZE / 2);
		g.scale(scale, scale);
		g.translate(-bounds.getCenterX(), -bounds.getCenterY());
		drawAll(g, rootLayer);
		g.dispose();
	}

	private static final Font font = new Font("Sans Serif", Font.BOLD, 12);

	public void draw(Graphics g, int width, int height) {
		g.drawImage(thumbnail, 0, 0, width, height, null);
		g.setFont(font);
		g.setColor(Color.white);
		g.drawString((frameNumber + 1) + "", 3, 13);
		g.setColor(Color.black);
		g.drawString((frameNumber + 1) + "", 2, 12);
	}

	private Rectangle bounds = new Rectangle();

	public Rectangle getBounds() {
		return bounds;
	}

	public void setBounds(int x, int y, int width, int height) {
		this.bounds.setBounds(x, y, width, height);
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("frameNumber", frameNumber);
		json.put("interpolator", interpolator.toJSON());
		JSONArray jsonLayerInfoMap = new JSONArray();
		for (Map.Entry<String, LayerInfo> entry : layerInfoMap.entrySet()) {
			JSONObject jsonEntry = new JSONObject();
			jsonEntry.put("key", entry.getKey());
			jsonEntry.put("value", entry.getValue().toJSON());
			jsonLayerInfoMap.put(jsonEntry);
		}
		json.put("layerInfoMap", jsonLayerInfoMap);
		return json;
	}

	public static Keyframe fromJSON(JSONObject json) throws JSONException {
		Keyframe keyframe = new Keyframe();
		keyframe.setFrameNumber(json.getInt("frameNumber"));
		keyframe.setInterpolator(Interpolator.fromJSON(json
				.getJSONObject("interpolator")));
		JSONArray jsonLayerInfoMap = json.getJSONArray("layerInfoMap");
		keyframe.layerInfoMap = new HashMap<String, LayerInfo>();
		for (int i = 0; i < jsonLayerInfoMap.length(); i++) {
			JSONObject jsonEntry = jsonLayerInfoMap.getJSONObject(i);
			String key = jsonEntry.getString("key");
			JSONObject jsonLayerInfo = jsonEntry.getJSONObject("value");
			keyframe.layerInfoMap.put(key, LayerInfo.fromJSON(jsonLayerInfo));
		}
		return keyframe;
	}
}
