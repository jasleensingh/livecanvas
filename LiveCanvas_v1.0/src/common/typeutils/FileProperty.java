package common.typeutils;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Field;

import javax.swing.filechooser.FileFilter;

public class FileProperty extends Property {

	public final File default_;

	public final FileFilter[] filters;

	public FileProperty(String name, String category, String description,
			boolean readonly, String default_, String[] filters) {
		super(name, category, description, readonly);
		this.default_ = Utils.parseFilename(default_);
		this.filters = Utils.createFilters(filters);
	}

	public FileProperty(PropertyDescriptor pd, Object obj, String category,
			boolean readonly, String default_, String[] filters) {
		super(pd, obj, category, readonly);
		this.default_ = Utils.parseFilename(default_);
		this.filters = Utils.createFilters(filters);
	}

	public FileProperty(Field field, Object obj, String name, String category,
			String description, boolean readonly, String default_,
			String[] filters) {
		super(field, obj, name, category, description, readonly);
		this.default_ = Utils.parseFilename(default_);
		this.filters = Utils.createFilters(filters);
	}

	public FileProperty(Field field, Object obj, FileType type) {
		this(field, obj, type.name(), type.category(), type.description(), type
				.readonly(), type.default_(), type.filters());
	}

	public FileProperty(FileProperty prop, String name, String category,
			String description, boolean readonly) {
		super(prop, name, category, description, readonly);
		this.default_ = prop.default_;
		this.filters = prop.filters;
	}

	public String toCode(PropertyCodeGenerator codeGen) {
		return codeGen.toCode(this);
	}
}
