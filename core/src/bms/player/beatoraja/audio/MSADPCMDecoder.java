package bms.player.beatoraja.audio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import bms.player.beatoraja.TableDataAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/* 
 * This class decodes Microsoft's WAV ADPCM codec into 16-bit PCM.
 * This is specifically MS-ADPCM, or RIFF Audio ID 0x0002 
 * 
 * @author Sarah A.
 */
public class MSADPCMDecoder {
    private static final Logger logger = LoggerFactory.getLogger(MSADPCMDecoder.class);

    private static final int[] AdaptionTable = {
        230, 230, 230, 230, 307, 409, 512, 614,
        768, 614, 512, 409, 307, 230, 230, 230
    };

    private static final int[] InitializationCoeff1 = {
            64, 128, 0, 48, 60, 115, 98
    };
    private static final int[] InitializationCoeff2 = {
            0, -64, 0, 16, 0, -52, -58
    };

    private int[] adaptCoeff1;
    private int[] adaptCoeff2;

    private int[] initialDelta;
    private int[] sample1;
    private int[] sample2;
    private short[][] channelSamples;

    private final int samplesPerBlock;
    private final int channels;
    private final int blockSize;
    private final int sampleRate;

    public MSADPCMDecoder(int channels, int sampleRate, int blockAlign) {
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.blockSize = blockAlign;
        // sizeof(header) = 7
        // each header contains two samples
        // channels * 2 + (blockSize - channels * sizeof(header)) * 2 ==> (blockSize - channels * 6) * 2
        this.samplesPerBlock = (blockSize - channels * 6) * 2 / channels;
    }

    public ByteBuffer decode(ByteBuffer in) throws IOException {
        // init decode context
        adaptCoeff1 = new int[channels];
        adaptCoeff2 = new int[channels];
        initialDelta = new int[channels];
        sample1 = new int[channels];
        sample2 = new int[channels];
        if (channels > 2)
            channelSamples = new short[channels][samplesPerBlock];

        if ((in.remaining() % blockSize) != 0){
            logger.error("Malformed MS ADPCM block");
            throw new IOException("too few elements left in input buffer");
            // Note: ffmpeg doesn't process incomplete blocks.
        }
        int blockCount = in.remaining() / blockSize;
        int blockSampleSize = samplesPerBlock * channels * 2;
        ByteBuffer out = ByteBuffer.allocate(blockCount * blockSampleSize);
        out.order(ByteOrder.LITTLE_ENDIAN);

        while (in.hasRemaining()) {
//            in.get(adpcmBlock, 0, blockSize);
            ByteBuffer block = in.slice();
            block.limit(blockSize);
            block = block.order(ByteOrder.LITTLE_ENDIAN);
            decode_block(out.asShortBuffer(), block);
            in.position(in.position() + blockSize);
            out.position(out.position() + blockSampleSize);
        }
//        logger.info("Return hit");
        out.flip();
        return out;
    }

    private void decode_block(ShortBuffer out, ByteBuffer blockData) throws IOException {
        //logger.info("decoding block");

        if (channels > 2) {
            // When channels > 2, channels are NOT interleaved.
            for (int ch = 0; ch < channels; ch++) {
                int predictor = Byte.toUnsignedInt(blockData.get());
                if (predictor > 6) {
                    logger.warn("Malformed block header");
                    throw new IOException("Malformed block header. Expected range for predictor 0..6, found "+ predictor);
                }

                // Initialize the Adaption coefficients for each channel by indexing
                // into the coeff. table with the predictor value (range 0..6)
                adaptCoeff1[ch] = InitializationCoeff1[predictor];
                adaptCoeff2[ch] = InitializationCoeff2[predictor];

                initialDelta[ch] = blockData.getShort();

                // Acquire initial uncompressed signed 16 bit PCM samples for initialization
                sample1[ch] = blockData.getShort();

                sample2[ch] = blockData.getShort();

                int samplePtr = 0;
                channelSamples[ch][samplePtr++] = (short) sample2[ch];
                channelSamples[ch][samplePtr++] = (short) sample1[ch];

                for (int n = (samplesPerBlock - 2) >> 1; n > 0; n--){
                    int currentByte = Byte.toUnsignedInt(blockData.get());

                    channelSamples[ch][samplePtr++] = expandNibble((currentByte & 0xFF) >> 4, ch);
                    channelSamples[ch][samplePtr++] = expandNibble((currentByte & 0xFF) & 0xf, ch);
                }
            }
            // interleave samples
            for (int i = 0; i < samplesPerBlock; i++){
                for (int j = 0; j < channels; j++){
                    out.put(channelSamples[j][i]);
                }
            }
        } else {
            /*
             * Obtain ADPCM block preamble for all channels.
             * Channels are interleaved for the block predictor.
             * iDelta, Sample's 1 and 2 are all signed 16 bit Shorts in little endian
             *
             * Here is an example block preamble layout for stereo
             *    Byte          Description
             *   ----------------------------------
             *      0       left  channel block predictor
             *      1       right channel block predictor
             *      2       left  channel idelta LOW
             *      3       left  channel idelta HIGH
             *      4       right channel idelta LOW
             *      5       right channel idelta HIGH
             *      6       left  channel sample1 LOW
             *      7       left  channel sample1 HIGH
             *      8       right channel sample1 LOW
             *      9       right channel sample1 HIGH
             *     10       left  channel sample2 LOW
             *     11       left  channel sample2 HIGH
             *     12       right channel sample2 LOW
             *     13       right channel sample2 HIGH
             */
            for (int ch = 0; ch < channels; ch++) {
                int predictor = Byte.toUnsignedInt(blockData.get());
                if (predictor > 6) {
                    logger.warn("Malformed block header");
                    throw new IOException("Malformed block header. Expected range for predictor 0..6, found "+ predictor);
                }

                // Initialize the Adaption coefficients for each channel by indexing
                // into the coeff. table with the predictor value (range 0..6)
                adaptCoeff1[ch] = InitializationCoeff1[predictor];
                adaptCoeff2[ch] = InitializationCoeff2[predictor];
            }

            for (int ch = 0; ch < channels; ch++) {
                initialDelta[ch] = blockData.getShort();
            }

            // Acquire initial uncompressed signed 16 bit PCM samples for initialization
            for (int ch = 0; ch < channels; ch++) {
                sample1[ch] = blockData.getShort();
            }

            for (int ch = 0; ch < channels; ch++) {
                sample2[ch] = blockData.getShort();
            }

            for (int ch = 0; ch < channels; ch++) {
                out.put((short) sample2[ch]);
            }

            for (int ch = 0; ch < channels; ch++) {
                out.put((short) sample1[ch]);
            }

            int ch = 0;

            // for (n = (nb_samples - 2) >> (1 - stereo); n > 0; n--)
            while (blockData.hasRemaining()) {
                int currentByte = Byte.toUnsignedInt(blockData.get());

                out.put(expandNibble((currentByte & 0xFF) >> 4, ch));
                ch = (ch + 1) % channels;

                out.put(expandNibble((currentByte & 0xFF) & 0xf, ch));
                ch = (ch + 1) % channels;
            }
        }


        //logger.info("===== BLOCK FINISH =====");
    }
    

    private short expandNibble(int nibble, int channel) {
        int signed;
        if (nibble >= 8) {
            signed = nibble - 16;
        } else {
            signed = nibble;
        }
        

        short predictor;
        int result = (sample1[channel] * adaptCoeff1[channel]) + (sample2[channel] * adaptCoeff2[channel]);
        predictor = clamp((result >> 6) + (signed * initialDelta[channel]));

        sample2[channel] = sample1[channel];
        sample1[channel] = predictor;

        initialDelta[channel] = (AdaptionTable[nibble] * initialDelta[channel]) >> 8;
        if (initialDelta[channel] < 16) {
            initialDelta[channel] = 16;
        }
        if (initialDelta[channel] > Integer.MAX_VALUE/768){
            logger.warn("idelta overflow");
            initialDelta[channel] = Integer.MAX_VALUE/768;
        }
        return predictor;
    }

    private short clamp (int value) {
        return (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, value));
    }
}
