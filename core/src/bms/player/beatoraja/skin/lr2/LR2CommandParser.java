package bms.player.beatoraja.skin.lr2;

import bms.player.beatoraja.skin.lr2.commands.CSVField;
import bms.player.beatoraja.skin.lr2.commands.LR2Command;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Helper class in charge of parsing csv line into a LR2Command. This class doesn't provide a common constructor, use
 *  getInstance instead. This is because this class is using annotation scan to automatically register classes.
 */
public class LR2CommandParser {
	private static final Logger logger = LoggerFactory.getLogger(LR2CommandParser.class);
	public static final String PACKAGE_NAME = "bms.player.beatoraja.skin.lr2.commands";
	private final Map<String, Class<?>> typeClassMap = new HashMap<>();

	private LR2CommandParser() {
		try {
			Reflections reflections = new Reflections(PACKAGE_NAME);
			Set<Class<?>> classes = reflections.getTypesAnnotatedWith(LR2Command.class);
			classes.forEach(clazz -> {
				LR2Command annotation = clazz.getAnnotation(LR2Command.class);

				String name = annotation.value();
				typeClassMap.put(name, clazz);
			});
		} catch (Exception e) {
			logger.error("Failed to scan LR2 commands", e);
			throw new RuntimeException(e);
		}
	}

	public static LR2CommandParser getInstance() {
		return LazyHolder.instance;
	}

	/**
	 * Parse one csv line
	 * @param line one csv line, the first element must be the command name
	 * @return a LR2Command instance
	 */
	public <T> T parse(String line) {
		String[] fields = splitCsvLine(line);
		if (fields.length == 0) {
			throw new IllegalArgumentException("Unexpected blank line");
		}
		String commandName = fields[0];
		String[] data = Arrays.copyOfRange(fields, 1, fields.length);
		return parse(commandName, data);
	}

	/**
	 * Wrapper of parseObject function, for compatibility of old implementation
	 */
	public <T> T parse(String[] fields) {
		String commandName = fields[0];
		String[] data = Arrays.copyOfRange(fields, 1, fields.length);
		return parse(commandName, data);
	}

	private <T> T parse(String commandName, String[] data) {
		Class<?> clazz = typeClassMap.get(commandName);
		if (clazz == null) {
			throw new IllegalArgumentException("Unknown LR2Command: " + commandName);
		}

		try {
			return ((T) parseObject(clazz, data, true));
		} catch (Exception e) {
			logger.error("Failed to parse LR2Command", e);
			return null;
		}
	}

	/**
	 * Parse an object recursively
	 * @param clazz class want to be converted
	 * @param data  elements, must be started after the name of the command
	 * @param isTopLevel false means we are handling an nested object, otherwise we're not
	 */
	private Object parseObject(Class<?> clazz, String[] data, boolean isTopLevel) throws Exception {
		Object instance = clazz.getDeclaredConstructor().newInstance();
		List<Field> fields = getAllFields(clazz);

		int offset = 0;

		for (Field field : fields) {
			field.setAccessible(true);

			if (isTopLevel) {
				CSVField anno = field.getAnnotation(CSVField.class);
				if (anno == null) continue;

				if (anno.value() != -1) {
					int idx = anno.value() + offset;
					String rawValue = (idx < data.length) ? data[idx] : "";
					if (shouldSkip(rawValue, anno.optional())) {
						continue;
					}
					Object converted = convertValue(field.getType(), rawValue);
					if (anno.option()) {
						converted = LR2DestinationOptions.convert(((int) converted));
					}
					field.set(instance, converted);
				} else if (anno.start() != -1) {
					int start = anno.start() + offset;
					Class<?> fieldType = field.getType();
					int objectLength = getNestedObjectLength(fieldType);
					int end = Math.min(start + objectLength, data.length);
					if (start >= end) {
						// NOTE: This is *the expected behavior*, sometimes the csv line is not complete,
						//  we need to tolerate this error
						continue;
					}
					String[] subData = Arrays.copyOfRange(data, start, end);
					Object nested = parseObject(fieldType, subData, false);

					field.set(instance, nested);
					offset += end - start - 1;
				} else {
					throw new IllegalArgumentException("Field: " + field.getName() + "doesn't have value nor start");
				}
			} else {
				CSVField anno = field.getAnnotation(CSVField.class);
				if (anno == null) continue;

				int idx = anno.value();
				if (idx < 0 || idx >= data.length) {
					if (anno.optional()) {
						continue;
					} else {
						throw new IndexOutOfBoundsException("Inner object: " + idx + " > " + data.length);
					}
				}
				String rawValue = data[idx];
				if (shouldSkip(rawValue, anno.optional())) {
					continue;
				}
				Object converted = convertValue(field.getType(), rawValue);
				if (anno.option()) {
					converted = LR2DestinationOptions.convert(((int) converted));
				}
				field.set(instance, converted);
			}
		}
		return instance;
	}

	private boolean shouldSkip(String rawValue, boolean optional) {
		return rawValue == null || rawValue.isEmpty() || optional;
	}

	private int getNestedObjectLength(Class<?> clazz) {
		List<Field> fields = getAllFields(clazz);
		int maxOffset = -1;
		for (Field field : fields) {
			CSVField anno = field.getAnnotation(CSVField.class);
			if (anno != null) {
				maxOffset = Math.max(maxOffset, anno.value());
			}
		}
		return maxOffset + 1;
	}

	private Object convertValue(Class<?> targetType, String rawValue) {
		rawValue = rawValue.replace("!", "-").replace(" ", "");
		if (targetType == int.class || targetType == Integer.class) {
			try {
				return Integer.parseInt(rawValue.trim());
			} catch (NumberFormatException e) {
				return 0;
			}
		} else if (targetType == long.class || targetType == Long.class) {
			try {
				return Long.parseLong(rawValue.trim());
			} catch (NumberFormatException e) {
				return 0;
			}
		} else if (targetType == double.class || targetType == Double.class) {
			try {
				return Double.parseDouble(rawValue.trim());
			} catch (NumberFormatException e) {
				return 0.0;
			}
		} else if (targetType == boolean.class || targetType == Boolean.class) {
			return Boolean.parseBoolean(rawValue.trim());
		} else if (targetType == String.class) {
			return rawValue;
		} else if (targetType.isEnum()) {
			return Enum.valueOf((Class<Enum>) targetType, rawValue.trim().toUpperCase());
		} else {
			throw new IllegalArgumentException("Unspported type: " + targetType);
		}
	}

	private List<Field> getAllFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		while (clazz != null && clazz != Object.class) {
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		}
		return fields;
	}

	private String[] splitCsvLine(String line) {
		return line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	}

	private static class LazyHolder {
		public static final LR2CommandParser instance = new LR2CommandParser();
	}
}
