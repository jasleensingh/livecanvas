package common.typeutils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

public class BooleanProperty extends Property {

	public BooleanProperty(String name, String category, String description,
			boolean readonly) {
		super(name, category, description, readonly);
	}

	public BooleanProperty(PropertyDescriptor pd, Object obj, String category,
			boolean readonly) {
		super(pd, obj, category, readonly);
	}

	public BooleanProperty(Field field, Object obj, String name,
			String category, String description, boolean readonly) {
		super(field, obj, name, category, description, readonly);
	}

	public BooleanProperty(Field field, Object obj, BooleanType type) {
		this(field, obj, type.name(), type.category(), type.description(), type
				.readonly());
	}

	public BooleanProperty(Property prop, String name, String category,
			String description, boolean readonly) {
		super(prop, name, category, description, readonly);
	}
	
	public String toCode(PropertyCodeGenerator codeGen) {
		return codeGen.toCode(this);
	}
}
