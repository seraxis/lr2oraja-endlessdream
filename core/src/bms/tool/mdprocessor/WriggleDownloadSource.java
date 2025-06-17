package bms.tool.mdprocessor;

import bms.player.beatoraja.Config;

public class WriggleDownloadSource implements HttpDownloadSource {
    public static final HttpDownloadSourceMeta META = new HttpDownloadSourceMeta(
            "wriggle",
            "https://bms.wrigglebug.xyz/download/package/%s",
            WriggleDownloadSource::new
    );

    private final String downloadURL;

    public WriggleDownloadSource(Config config) {
        // override download url if user ask to do so
        String overrideDownloadURL = config.getOverrideDownloadURL();
        this.downloadURL = overrideDownloadURL != null && !overrideDownloadURL.isEmpty()
                ? overrideDownloadURL
                : META.getDefaultURL();
    }

    /**
     * The download url should be a pattern with only one %s placeholder. If not, anything could happen.
     */
    @Override
    public String getDownloadURLBasedOnMd5(String md5) {
        return String.format(downloadURL, md5);
    }

    @Override
    public String getName() {
        return META.getName();
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
        return false;
    }
}
