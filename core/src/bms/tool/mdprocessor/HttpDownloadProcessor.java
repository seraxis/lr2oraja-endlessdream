package bms.tool.mdprocessor;

import bms.player.beatoraja.MainController;
import com.badlogic.gdx.graphics.Color;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * @implNote Remember to update DOWNLOAD_SOURCES after adding a download source
 * @since Tue, 10 Jun 2025 05:33 PM
 */
public class HttpDownloadProcessor {
    public static final Map<String, HttpDownloadSourceMeta> DOWNLOAD_SOURCES = new HashMap<>();
    public static final int MAXIMUM_DOWNLOAD_COUNT = 5;
    // TODO: make this magic constants configurable? I think not very worthy though
    public static final String DOWNLOAD_DIRECTORY = "wriggle_download";

    static {
        // Wriggle
        HttpDownloadSourceMeta wriggleDownloadSourceMeta = WriggleDownloadSource.META;
        DOWNLOAD_SOURCES.put(wriggleDownloadSourceMeta.getName(), wriggleDownloadSourceMeta);
    }

    // id => task
    private final Map<Integer, DownloadTask> tasks = new ConcurrentHashMap<>();
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
        String downloadURL = httpDownloadSource.getDownloadURLBasedOnMd5(md5);

        int taskId = idGenerator.addAndGet(1);
        DownloadTask downloadTask = new DownloadTask(taskId, downloadURL, taskName);
        synchronized (tasks) {
            tasks.put(taskId, downloadTask);
        }
        pushMessage(String.format("New download task[%s] submitted", taskName), null);

        executor.submit(() -> {
            Logger.getGlobal().info(String.format("[HttpDownloadProcessor] Trying to kick new download task[%s](%s)", taskName, downloadURL));
            downloadTask.setDownloadTaskStatus(DownloadTask.DownloadTaskStatus.Downloading);
            try {
                Path result;
                try {
                    result = downloadFileFromURL(taskId, downloadTask.getUrl(), fileName);
                } catch (Exception e) {
                    throw e;
                }
                try (SevenZFile sevenZFile = SevenZFile.builder().setFile(result.toFile()).get()) {
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
                Logger.getGlobal().severe(String.format("[HttpDownloadProcessor] Remote server[%s] returns 404 back", httpDownloadSource.getName()));
                downloadTask.setDownloadTaskStatus(DownloadTask.DownloadTaskStatus.Error);
                pushMessage(String.format("Cannot find specified song from %s", httpDownloadSource.getName()), null);
            } catch (Exception e) {
                e.printStackTrace();
                downloadTask.setDownloadTaskStatus(DownloadTask.DownloadTaskStatus.Error);
                Logger.getGlobal().severe("Failed to download, exception: " + e.getMessage());
                pushMessage(String.format("Unexpected error: %s", e.getMessage()), null);
            }
        });
    }

    /**
     * Download a file from url (no intermediate file protection)
     *
     * @param taskId           download task's unique id, for submitting result
     * @param downloadURL      remote url
     * @param fallbackFileName fallback file name if remote server's response doesn't contain a valid file name
     * @return result file path, null if failed
     */
    private Path downloadFileFromURL(int taskId, String downloadURL, String fallbackFileName) throws FileNotFoundException {
        HttpURLConnection conn = null;
        InputStream is = null;
        FileOutputStream fos = null;
        Path result = null;

        // TODO: The race condition seems harmless...There is only one write side & one read side while the read side
        // is only copying the reference
        DownloadTask task = getTaskById(taskId).orElseThrow();

        try {
            URL url = new URL(downloadURL);
            conn = ((HttpURLConnection) url.openConnection());
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    throw new FileNotFoundException();
                }
                throw new IllegalStateException("Unexpected http response code: " + responseCode);
            }
            // Prepare the file name
            String fileName = fallbackFileName;
            String contentDisposition = conn.getHeaderField("Content-Disposition");
            String candidateFileName = "";
            if (contentDisposition != null && !contentDisposition.isEmpty()) {
                Matcher matcher = Pattern.compile("filename=\"?([^\"]+)\"?").matcher(contentDisposition);
                if (matcher.find()) {
                    candidateFileName = matcher.group(1);
                }
            }
            if (candidateFileName != null && !candidateFileName.isEmpty()) {
                fileName = candidateFileName;
            }

            long contentLength = conn.getContentLengthLong();
            is = conn.getInputStream();
            result = Path.of(DOWNLOAD_DIRECTORY, fileName);
            fos = new FileOutputStream(result.toFile());

            // TODO: We can bind the buffer to the worker thread instead of creating & releasing it repeatedly
            byte[] buffer = new byte[8192];
            long downloadBytes = 0;

            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
                downloadBytes += read;
                task.setDownloadSize(downloadBytes);
                task.setContentLength(contentLength);
            }
            Logger.getGlobal().info(String.format("[HttpDownloadProcessor] Download successfully to %s", result));
            task.setDownloadTaskStatus(DownloadTask.DownloadTaskStatus.Success);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getGlobal().info("[HttpDownloadProcessor] Failed to download file from url: " + e.getMessage());
            task.setDownloadSize(0);
            task.setContentLength(0);
            task.setErrorMessage(e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                }
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                // Do nothing...
            }
        }
        return result;
    }
}
