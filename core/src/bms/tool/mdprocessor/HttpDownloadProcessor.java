package bms.tool.mdprocessor;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class HttpDownloadProcessor {
    public static final int MAXIMUM_DOWNLOAD_COUNT = 5;
    // id => task
    private final Map<Integer, DownloadTask> tasks = new HashMap<>();
    // In-memory self-add id generator
    private final AtomicInteger idGenerator = new AtomicInteger(0);
    // Multi-thread download thread pool
    private final ExecutorService executor = Executors.newFixedThreadPool(MAXIMUM_DOWNLOAD_COUNT);

    private Optional<DownloadTask> getTaskById(int taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    /**
     * Submit a download task based on md5
     *
     * @param md5 missing sabun's md5
     */
    public void submitMD5Task(String md5) {
        Logger.getGlobal().info("[HttpDownloadProcessor] New md5" + md5 + " download task submitted");
        String fileName = String.format("%s.7z", md5);
        Path downloadFilePath = Path.of("wriggle_download", fileName);
        DownloadTask downloadTask = new DownloadTask(String.format("https://bms.wrigglebug.xyz/download/package/%s", md5), downloadFilePath);
        // If you want to render all tasks for user, you'll have to store all task's reference
        // tasks.put(taskId, downloadTask);
        executor.submit(() -> {
            try {
                URL url = new URL(downloadTask.getUrl());
                ReadableByteChannel readChannel = Channels.newChannel(url.openStream());
                try (FileOutputStream outputStream = new FileOutputStream(downloadFilePath.toFile())) {
                    FileChannel writeChannel = outputStream.getChannel();
                    writeChannel.transferFrom(readChannel, 0, Long.MAX_VALUE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try (SevenZFile sevenZFile = SevenZFile.builder().setFile(downloadFilePath.toFile()).get()) {
                    SevenZArchiveEntry entry;
                    while ((entry = sevenZFile.getNextEntry()) != null) {
                        if (entry.isDirectory()) continue;
                        File outputFile = new File("wriggle_download", entry.getName());
                        outputFile.getParentFile().mkdirs();

                        try (FileOutputStream fos = new FileOutputStream(outputFile);
                             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                            byte[] buf = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = sevenZFile.read(buf)) != -1) {
                                bos.write(buf, 0, bytesRead);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
                Logger.getGlobal().info("Successfully extracted");
            } catch (FileNotFoundException e) {
                System.out.println("No such song on wriggle site?");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
