package common.typeutils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

public class ListProperty extends Property {

	public final Class clazz;

	public ListProperty(String name, String category, String description,
			boolean readonly, Class clazz) {
		super(name, category, description, readonly);
		this.clazz = clazz;
	}

	public ListProperty(PropertyDescriptor pd, Object obj, String category,
			boolean readonly, Class clazz) {
		super(pd, obj, category, readonly);
		this.clazz = clazz;
	}

	public ListProperty(Field field, Object obj, String name, String category,
			String description, boolean readonly, Class clazz) {
		super(field, obj, name, category, description, readonly);
		this.clazz = clazz;
	}

	public ListProperty(Field field, Object obj, ListType type) {
		this(field, obj, type.name(), type.category(), type.description(), type
				.readonly(), type.clazz());
	}

	public ListProperty(ListProperty prop, String name, String category,
			String description, boolean readonly) {
		super(prop, name, category, description, readonly);
		this.clazz = prop.clazz;
	}

	public String toCode(PropertyCodeGenerator codeGen) {
		return codeGen.toCode(this);
	}
}
