package common.typeutils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

public class DoubleProperty extends Property {

	public final double min;

	public final double max;

	public final double step;

	public DoubleProperty(String name, String category, String description,
			boolean readonly, double min, double max, double step) {
		super(name, category, description, readonly);
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public DoubleProperty(PropertyDescriptor pd, Object obj, String category,
			boolean readonly, double min, double max, double step) {
		super(pd, obj, category, readonly);
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public DoubleProperty(Field field, Object obj, String name,
			String category, String description, boolean readonly, double min,
			double max, double step) {
		super(field, obj, name, category, description, readonly);
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public DoubleProperty(Field field, Object obj, DoubleType type) {
		this(field, obj, type.name(), type.category(), type.description(), type
				.readonly(), type.min(), type.max(), type.step());
	}

	public DoubleProperty(DoubleProperty prop, String name, String description,
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
