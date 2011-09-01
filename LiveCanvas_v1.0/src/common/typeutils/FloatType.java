package common.typeutils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface FloatType {
	public String name();

	public String category() default "";

	public String description() default "";

	public boolean readonly() default false;

	public float min() default -65535.0f;

	public float max() default 65535.0f;

	public float step() default 1.0f;

}
