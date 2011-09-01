package common.typeutils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

public class EnumProperty extends Property {

	public final String[] allowed;

	public EnumProperty(String name, String category, String description,
			boolean readonly, String[] allowed) {
		super(name, category, description, readonly);
		this.allowed = allowed;
	}

	public EnumProperty(PropertyDescriptor pd, Object obj, String category,
			boolean readonly, String[] allowed) {
		super(pd, obj, category, readonly);
		this.allowed = allowed;
	}

	public EnumProperty(Field field, Object obj, String name, String category,
			String description, boolean readonly, String[] allowed) {
		super(field, obj, name, category, description, readonly);
		this.allowed = allowed;
	}

	public EnumProperty(Field field, Object obj, EnumType type) {
		this(field, obj, type.name(), type.category(), type.description(), type
				.readonly(), type.allowed());
	}

	public EnumProperty(EnumProperty prop, String name, String category,
			String description, boolean readonly) {
		super(prop, name, category, description, readonly);
		this.allowed = prop.allowed;
	}
	
	public String toCode(PropertyCodeGenerator codeGen) {
		return codeGen.toCode(this);
	}
}
