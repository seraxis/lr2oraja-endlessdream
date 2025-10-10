package bms.tool.mdprocessor;

import java.nio.file.Path;

public class DownloadTask {
    final private int id;
    final private String url;
    final private String name;
    final private String hash;

    private volatile DownloadTaskStatus downloadTaskStatus;
    private volatile long downloadSize;
    private volatile long contentLength;
    private volatile String errorMessage;
    private volatile long timeFinished;

    public DownloadTask(int id, String url, String name, String hash) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.hash = hash;
        this.downloadTaskStatus = DownloadTaskStatus.Prepare;
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getHash() {
        return hash;
    }

    public DownloadTaskStatus getDownloadTaskStatus() {
        return downloadTaskStatus;
    }

    public void setDownloadTaskStatus(DownloadTaskStatus downloadTaskStatus) {
        this.downloadTaskStatus = downloadTaskStatus;
        if (1 < downloadTaskStatus.value) {
            timeFinished = System.nanoTime();
        }
    }

    public long getDownloadSize() {
        return downloadSize;
    }

    public void setDownloadSize(long downloadSize) {
        this.downloadSize = downloadSize;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getName() {
        return name;
    }

    public long getTimeFinished() {
        return timeFinished;
    }

    // TODO: Success state should be split into multiple different states like `Download successfully` and `Extract successfully`
    public enum DownloadTaskStatus {
        Prepare(0, "Prepare"),
        Downloading(1, "Downloading"),
        Success(2, "Success"),
        Error(3, "Error"),
        Cancel(4, "Cancel");

        private final int value;
        private final String name;

        DownloadTaskStatus(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }
}
