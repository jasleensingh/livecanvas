package common.typeutils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public abstract class Property {
	private static final int MODE_FIELD = 0, MODE_WRAP = 1, MODE_CUSTOM = 2;

	public final String name;

	public final String category;

	public final String description;

	public boolean readonly;

	public boolean hidden;

	private List<PropertyListener> listeners = new LinkedList<PropertyListener>();

	private Field field;

	private Method getter, setter;

	private Object obj;

	private Property wrappedProp;

	private final int mode;

	public Property(String name, String category, String description,
			boolean readonly) {
		mode = MODE_CUSTOM;
		this.name = name;
		this.category = category;
		this.description = description;
		this.readonly = readonly;
	}

	public Property(PropertyDescriptor pd, Object obj, String category,
			boolean readonly) {
		mode = MODE_FIELD;
		getter = pd.getReadMethod();
		setter = pd.getWriteMethod();
		this.obj = obj;
		this.name = pd.getDisplayName();
		this.category = category;
		this.description = pd.getShortDescription();
		this.readonly = readonly;
	}

	public Property(Field field, Object obj, String name, String category,
			String description, boolean readonly) {
		mode = MODE_FIELD;
		this.field = field;
		String fieldName = field.getName();
		try {
			getter = obj.getClass().getMethod(getterFor(fieldName));
		} catch (Exception e) {
			getter = null;
		}
		try {
			setter = obj.getClass().getMethod(setterFor(fieldName),
					field.getType());
		} catch (Exception e) {
			setter = null;
		}
		this.obj = obj;
		this.name = name;
		this.category = category;
		this.description = description;
		this.readonly = readonly;
	}

	public Property(Property prop, String name, String category,
			String description, boolean readonly) {
		mode = MODE_WRAP;
		this.wrappedProp = prop;
		for (PropertyListener pl : prop.listeners) {
			addListener(pl);
		}
		this.name = name;
		this.category = category;
		this.description = description;
		this.readonly = readonly;
	}

	public void addListener(PropertyListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	public void removeListener(PropertyListener l) {
		listeners.remove(l);
	}

	public void firePropertyChanged() {
		for (PropertyListener l : listeners) {
			l.valueChanged(this);
		}
	}

	public final Object get() {
		switch (mode) {
		case MODE_FIELD:
			try {
				Object value;
				if (getter != null) {
					value = getter.invoke(obj);
				} else {
					value = field.get(obj);
				}
				return value;
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case MODE_WRAP:
			return wrappedProp.get();
		case MODE_CUSTOM:
			return custom_get();
		}
		return null;
	}

	public final void set(Object value) {
		switch (mode) {
		case MODE_FIELD:
			try {
				Object oldValue = get();
				if ((oldValue != value)
						&& (oldValue == null || !oldValue.equals(value))) {
					if (setter != null) {
						setter.invoke(obj, new Object[] { value });
					} else {
						field.set(obj, value);
					}
					firePropertyChanged();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case MODE_WRAP:
			wrappedProp.set(value);
			firePropertyChanged();
			break;
		case MODE_CUSTOM:
			custom_set(value);
			firePropertyChanged();
			break;
		}
	}

	public static String getterFor(String fieldName) {
		StringBuilder sb = new StringBuilder("get");
		sb.append(Character.toUpperCase(fieldName.charAt(0)));
		if (fieldName.length() > 1) {
			sb.append(fieldName.substring(1));
		}
		return sb.toString();
	}

	public static String setterFor(String fieldName) {
		StringBuilder sb = new StringBuilder("set");
		sb.append(Character.toUpperCase(fieldName.charAt(0)));
		if (fieldName.length() > 1) {
			sb.append(fieldName.substring(1));
		}
		return sb.toString();
	}

	public Object custom_get() {
		throw new RuntimeException("Custom get method not over-ridden");
	}

	public void custom_set(Object value) {
		throw new RuntimeException("Custom set method not over-ridden");
	}

	public abstract String toCode(PropertyCodeGenerator codeGen);
}
