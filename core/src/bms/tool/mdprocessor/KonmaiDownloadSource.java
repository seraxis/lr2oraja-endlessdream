package bms.tool.mdprocessor;

import bms.player.beatoraja.Config;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;

public class KonmaiDownloadSource implements HttpDownloadSource {
    public static final HttpDownloadSourceMeta META = new HttpDownloadSourceMeta(
            "konmai",
            "https://bms.alvorna.com/api/hash?md5=%s",
            KonmaiDownloadSource::new
    );
    public static final String SUCCESS_RESULT = "success";

    private final String downloadQueryURL;
    private final ObjectMapper om = new ObjectMapper();

    public KonmaiDownloadSource(Config config) {
        // override download url if user ask to do so
        String overrideDownloadURL = config.getOverrideDownloadURL();
        this.downloadQueryURL = overrideDownloadURL != null && !overrideDownloadURL.isEmpty()
                ? overrideDownloadURL
                : META.getDefaultURL();
    }

    @Override
    public String getName() {
        return META.getName();
    }

    /**
     * Konmai backend uses a meta query endpoint instead of direct download link.<br>
     * Similar to wriggle, the url must be a pattern string with only one %s placeholder and anything could happen
     * if not. It also requires authentication so we have to grab token if we don't have one or the server reports
     * that it's expired.
     */
    @Override
    public String getDownloadURLBasedOnMd5(String md5) throws FileNotFoundException, RuntimeException {
        String metaURL = String.format(downloadQueryURL, md5);
        // TODO: Server side doesn't provide auth currently
        HttpURLConnection conn = null;
        String downloadURL;
        try {
            URL url = new URL(metaURL);
            conn = ((HttpURLConnection) url.openConnection());
            conn.connect();
            // Konmai backend doesn't offer an 404 status code
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    throw new FileNotFoundException();
                }
                throw new RuntimeException("Unexpected http response code: " + responseCode);
            }
            RespData<ChartMeta> respData = om.readValue(conn.getInputStream(), new TypeReference<>() {
            });
            // Instead, Konmai returns an empty 'song_url' or 'result: fail' to indicate song is not exist
            if (!SUCCESS_RESULT.equals(respData.result)) {
                throw new RuntimeException("Unexpected error: " + respData.msg);
            }
            ChartMeta chartMeta = respData.data;
            if (chartMeta.songUrl == null || chartMeta.songUrl.isEmpty()) {
                throw new FileNotFoundException();
            }
            downloadURL = chartMeta.songUrl;
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

    /**
     * Response wrapper from Konmai
     */
    private static class RespData<T> {
        private String result;
        private String msg;
        private String chart;
        private T data;

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getChart() {
            return chart;
        }

        public void setChart(String chart) {
            this.chart = chart;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    /**
     * Represents one chart meta info from Konmai
     */
    private static class ChartMeta {
        @JsonProperty("chart_name")
        private String chartName;
        private String md5;
        private String sha256;
        @JsonProperty("song_name")
        private String songName;
        @JsonProperty("song_url")
        private String songUrl;

        public String getChartName() {
            return chartName;
        }

        public void setChartName(String chartName) {
            this.chartName = chartName;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        public String getSha256() {
            return sha256;
        }

        public void setSha256(String sha256) {
            this.sha256 = sha256;
        }

        public String getSongName() {
            return songName;
        }

        public void setSongName(String songName) {
            this.songName = songName;
        }

        public String getSongUrl() {
            return songUrl;
        }

        public void setSongUrl(String songUrl) {
            this.songUrl = songUrl;
        }
    }
}
