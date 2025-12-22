package bms.player.beatoraja.i18n;

import org.slf4j.Logger;
import org.slf4j.Marker;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * I18nLogger is a proxy that composes a Logger interface from slf4j and a resource bundle.
 * It does the following transformation:
 * <ul>
 *     <li>if the message is a key inside resource bundle, translating it by using the bundle</li>
 *     <li>otherwise, log the message without modification</li>
 * </ul>
 */
public class I18nLogger implements Logger {
	private final Logger logger;
	private final ResourceBundle bundle;

	public I18nLogger(Logger logger, ResourceBundle bundle) {
		this.logger = logger;
		this.bundle = bundle;
	}

	// A TranslationHelper is an Either<String, Unit>
	private class TranslationHelper {
		protected String translated = null;

		public TranslationHelper(String msg) {
			if (bundle.containsKey(msg)) {
				translated = bundle.getString(msg);
			}
		}

		public TranslationHelper(String msg, Object... args) {
			if (bundle.containsKey(msg)) {
				translated = MessageFormat.format(msg, args);
			}
		}

		public void execute(Consumer<String> onTranslated, Runnable onPlainString) {
			if (translated != null) {
				onTranslated.accept(translated);
			} else {
				onPlainString.run();
			}
		}
	}

	private class ErrorMessageHelper extends TranslationHelper {
		public ErrorMessageHelper(String msg) {
			super(msg);
			appendErrorCode(msg);
		}

		public ErrorMessageHelper(String msg, Object... args) {
			super(msg, args);
			appendErrorCode(msg);
		}

		private void appendErrorCode(String msg) {
			if (translated != null) {
				I18nError error = I18nError.valueOf(msg);
				this.translated = String.format("[%04d] %s", error.ordinal() + 1, translated);
			}
		}
	}

	@Override
	public String getName() {
		return logger.getName();
	}

	@Override
	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}

	@Override
	public void trace(String msg) {
		new TranslationHelper(msg).execute(
				logger::trace,
				() -> logger.trace(msg)
		);
	}

	@Override
	public void trace(String format, Object arg) {
		new TranslationHelper(format, arg).execute(
				logger::trace,
				() -> logger.trace(format, arg)
		);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		new TranslationHelper(format, arg1, arg2).execute(
				logger::trace,
				() -> logger.trace(format, arg1, arg2)
		);
	}

	@Override
	public void trace(String format, Object... arguments) {
		new TranslationHelper(format, arguments).execute(
				logger::trace,
				() -> logger.trace(format, arguments)
		);
	}

	@Override
	public void trace(String msg, Throwable t) {
		new TranslationHelper(msg).execute(
				translated -> logger.trace(translated, t),
				() -> logger.trace(msg, t)
		);
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return logger.isTraceEnabled(marker);
	}

	@Override
	public void trace(Marker marker, String msg) {
		new TranslationHelper(msg).execute(
				translated -> logger.trace(marker, translated),
				() -> logger.trace(marker, msg)
		);
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		new TranslationHelper(format, arg).execute(
				translated -> logger.trace(marker, translated),
				() -> logger.trace(marker, format, arg)
		);
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		new TranslationHelper(format, arg1, arg2).execute(
				translated -> logger.trace(marker, translated),
				() -> logger.trace(marker, format, arg1, arg2)
		);
	}

	@Override
	public void trace(Marker marker, String format, Object... argArray) {
		new TranslationHelper(format, argArray).execute(
				translated -> logger.trace(marker, translated),
				() -> logger.trace(marker, format, argArray)
		);
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		new TranslationHelper(msg).execute(
				translated -> logger.trace(marker, translated, t),
				() -> logger.trace(marker, msg, t)
		);
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	@Override
	public void debug(String msg) {
		new TranslationHelper(msg).execute(
				logger::debug,
				() -> logger.debug(msg)
		);
	}

	@Override
	public void debug(String format, Object arg) {
		new TranslationHelper(format, arg).execute(
				logger::debug,
				() -> logger.debug(format, arg)
		);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		new TranslationHelper(format, arg1, arg2).execute(
				logger::debug,
				() -> logger.debug(format, arg1, arg2)
		);
	}

	@Override
	public void debug(String format, Object... arguments) {
		new TranslationHelper(format, arguments).execute(
				logger::debug,
				() -> logger.debug(format, arguments)
		);
	}

	@Override
	public void debug(String msg, Throwable t) {
		new TranslationHelper(msg).execute(
				translated -> logger.debug(translated, t),
				() -> logger.debug(msg, t)
		);
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return logger.isDebugEnabled(marker);
	}

	@Override
	public void debug(Marker marker, String msg) {
		new TranslationHelper(msg).execute(
				translated -> logger.debug(marker, translated),
				() -> logger.debug(marker, msg)
		);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		new TranslationHelper(format, arg).execute(
				translated -> logger.debug(marker, translated),
				() -> logger.debug(marker, format, arg)
		);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		new TranslationHelper(format, arg1, arg2).execute(
				translated -> logger.debug(marker, translated),
				() -> logger.debug(marker, format, arg1, arg2)
		);
	}

	@Override
	public void debug(Marker marker, String format, Object... argArray) {
		new TranslationHelper(format, argArray).execute(
				translated -> logger.debug(marker, translated),
				() -> logger.debug(marker, format, argArray)
		);
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
		new TranslationHelper(msg).execute(
				translated -> logger.debug(marker, translated, t),
				() -> logger.debug(marker, msg, t)
		);
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	@Override
	public void info(String msg) {
		new TranslationHelper(msg).execute(
				logger::info,
				() -> logger.info(msg)
		);
	}

	@Override
	public void info(String format, Object arg) {
		new TranslationHelper(format, arg).execute(
				logger::info,
				() -> logger.info(format, arg)
		);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		new TranslationHelper(format, arg1, arg2).execute(
				logger::info,
				() -> logger.info(format, arg1, arg2)
		);
	}

	@Override
	public void info(String format, Object... arguments) {
		new TranslationHelper(format, arguments).execute(
				logger::info,
				() -> logger.info(format, arguments)
		);
	}

	@Override
	public void info(String msg, Throwable t) {
		new TranslationHelper(msg).execute(
				translated -> logger.info(translated, t),
				() -> logger.info(msg, t)
		);
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return logger.isInfoEnabled(marker);
	}

	@Override
	public void info(Marker marker, String msg) {
		new TranslationHelper(msg).execute(
				translated -> logger.info(marker, translated),
				() -> logger.info(marker, msg)
		);
	}

	@Override
	public void info(Marker marker, String format, Object arg) {
		new TranslationHelper(format, arg).execute(
				translated -> logger.info(marker, translated),
				() -> logger.info(marker, format, arg)
		);
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		new TranslationHelper(format, arg1, arg2).execute(
				translated -> logger.info(marker, translated),
				() -> logger.info(marker, format, arg1, arg2)
		);
	}

	@Override
	public void info(Marker marker, String format, Object... argArray) {
		new TranslationHelper(format, argArray).execute(
				translated -> logger.info(marker, translated),
				() -> logger.info(marker, format, argArray)
		);
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
		new TranslationHelper(msg).execute(
				translated -> logger.info(marker, translated, t),
				() -> logger.info(marker, msg, t)
		);
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}

	@Override
	public void warn(String msg) {
		new TranslationHelper(msg).execute(
				logger::warn,
				() -> logger.warn(msg)
		);
	}

	@Override
	public void warn(String format, Object arg) {
		new TranslationHelper(format, arg).execute(
				logger::warn,
				() -> logger.warn(format, arg)
		);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		new TranslationHelper(format, arg1, arg2).execute(
				logger::warn,
				() -> logger.warn(format, arg1, arg2)
		);
	}

	@Override
	public void warn(String format, Object... arguments) {
		new TranslationHelper(format, arguments).execute(
				logger::warn,
				() -> logger.warn(format, arguments)
		);
	}

	@Override
	public void warn(String msg, Throwable t) {
		new TranslationHelper(msg).execute(
				translated -> logger.warn(translated, t),
				() -> logger.warn(msg, t)
		);
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return logger.isWarnEnabled(marker);
	}

	@Override
	public void warn(Marker marker, String msg) {
		new TranslationHelper(msg).execute(
				translated -> logger.warn(marker, translated),
				() -> logger.warn(marker, msg)
		);
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		new TranslationHelper(format, arg).execute(
				translated -> logger.warn(marker, translated),
				() -> logger.warn(marker, format, arg)
		);
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		new TranslationHelper(format, arg1, arg2).execute(
				translated -> logger.warn(marker, translated),
				() -> logger.warn(marker, format, arg1, arg2)
		);
	}

	@Override
	public void warn(Marker marker, String format, Object... argArray) {
		new TranslationHelper(format, argArray).execute(
				translated -> logger.warn(marker, translated),
				() -> logger.warn(marker, format, argArray)
		);
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
		new TranslationHelper(msg).execute(
				translated -> logger.warn(marker, translated, t),
				() -> logger.warn(marker, msg, t)
		);
	}

	@Override
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}

	@Override
	public void error(String msg) {
		new ErrorMessageHelper(msg).execute(
				logger::error,
				() -> logger.error(msg)
		);
	}

	@Override
	public void error(String format, Object arg) {
		new ErrorMessageHelper(format, arg).execute(
				logger::error,
				() -> logger.error(format, arg)
		);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		new ErrorMessageHelper(format, arg1, arg2).execute(
				logger::error,
				() -> logger.error(format, arg1, arg2)
		);
	}

	@Override
	public void error(String format, Object... arguments) {
		new ErrorMessageHelper(format, arguments).execute(
				logger::error,
				() -> logger.error(format, arguments)
		);
	}

	@Override
	public void error(String msg, Throwable t) {
		new ErrorMessageHelper(msg).execute(
				translated -> logger.error(translated, t),
				() -> logger.error(msg, t)
		);
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return logger.isErrorEnabled(marker);
	}

	@Override
	public void error(Marker marker, String msg) {
		new ErrorMessageHelper(msg).execute(
				translated -> logger.error(marker, translated),
				() -> logger.error(marker, msg)
		);
	}

	@Override
	public void error(Marker marker, String format, Object arg) {
		new ErrorMessageHelper(format, arg).execute(
				translated -> logger.error(marker, translated),
				() -> logger.error(marker, format, arg)
		);
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		new ErrorMessageHelper(format, arg1, arg2).execute(
				translated -> logger.error(marker, translated),
				() -> logger.error(marker, format, arg1, arg2)
		);
	}

	@Override
	public void error(Marker marker, String format, Object... argArray) {
		new ErrorMessageHelper(format, argArray).execute(
				translated -> logger.error(marker, translated),
				() -> logger.error(marker, format, argArray)
		);
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {
		new ErrorMessageHelper(msg).execute(
				translated -> logger.error(marker, translated, t),
				() -> logger.error(marker, msg, t)
		);
	}
}
