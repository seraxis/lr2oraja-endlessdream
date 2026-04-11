package bms.player.beatoraja.skin.lr2.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used for customized destination commands. As for now it hasn't been used because
 *  all the dst commands are sharing the same pattern.
 *
 * If in the future we do need a customized destination command, let's say it's called as "DST_FOO",
 *  you can create a new class like below:
 * <pre><code>
 *     {@code @LR2Command("DST_FOO")}
 *     public class DestinationFoo {
 *          .....
 *     }
 * </code></pre>
 * Then the parser can extract the fields automatically for you.
 *
 * @see bms.player.beatoraja.skin.lr2.commands.StandardDestination
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LR2Command {
	/**
	 * Command name
	 */
	String value();
}
