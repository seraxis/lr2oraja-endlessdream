import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.compress.utils.IOUtils;


/* 
 * This class decodes Microsoft's WAV ADPCM codec into 16-bit PCM.
 * This is specifically MS-ADPCM, or RIFF Audio ID 0x0002 
 * 
 * @author Sarah A.
 */
public class MSADPCMDecoder {


    public static void main(String[] args) {
        try (FileInputStream input = new FileInputStream("msadpcm.wav")) {
            input.skip(70);

            int channels = 2;
            int sampleRate = 44100;
            //bitsPerSample = input.bitsPerSample;
            int blockAlign = 2048;
            Logger.getGlobal().info("channels: " + channels);
            Logger.getGlobal().info("sample rate: " + sampleRate);
            Logger.getGlobal().info("block align" + blockAlign);

            byte[] bytes = IOUtils.toByteArray(input);
            ByteBuffer output = ByteBuffer.wrap(bytes);

            ByteBuffer pcm = ByteBuffer.wrap(bytes.order(ByteOrder.LITTLE_ENDIAN);
            pcm.limit(output.size());

            ByteBuffer result = ByteBuffer.allocate(output.size());
            MSADPCMDecoder decoder = new MSADPCMDecoder(channels, sampleRate, blockAlign);
            decoder.decode(pcm, result);

            pcm = result;

        } catch (Exception ex) {

        }
    }

    private static final int[] AdaptionTable = {
        230, 230, 230, 230, 307, 409, 521, 614,
        768, 614, 512, 409, 307, 230, 230, 230
    };

    private static final int[] InitializationCoeff1 = {
        256, 512, 0, 192, 240, 460, 392
    };
    private static final int[] InitializationCoeff2 = {
        0, -256, 0, 64, 0, -208, -232
    };

    private int[] AdaptCoeff1;
    private int[] AdaptCoeff2;

    private short[] InitialDelta = new short[2];
    private short[] Sample1 = new short[2];
    private short[] Sample2 = new short[2];

    private final short[] pcmBlock;
    private final byte[]  adpcmBlock;

    private final int samplesPerBlock;
    private final int channels;
    private final int blocksize;
    private final int sampleRate;

    public MSADPCMDecoder(int channels, int sampleRate, int blockAlign) {
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.blocksize = blockAlign;
        this.samplesPerBlock = (blocksize - channels * 4) * (channels ^ 3) + 1;

        pcmBlock        = new short[samplesPerBlock * channels];
        adpcmBlock      = new byte[blocksize];
    }

    public ByteBuffer decode(ByteBuffer in, ByteBuffer out) throws IOException {
        while (in.hasRemaining()) {
            int samplesPerBlock = (blocksize - channels * 4) * (channels ^ 3) + 1;
            int blockAdpcmSamples = samplesPerBlock;
            int blockPcmSamples   = samplesPerBlock;
            int currentBlockSize  = blocksize;

            int numSamples = out.remaining()/channels;
            if (blockAdpcmSamples > numSamples) {
                blockAdpcmSamples = ((numSamples + 6) & ~7) + 1;
                currentBlockSize  = (blockAdpcmSamples - 1) / (channels ^ 3) + (channels * 4);
                blockPcmSamples   = numSamples;
            }
            Logger.getGlobal().info("Good decode pass");
            Logger.getGlobal().warning("block size: " +currentBlockSize);
            Logger.getGlobal().warning("numSamples: " + numSamples );
            Logger.getGlobal().warning("blockadpcm samples " + blockAdpcmSamples);
            Logger.getGlobal().warning("in.remaining() " + in.remaining());
            Logger.getGlobal().warning("out.remaining() " + out.remaining());

            if (in.remaining() < currentBlockSize) {
                in.get(adpcmBlock, 0, in.remaining());
                //throw new IOException("too few elements left in input buffer");
            } else {
                in.get(adpcmBlock, 0, currentBlockSize);
            }


            decode_block(pcmBlock, adpcmBlock, currentBlockSize);


            out.asShortBuffer().put(pcmBlock, 0, blockPcmSamples * channels);
        }

        Logger.getGlobal().info("Return hit");
        return out;
    }
    
    private void decode_block(short[] out, byte[] block_data, int inSize) throws IOException {
        Logger.getGlobal().info("decoding block");
        
        AdaptCoeff1 = new int[channels];
        AdaptCoeff2 = new int[channels];
        
        int outPtr = 0;
        int inPtr = 0;
        
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
            int predictor = Byte.toUnsignedInt(block_data[inPtr]);
            if (predictor > 6) {
                Logger.getGlobal().warning("Malformed block header");
                throw new IOException("Malformed block header. Expected range for predictor 0..6, found "+ predictor);
            }
            inPtr += 1;
            
            // Initialize the Adaption coefficients for each channel by indexing
            // into the coeff. table with the predictor value (range 0..6)
            AdaptCoeff1[ch] = InitializationCoeff1[predictor];
            AdaptCoeff2[ch] = InitializationCoeff2[predictor];
        }

        // TODO: Revisit this index maths
        for (int ch = 0; ch < channels; ch++) {
            ByteBuffer iDeltaBuf = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
            iDeltaBuf.put(block_data[inPtr]);
            iDeltaBuf.put(block_data[inPtr + 1]);
            InitialDelta[ch] = iDeltaBuf.getShort(0); 
            inPtr += 2;
        }

        for (int ch = 0; ch < channels; ch++) {
            // Acquire initial uncompressed signed 16 bit PCM samples for initialization
            ByteBuffer Sample1Buf = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
            Sample1Buf.put(block_data[inPtr]);
            Sample1Buf.put(block_data[inPtr + 1]);
            Sample1[ch] = Sample1Buf.getShort(0);
            inPtr += 2;
        }

        for (int ch = 0; ch < channels; ch++) {
            ByteBuffer Sample2Buf = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
            Sample2Buf.put(block_data[inPtr]);
            Sample2Buf.put(block_data[inPtr + 1]);
            Sample2[ch] = Sample2Buf.getShort(0);
            inPtr += 2;

            out[outPtr++] = Sample2[ch];
        } 


        for (int ch = 0; ch < channels; ch++) { out[outPtr++] = Sample1[ch]; }

        int ch = 0;
        
        while (inPtr < inSize) {
            // need larger type than byte to avoid signed byte being used
            byte currentByte = block_data[inPtr++];

            Logger.getGlobal().info(String.format("0x%02X", currentByte));

            
            out[outPtr++] = expandNibble((currentByte & 0xFF) >> 4, ch);
            ch = (ch + 1) % channels;

            out[outPtr++] = expandNibble((currentByte & 0xFF) & 0xf, ch);
            ch = (ch + 1) % channels;
        }
        Logger.getGlobal().info("===== BLOCK FINISH =====");
    }
    

    private short expandNibble(int nibble, int channel) {
        int signed = 0;
        if (nibble >= 8) {
            signed = nibble - 16;
        } else {
            signed = nibble;
        }
        

        short predictor = 0;
        //Logger.getGlobal().info("Preditor reassign, channels: " + channel);
        try {
            //Logger.getGlobal().info("Sample1 + Sample2 " + Arrays.toString(Sample1) + " " + Arrays.toString(Sample2));
            //Logger.getGlobal().info("AdaptCoeff1 + AdaptCoeff2 " + Arrays.toString(AdaptCoeff1) + " " + Arrays.toString(AdaptCoeff2));
            //Logger.getGlobal().info("IntialDelta " + Arrays.toString(InitialDelta));
            int result = (Sample1[channel] * AdaptCoeff1[channel]) + (Sample2[channel] * AdaptCoeff2[channel]);
            predictor = clamp((result >> 8) + (signed * InitialDelta[channel]));

            Sample2[channel] = Sample1[channel];
            Sample1[channel] = predictor;
        } catch (Exception ex) {
            Logger.getGlobal().warning("caught: " + ex);
            throw ex;
        }

        try {
            //Logger.getGlobal().info("idelta reassign");
            //Logger.getGlobal().info("signed " + signed);
            InitialDelta[channel] = (short) Math.floor(AdaptionTable[nibble] * InitialDelta[channel] / 256);
            if (InitialDelta[channel] < 16) {
                InitialDelta[channel] = 16;
            }

        } catch (Exception ex) {
            Logger.getGlobal().warning("caught: " + ex);
            throw ex;
        }
        return predictor;
    }

    private short clamp (int value) {
        return (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, value));
    }
}
