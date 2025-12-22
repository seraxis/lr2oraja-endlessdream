package bms.player.beatoraja.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;

/**
 * Endless Dream customized logger factory. You should use this factory instead of
 * LoggerFactory everywhere!
 */
public class I18nLoggerFactory {
	private static class LazyDuplexLoggerHolder {
		private static final DuplexLogger INSTANCE = create();
		private static ResourceBundle actualBundle;
		private static ResourceBundle rootBundle;

		private static DuplexLogger create() {
			java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
			try {
				rootLogger.addHandler(new FileHandler("beatoraja_log.txt"));
			} catch (Throwable e) {
				e.printStackTrace();
			}

			actualBundle = ResourceBundle.getBundle("resources.UIResources");
			Locale actualLoddedLocale = actualBundle.getLocale();
			boolean isEnglishLocale = actualLoddedLocale == null || actualLoddedLocale.getDisplayName().isEmpty();
			if (!isEnglishLocale) {
				try {
					java.util.logging.Logger enLogger = java.util.logging.Logger.getLogger("en");
					enLogger.setUseParentHandlers(false);
					Handler[] handlers = enLogger.getHandlers();
					for (Handler handler : handlers) {
						enLogger.removeHandler(handler);
						handler.close();
					}
					enLogger.addHandler(new FileHandler("beatoraja_log_en.txt"));
					try (InputStream input = I18nLoggerFactory.class.getClassLoader().getResourceAsStream("resources/UIResources.properties")) {
						rootBundle = new PropertyResourceBundle(input);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					LogManager.getLogManager().addLogger(enLogger);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			return isEnglishLocale
					? new DuplexLogger(new I18nLogger(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME), actualBundle))
					: new DuplexLogger(
						new I18nLogger(LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME), actualBundle),
						new I18nLogger(LoggerFactory.getLogger("en"), rootBundle)
					);
		}
	}

	public static Logger getLogger(Class<?> clazz) {
		return LazyDuplexLoggerHolder.INSTANCE;
	}
}
