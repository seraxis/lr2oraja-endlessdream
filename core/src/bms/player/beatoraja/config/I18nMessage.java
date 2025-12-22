package bms.player.beatoraja.config;

import java.text.MessageFormat;
import java.util.Locale;
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

	public static Locale getDefaultBundleLocale() {
		if (bundle == null) {
			bundle = ResourceBundle.getBundle("resources.UIResources", Locale.getDefault());
		}
		return bundle.getLocale();
	}

	public static boolean hasKey(String key) {
		return bundle.containsKey(key);
	}

	/**
	 * For creating an error code automatically, all error messages should be registered here with their full-name
	 */
	private enum ErrorMessage {
		IR_SEND_SCORE_FAILED,
		TEST_ERROR;
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

	public static String getI18nError(String key, Object... args) {
		try {
			ErrorMessage errorMessage = ErrorMessage.valueOf(key);
			// Error code starts from 1 (we don't want a code like 0000)
			int errorCode = errorMessage.ordinal() + 1;
			String originalMessage = getI18nMessage(key, args);
			return String.format("[%04d]", errorCode) + originalMessage;
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}
}
