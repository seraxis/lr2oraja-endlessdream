package bms.tool.mdprocessor;

import bms.player.beatoraja.MainController;
import com.badlogic.gdx.graphics.Color;
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
    // A reference to the main controller, only used for updating folder
    private final MainController main;

    public HttpDownloadProcessor(MainController main) {
        this.main = main;
    }

    private Optional<DownloadTask> getTaskById(int taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    /**
     * Send a new message to MainController to render
     *
     * @param content message
     * @param color message color. Fallback to Blue if passing null
     */
    private void pushMessage(String content, Color color) {
        Color renderColor = color == null ? Color.BLUE : color;
        main.getMessageRenderer().addMessage(content, 5000, renderColor, 1);
    }

    /**
     * Submit a download task based on md5
     *
     * @param md5 missing sabun's md5
     */
    public void submitMD5Task(String md5) {
        Logger.getGlobal().info("[HttpDownloadProcessor] New md5 " + md5 + " download task submitted");
        String fileName = String.format("%s.7z", md5);
        Path downloadFilePath = Path.of("wriggle_download", fileName);
        DownloadTask downloadTask = new DownloadTask(String.format("https://bms.wrigglebug.xyz/download/package/%s", md5), downloadFilePath);
        // If you want to render all tasks for user, you'll have to store all task's reference
        // tasks.put(taskId, downloadTask);
        pushMessage("New download task submitted", null);
        executor.submit(() -> {
            try {
                URL url = new URL(downloadTask.getUrl());
                ReadableByteChannel readChannel = Channels.newChannel(url.openStream());
                try (FileOutputStream outputStream = new FileOutputStream(downloadFilePath.toFile())) {
                    FileChannel writeChannel = outputStream.getChannel();
                    writeChannel.transferFrom(readChannel, 0, Long.MAX_VALUE);
                } catch (Exception e) {
                    e.printStackTrace();
                    pushMessage("Failed to download from wriggle", null);
                    throw new RuntimeException(e.getMessage());
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
                    pushMessage("Failed to extract compressed file", null);
                    throw new RuntimeException(e.getMessage());
                }
                // TODO: We cannot get real file name here. By refactoring the download code to use plain http
                // download and extract the file-name parameter from `Content-Disposition` is a way, but I
                // prefer the server side provide an endpoint for retrieving the meta data instead
                Logger.getGlobal().info("Successfully extracted");
                // TODO: Directory update is protected, this might cause some uncovered situation. Personally speaking,
                // I don't think this has any issue since user can always turn back to root directory
                // and update the "wriggle_download" directory manually
                pushMessage("Successfully downloaded. Trying to rebuild directory", null);
                // TODO: make this magic constants configurable? I think not very worthy though
                main.updateSong("wriggle_download");
            } catch (FileNotFoundException e) {
                pushMessage("Cannot find specified song from wriggle", null);
            } catch (Exception e) {
                Logger.getGlobal().severe("Failed to download, exception: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
