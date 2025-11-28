package bms.tool.mdprocessor;

import java.util.HashSet;
import java.util.Set;

/**
 * DownloadRequest is a simple data object that records the context data during the process of downloading a package.
 */
public class DownloadRequest {
	private String md5;
	private String name;
	private Set<String> triedSources;

	public DownloadRequest(String md5, String name) {
		this.md5 = md5;
		this.name = name;
		this.triedSources = new HashSet<>();
	}

	public void markTried(HttpDownloadSource downloadSource) {
		this.triedSources.add(downloadSource.getName());
	}

	public boolean hasTried(String downloadSource) {
		return this.triedSources.contains(downloadSource);
	}

	public String getMd5() {
		return md5;
	}

	public String getName() {
		return name;
	}
}
