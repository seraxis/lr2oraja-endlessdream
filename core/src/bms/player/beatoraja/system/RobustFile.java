package bms.player.beatoraja.system;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.channels.FileChannel;
import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardCopyOption.*;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RobustFile {
    private static final Logger logger = LoggerFactory.getLogger(RobustFile.class);
    // an interface for supplying a parsing function to the load methods
    // this function is expected to throw ParseException on any
    // encountered issues to trigger an attempt to restore from a backup
    @FunctionalInterface
    public interface Parser<T> {
        T apply(byte[] data) throws ParseException;
    }

    private static Path backupPath(Path original) throws InvalidPathException {
        return original.resolveSibling(original.getFileName() + ".bak");
    }

    private static Path temporaryPath(Path original) throws InvalidPathException {
        return original.resolveSibling(original.getFileName() + ".tmp");
    }

    public static <T> T load(Path file, Parser<T> parser) throws IOException {
        // reads and parses the file
        // in case of failure, falls back to trying the backup file
        byte[] data;
        try {
            data = Files.readAllBytes(file);
            return parser.apply(data);
        }
        catch (IOException e) {
            // could not read the original file - try the backup
            logger.error(e.getMessage());
            return loadBackup(file, parser);
        }
        catch (ParseException e) {
            // the read reported no errors but, for some reason, parsing the received
            // data failed (possible corruption) - log the problem, then restore from backup
            logger.error(e.getMessage());
            return loadBackup(file, parser);
        }
    }

    public static <T> T loadBackup(Path original, Parser<T> parser) throws IOException {
        Path file = backupPath(original);
        if (!Files.isRegularFile(file)) {
            throw new IOException("File load failed: No backup file. \nPath: " + original);
        }

        byte[] data;
        try {
            data = Files.readAllBytes(file);
            return parser.apply(data);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new IOException("File load failed.\nPath: " + original + "\nReason: " +
                                      e.getClass().getSimpleName() + "\n" + e.getMessage(),
                                  e);
        }
        catch (ParseException e) {
            logger.error(e.getMessage());
            throw new IOException("File load failed.\nPath: " + original + "\nReason: " +
                                      e.getClass().getSimpleName() + "\n" + e.getMessage(),
                                  e);
        }
    }

    public static void write(Path file, byte[] data) throws IOException {
        //  write backup & fsync
        //  write temporary file & fsync
        //  rename temporary to original

        // each of these writes can individually throw, aborting the operation
        // we don't perform any retries, since the error might be persistent
        writeFile(backupPath(file), data);
        writeFile(temporaryPath(file), data);
        // we only perform the final rename if both writes completed successfully

        // Note that, even though we request an atomic rename, this is not actually an atomic
        // operation with respect to system crashes, and not at all on certain filesystems.

        // That's the reason for the double-write scheme, where we first create
        // a backup, then a temporary copy and rename the temporary into the original.
        // Even if replacing the original with the temporary fails, we should
        // still be able to read the new data from its backup; if creating
        // the backup fails, the original file will remain untouched.

        try {
            Files.move(temporaryPath(file), file, REPLACE_EXISTING, ATOMIC_MOVE);
        }
        catch (AtomicMoveNotSupportedException e) {
			logger.warn("RobustFile.write: Could not perform an atomic move to {}", file);
            Files.move(temporaryPath(file), file, REPLACE_EXISTING);
        }

        // This approach does nothing whatsoever to protect against in-memory data corruption,
        // or erroneous writes; which means this operation can complete successfully,
        // but as a result overwrites the config file with unusable data.
        // Checksumming each file and verifying after the write would be an expensive operation,
        // and possibly unproductive on systems where we can't ensure that the data we read
        // back actually comes from the device, rather than from cache.

        // In the case that both the original and backup files become damaged
        // and cannot be loaded, we might want to consider entirely preventing
        // the game from launching and inadvertently resetting the config file
        // to default values, as minor corruption might still be manually fixable.
    }

    public static void writeFile(Path file, byte[] data) throws IOException {
        try (FileChannel outChannel = FileChannel.open(file, CREATE, TRUNCATE_EXISTING, WRITE)) {
            // can also throw UnsupportedOperationException
            outChannel.write(ByteBuffer.wrap(data));

            outChannel.force(true);

            // force corresponds to:
            // on linux, fsync(fd)
            // on macOS, fcntl(fd, F_FULLFSYNC)
            // on windows, FlushFileBuffers(hFile)

            // all of these should request that the data is
            // actually written to device before proceeding
        }
    }
}
