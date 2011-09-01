package common.typeutils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

public class FontProperty extends Property {

	public FontProperty(String name, String category, String description,
			boolean readonly) {
		super(name, category, description, readonly);
	}

	public FontProperty(PropertyDescriptor pd, Object obj, String category,
			boolean readonly) {
		super(pd, obj, category, readonly);
	}

	public FontProperty(Field field, Object obj, String name, String category,
			String description, boolean readonly) {
		super(field, obj, name, category, description, readonly);
	}

	public FontProperty(Field field, Object obj, FontType type) {
		this(field, obj, type.name(), type.category(), type.description(), type
				.readonly());
	}

	public FontProperty(FontProperty prop, String name, String category,
			String description, boolean readonly) {
		super(prop, name, category, description, readonly);
	}
	
	public String toCode(PropertyCodeGenerator codeGen) {
		return codeGen.toCode(this);
	}
}
