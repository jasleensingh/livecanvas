package common.typeutils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ListType {
	public String name();

	public String category();

	public String description() default "";

	public boolean readonly() default false;

	public Class clazz();
}
