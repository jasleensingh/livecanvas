package common.typeutils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface DoubleType {
	public String name();

	public String category() default "";

	public String description() default "";

	public boolean readonly() default false;

	public double min() default 0;

	public double max() default 65535.0f;

	public double step() default 1.0;

}
