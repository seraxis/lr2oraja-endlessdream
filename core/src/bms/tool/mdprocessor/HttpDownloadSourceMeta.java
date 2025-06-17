package bms.tool.mdprocessor;

import bms.player.beatoraja.Config;

import java.util.function.Function;

public class HttpDownloadSourceMeta {
    private final String name;
    // TODO: This is a bad design since it doesn't reserved the space for other download strategies
    // (e.g. download through an unique field from IR server or simply sha256). Could be extended
    // in the near future. As for now, keep it simple and stupid
    // However, it's not very easy to give user such flexibility
    private final String defaultURL;
    private final Function<Config, HttpDownloadSource> builder;

    public HttpDownloadSourceMeta(String name, String defaultURL, Function<Config, HttpDownloadSource> builder) {
        this.name = name;
        this.defaultURL = defaultURL;
        this.builder = builder;
    }

    public HttpDownloadSource build(Config config) {
        return builder.apply(config);
    }

    public String getName() {
        return name;
    }

    public String getDefaultURL() {
        return defaultURL;
    }
}
