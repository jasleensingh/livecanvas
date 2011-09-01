package common.typeutils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

public class StringArrayProperty extends Property {

	public StringArrayProperty(String name, String category,
			String description, boolean readonly) {
		super(name, category, description, readonly);
	}

	public StringArrayProperty(PropertyDescriptor pd, Object obj, String category,
			boolean readonly) {
		super(pd, obj, category, readonly);
	}

	public StringArrayProperty(Field field, Object obj, String name,
			String description, String category, boolean readonly) {
		super(field, obj, name, category, description, readonly);
	}

	public StringArrayProperty(Field field, Object obj, StringArrayType type) {
		this(field, obj, type.name(), type.category(), type.description(), type
				.readonly());
	}

	public StringArrayProperty(StringArrayProperty prop, String name,
			String category, String description, boolean readonly) {
		super(prop, name, category, description, readonly);
	}
	
	public String toCode(PropertyCodeGenerator codeGen) {
		return codeGen.toCode(this);
	}
}
