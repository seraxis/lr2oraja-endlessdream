package bms.tool.mdprocessor;

import java.nio.file.Path;

public class DownloadTask {
    private int id;
    private String url;
    private String name;
    private DownloadTaskStatus downloadTaskStatus;
    private Path downloadFilePath;
    private double downloadSize;
    private double contentLength;
    private String errorMessage;
    public DownloadTask() {

    }

    public DownloadTask(int id, String url, String name, Path downloadFilePath) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.downloadFilePath = downloadFilePath;
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

    public double getDownloadSize() {
        return downloadSize;
    }

    public void setDownloadSize(double downloadSize) {
        this.downloadSize = downloadSize;
    }

    public double getContentLength() {
        return contentLength;
    }

    public void setContentLength(double contentLength) {
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
