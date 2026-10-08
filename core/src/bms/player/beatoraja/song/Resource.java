package bms.player.beatoraja.song;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A resource is a "thing" referenced by a chart - a song or an image or whatever.
 *
 * It is either backed by disk bytes with a filepath, or in-memory. Backbeat stores
 * small assets in memory, which is a colossal performance improvement.
 *
 * Passing this around, and you just get an interface with the bits you need to interact
 * with an asset.
 */
public interface Resource {
	/**
	 * Unique ID for caching.
	 */
	String key();

	/**
	 * Name of this asset (file extension usually qualifies how to interpret it)
	 */
	String filename();

	/**
	 * Get da bytes
	 */
	InputStream openStream() throws IOException;

	/**
	 * Filesystem path if present
	 */
	Optional<Path> path();

	default byte[] readAllBytes() throws IOException {
		try (InputStream input = openStream()) {
			return input.readAllBytes();
		}
	}

	/**
	 * Make a new resource from a path to a file
	 */
	static Resource file(Path path) {
		return file(path, path.getFileName().toString());
	}

	static Resource file(Path path, String filename) {
		Path absolute = path.toAbsolutePath().normalize();
		return new Resource() {
			@Override
			public String key() {
				return "file:" + absolute;
			}

			@Override
			public String filename() {
				return filename;
			}

			@Override
			public InputStream openStream() throws IOException {
				return Files.newInputStream(absolute);
			}

			@Override
			public Optional<Path> path() {
				return Optional.of(absolute);
			}
		};
	}

	/**
	 * Make a new resource from a set of in-memory bytes (and the filename it should have)
	 */
	static Resource bytes(String key, String filename, byte[] data) {
		byte[] owned = data.clone();
		return new Resource() {
			@Override
			public String key() {
				return key;
			}

			@Override
			public String filename() {
				return filename;
			}

			@Override
			public InputStream openStream() {
				return new ByteArrayInputStream(owned);
			}

			@Override
			public Optional<Path> path() {
				return Optional.empty();
			}
		};
	}
}
