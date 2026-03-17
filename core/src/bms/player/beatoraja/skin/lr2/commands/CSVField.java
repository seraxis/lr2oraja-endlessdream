package bms.player.beatoraja.skin.lr2.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface CSVField {
	/**
	 * Column index of the csv line
	 */
	int value() default -1;

	/**
	 * For composed objects, when used, index will be ignored
	 */
	int start() default -1;

	/**
	 * Is this field an option? If so, there's an extra conversion will be done at runtime
	 */
	boolean option() default false;

	/**
	 * Indicates if a field is optional
	 */
	boolean optional() default false;
}
