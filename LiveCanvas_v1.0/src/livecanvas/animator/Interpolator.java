package livecanvas.animator;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import livecanvas.Vec2;
import livecanvas.Vec3;
import livecanvas.components.Keyframe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import common.typeutils.IntegerType;

public class Interpolator {
	@IntegerType(name = "Intermediate Frames", min = 0, max = 100)
	public int intermediateFramesCount = 0;
	public Point2D.Float control1 = new Point2D.Float(0, 0);
	public Point2D.Float control2 = new Point2D.Float(1, 1);

	public Interpolator() {
	}

	public Interpolator(float c1x, float c1y, float c2x, float c2y) {
		setControl1(c1x, c1y);
		setControl2(c2x, c2y);
	}

	public void setControl1(float c1x, float c1y) {
		control1.setLocation(c1x, c1y);
	}

	public void setControl2(float c2x, float c2y) {
		control2.setLocation(c2x, c2y);
	}

	public Point2D.Float solve(float t) {
		float tinv = 1 - t;
		return new Point2D.Float(solvex(t, tinv, control1.x, control2.x),
				solvex(t, tinv, control1.y, control2.y));
	}

	private static final float solvex(float t, float tinv, float c1x, float c2x) {
		return t * t * t + 3 * t * t * tinv * c2x + 3 * t * tinv * tinv * c1x;
	}

	public float findYForX(float x) {
		float t0 = 0, t1 = 1, t = (t0 + t1) / 2, gx;
		while (t0 < t1
				&& Math.abs(x - (gx = solvex(t, 1 - t, control1.x, control2.x))) > 0.001) {
			if (gx > x) {
				t1 = t;
			} else if (gx < x) {
				t0 = t;
			}
			t = (t1 + t0) / 2;
		}
		return solvex(t, 1 - t, control1.y, control2.y);
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("intermediateFramesCount", intermediateFramesCount);
		json.put("control1x", control1.x);
		json.put("control1y", control1.y);
		json.put("control2x", control2.x);
		json.put("control2y", control2.y);
		return json;
	}

	public static Interpolator fromJSON(JSONObject json) throws JSONException {
		Interpolator in = new Interpolator();
		in.intermediateFramesCount = json.getInt("intermediateFramesCount");
		in.control1.x = (float) json.getDouble("control1x");
		in.control1.y = (float) json.getDouble("control1y");
		in.control2.x = (float) json.getDouble("control2x");
		in.control2.y = (float) json.getDouble("control2y");
		return in;
	}
}
