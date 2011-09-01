package common.typeutils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

public class IntegerProperty extends Property {

	public final int min;

	public final int max;

	public final int step;

	public IntegerProperty(String name, String category, String description,
			boolean readonly, int min, int max, int step) {
		super(name, category, description, readonly);
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public IntegerProperty(PropertyDescriptor pd, Object obj, String category,
			boolean readonly, int min, int max, int step) {
		super(pd, obj, category, readonly);
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public IntegerProperty(Field field, Object obj, String name,
			String category, String description, boolean readonly, int min,
			int max, int step) {
		super(field, obj, name, category, description, readonly);
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public IntegerProperty(Field field, Object obj, IntegerType type) {
		this(field, obj, type.name(), type.category(), type.description(), type
				.readonly(), type.min(), type.max(), type.step());
	}

	public IntegerProperty(IntegerProperty prop, String name, String category,
			String description, boolean readonly) {
		super(prop, name, category, description, readonly);
		this.min = prop.min;
		this.max = prop.max;
		this.step = prop.step;
	}
	
	public String toCode(PropertyCodeGenerator codeGen) {
		return codeGen.toCode(this);
	}
}
