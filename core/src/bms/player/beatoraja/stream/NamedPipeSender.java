package bms.player.beatoraja.stream;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NamedPipeSender {
	private static final Logger logger = LoggerFactory.getLogger(NamedPipeSender.class);

	private final String pipePath;

	public NamedPipeSender(String pipeName) {
		pipePath = "\\\\.\\pipe\\" + pipeName;
	}

	public void sendLine(String line) {
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(pipePath), StandardCharsets.UTF_8))) {
			writer.write(line);
			writer.newLine();
			dump(line, null);
		} catch (Exception e) {
			dump(line, e);
			logger.debug("Named pipe送信失敗({}): {}", pipePath, e.getMessage());
		}
	}

	private void dump(String line, Exception error) {
		try {
			Path logDir = Path.of("log");
			Files.createDirectories(logDir);
			StringBuilder dump = new StringBuilder();
			dump.append('{');
			appendJsonField(dump, "time", LocalDateTime.now().toString()).append(',');
			appendJsonField(dump, "pipe", pipePath).append(',');
			appendJsonField(dump, "status", error == null ? "sent" : "failed").append(',');
			appendJsonField(dump, "error", error == null ? "" : error.getMessage()).append(',');
			appendJsonField(dump, "line", line);
			dump.append("}\n");
			Files.writeString(logDir.resolve("oraja_helper_pipe_send.jsonl"), dump.toString(), StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (Exception ignored) {
		}
	}

	private StringBuilder appendJsonField(StringBuilder builder, String name, String value) {
		builder.append('"').append(escapeJson(name)).append("\":\"").append(escapeJson(value)).append('"');
		return builder;
	}

	private String escapeJson(String value) {
		if (value == null) {
			return "";
		}
		StringBuilder escaped = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '"' -> escaped.append("\\\"");
			case '\\' -> escaped.append("\\\\");
			case '\b' -> escaped.append("\\b");
			case '\f' -> escaped.append("\\f");
			case '\n' -> escaped.append("\\n");
			case '\r' -> escaped.append("\\r");
			case '\t' -> escaped.append("\\t");
			default -> {
				if (c < 0x20) {
					escaped.append(String.format("\\u%04x", (int) c));
				} else {
					escaped.append(c);
				}
			}
			}
		}
		return escaped.toString();
	}
}
