package common.typeutils;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import common.typeutils.StrokeEx.CompositeStroke;

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface StrokeType {
	public static Stroke[] STROKES = {
			new BasicStroke(1), // Solid
			new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL,
					0.0f, new float[] { 10.0f }, 0.0f), // Dash
			new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL,
					0.0f, new float[] { 20.0f, 10.0f }, 0.0f), // Long Dash
			new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL,
					0.0f, new float[] { 10.0f, 10.0f, 5.0f, 10.0f }, 0.0f),
			// Dash Dot
			new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL,
					0.0f, new float[] { 20.0f, 10.0f, 10.0f, 10.0f }, 0.0f),
			// Long Dash Dot
			new CompositeStroke(new BasicStroke(10.0f), new BasicStroke(1.0f)) };

	public String name();

	public String category() default "";

	public String description() default "";

	public boolean readonly() default false;
}
