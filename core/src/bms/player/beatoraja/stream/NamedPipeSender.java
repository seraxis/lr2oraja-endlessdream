package bms.player.beatoraja.stream;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

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
		} catch (Exception e) {
			logger.debug("Named pipe送信失敗({}): {}", pipePath, e.getMessage());
		}
	}
}
