package common.typeutils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

public class TextProperty extends Property {

	public TextProperty(String name, String category, String description,
			boolean readonly) {
		super(name, category, description, readonly);
	}

	public TextProperty(PropertyDescriptor pd, Object obj, String category,
			boolean readonly) {
		super(pd, obj, category, readonly);
	}

	public TextProperty(Field field, Object obj, String name, String category,
			String description, boolean readonly) {
		super(field, obj, name, category, description, readonly);
	}

	public TextProperty(Field field, Object obj, TextType type) {
		this(field, obj, type.name(), type.category(), type.description(), type
				.readonly());
	}

	public TextProperty(TextProperty prop, String name, String category,
			String description, boolean readonly) {
		super(prop, name, category, description, readonly);
	}
	
	public String toCode(PropertyCodeGenerator codeGen) {
		return codeGen.toCode(this);
	}
}
