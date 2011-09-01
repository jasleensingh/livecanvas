package livecanvas;

import org.json.JSONException;
import org.json.JSONObject;

public class Face {
	public int v1Index;
	public int v2Index;
	public int v3Index;
	public double weight = 1.0;
	
	public int index;

	public Face() {
	}

	public Face(int v1Index, int v2Index, int v3Index) {
		this.v1Index = v1Index;
		this.v2Index = v2Index;
		this.v3Index = v3Index;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("v1Index", v1Index);
		json.put("v2Index", v2Index);
		json.put("v3Index", v3Index);
		json.put("index", index);
		return json;
	}

	public static Face fromJSON(JSONObject json) throws JSONException {
		Face f = new Face();
		f.v1Index = json.getInt("v1Index");
		f.v2Index = json.getInt("v2Index");
		f.v3Index = json.getInt("v3Index");
		f.index = json.getInt("index");
		return f;
	}
}
