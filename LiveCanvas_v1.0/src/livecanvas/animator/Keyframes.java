package livecanvas.animator;

import java.util.LinkedList;

import livecanvas.components.Keyframe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Keyframes extends LinkedList<Keyframe> {
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		JSONArray jsonKeyframes = new JSONArray();
		for (Keyframe kf : this) {
			jsonKeyframes.put(kf.toJSON());
		}
		json.put("keyframes", jsonKeyframes);
		return json;
	}

	public static Keyframes fromJSON(JSONObject json) throws JSONException {
		Keyframes keyframes = new Keyframes();
		JSONArray jsonKeyframes = json.getJSONArray("keyframes");
		for (int i = 0; i < jsonKeyframes.length(); i++) {
			keyframes.add(Keyframe.fromJSON(jsonKeyframes.getJSONObject(i)));
		}
		return keyframes;
	}
}