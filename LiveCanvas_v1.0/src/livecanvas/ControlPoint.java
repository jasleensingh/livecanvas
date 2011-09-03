package livecanvas;

import org.json.JSONException;
import org.json.JSONObject;

public class ControlPoint {
	public int vIndex;

	public ControlPoint() {
	}

	public ControlPoint(int index) {
		this.vIndex = index;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ControlPoint)) {
			return false;
		}
		return ((ControlPoint) obj).vIndex == vIndex;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("vIndex", vIndex);
		return json;
	}

	public static ControlPoint fromJSON(JSONObject json)
			throws JSONException {
		ControlPoint cp = new ControlPoint();
		cp.vIndex = json.getInt("vIndex");
		return cp;
	}
}