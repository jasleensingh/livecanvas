package livecanvas;

import org.json.JSONException;
import org.json.JSONObject;

public class Vec2 {
	public double x, y;

	public Vec2() {
		this(0, 0);
	}

	public Vec2(Vec2 copy) {
		this(copy.x, copy.y);
	}

	public Vec2(double v1, double v2) {
		this.x = v1;
		this.y = v2;
	}

	public Vec2(Vec2 from, Vec2 to) {
		this.x = to.x - from.x;
		this.y = to.y - from.y;
	}

	public void set(double v1, double v2) {
		this.x = v1;
		this.y = v2;
	}

	public void set(Vec2 vec) {
		this.x = vec.x;
		this.y = vec.y;
	}

	public void translate(double dx, double dy) {
		x += dx;
		y += dy;
	}

	public void add(Vec2 vec, Vec2 result) {
		result.x = this.x + vec.x;
		result.y = this.y + vec.y;
	}

	public void addSelf(Vec2 vec) {
		add(vec, this);
	}

	public void subtract(Vec2 vec, Vec2 result) {
		result.x = this.x - vec.x;
		result.y = this.y - vec.y;
	}

	public void subtractSelf(Vec2 vec) {
		subtract(vec, this);
	}

	public void multiply(double s, Vec2 result) {
		result.x = s * this.x;
		result.y = s * this.y;
	}

	public void multiplySelf(double s) {
		multiply(s, this);
	}

	public void normalizeSelf() {
		double length = length();
		x /= length;
		y /= length;
	}

	public double length() {
		return Math.sqrt(x * x + y * y);
	}

	@Override
	public String toString() {
		return x + ", " + y;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("x", x);
		json.put("y", y);
		return json;
	}

	public static Vec2 fromJSON(JSONObject json) throws JSONException {
		Vec2 v = new Vec2();
		v.x = json.getInt("x");
		v.y = json.getInt("y");
		return v;
	}
}
