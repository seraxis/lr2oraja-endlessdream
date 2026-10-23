package bms.player.beatoraja.skin.lr2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Codes are from DXArchive (DX Library -> Tool -> DXArchive -> Source) , with some modification
 * Original author: 山田 巧 (Takumi Yamada)
 * Homepage: <a href="https://dxlib.xsrv.jp/dxtec.html">...</a>
 *
 * @author Ported by Catizard (If you have any question, don't ask me, ask god)
 */
public class DXADecoder {
	/**
	 * Magic Number, 0x5844 is equals to "DX"
	 */
	private static final int DXA_HEAD_VER5 = 0x5844;

	/**
	 * Highest supported version
	 */
	private static final int DXA_VER_VER5 = 0x0005;

	/**
	 * Internal buffer size (16MB)
	 */
	private static final int DXA_BUFFERSIZE_VER5 = 0x1000000;

	/**
	 * Secret key length (12 bytes)
	 */
	private static final int DXA_KEYSTR_LENGTH_VER5 = 12;

	/**
	 * Minimum compress length
	 */
	private static final int MIN_COMPRESS_VER5 = 4;

	/**
	 * Directory flag
	 */
	public static final int FILE_ATTRIBUTE_DIRECTORY = 0x00000010;

	/**
	 * Uncompressed flag, -1 equals to 0xffffffff
	 */
	public static final int COMPRESSED_DATA_SIZE_RAW = -1;

	/**
	 * Structure size
	 *
	 * @implNote Please note that the original c++ code uses pack(push) & pack(1)
	 */
	private static final int SIZEOF_FILEHEAD_VER1 = 40;
	private static final int SIZEOF_FILEHEAD_VER5 = 44;
	private static final int SIZEOF_HEAD_VER5 = 28;
	private static final int SIZEOF_DIRECTORY_VER5 = 16;

	/**
	 * Uncompress a dxa file into memory
	 *
	 * @param dxaFilePath the path to uncompressing dxa file
	 * @param password    secret key, optional
	 */
	public static Map<String, byte[]> decodeToMemory(String dxaFilePath, String password) throws IOException {
		return decodeArchive(dxaFilePath, password);
	}

	/**
	 * Uncompress a dxa file into memory without password
	 */
	public static Map<String, byte[]> decodeToMemory(String dxaFilePath) throws IOException {
		return decodeToMemory(dxaFilePath, null);
	}

	/**
	 * Decompress a dxa file to disk
	 *
	 * @param dxaFilePath the path to uncompressing dxa file
	 * @param outputDir   disk path
	 * @param password    secret key, optional
	 */
	public static void decodeToFiles(String dxaFilePath, Path outputDir, String password) throws IOException {
		Map<String, byte[]> map = decodeArchive(dxaFilePath, password);
		for (Map.Entry<String, byte[]> e : map.entrySet()) {
			Path target = outputDir.resolve(e.getKey()).normalize();
			Files.createDirectories(target.getParent());
			Files.write(target, e.getValue());
		}
	}

	/**
	 * Decompress a dxa file into a directory that has the same name with dxa on disk
	 * @param dxaFilePath the path to uncompressing dxa file
	 * @param password secret key, optional
	 */
	public static void extractToSameDirectory(String dxaFilePath, String password) throws IOException {
		Path path = Paths.get(dxaFilePath);
		String fileName = path.getFileName().toString();
		String baseName = fileName;
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex > 0) {
			baseName = fileName.substring(0, dotIndex);
		}
		Path parentDir = path.getParent();
		Path outputDir = parentDir != null ? parentDir.resolve(baseName) : Paths.get(baseName);

		decodeToFiles(dxaFilePath, outputDir, password);
	}

	private static byte[] keyCreate(String source) {
		byte[] key = new byte[DXA_KEYSTR_LENGTH_VER5];
		if (source == null) {
			Arrays.fill(key, (byte) 0xAA);
		} else {
			byte[] srcBytes = source.getBytes();
			int len = srcBytes.length;
			if (len >= DXA_KEYSTR_LENGTH_VER5) {
				System.arraycopy(srcBytes, 0, key, 0, DXA_KEYSTR_LENGTH_VER5);
			} else {
				int i = 0;
				while (i + len <= DXA_KEYSTR_LENGTH_VER5) {
					System.arraycopy(srcBytes, 0, key, i, len);
					i += len;
				}
				if (i < DXA_KEYSTR_LENGTH_VER5) {
					System.arraycopy(srcBytes, 0, key, i, DXA_KEYSTR_LENGTH_VER5 - i);
				}
			}
		}

		key[0] = (byte) ~key[0];
		key[1] = (byte) (((key[1] & 0xFF) >>> 4) | ((key[1] & 0xFF) << 4));
		key[2] ^= (byte) 0x8A;
		key[3] = (byte) ~(((key[3] & 0xFF) >>> 4) | ((key[3] & 0xFF) << 4));
		key[4] = (byte) ~key[4];
		key[5] ^= (byte) 0xAC;
		key[6] = (byte) ~key[6];
		key[7] = (byte) ~(((key[7] & 0xFF) >>> 3) | ((key[7] & 0xFF) << 5));
		key[8] = (byte) (((key[8] & 0xFF) >>> 5) | ((key[8] & 0xFF) << 3));
		key[9] ^= (byte) 0x7F;
		key[10] = (byte) ((((key[10] & 0xFF) >>> 4) | ((key[10] & 0xFF) << 4)) ^ 0xD6);
		key[11] ^= (byte) 0xCC;
		return key;
	}

	private static void keyConv(byte[] data, int size, int position, byte[] key) {
		int j = position % DXA_KEYSTR_LENGTH_VER5;
		for (int i = 0; i < size; i++) {
			data[i] ^= key[j];
			j = (j + 1) % DXA_KEYSTR_LENGTH_VER5;
		}
	}

	private static byte[] keyConvFileRead(RandomAccessFile fp, int size, byte[] key) throws IOException {
		long pos = fp.getFilePointer();
		byte[] data = new byte[size];
		fp.readFully(data);
		keyConv(data, size, (int) pos, key);
		return data;
	}

	private static byte[] keyConvFileRead(RandomAccessFile fp, int size, byte[] key, int position) throws IOException {
		byte[] data = new byte[size];
		fp.readFully(data);
		keyConv(data, size, position, key);
		return data;
	}

	private static String getOriginalFileName(byte[] fileNameTable, int nameAddress) {
		int v = (fileNameTable[nameAddress] & 0xFF) | ((fileNameTable[nameAddress + 1] & 0xFF) << 8);
		int strOff = v * 4 + 4;
		int start = nameAddress + strOff;
		int end = start;
		while (end < fileNameTable.length && fileNameTable[end] != 0) end++;
		return new String(fileNameTable, start, end - start);
	}

	private static byte[] decompress(byte[] src) {
		int destSize = ByteBuffer.wrap(src, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
		int srcSize = ByteBuffer.wrap(src, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt() - 9;
		int keyCode = src[8] & 0xFF;

		byte[] dest = new byte[destSize];
		int sp = 9;
		int dp = 0;

		while (srcSize > 0) {
			if ((src[sp] & 0xFF) != keyCode) {
				dest[dp++] = src[sp++];
				srcSize--;
				continue;
			}
			if (sp + 1 < src.length && (src[sp + 1] & 0xFF) == keyCode) {
				dest[dp++] = (byte) keyCode;
				sp += 2;
				srcSize -= 2;
				continue;
			}

			int code = src[sp + 1] & 0xFF;
			if (code > keyCode) code--;
			sp += 2;
			srcSize -= 2;

			int combo = code >>> 3;
			if ((code & 0x04) != 0) {
				combo |= (src[sp] & 0xFF) << 5;
				sp++;
				srcSize--;
			}
			combo += MIN_COMPRESS_VER5;

			int indexSize = code & 0x03;
			int index;
			switch (indexSize) {
				case 0:
					index = src[sp] & 0xFF;
					sp++;
					srcSize--;
					break;
				case 1:
					index = (src[sp] & 0xFF) | ((src[sp + 1] & 0xFF) << 8);
					sp += 2;
					srcSize -= 2;
					break;
				case 2:
					index = (src[sp] & 0xFF) | ((src[sp + 1] & 0xFF) << 8) | ((src[sp + 2] & 0xFF) << 16);
					sp += 3;
					srcSize -= 3;
					break;
				default:
					throw new IllegalStateException("Invalid indexsize: " + indexSize);
			}
			index++;

			if (index < combo) {
				int num = index;
				while (combo > num) {
					System.arraycopy(dest, dp - num, dest, dp, num);
					dp += num;
					combo -= num;
					num += num;
				}
				if (combo != 0) {
					System.arraycopy(dest, dp - num, dest, dp, combo);
					dp += combo;
				}
			} else {
				System.arraycopy(dest, dp - index, dest, dp, combo);
				dp += combo;
			}
		}
		return dest;
	}

	private static void directoryDecode(
			byte[] headBuffer,
			DARCHeadVer5 head,
			DARCDirectoryVer5 dir,
			RandomAccessFile raf,
			byte[] key,
			String curPath,
			Map<String, byte[]> output) throws IOException {

		int nameOff = 0;
		int fileOff = head.fileTableStartAddress;
		int dirOff = head.directoryTableStartAddress;

		String effectivePath = curPath;
		if (dir.directoryAddress != -1 && dir.parentDirectoryAddress != -1) {
			DARCFileHeadVer5 dirFile = new DARCFileHeadVer5(headBuffer, fileOff + dir.directoryAddress, head.version & 0xFFFF);
			String dirName = getOriginalFileName(headBuffer, nameOff + dirFile.nameAddress);
			effectivePath = curPath + File.separator + dirName;
		}

		int fileHeadSize = (head.version & 0xFFFF) >= 0x0002 ? SIZEOF_FILEHEAD_VER5 : SIZEOF_FILEHEAD_VER1;
		int addr = fileOff + dir.fileHeadAddress;

		for (int i = 0; i < dir.fileHeadNum; i++) {
			DARCFileHeadVer5 file = new DARCFileHeadVer5(headBuffer, addr, head.version & 0xFFFF);
			addr += fileHeadSize;

			if ((file.attributes & FILE_ATTRIBUTE_DIRECTORY) != 0) {
				DARCDirectoryVer5 subDir = new DARCDirectoryVer5(headBuffer, dirOff + file.dataAddress);
				directoryDecode(headBuffer, head, subDir, raf, key, effectivePath, output);
			} else {
				String fileName = getOriginalFileName(headBuffer, nameOff + file.nameAddress);
				String fullPath = effectivePath + File.separator + fileName;

				if (file.dataSize == 0) {
					output.put(fullPath, new byte[0]);
					continue;
				}

				long filePos = (long) head.dataStartAddress + (long) file.dataAddress;
				if (raf.getFilePointer() != filePos) {
					raf.seek(filePos);
				}

				byte[] fileData;
				if ((head.version & 0xFFFF) >= 0x0002 && file.compressedDataSize != COMPRESSED_DATA_SIZE_RAW) {
					byte[] compressed;
					if ((head.version & 0xFFFF) >= 0x0005) {
						compressed = keyConvFileRead(raf, file.compressedDataSize, key, file.dataSize);
					} else {
						compressed = keyConvFileRead(raf, file.compressedDataSize, key);
					}
					fileData = decompress(compressed);
				} else {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					int written = 0;
					while (written < file.dataSize) {
						int readSize = Math.min(DXA_BUFFERSIZE_VER5, file.dataSize - written);
						byte[] chunk;
						if ((head.version & 0xFFFF) >= 0x0005) {
							chunk = keyConvFileRead(raf, readSize, key, file.dataSize + written);
						} else {
							chunk = keyConvFileRead(raf, readSize, key);
						}
						baos.write(chunk);
						written += readSize;
					}
					fileData = baos.toByteArray();
				}
				output.put(fullPath, fileData);
			}
		}
	}

	private static Map<String, byte[]> decodeArchive(String path, String password) throws IOException {
		byte[] key = keyCreate(password);
		try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
			byte[] headData = keyConvFileRead(raf, SIZEOF_HEAD_VER5, key, 0);
			DARCHeadVer5 head = new DARCHeadVer5(headData);

			// Check magic number, according to the code there's an old default key as 0XFFFFFFFF. If it doesn't match
			//  we need to try with old default key again
			if ((head.head & 0xFFFF) != DXA_HEAD_VER5) {
				// Try with old default key
				byte[] keyFallback = new byte[DXA_KEYSTR_LENGTH_VER5];
				Arrays.fill(keyFallback, (byte) 0xFF);
				raf.seek(0);
				headData = keyConvFileRead(raf, SIZEOF_HEAD_VER5, keyFallback, 0);
				head = new DARCHeadVer5(headData);
				if ((head.head & 0xFFFF) != DXA_HEAD_VER5) {
					throw new IOException("Not a DXA archive (magic mismatch)");
				}
				// The old default key succeeded, don't forget to reset it to the key we're using!
				System.arraycopy(keyFallback, 0, key, 0, DXA_KEYSTR_LENGTH_VER5);
			}

			if ((head.version & 0xFFFF) > DXA_VER_VER5) {
				throw new IOException("Unsupported DXA version: " + head.version);
			}

			raf.seek(head.fileNameTableStartAddress & 0xFFFFFFFFL);
			byte[] headBuffer;
			if ((head.version & 0xFFFF) >= 0x0005) {
				headBuffer = keyConvFileRead(raf, head.headSize, key, 0);
			} else {
				headBuffer = keyConvFileRead(raf, head.headSize, key);
			}

			DARCDirectoryVer5 rootDir = new DARCDirectoryVer5(headBuffer, head.directoryTableStartAddress);

			Map<String, byte[]> output = new HashMap<>();
			directoryDecode(headBuffer, head, rootDir, raf, key, ".", output);
			return output;
		}
	}

	private static class DARCHeadVer5 {
		public short head;
		public short version;
		public int headSize;
		public int dataStartAddress;
		public int fileNameTableStartAddress;
		public int fileTableStartAddress;
		public int directoryTableStartAddress;
		public int codePage;

		public DARCHeadVer5(byte[] data) {
			ByteBuffer bb = ByteBuffer.wrap(data, 0, SIZEOF_HEAD_VER5).order(ByteOrder.LITTLE_ENDIAN);
			this.head = bb.getShort();
			this.version = bb.getShort();
			this.headSize = bb.getInt();
			this.dataStartAddress = bb.getInt();
			this.fileNameTableStartAddress = bb.getInt();
			this.fileTableStartAddress = bb.getInt();
			this.directoryTableStartAddress = bb.getInt();
			this.codePage = bb.getInt();
		}
	} // sizeof(DARCHeadVer5) == 28; 2 short = 4, 6 int = 24, 4+24=28

	private static class DARCFileTimeVer5 {
		public long create;
		public long lastAccess;
		public long lastWrite;
	} // sizeof(DARCFileTimeVer5) == 24; 3 long = 24;

	private static class DARCFileHeadVer5 {
		public int nameAddress;
		public int attributes;
		public DARCFileTimeVer5 time = new DARCFileTimeVer5();
		public int dataAddress;
		public int dataSize;
		// -1 means uncompressed
		public int compressedDataSize;

		public DARCFileHeadVer5(byte[] data, int offset, int version) {
			boolean isVer2OrLater = version >= 0x0002;
			int size = isVer2OrLater ? SIZEOF_FILEHEAD_VER5 : SIZEOF_FILEHEAD_VER1;
			ByteBuffer bb = ByteBuffer.wrap(data, offset, size).order(ByteOrder.LITTLE_ENDIAN);
			this.nameAddress = bb.getInt();
			this.attributes = bb.getInt();
			this.time.create = bb.getLong();
			this.time.lastAccess = bb.getLong();
			this.time.lastWrite = bb.getLong();
			this.dataAddress = bb.getInt();
			this.dataSize = bb.getInt();
			this.compressedDataSize = isVer2OrLater ? bb.getInt() : COMPRESSED_DATA_SIZE_RAW;
		}
	} // sizeof(DARCFileHeadVer5) == 44; 5 int = 20, DARCFileTimeVer5 = 24; 20+24=44

	private static class DARCDirectoryVer5 {
		public int directoryAddress;
		public int parentDirectoryAddress;
		public int fileHeadNum;
		public int fileHeadAddress;

		public DARCDirectoryVer5(byte[] data, int offset) {
			ByteBuffer bb = ByteBuffer.wrap(data, offset, SIZEOF_DIRECTORY_VER5).order(ByteOrder.LITTLE_ENDIAN);
			this.directoryAddress = bb.getInt();
			this.parentDirectoryAddress = bb.getInt();
			this.fileHeadNum = bb.getInt();
			this.fileHeadAddress = bb.getInt();
		}
	} // sizeof(DARCDirectoryVer5) = 16; 4 int = 16
}