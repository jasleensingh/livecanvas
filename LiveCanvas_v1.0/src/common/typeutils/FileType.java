package common.typeutils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface FileType {
	public String name();

	public String category() default "";

	public String description() default "";

	public boolean readonly() default false;

	public String default_();

	public String[] filters();
}
