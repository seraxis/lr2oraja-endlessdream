package bms.player.beatoraja.audio;

import bms.tool.util.Pair;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SevenZArchiveContext loads a 7z archive file eagerly because 7z file is forcing to be read sequentially.
 *
 * @implNote All the query functions must match the filename without extension. This is because the parameter passed
 *  to here are normally ends with '.wav', while the actual file provided in archive file can be .wav, .ogg, .walc so
 *  we have to compare then without extension name.
 */
public class SevenZArchiveContext {
	/**
	 * file name(with extension name) => input stream
	 */
	private final ConcurrentHashMap<String, ByteArrayInputStream> data = new ConcurrentHashMap<>();

	private SevenZArchiveContext(Path path) throws IOException {
		try (SevenZFile sevenZFile = SevenZFile.builder().setFile(path.toFile()).get()) {
			SevenZArchiveEntry entry;
			while ((entry = sevenZFile.getNextEntry()) != null) {
				InputStream is = sevenZFile.getInputStream(entry);
				byte[] bytes = is.readAllBytes();
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
				data.put(entry.getName(), byteArrayInputStream);
			}
		}
	}

	public static SevenZArchiveContext create(Path path) {
		try {
			return new SevenZArchiveContext(path);
		} catch (IOException e) {
			return null;
		}
	}

	public Pair<String, InputStream> getInputStream(String name) {
		Optional<Map.Entry<String, ByteArrayInputStream>> any = data.entrySet().stream().filter(entry -> FilenameUtils.removeExtension(entry.getKey()).equals(FilenameUtils.removeExtension(name))).findAny();
		return any.map(entry -> Pair.of(entry.getKey(), (InputStream) entry.getValue())).orElse(null);
	}

	public boolean hasEntry(String fileName) {
		return data.entrySet().stream().anyMatch(entry -> FilenameUtils.removeExtension(entry.getKey()).equals(FilenameUtils.removeExtension(fileName)));
	}
}
