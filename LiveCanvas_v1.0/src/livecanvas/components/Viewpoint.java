package livecanvas.components;

import livecanvas.Path;

import org.json.JSONException;
import org.json.JSONObject;

public class Viewpoint {
	public final int viewpointX, viewpointY;
	private Path path;

	public Viewpoint(int viewpointX, int viewpointY) {
		this.viewpointX = viewpointX;
		this.viewpointY = viewpointY;
		path = new Path();
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public boolean isGenerated() {
		return !path.isFinalized();
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("viewpointX", viewpointX);
		json.put("viewpointY", viewpointY);
		if (!isGenerated()) {
			json.put("path", path.toJSON());
		}
		return json;
	}

	public static Viewpoint fromJSON(JSONObject json) throws JSONException {
		int viewpointX = json.getInt("viewpointX");
		int viewpointY = json.getInt("viewpointY");
		Viewpoint viewpoint = new Viewpoint(viewpointX, viewpointY);
		JSONObject jsonPath;
		if ((jsonPath = json.optJSONObject("path")) != null) {
			viewpoint.setPath(Path.fromJSON(jsonPath));
		}
		return viewpoint;
	}
}
