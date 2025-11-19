package bms.player.beatoraja.audio;

import bms.model.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BMSRenderer {
	private static final Logger logger = LoggerFactory.getLogger(BMSRenderer.class);

	private final int sampleRate;
	private final int channels;

	public BMSRenderer(int sampleRate, int channels) {
		this.sampleRate = sampleRate;
		this.channels = channels;
	}

	public BMSRenderer() {
		this(44100, 2);
	}

	public RenderResult render(Path bmsPath) {
		return render(bmsPath, 0);
	}

	public RenderResult render(Path bmsPath, long maxDurationMs) {
		logger.info("Starting BMS rendering: {}", bmsPath);

		ChartDecoder decoder = ChartDecoder.getDecoder(bmsPath);
		if (decoder == null) {
			logger.warn("Unsupported file format: {}", bmsPath);
			return null;
		}

		BMSModel model = decoder.decode(bmsPath);
		if (model == null) {
			logger.warn("Failed to load BMS file: {}", bmsPath);
			return null;
		}

		return renderBMS(model, maxDurationMs);
	}

	public RenderResult renderBMS(BMSModel model) {
		return renderBMS(model, 0);
	}

	public RenderResult renderBMS(BMSModel model, long maxDurationMs) {
		Map<Integer, PCM> wavCache = loadWavFiles(model);

		// Calculate output buffer size
		// (number of samples = sampling rate * seconds)
		long endTime = model.getLastMilliTime();
		
		// Apply time limit if specified (0 = no limit)
		if (maxDurationMs > 0 && endTime > maxDurationMs) {
			logger.info("Limiting render duration from {}ms to {}ms", endTime, maxDurationMs);
			endTime = maxDurationMs;
		}
		
		long totalSamples = endTime * sampleRate / 1000;
		int bytesPerSample = 2; // 16-bit
		int bufferSize = (int) (totalSamples * channels * bytesPerSample);

		logger.info("Rendering chart: 0ms - {}ms (total {} samples, {} bytes)", endTime, totalSamples, bufferSize);

		// Create output buffer
		ByteBuffer outputBuffer = ByteBuffer.allocate(bufferSize);
		outputBuffer.order(ByteOrder.LITTLE_ENDIAN);
		float[] mixBuffer = new float[(int) totalSamples * channels];

		// Process all timelines
		TimeLine[] timelines = model.getAllTimeLines();

		for (TimeLine tl : timelines) {
			long time = tl.getMilliTime();
			if (time >= endTime) {
				break;
			}
			for (Note note : tl.getBackGroundNotes()) {
				renderNote(note, time, wavCache, mixBuffer);
			}
			int lanes = model.getMode().key;
			for (int i = 0; i < lanes; i++) {
				Note note = tl.getNote(i);
				if (note != null) {
					renderNote(note, time, wavCache, mixBuffer);
					for (Note layered : note.getLayeredNotes()) {
						renderNote(layered, time, wavCache, mixBuffer);
					}
				}
			}
		}

		// Float -> Int16
        for (float sample : mixBuffer) {
			// -6dB headroom to try to alleviate clipping
            sample *= 0.5f;
            
            if (sample > 1.0f) sample = 1.0f;
            if (sample < -1.0f) sample = -1.0f;

            outputBuffer.putShort((short) (sample * 32767.0f));
        }

		outputBuffer.flip();

		RenderResult result = new RenderResult();
		result.pcmData = outputBuffer;
		result.sampleRate = sampleRate;
		result.channels = channels;
		result.durationMs = endTime;
		result.model = model;

		return result;
	}

	private boolean renderNote(Note note, long noteTime, Map<Integer, PCM> wavCache, float[] mixBuffer) {
		int wavId = note.getWav();
		if (wavId < 0) {
			return false;
		}
		PCM pcm = wavCache.get(wavId);
		if (pcm == null) {
			return false;
		}

		long startSample = noteTime * sampleRate / 1000;
		long microStartTime = note.getMicroStarttime();
		long microDuration = note.getMicroDuration();

		PCM renderPcm = pcm;
		if (microStartTime > 0 || microDuration > 0) {
			renderPcm = pcm.slice(microStartTime, microDuration);
			if (renderPcm == null) {
				return false;
			}
		}

		// Mix PCM data
		mixPCM(renderPcm, (int) startSample, mixBuffer);
		return true;
	}

	private void mixPCM(PCM pcm, int startSample, float[] mixBuffer) {
		if (pcm.sampleRate != sampleRate) {
			pcm = pcm.changeSampleRate(sampleRate);
		}
		if (pcm.channels != channels) {
			pcm = pcm.changeChannels(channels);
		}
		if (pcm instanceof ShortPCM) {
			mixShortPCM((ShortPCM) pcm, startSample, mixBuffer);
		} else if (pcm instanceof ShortDirectPCM) {
			mixShortDirectPCM((ShortDirectPCM) pcm, startSample, mixBuffer);
		} else if (pcm instanceof FloatPCM) {
			mixFloatPCM((FloatPCM) pcm, startSample, mixBuffer);
		} else if (pcm instanceof BytePCM) {
			mixBytePCM((BytePCM) pcm, startSample, mixBuffer);
		}
	}

	private void mixShortPCM(ShortPCM pcm, int startSample, float[] mixBuffer) {
		short[] samples = pcm.sample;
		int srcIndex = pcm.start;
		int dstIndex = startSample * channels;
		int len = pcm.len;

		for (int i = 0; i < len && dstIndex < mixBuffer.length; i++, srcIndex++, dstIndex++) {
			mixBuffer[dstIndex] += samples[srcIndex] / 32768.0f;
		}
	}

	private void mixShortDirectPCM(ShortDirectPCM pcm, int startSample, float[] mixBuffer) {
		ByteBuffer buffer = pcm.sample;
		int srcIndex = pcm.start * 2; // 2 bytes per short
		int dstIndex = startSample * channels;
		int len = pcm.len;

		for (int i = 0; i < len && dstIndex < mixBuffer.length; i++, srcIndex += 2, dstIndex++) {
			short sample = buffer.getShort(srcIndex);
			mixBuffer[dstIndex] += sample / 32768.0f;
		}
	}

	private void mixFloatPCM(FloatPCM pcm, int startSample, float[] mixBuffer) {
		float[] samples = pcm.sample;
		int srcIndex = pcm.start;
		int dstIndex = startSample * channels;
		int len = pcm.len;

		for (int i = 0; i < len && dstIndex < mixBuffer.length; i++, srcIndex++, dstIndex++) {
			mixBuffer[dstIndex] += samples[srcIndex];
		}
	}

	private void mixBytePCM(BytePCM pcm, int startSample, float[] mixBuffer) {
		byte[] samples = pcm.sample;
		int srcIndex = pcm.start;
		int dstIndex = startSample * channels;
		int len = pcm.len;

		for (int i = 0; i < len && dstIndex < mixBuffer.length; i++, srcIndex++, dstIndex++) {
			mixBuffer[dstIndex] += samples[srcIndex] / 128.0f;
		}
	}

	private Map<Integer, PCM> loadWavFiles(BMSModel model) {
		logger.info("Loading audio files...");

		Map<Integer, PCM> result = new HashMap<>();
		String[] wavList = model.getWavList();
		Path basePath = Paths.get(model.getPath()).getParent();

		int loaded = 0;
		DummyAudioDriver driver = new DummyAudioDriver(sampleRate, channels);
		for (int i = 0; i < wavList.length; i++) {
			if (wavList[i] == null || wavList[i].isEmpty()) {
				continue;
			}

			// Resolve audio file path
			Path wavPath = null;
			Path resolvedPath = basePath.resolve(wavList[i]).toAbsolutePath();
			Path[] candidates = AudioDriver.getPaths(resolvedPath.toString());

			for (Path candidate : candidates) {
				if (candidate.toFile().exists()) {
					wavPath = candidate;
					break;
				}
			}

			if (wavPath == null) {
				logger.warn("Audio file not found: {}", wavList[i]);
				continue;
			}

			// Load as PCM
			PCM pcm = PCM.load(wavPath, driver);
			if (pcm != null) {
				result.put(i, pcm);
				loaded++;
			} else {
				logger.trace("Failed to load audio file: {}", wavPath);
			}
		}

		logger.info("Audio files loaded: {} / {}", loaded, wavList.length);
		return result;
	}

	public static class RenderResult {
		public ByteBuffer pcmData;
		public int sampleRate;
		public int channels;
		public long durationMs;
		public BMSModel model;
	}

	private static class DummyAudioDriver extends AbstractAudioDriver<PCM> {

		public DummyAudioDriver(int sampleRate, int channels) {
			super(1);
			setSampleRate(sampleRate);
			this.channels = channels;
		}

		@Override
		protected PCM getKeySound(Path p) {
			return PCM.load(p, this);
		}

		@Override
		protected PCM getKeySound(PCM pcm) {
			return pcm;
		}

		@Override
		protected void disposeKeySound(PCM pcm) {
		}

		@Override
		protected void play(PCM wav, int channel, float volume, float pitch) {
		}

		@Override
		protected void play(AudioElement<PCM> id, float volume, boolean loop) {
		}

		@Override
		protected void setVolume(AudioElement<PCM> id, float volume) {
		}

		@Override
		protected boolean isPlaying(PCM id) {
			return false;
		}

		@Override
		protected void stop(PCM id) {
		}

		@Override
		protected void stop(PCM id, int channel) {
		}

		@Override
		protected void setVolume(PCM id, int channel, float volume) {
		}

		@Override
		public void dispose() {
		}
	}
}
