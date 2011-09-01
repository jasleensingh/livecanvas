package common.typeutils;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Field;

public class DirectoryProperty extends Property {

	public final File default_;

	public DirectoryProperty(String name, String category, String description,
			boolean readonly, String default_) {
		super(name, category, description, readonly);
		this.default_ = Utils.parseFilename(default_);
	}

	public DirectoryProperty(PropertyDescriptor pd, Object obj,
			String category, boolean readonly, String default_) {
		super(pd, obj, category, readonly);
		this.default_ = Utils.parseFilename(default_);
	}

	public DirectoryProperty(Field field, Object obj, String name,
			String category, String description, boolean readonly,
			String default_) {
		super(field, obj, name, category, description, readonly);
		this.default_ = Utils.parseFilename(default_);
	}

	public DirectoryProperty(Field field, Object obj, DirectoryType type) {
		this(field, obj, type.name(), type.category(), type.description(), type
				.readonly(), type.default_());
	}

	public DirectoryProperty(DirectoryProperty prop, String name,
			String category, String description, boolean readonly) {
		super(prop, name, category, description, readonly);
		this.default_ = prop.default_;
	}

	public String toCode(PropertyCodeGenerator codeGen) {
		return codeGen.toCode(this);
	}
}
