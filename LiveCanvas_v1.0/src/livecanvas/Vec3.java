package livecanvas;

import org.json.JSONException;
import org.json.JSONObject;

public class Vec3 {
	public double x, y, z;

	public Vec3() {
		this(0, 0, 0);
	}

	public Vec3(Vec3 copy) {
		this(copy.x, copy.y, copy.z);
	}

	public Vec3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3(Vec3 from, Vec3 to) {
		this.x = to.x - from.x;
		this.y = to.y - from.y;
		this.z = to.z - from.z;
	}

	public void set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(Vec3 vec) {
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.z;
	}

	public void translate(double dx, double dy, double dz) {
		x += dx;
		y += dy;
		z += dz;
	}

	public Vec3 add(Vec3 vec) {
		Vec3 result = new Vec3();
		add(vec, result);
		return result;
	}

	public void add(Vec3 vec, Vec3 result) {
		result.x = this.x + vec.x;
		result.y = this.y + vec.y;
		result.z = this.z + vec.z;
	}

	public void addSelf(Vec3 vec) {
		add(vec, this);
	}

	public Vec3 subtract(Vec3 vec) {
		Vec3 result = new Vec3();
		subtract(vec, result);
		return result;
	}

	public void subtract(Vec3 vec, Vec3 result) {
		result.x = this.x - vec.x;
		result.y = this.y - vec.y;
		result.z = this.z - vec.z;
	}

	public void subtractSelf(Vec3 vec) {
		subtract(vec, this);
	}

	public Vec3 multiply(double s) {
		Vec3 result = new Vec3();
		multiply(s, result);
		return result;
	}

	public void multiply(double s, Vec3 result) {
		result.x = s * this.x;
		result.y = s * this.y;
		result.z = s * this.z;
	}

	public void multiplySelf(double s) {
		multiply(s, this);
	}

	public Vec3 getNormalized() {
		Vec3 result = new Vec3(this);
		result.normalizeSelf();
		return result;
	}

	public void normalizeSelf() {
		double length = length();
		x /= length;
		y /= length;
		z /= length;
	}

	public double dot(Vec3 vec) {
		return x * vec.x + y * vec.y + z * vec.z;
	}

	public double length() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public final double length_sqr() {
		return x * x + y * y + z * z;
	}

	@Override
	public String toString() {
		return x + ", " + y + ", " + z;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("x", Double.isNaN(x) ? Integer.MAX_VALUE : x);
		json.put("y", Double.isNaN(y) ? Integer.MAX_VALUE : y);
		json.put("z", Double.isNaN(z) ? Integer.MAX_VALUE : z);
		return json;
	}

	public static Vec3 fromJSON(JSONObject json) throws JSONException {
		Vec3 v = new Vec3();
		v.x = json.getInt("x");
		if (Integer.MAX_VALUE == v.x) {
			v.x = Double.NaN;
		}
		v.y = json.getInt("y");
		if (Integer.MAX_VALUE == v.y) {
			v.y = Double.NaN;
		}
		v.z = json.getInt("z");
		if (Integer.MAX_VALUE == v.z) {
			v.z = Double.NaN;
		}
		return v;
	}
}
