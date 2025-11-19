package bms.player.beatoraja.audio;

import bms.model.BMSModel;
import bms.player.beatoraja.Config;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bms.player.beatoraja.PerformanceMetrics;
import io.github.llm96.ebur128java.Channel;
import io.github.llm96.ebur128java.Error;
import io.github.llm96.ebur128java.Mode;
import io.github.llm96.ebur128java.State;

public class BMSLoudnessAnalyzer {
	private static final Logger logger = LoggerFactory.getLogger(BMSLoudnessAnalyzer.class);

	private final ExecutorService executor;
	private final boolean available;
	private final Path cacheDir;

	public static class AnalysisResult {
		public final BMSModel model;
		public final double loudnessLUFS;
		public final boolean success;
		public final String errorMessage;

		public AnalysisResult(BMSModel model, double loudnessLUFS) {
			this.model = model;
			this.loudnessLUFS = loudnessLUFS;
			this.success = true;
			this.errorMessage = null;
		}

		public AnalysisResult(BMSModel model, String errorMessage) {
			this.model = model;
			this.loudnessLUFS = Double.NaN;
			this.success = false;
			this.errorMessage = errorMessage;
		}

		public float calculateAdjustedVolume(float baseVolume) {
			if (!success || Double.isNaN(loudnessLUFS)) {
				return baseVolume;
			}

			// Average loudness level (50% volume)
			final double AVERAGE_LUFS = -12.00;

			double loudnessDiff = loudnessLUFS - AVERAGE_LUFS;
			double gainAdjustment = Math.pow(10.0, -loudnessDiff / 20.0);

			float adjustedVolume = (float) (0.5f * gainAdjustment);
			return Math.max(0.0f, Math.min(1.0f, adjustedVolume));
		}
	}

	public BMSLoudnessAnalyzer(Config config) {
		this.available = checkLibraryAvailable();
		this.cacheDir = Paths.get("cache/normalize");

		if (config.getAudioConfig().isNormalizeVolume()) {
			try {
				Files.createDirectories(cacheDir);
			} catch (Exception e) {
				logger.warn("Failed to create cache directory: {}", e.getMessage());
			}
		}

		if (available) {
			this.executor = Executors.newSingleThreadExecutor(r -> {
				Thread t = new Thread(r, "analyze");
				t.setDaemon(true);
				return t;
			});
		} else {
			this.executor = null;
		}
	}

	public boolean isAvailable() {
		return available;
	}

	private boolean checkLibraryAvailable() {
		try {
			// Try to use the library to see if it's working
			State testState = new State(2, 44100, Mode.MODE_I);
			testState.close();
			return true;
		} catch (Exception e) {
			logger.warn("libebur128 not available: {}", e.getMessage());
			return false;
		}
	}

	public Future<AnalysisResult> analyzeAsync(BMSModel model) {
		if (!available) {
			return CompletableFuture.completedFuture(
				new AnalysisResult(model, "libebur128 not available")
			);
		}

		return executor.submit(() -> analyze(model));
	}

	private AnalysisResult analyze(BMSModel model) {
		try {
			// Check cache first
			String hash = model.getSHA256();
			if (hash != null && !hash.isEmpty()) {
				Double value = readFromCache(hash);
				if (value != null) {
					return new AnalysisResult(model, value);
				}
			}

			BMSRenderer.RenderResult result;

			try (var perf = PerformanceMetrics.get().Event("Render BMS to PCM")) {
				BMSRenderer renderer = new BMSRenderer();
				result = renderer.renderBMS(model, 10 * 60 * 1000); // 10 minute limit
				if (result == null) {
					return new AnalysisResult(model, "Failed to render BMS file");
				}
			}

			try (var perf = PerformanceMetrics.get().Event("Analyze chart loudness")) {
				double loudness = analyzeLoudness(result);
				if (hash != null && !hash.isEmpty()) {
					writeToCache(hash, loudness);
				}
				return new AnalysisResult(model, loudness);
			}
		} catch (Exception e) {
			logger.error("Loudness analysis failed: {}", e.getMessage());
			e.printStackTrace();
			return new AnalysisResult(model, "Analysis error: " + e.getMessage());
		}
	}

	private double analyzeLoudness(BMSRenderer.RenderResult result) {
		try (State state = new State(result.channels, result.sampleRate, Mode.MODE_I)) {
			if (result.channels == 2) {
				state.setChannel(0, Channel.LEFT);
				state.setChannel(1, Channel.RIGHT);
			}

			// Convert PCM data to short array
			ByteBuffer pcmData = result.pcmData.duplicate();
			pcmData.rewind();
			pcmData.order(ByteOrder.LITTLE_ENDIAN);

			int total = pcmData.remaining() / 2;
			short[] samples = new short[total];
			pcmData.asShortBuffer().get(samples);

			long frames = total / result.channels;
			int ret = state.addFramesShort(samples, frames);
			if (ret != Error.SUCCESS) {
				throw new RuntimeException("Failed to add frames: error code " + ret);
			}

			double loudness = state.getLoudnessGlobal();
			if (Double.isInfinite(loudness) && loudness < 0) {
				throw new RuntimeException("Failed to get integrated loudness");
			}
			return loudness;
		}
	}

	private Double readFromCache(String hash) {
		try {
			Path cacheFile = cacheDir.resolve(hash + ".lufs");
			if (Files.exists(cacheFile)) {
				String content = new String(Files.readAllBytes(cacheFile)).trim();
				return Double.parseDouble(content);
			}
		} catch (Exception e) {
			logger.warn("Failed to read cache for {}: {}", hash, e.getMessage());
		}
		return null;
	}

	private void writeToCache(String hash, double loudness) {
		try {
			Path cacheFile = cacheDir.resolve(hash + ".lufs");
			Files.write(cacheFile, String.valueOf(loudness).getBytes());
			logger.info("Cached loudness for {}: {} LUFS", hash, loudness);
		} catch (Exception e) {
			logger.warn("Failed to write cache for {}: {}", hash, e.getMessage());
		}
	}

	public void shutdown() {
		if (executor != null) {
			executor.shutdown();
			try {
				if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
			}
		}
	}
}
