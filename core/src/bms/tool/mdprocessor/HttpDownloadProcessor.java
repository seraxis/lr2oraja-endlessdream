package bms.tool.mdprocessor;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.MainController;
import com.badlogic.gdx.graphics.Color;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * In-game download processor. In charge of:
 * <ul>
 *     <li>Manage all download tasks(stored in memory)</li>
 *     <li>Accept download task submission</li>
 *     <li>Download compressed files from remote http server</li>
 *     <li>Extract & update the 'songdata.db' automatically</li>
 * </ul>
 *
 * @author Catizard
 * @since Tue, 10 Jun 2025 05:33 PM
 * @implNote Remember to update DOWNLOAD_SOURCES after adding a download source
 */
public class HttpDownloadProcessor {
    public static final Map<String, HttpDownloadSourceMeta> DOWNLOAD_SOURCES = new HashMap<>();
    static {
        // Wriggle
        HttpDownloadSourceMeta wriggleDownloadSourceMeta = WriggleDownloadSource.META;
        DOWNLOAD_SOURCES.put(wriggleDownloadSourceMeta.getName(), wriggleDownloadSourceMeta);
    }

    public static final int MAXIMUM_DOWNLOAD_COUNT = 5;
    // TODO: make this magic constants configurable? I think not very worthy though
    public static final String DOWNLOAD_DIRECTORY = "wriggle_download";
    // id => task
    private final Map<Integer, DownloadTask> tasks = new HashMap<>();
    // In-memory self-add id generator
    private final AtomicInteger idGenerator = new AtomicInteger(0);
    // Multi-thread download thread pool
    private final ExecutorService executor = Executors.newFixedThreadPool(MAXIMUM_DOWNLOAD_COUNT);
    // A reference to the main controller, only used for updating folder and rendering the message
    private final MainController main;
    private final HttpDownloadSource httpDownloadSource;

    public HttpDownloadProcessor(MainController main, HttpDownloadSource httpDownloadSource) {
        this.main = main;
        this.httpDownloadSource = httpDownloadSource;
    }

    public static HttpDownloadSourceMeta getDefaultDownloadSource() {
        return WriggleDownloadSource.META;
    }

    private Optional<DownloadTask> getTaskById(int taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    /**
     * @return Current snapshot of all tasks;
     */
    public List<DownloadTask> getAllTaskSnapshot() {
        synchronized (tasks) {
            return tasks.values().stream().map(DownloadTask::copy).toList();
        }
    }

    /**
     * Send a new message to MainController to render
     *
     * @param content message
     * @param color   message color. Fallback to Blue if passing null
     */
    private void pushMessage(String content, Color color) {
        Color renderColor = color == null ? Color.BLUE : color;
        main.getMessageRenderer().addMessage(content, 5000, renderColor, 1);
    }

    /**
     * Submit a download task based on md5
     *
     * @param md5      missing sabun's md5
     * @param taskName task name, normally sabun's name
     */
    public void submitMD5Task(String md5, String taskName) {
        Logger.getGlobal().info(String.format("[HttpDownloadProcessor] Trying to submit new download task[%s](based on md5: %s)", taskName, md5));
        // TODO: Implement an intermediate file rename strategy could be better
        String fileName = String.format("%s.7z", md5);
        Path downloadFilePath = Path.of(DOWNLOAD_DIRECTORY, fileName);
        String downloadURL = httpDownloadSource.getDownloadURLBasedOnMd5(md5);

        int taskId = idGenerator.addAndGet(1);
        DownloadTask downloadTask = new DownloadTask(taskId, downloadURL, taskName, downloadFilePath);
        synchronized (tasks) {
            tasks.put(taskId, downloadTask);
        }
        pushMessage(String.format("New download task[%s] submitted", taskName), null);

        executor.submit(() -> {
            try {
                Logger.getGlobal().info(String.format("[HttpDownloadProcessor] Trying to kick new download task[%s](%s)", taskName, downloadURL));
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
                        File outputFile = new File(DOWNLOAD_DIRECTORY, entry.getName());
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
                // and update the download directory manually
                pushMessage("Successfully downloaded. Trying to rebuild directory", null);
                main.updateSong(DOWNLOAD_DIRECTORY);
            } catch (FileNotFoundException e) {
                pushMessage("Cannot find specified song from wriggle", null);
            } catch (Exception e) {
                Logger.getGlobal().severe("Failed to download, exception: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
