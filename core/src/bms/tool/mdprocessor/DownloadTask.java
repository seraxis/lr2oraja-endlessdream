package bms.tool.mdprocessor;

import java.nio.file.Path;

public class DownloadTask {
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

    private int id;
    private String url;
    private DownloadTaskStatus downloadTaskStatus;
    private Path downloadFilePath;
    private double downloadSize;
    private double contentLength;
    private String errorMessage;

    public DownloadTask(String url, Path downloadFilePath) {
        this.url = url;
        this.downloadFilePath = downloadFilePath;
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
}
