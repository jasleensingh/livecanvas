package common.typeutils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PropertyFactory {

	public static Property[] createProperties(Object obj) {
		List<Property> list = new LinkedList<Property>();
		if (obj instanceof PropertyContainer) {
			Property[] contProps = ((PropertyContainer) obj)
					.getContainedProperties();
			if (contProps != null) {
				list.addAll(Arrays.asList(contProps));
			}
		}
		Class itemClass = obj.getClass();
		Field[] fields = itemClass.getFields();
		for (Field field : fields) {
			Property prop = createProperty(field, obj);
			if (prop != null) {
				list.add(prop);
			}
		}
		return list.toArray(new Property[0]);
	}

	public static Property createProperty(String fieldName, Object obj) {
		try {
			return createProperty(obj.getClass().getField(fieldName), obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Property createProperty(Field field, Object obj) {
		Property prop = null;
		Annotation an;
		try {
			if ((an = field.getAnnotation(BooleanType.class)) != null) {
				BooleanType bt = (BooleanType) an;
				prop = new BooleanProperty(field, obj, bt);
			} else if ((an = field.getAnnotation(DoubleType.class)) != null) {
				DoubleType dt = (DoubleType) an;
				prop = new DoubleProperty(field, obj, dt);
			} else if ((an = field.getAnnotation(FloatType.class)) != null) {
				FloatType ft = (FloatType) an;
				prop = new FloatProperty(field, obj, ft);
			} else if ((an = field.getAnnotation(EnumType.class)) != null) {
				EnumType et = (EnumType) an;
				prop = new EnumProperty(field, obj, et);
			} else if ((an = field.getAnnotation(IntegerType.class)) != null) {
				IntegerType it = (IntegerType) an;
				prop = new IntegerProperty(field, obj, it);
			} else if ((an = field.getAnnotation(StringType.class)) != null) {
				StringType st = (StringType) an;
				prop = new StringProperty(field, obj, st);
			} else if ((an = field.getAnnotation(PasswordType.class)) != null) {
				PasswordType pt = (PasswordType) an;
				prop = new PasswordProperty(field, obj, pt);
			} else if ((an = field.getAnnotation(StringArrayType.class)) != null) {
				StringArrayType sat = (StringArrayType) an;
				prop = new StringArrayProperty(field, obj, sat);
			} else if ((an = field.getAnnotation(TextType.class)) != null) {
				TextType tt = (TextType) an;
				prop = new TextProperty(field, obj, tt);
			} else if ((an = field.getAnnotation(FontType.class)) != null) {
				FontType ft = (FontType) an;
				prop = new FontProperty(field, obj, ft);
			} else if ((an = field.getAnnotation(ColorType.class)) != null) {
				ColorType ct = (ColorType) an;
				prop = new ColorProperty(field, obj, ct);
			} else if ((an = field.getAnnotation(StrokeType.class)) != null) {
				StrokeType st = (StrokeType) an;
				prop = new StrokeProperty(field, obj, st);
			} else if ((an = field.getAnnotation(FileType.class)) != null) {
				FileType ft = (FileType) an;
				prop = new FileProperty(field, obj, ft);
			} else if ((an = field.getAnnotation(DirectoryType.class)) != null) {
				DirectoryType dt = (DirectoryType) an;
				prop = new DirectoryProperty(field, obj, dt);
			} else if ((an = field.getAnnotation(DateType.class)) != null) {
				DateType dt = (DateType) an;
				prop = new DateProperty(field, obj, dt);
			} else if ((an = field.getAnnotation(GradientType.class)) != null) {
				GradientType gt = (GradientType) an;
				prop = new GradientProperty(field, obj, gt);
			} else if ((an = field.getAnnotation(ListType.class)) != null) {
				ListType lt = (ListType) an;
				prop = new ListProperty(field, obj, lt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prop;
	}
}
