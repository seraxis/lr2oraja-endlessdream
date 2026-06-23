package bms.tool.mdprocessor;

public record HttpDownloadErrorEvent(String errorMessage, String logMessage) {
	public HttpDownloadErrorEvent(String errorMessage) {
		this(errorMessage, errorMessage);
	}
}
