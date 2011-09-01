package common.typeutils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateProperty extends Property {

	public DateProperty(String name, String category, String description,
			boolean readonly) {
		super(name, category, description, readonly);
	}

	public DateProperty(PropertyDescriptor pd, Object obj, String category,
			boolean readonly) {
		super(pd, obj, category, readonly);
	}

	public DateProperty(Field field, Object obj, String name,
			String category, String description, boolean readonly) {
		super(field, obj, name, category, description, readonly);
	}

	public DateProperty(Field field, Object obj, DateType type) {
		this(field, obj, type.name(), type.category(), type.description(), type
				.readonly());
	}

	public DateProperty(DateProperty prop, String name, String category,
			String description, boolean readonly) {
		super(prop, name, category, description, readonly);
	}
	
	public static Date toDate(String str) {
		if (str == null) {
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		Date parsedDate = null;
		try {
			parsedDate = format.parse(str);
		} catch (ParseException e) {
			System.err.println("Error: Can't parse date: " + str);
		}
		return parsedDate;
	}

	public static String fromDate(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		return format.format(date);
	}
		
	public String toCode(PropertyCodeGenerator codeGen) {
		return codeGen.toCode(this);
	}
}
