package bms.tool.mdprocessor;

import bms.player.beatoraja.MainController;
import bms.player.beatoraja.modmenu.ImGuiNotify;
import com.badlogic.gdx.graphics.Color;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
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
    public static final String DOWNLOAD_DIRECTORY = "http_download";

    static {
        // Wriggle
        HttpDownloadSourceMeta wriggleDownloadSourceMeta = WriggleDownloadSource.META;
        DOWNLOAD_SOURCES.put(wriggleDownloadSourceMeta.getName(), wriggleDownloadSourceMeta);
        // Konmai
//        HttpDownloadSourceMeta konmaiDownloadSourceMeta = KonmaiDownloadSource.META;
//        DOWNLOAD_SOURCES.put(konmaiDownloadSourceMeta.getName(), konmaiDownloadSourceMeta);
    }

    // id => task
    private final Map<Integer, DownloadTask> tasks = new ConcurrentHashMap<>();
    // In-memory self-add id generator
    private final AtomicInteger idGenerator = new AtomicInteger(0);
    // Multi-thread download thread pool
    private final ExecutorService executor = Executors.newFixedThreadPool(MAXIMUM_DOWNLOAD_COUNT);
    private final ExecutorService submitter = Executors.newSingleThreadExecutor();
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

    // Would be best if this returned an immutable view over the tasks,
    // without creating a copy, in the interest of efficiency,
    // however I'm not sure if that is possible in java
    public Map<Integer, DownloadTask> getAllTasks() { return tasks; }

    /**
     * Submit a download task based on md5
     *
     * @param md5      missing sabun's md5
     * @param taskName task name, normally sabun's name
     */
    public void submitMD5Task(String md5, String taskName) {
        Logger.getGlobal().info(String.format("[HttpDownloadProcessor] Trying to submit new download task[%s](based on md5: %s)", taskName, md5));
        String sourceName = httpDownloadSource.getName();
        String downloadURL;
        try {
            downloadURL = httpDownloadSource.getDownloadURLBasedOnMd5(md5);
        } catch (FileNotFoundException e) {
            Logger.getGlobal().severe(String.format("[HttpDownloadProcessor] Remote server[%s] reports no such data", sourceName));
            ImGuiNotify.error(String.format("Cannot find specified song from %s", sourceName));
            return;
        } catch (RuntimeException e) {
            e.printStackTrace();
            Logger.getGlobal().severe(String.format("[HttpDownloadProcessor] Cannot get download url from remote server[%s] due to unexpected exception: %s", sourceName, e.getMessage()));
            return;
        }

        // NOTE: The reason of using executor instead of using 'synchronized' on tasks directly is forcing
        // it to run the submit step on an different thread to get rid of the re-entrant feature of 'synchronized'.
        // Alternative way is providing a wait queue and an extra thread polling submit request routinely
        Future<DownloadTask> submit = submitter.submit(() -> {
            synchronized (tasks) {
                // NOTE: This reject strategy works for Konmai because the download url could be considered as a unique
                // info, but not wriggle since it doesn't offer a meta query api.
                if (tasks.values().stream().anyMatch(task -> task.getUrl().equals(downloadURL))) {
                    Logger.getGlobal().severe(String.format("[HttpDownloadProcessor] Rejecting download task[%s] because duplication has been found", downloadURL));
                    ImGuiNotify.warning("Already submitted");
                    return null;
                }
                int taskId = idGenerator.addAndGet(1);
                DownloadTask downloadTask = new DownloadTask(taskId, downloadURL, taskName, md5);
                tasks.put(taskId, downloadTask);
                ImGuiNotify.info(String.format("New download task[%s] submitted", taskName));
                return downloadTask;
            }
        });

        DownloadTask downloadTask;
        try {
            downloadTask = submit.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            Logger.getGlobal().severe("Unexpected error from submitting download task: " + e.getMessage());
            return;
        }

        if (downloadTask == null) {
            return;
        }

        executeDownloadTask(downloadTask);
    }

    /**
     * Execute the download task, which are chained steps:
     * <ol>
     *     <li>Download the archive file from url</li>
     *     <li>Extract the package</li>
     *     <li>Update download directory</li>
     *     <li>Delete the archive file</li>
     * </ol>
     *
     * @param downloadTask task
     */
    public void executeDownloadTask(DownloadTask downloadTask) {
        executor.submit(() -> {
            String taskName = downloadTask.getName();
            String downloadURL = downloadTask.getUrl();
            String hash = downloadTask.getHash();
            Logger.getGlobal().info(String.format("[HttpDownloadProcessor] Trying to kick new download task[%s](%s)", taskName, downloadURL));
            downloadTask.setDownloadTaskStatus(DownloadTask.DownloadTaskStatus.Downloading);
            Path result = null;
            // 1) Download file from remote http server
            try {
                result = downloadFileFromURL(downloadTask, String.format("%s.7z", hash));
            } catch (Exception e) {
                e.printStackTrace();
                ImGuiNotify.error(String.format("Failed downloading from %s due to %s", httpDownloadSource.getName(), e.getMessage()));
            }
            if (result == null) {
                // Download failed, skip the remaining steps
                downloadTask.setDownloadTaskStatus(DownloadTask.DownloadTaskStatus.Error);
                return;
            }
            // 2) Extract the compressed archive & update download directory automatically
            boolean successfullyExtracted = false;
            try {
                extractCompressedFile(result.toFile(), null);
                successfullyExtracted = true;
                downloadTask.setDownloadTaskStatus(DownloadTask.DownloadTaskStatus.Extracted);
            } catch (Exception e) {
                e.printStackTrace();
                ImGuiNotify.error(String.format("Failed extracting file: %s due to %s", result.getFileName(), e.getMessage()));
            }
            if (successfullyExtracted) {
                // TODO: Directory update is protected, this might cause some uncovered situation. Personally speaking,
                // I don't think this has any issue since user can always turn back to root directory
                // and update the download directory manually
                ImGuiNotify.info("Successfully downloaded & extracted. Trying to rebuild download directory");
                main.updateSong(DOWNLOAD_DIRECTORY);
                // If everything works well, trying to delete the downloaded archive
                try {
                    Files.delete(result);
                } catch (IOException e) {
                    e.printStackTrace();
                    ImGuiNotify.error("Failed deleting archive file automatically");
                }
            }
        });
    }

    /**
     * Retry a download task
     */
    public void retryDownloadTask(DownloadTask downloadTask) {
        downloadTask.setDownloadTaskStatus(DownloadTask.DownloadTaskStatus.Prepare);
        executeDownloadTask(downloadTask);
    }

    /**
     * Download a file from url (no intermediate file protection)
     *
     * @param fallbackFileName fallback file name if remote server's response doesn't contain a valid file name
     * @return result file path, null if failed
     */
    private Path downloadFileFromURL(DownloadTask task, String fallbackFileName) {
        HttpURLConnection conn = null;
        InputStream is = null;
        FileOutputStream fos = null;
        Path result = null;

        try {
            URL url = new URL(task.getUrl());
            conn = ((HttpURLConnection) url.openConnection());
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    throw new FileNotFoundException("Package not found at " + httpDownloadSource.getName());
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
            task.setDownloadTaskStatus(DownloadTask.DownloadTaskStatus.Downloaded);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getGlobal().info("[HttpDownloadProcessor] Failed to download file from url: " + e.getMessage());
            task.setDownloadSize(0);
            task.setContentLength(0);
            task.setErrorMessage(e.getMessage());
            // All other unexpected exception are rethrown as RuntimeException
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

    /**
     * Extract a compressed file into targetPath
     *
     * @param file       compressed archive
     * @param targetPath target directory, fallback to DOWNLOAD_DIRECTORY if null
     */
    private void extractCompressedFile(File file, Path targetPath) {
        Path resultDirectory = targetPath == null ? Path.of(DOWNLOAD_DIRECTORY) : targetPath;
        try (SevenZFile sevenZFile = SevenZFile.builder().setFile(file).get()) {
            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                File outputFile = new File(resultDirectory.toString(), entry.getName());
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
    }
}
