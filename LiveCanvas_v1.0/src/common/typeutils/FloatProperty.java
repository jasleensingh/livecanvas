package common.typeutils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

public class FloatProperty extends Property {

	public final float min;

	public final float max;

	public final float step;

	public FloatProperty(String name, String category, String description,
			boolean readonly, float min, float max, float step) {
		super(name, category, description, readonly);
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public FloatProperty(PropertyDescriptor pd, Object obj, String category,
			boolean readonly, float min, float max, float step) {
		super(pd, obj, category, readonly);
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public FloatProperty(Field field, Object obj, String name, String category,
			String description, boolean readonly, float min, float max,
			float step) {
		super(field, obj, name, category, description, readonly);
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public FloatProperty(Field field, Object obj, FloatType type) {
		this(field, obj, type.name(), type.category(), type.description(), type
				.readonly(), type.min(), type.max(), type.step());
	}

	public FloatProperty(FloatProperty prop, String name, String description,
			String category, boolean readonly) {
		super(prop, name, category, description, readonly);
		this.min = prop.min;
		this.max = prop.max;
		this.step = prop.step;
	}

	public String toCode(PropertyCodeGenerator codeGen) {
		return codeGen.toCode(this);
	}
}
