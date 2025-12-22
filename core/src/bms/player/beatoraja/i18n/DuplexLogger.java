package bms.player.beatoraja.i18n;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * DuplexLogger is a proxy that composes one or two loggers. In endless dream, it's
 * designed to match the requirement that we want an English-locale log file if user's
 * locale isn't English.<br>
 * See #100 for more details
 */
public class DuplexLogger implements Logger {
	private final Pair<Logger, Logger> loggerPair;
	private final boolean noDuplex;

	public DuplexLogger(Logger logger) {
		this.loggerPair = Pair.of(logger, null);
		this.noDuplex = true;
	}

	public DuplexLogger(Logger firstLogger, Logger secondLogger) {
		this.loggerPair = Pair.of(firstLogger, secondLogger);
		this.noDuplex = false;
	}

	private void match(Consumer<Logger> f, BiConsumer<Logger, Logger> s) {
		if (noDuplex) {
			f.accept(loggerPair.getLeft());
		} else {
			s.accept(loggerPair.getLeft(), loggerPair.getRight());
		}
	}

	private <T> T match(Function<Logger, T> f, BiFunction<Logger, Logger, T> s) {
		if (noDuplex) {
			return f.apply(loggerPair.getLeft());
		} else {
			return s.apply(loggerPair.getLeft(), loggerPair.getRight());
		}
	}

	@Override
	public String getName() {
		return noDuplex
				? String.format("DuplexLogger(%s)", loggerPair.getLeft().getName())
				: String.format("DuplexLogger(%s, %s)", loggerPair.getLeft().getName(), loggerPair.getRight().getName());
	}

	@Override
	public boolean isTraceEnabled() {
		return match(
				Logger::isTraceEnabled,
				(a, b) -> a.isTraceEnabled() && b.isTraceEnabled()
		);
	}

	@Override
	public void trace(String msg) {
		match(
				logger -> logger.trace(msg),
				(a, b) -> {
					a.trace(msg);
					b.trace(msg);
				}
		);
	}

	@Override
	public void trace(String format, Object arg) {
		match(
				logger -> logger.trace(format, arg),
				(a, b) -> {
					a.trace(format, arg);
					b.trace(format, arg);
				}
		);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		match(
				logger -> logger.trace(format, arg1, arg2),
				(a, b) -> {
					a.trace(format, arg1, arg2);
					b.trace(format, arg1, arg2);
				}
		);
	}

	@Override
	public void trace(String format, Object... arguments) {
		match(
				logger -> logger.trace(format, arguments),
				(a, b) -> {
					a.trace(format, arguments);
					b.trace(format, arguments);
				}
		);
	}

	@Override
	public void trace(String msg, Throwable t) {
		match(
				logger -> logger.trace(msg, t),
				(a, b) -> {
					a.trace(msg, t);
					b.trace(msg, t);
				}
		);
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return match(
				logger -> logger.isTraceEnabled(marker),
				(a, b) -> a.isTraceEnabled(marker) && b.isTraceEnabled(marker)
		);
	}

	@Override
	public void trace(Marker marker, String msg) {
		match(
				logger -> logger.trace(marker, msg),
				(a, b) -> {
					a.trace(marker, msg);
					b.trace(marker, msg);
				}
		);
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		match(
				logger -> logger.trace(marker, format, arg),
				(a, b) -> {
					a.trace(marker, format, arg);
					b.trace(marker, format, arg);
				}
		);
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		match(
				logger -> logger.trace(marker, format, arg1, arg2),
				(a, b) -> {
					a.trace(marker, format, arg1, arg2);
					b.trace(marker, format, arg1, arg2);
				}
		);
	}

	@Override
	public void trace(Marker marker, String format, Object... argArray) {
		match(
				logger -> logger.trace(marker, format, argArray),
				(a, b) -> {
					a.trace(marker, format, argArray);
					b.trace(marker, format, argArray);
				}
		);
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		match(
				logger -> logger.trace(marker, msg, t),
				(a, b) -> {
					a.trace(marker, msg, t);
					b.trace(marker, msg, t);
				}
		);
	}

	@Override
	public boolean isDebugEnabled() {
		return match(
				Logger::isDebugEnabled,
				(a, b) -> a.isDebugEnabled() && b.isDebugEnabled()
		);
	}

	@Override
	public void debug(String msg) {
		match(
				logger -> logger.debug(msg),
				(a, b) -> {
					a.debug(msg);
					b.debug(msg);
				}
		);
	}

	@Override
	public void debug(String format, Object arg) {
		match(
				logger -> logger.debug(format, arg),
				(a, b) -> {
					a.debug(format, arg);
					b.debug(format, arg);
				}
		);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		match(
				logger -> logger.debug(format, arg1, arg2),
				(a, b) -> {
					a.debug(format, arg1, arg2);
					b.debug(format, arg1, arg2);
				}
		);
	}

	@Override
	public void debug(String format, Object... arguments) {
		match(
				logger -> logger.debug(format, arguments),
				(a, b) -> {
					a.debug(format, arguments);
					b.debug(format, arguments);
				}
		);
	}

	@Override
	public void debug(String msg, Throwable t) {
		match(
				logger -> logger.debug(msg, t),
				(a, b) -> {
					a.debug(msg, t);
					b.debug(msg, t);
				}
		);
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return match(
				logger -> logger.isDebugEnabled(marker),
				(a, b) -> a.isDebugEnabled(marker) && b.isDebugEnabled(marker)
		);
	}

	@Override
	public void debug(Marker marker, String msg) {
		match(
				logger -> logger.debug(marker, msg),
				(a, b) -> {
					a.debug(marker, msg);
					b.debug(marker, msg);
				}
		);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		match(
				logger -> logger.debug(marker, format, arg),
				(a, b) -> {
					a.debug(marker, format, arg);
					b.debug(marker, format, arg);
				}
		);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		match(
				logger -> logger.debug(marker, format, arg1, arg2),
				(a, b) -> {
					a.debug(marker, format, arg1, arg2);
					b.debug(marker, format, arg1, arg2);
				}
		);
	}

	@Override
	public void debug(Marker marker, String format, Object... argArray) {
		match(
				logger -> logger.debug(marker, format, argArray),
				(a, b) -> {
					a.debug(marker, format, argArray);
					b.debug(marker, format, argArray);
				}
		);
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
		match(
				logger -> logger.debug(marker, msg, t),
				(a, b) -> {
					a.debug(marker, msg, t);
					b.debug(marker, msg, t);
				}
		);
	}

	@Override
	public boolean isInfoEnabled() {
		return match(
				Logger::isInfoEnabled,
				(a, b) -> a.isInfoEnabled() && b.isInfoEnabled()
		);
	}

	@Override
	public void info(String msg) {
		match(
				logger -> logger.info(msg),
				(a, b) -> {
					a.info(msg);
					b.info(msg);
				}
		);
	}

	@Override
	public void info(String format, Object arg) {
		match(
				logger -> logger.info(format, arg),
				(a, b) -> {
					a.info(format, arg);
					b.info(format, arg);
				}
		);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		match(
				logger -> logger.info(format, arg1, arg2),
				(a, b) -> {
					a.info(format, arg1, arg2);
					b.info(format, arg1, arg2);
				}
		);
	}

	@Override
	public void info(String format, Object... arguments) {
		match(
				logger -> logger.info(format, arguments),
				(a, b) -> {
					a.info(format, arguments);
					b.info(format, arguments);
				}
		);
	}

	@Override
	public void info(String msg, Throwable t) {
		match(
				logger -> logger.info(msg, t),
				(a, b) -> {
					a.info(msg, t);
					b.info(msg, t);
				}
		);
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return match(
				logger -> logger.isInfoEnabled(marker),
				(a, b) -> a.isInfoEnabled(marker) && b.isInfoEnabled(marker)
		);
	}

	@Override
	public void info(Marker marker, String msg) {
		match(
				logger -> logger.info(marker, msg),
				(a, b) -> {
					a.info(marker, msg);
					b.info(marker, msg);
				}
		);
	}

	@Override
	public void info(Marker marker, String format, Object arg) {
		match(
				logger -> logger.info(marker, format, arg),
				(a, b) -> {
					a.info(marker, format, arg);
					b.info(marker, format, arg);
				}
		);
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		match(
				logger -> logger.info(marker, format, arg1, arg2),
				(a, b) -> {
					a.info(marker, format, arg1, arg2);
					b.info(marker, format, arg1, arg2);
				}
		);
	}

	@Override
	public void info(Marker marker, String format, Object... argArray) {
		match(
				logger -> logger.info(marker, format, argArray),
				(a, b) -> {
					a.info(marker, format, argArray);
					b.info(marker, format, argArray);
				}
		);
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
		match(
				logger -> logger.info(marker, msg, t),
				(a, b) -> {
					a.info(marker, msg, t);
					b.info(marker, msg, t);
				}
		);
	}

	@Override
	public boolean isWarnEnabled() {
		return match(
				Logger::isWarnEnabled,
				(a, b) -> a.isWarnEnabled() && b.isWarnEnabled()
		);
	}

	@Override
	public void warn(String msg) {
		match(
				logger -> logger.warn(msg),
				(a, b) -> {
					a.warn(msg);
					b.warn(msg);
				}
		);
	}

	@Override
	public void warn(String format, Object arg) {
		match(
				logger -> logger.warn(format, arg),
				(a, b) -> {
					a.warn(format, arg);
					b.warn(format, arg);
				}
		);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		match(
				logger -> logger.warn(format, arg1, arg2),
				(a, b) -> {
					a.warn(format, arg1, arg2);
					b.warn(format, arg1, arg2);
				}
		);
	}

	@Override
	public void warn(String format, Object... arguments) {
		match(
				logger -> logger.warn(format, arguments),
				(a, b) -> {
					a.warn(format, arguments);
					b.warn(format, arguments);
				}
		);
	}

	@Override
	public void warn(String msg, Throwable t) {
		match(
				logger -> logger.warn(msg, t),
				(a, b) -> {
					a.warn(msg, t);
					b.warn(msg, t);
				}
		);
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return match(
				logger -> logger.isWarnEnabled(marker),
				(a, b) -> a.isWarnEnabled(marker) && b.isWarnEnabled(marker)
		);
	}

	@Override
	public void warn(Marker marker, String msg) {
		match(
				logger -> logger.warn(marker, msg),
				(a, b) -> {
					a.warn(marker, msg);
					b.warn(marker, msg);
				}
		);
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		match(
				logger -> logger.warn(marker, format, arg),
				(a, b) -> {
					a.warn(marker, format, arg);
					b.warn(marker, format, arg);
				}
		);
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		match(
				logger -> logger.warn(marker, format, arg1, arg2),
				(a, b) -> {
					a.warn(marker, format, arg1, arg2);
					b.warn(marker, format, arg1, arg2);
				}
		);
	}

	@Override
	public void warn(Marker marker, String format, Object... argArray) {
		match(
				logger -> logger.warn(marker, format, argArray),
				(a, b) -> {
					a.warn(marker, format, argArray);
					b.warn(marker, format, argArray);
				}
		);
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
		match(
				logger -> logger.warn(marker, msg, t),
				(a, b) -> {
					a.warn(marker, msg, t);
					b.warn(marker, msg, t);
				}
		);
	}

    @Override
    public boolean isErrorEnabled() {
        return match(
                Logger::isErrorEnabled,
                (a, b) -> a.isErrorEnabled() && b.isErrorEnabled()
        );
    }

    @Override
    public void error(String msg) {
        match(
                logger -> logger.error(msg),
                (a, b) -> {
                    a.error(msg);
                    b.error(msg);
                }
        );
    }

    @Override
    public void error(String format, Object arg) {
        match(
                logger -> logger.error(format, arg),
                (a, b) -> {
                    a.error(format, arg);
                    b.error(format, arg);
                }
        );
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        match(
                logger -> logger.error(format, arg1, arg2),
                (a, b) -> {
                    a.error(format, arg1, arg2);
                    b.error(format, arg1, arg2);
                }
        );
    }

    @Override
    public void error(String format, Object... arguments) {
        match(
                logger -> logger.error(format, arguments),
                (a, b) -> {
                    a.error(format, arguments);
                    b.error(format, arguments);
                }
        );
    }

    @Override
    public void error(String msg, Throwable t) {
        match(
                logger -> logger.error(msg, t),
                (a, b) -> {
                    a.error(msg, t);
                    b.error(msg, t);
                }
        );
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return match(
                logger -> logger.isErrorEnabled(marker),
                (a, b) -> a.isErrorEnabled(marker) && b.isErrorEnabled(marker)
        );
    }

    @Override
    public void error(Marker marker, String msg) {
        match(
                logger -> logger.error(marker, msg),
                (a, b) -> {
                    a.error(marker, msg);
                    b.error(marker, msg);
                }
        );
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        match(
                logger -> logger.error(marker, format, arg),
                (a, b) -> {
                    a.error(marker, format, arg);
                    b.error(marker, format, arg);
                }
        );
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        match(
                logger -> logger.error(marker, format, arg1, arg2),
                (a, b) -> {
                    a.error(marker, format, arg1, arg2);
                    b.error(marker, format, arg1, arg2);
                }
        );
    }

    @Override
    public void error(Marker marker, String format, Object... argArray) {
        match(
                logger -> logger.error(marker, format, argArray),
                (a, b) -> {
                    a.error(marker, format, argArray);
                    b.error(marker, format, argArray);
                }
        );
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        match(
                logger -> logger.error(marker, msg, t),
                (a, b) -> {
                    a.error(marker, msg, t);
                    b.error(marker, msg, t);
                }
        );
    }
}
