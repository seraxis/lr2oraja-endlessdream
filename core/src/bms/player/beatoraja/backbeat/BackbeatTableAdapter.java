package bms.player.beatoraja.backbeat;

import bms.model.Mode;
import bms.player.beatoraja.TableData;
import bms.player.beatoraja.TableDataAccessor;
import bms.player.beatoraja.song.SongData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Backbeat has tables, but they're in a slightly different format, and support "extra folders".
 * This is a shim that adapts it into the existing table infra.
 */
public final class BackbeatTableAdapter extends TableDataAccessor.TableAccessor {
    private static final Logger logger = LoggerFactory.getLogger(BackbeatTableAdapter.class);

    private final BackbeatIntegration integration;
    private final String url;

    public BackbeatTableAdapter(BackbeatIntegration integration, String url) {
        super("backbeat:" + url);
        this.integration = integration;
        this.url = url;
    }

    @Override
    public TableData read() {
        try {
            return integration.installedTable(url).map(BackbeatTableAdapter::toTableData).orElse(null);
        } catch (RuntimeException error) {
            logger.warn("Failed to load Backbeat table {}: {}", url, error.getMessage());
            return null;
        }
    }

    /**
     * Backbeat tables are managed by backbeat, nothing for you to write or save.
     */
    @Override
    public void write(TableData table) {}

    public static TableData toTableData(BackbeatIntegration.CatalogTable source) {
        TableData table = new TableData();
        table.setUrl(source.url());
        // distinguish from regular tables I guess...
        table.setName("Table: " + source.name());
        table.setTag(source.symbol());
        int mode = mode(source.gamemode()).map(value -> value.id).orElse(0);

        table.setFolder(source.sections().stream().map(section -> {
            TableData.TableFolder folder = new TableData.TableFolder();
            folder.setName(section.name());
            folder.setSong(section.charts().stream().map(chart -> {
                SongData song = new SongData();

                if (chart.id().startsWith("md5/")) {
                    song.setMd5(chart.id().substring("md5/".length()));
                } else if (chart.id().startsWith("sha256/")) {
                    song.setSha256(chart.id().substring("sha256/".length()));
                } else {
                    // Chart ID algs we don't recognise just shouldn't work
                    // nothing we can do about it.
                }

                song.setTitle(chart.description().isBlank() ? chart.id() : chart.description());
                song.setMode(mode);
                return song;
            }).toArray(SongData[]::new));
            return folder;
        }).toArray(TableData.TableFolder[]::new));
        return table;
    }

    /**
     * The gamemode field in a backbeat collection isn't some ordained-by-god thing,
     * but general consensus is that these are the ones we use to describe tables for
     * these games.
     *
     * Backbeat supports any rhythm game, so we need to be able to filter out e.g. kshoot
     * or stepmania tables from being in your game. lol.
     */
    private static Optional<Mode> mode(String gamemode) {
        return Optional.ofNullable(switch (gamemode) {
            case "bms-5k" -> Mode.BEAT_5K;
            case "bms-7k" -> Mode.BEAT_7K;
            case "bms-10k" -> Mode.BEAT_10K;
            case "bms-14k" -> Mode.BEAT_14K;
            case "pms-5b" -> Mode.POPN_5K;
            case "pms-9b" -> Mode.POPN_9K;
            case "kms-24k" -> Mode.KEYBOARD_24K;
            case "kms-48k" -> Mode.KEYBOARD_24K_DOUBLE;
            default -> null;
        });
    }
}
