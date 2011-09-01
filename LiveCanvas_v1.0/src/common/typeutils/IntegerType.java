package common.typeutils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface IntegerType {
	public String name();

	public String category() default "";

	public String description() default "";

	public boolean readonly() default false;

	public int min() default -65535;

	public int max() default 65535;

	public int step() default 1;
}