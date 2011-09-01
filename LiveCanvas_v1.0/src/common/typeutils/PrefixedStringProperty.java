package common.typeutils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

public class PrefixedStringProperty extends StringProperty {

	public final String prefix;
	
	public PrefixedStringProperty(String name, String prefix, String category,
			String description, boolean readonly) {
		super(name, category, description, readonly);
		this.prefix = prefix;
	}

	public PrefixedStringProperty(PropertyDescriptor pd, String prefix,
			Object obj, String category, boolean readonly) {
		super(pd, obj, category, readonly);
		this.prefix = prefix;
	}

	public PrefixedStringProperty(Field field, Object obj, String name,
			String prefix, String category, String description, boolean readonly) {
		super(field, obj, name, category, description, readonly);
		this.prefix = prefix;
	}

	public PrefixedStringProperty(Field field, Object obj, StringType type,
			String prefix) {
		this(field, obj, type.name(), prefix, type.category(), type
				.description(), type.readonly());
	}

	public PrefixedStringProperty(Field field, Object obj, PrefixedStringType type) {
		this(field, obj, type.name(), type.prefix(), type.category(), type
				.description(), type.readonly());
	}

	public PrefixedStringProperty(StringProperty prop, String name,
			String prefix, String category, String description, boolean readonly) {
		super(prop, name, category, description, readonly);
		this.prefix = prefix;
	}

	public PrefixedStringProperty(PrefixedStringProperty prop, String name,
			String prefix, String category, String description, boolean readonly) {
		super(prop, name, category, description, readonly);
		this.prefix = prefix;
	}

	public String toCode(PropertyCodeGenerator codeGen) {
		return codeGen.toCode(this);
	}
}
