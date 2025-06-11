package bms.tool.mdprocessor;

import java.nio.file.Path;

public class DownloadTask {
    private int id;
    private String url;
    private String name;
    private DownloadTaskStatus downloadTaskStatus;
    private Path downloadFilePath;
    private long downloadSize;
    private long contentLength;
    private String errorMessage;
    public DownloadTask() {

    }

    public DownloadTask(int id, String url, String name) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.downloadTaskStatus = DownloadTaskStatus.Prepare;
    }

    /**
     * @return A copied DownloadTask instance, only used for rendering
     */
    public DownloadTask copy() {
        DownloadTask downloadTask = new DownloadTask();
        // Yeah this is java
        downloadTask.id = this.id;
        downloadTask.url = this.url;
        downloadTask.name = this.name;
        downloadTask.downloadTaskStatus = this.downloadTaskStatus;
        downloadTask.downloadSize = this.downloadSize;
        downloadTask.contentLength = this.contentLength;
        downloadTask.errorMessage = this.errorMessage;
        return downloadTask;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public DownloadTaskStatus getDownloadTaskStatus() {
        return downloadTaskStatus;
    }

    public void setDownloadTaskStatus(DownloadTaskStatus downloadTaskStatus) {
        this.downloadTaskStatus = downloadTaskStatus;
    }

    public Path getDownloadFilePath() {
        return downloadFilePath;
    }

    public void setDownloadFilePath(Path downloadFilePath) {
        this.downloadFilePath = downloadFilePath;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
