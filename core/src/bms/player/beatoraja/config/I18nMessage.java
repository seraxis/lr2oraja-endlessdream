package bms.player.beatoraja.config;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * This class holds the keys from UIResources.properties, because the keys from it
 * are unstructured. And some functions to get i18n messages.
 *
 * @apiNote At most cases, you can import static I18nMessage to access the keys and
 * functions easier.
 */
public class I18nMessage {
	private static ResourceBundle bundle;

	public static class TEST {
		public static final String WELCOME = "TEST_WELCOME";
	}

	public static class IR {
		public static final String IR_SEND_SCORE_SUCCESS = "IR_SEND_SCORE_SUCCESS";
		public static final String IR_SEND_SCORE_FAILED = "IR_SEND_SCORE_FAILED";
	}

	/**
	 * An explicit function that allows you set a bundle manually
	 *
	 * @apiNote provided for possible locale change. For example, if we want to test
	 * japan locale, we can do this by calling <pre><code>setBundle(ResourceBundle.getBundle(..., Locale.Japan))</pre>
	 */
	public static void setBundle(ResourceBundle bundle) {
		I18nMessage.bundle = bundle;
	}

	public static String getI18nMessage(String key) {
		if (bundle == null) {
			bundle = ResourceBundle.getBundle("resources.UIResources");
		}
		return bundle.getString(key);
	}

	public static String getI18nMessage(String key, Object... args) {
		return MessageFormat.format(getI18nMessage(key), args);
	}
}
