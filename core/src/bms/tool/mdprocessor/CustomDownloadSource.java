package bms.tool.mdprocessor;

import bms.player.beatoraja.Config;

/**
 * Custom download source is a special source that must be configured with override URL and without meta-query api. It's
 *  derived from the old design of wriggle, which connects the client and a download service as minimal as possible:
 *  server side provides a download URL pattern and user pastes it into the client, done.
 * There're two disadvantages: first, without a meta-query endpoint, client cannot tell whether two download links are
 *  actually pointing to a same file when they have different URLs. For example, two different sabun's md5 hash in a
 *  package, which will lead to different 2 urls. Second, the user must change the download url themselves.
 */
public class CustomDownloadSource implements HttpDownloadSource {
    public static final HttpDownloadSourceMeta META = new HttpDownloadSourceMeta(
            "Custom",
            "",
            CustomDownloadSource::new
    );

    private final String downloadURL;

    public CustomDownloadSource(Config config) {
        this(config.getOverrideDownloadURL());
    }

    public CustomDownloadSource(String overrideDownloadURL) {
        // override download url if user ask to do so
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
