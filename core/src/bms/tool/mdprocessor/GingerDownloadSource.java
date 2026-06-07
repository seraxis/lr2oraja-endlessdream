package bms.tool.mdprocessor;

import bms.player.beatoraja.Config;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;

public class GingerDownloadSource implements HttpDownloadSource {
	public static final HttpDownloadSourceMeta META = new HttpDownloadSourceMeta(
			"ginger",
			"https://gingerrush.com/download/package/%s",
			GingerDownloadSource::new
	);

	private final String downloadQueryURL;
	private final ObjectMapper om = new ObjectMapper();

	public GingerDownloadSource(Config config) {
		String overrideDownloadURL = config.getOverrideDownloadURL();
		this.downloadQueryURL = overrideDownloadURL != null && !overrideDownloadURL.isEmpty()
				? overrideDownloadURL
				: META.getDefaultURL();
	}

	@Override
	public String getName() {
		return META.getName();
	}

	@Override
	public String getDownloadURLBasedOnMd5(String md5) throws FileNotFoundException, RuntimeException {
		String metaURL = String.format(downloadQueryURL, md5);
        HttpURLConnection conn = null;
        String downloadURL;
        try {
            URL url = new URL(metaURL);
            conn = ((HttpURLConnection) url.openConnection());
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    throw new FileNotFoundException();
                }
                throw new RuntimeException("Unexpected http response code: " + responseCode);
            }
			PackageData pkg = om.readValue(conn.getInputStream(), PackageData.class);
            if (pkg.downloadURL == null || pkg.downloadURL.isEmpty()) {
                throw new FileNotFoundException();
            }
            downloadURL = pkg.downloadURL;
        } catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return downloadURL;
	}

	@Override
	public boolean isAllowDownloadThroughMd5() {
		return true;
	}

	@Override
	public boolean isAllowDownloadThroughSha256() {
		return false;
	}

	@Override
	public boolean isAllowMetaQuery() {
		return true;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class PackageData {
		private String fileName;
		private Long fileSize;
		@JsonProperty("downloadURL")
		private String downloadURL;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public Long getFileSize() {
			return fileSize;
		}

		public void setFileSize(Long fileSize) {
			this.fileSize = fileSize;
		}

		public String getDownloadURL() {
			return downloadURL;
		}

		public void setDownloadURL(String downloadURL) {
			this.downloadURL = downloadURL;
		}
	}
}
