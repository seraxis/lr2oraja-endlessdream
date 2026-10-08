package bms.player.beatoraja.backbeat;

import ac.backbeat.sdk.AssetData;
import ac.backbeat.sdk.CourseContents;
import ac.backbeat.sdk.PackContents;
import ac.backbeat.sdk.Store;
import ac.backbeat.sdk.TableContents;
import ac.backbeat.sdk.TableContentsChart;
import ac.backbeat.sdk.TableContentsFolder;
import ac.backbeat.sdk.TableContentsLevel;
import bms.player.beatoraja.song.Resource;
import bms.player.beatoraja.song.SongData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class BackbeatIntegration implements AutoCloseable {
    private static final String[] GAMEMODES_WE_ACKNLOWLEDGE = {
            "bms-5k", "bms-7k", "bms-10k", "bms-14k",
            "pms-5b", "pms-9b", "kms-24k", "kms-48k"
    };

    private final Store store;

    private BackbeatIntegration(Store store) {
        this.store = store;
    }

    public static BackbeatIntegration open() {
        return new BackbeatIntegration(Store.open());
    }

    public String sqliteAttachCommand() {
        return store.sqliteAttachCommand();
    }

    public String sqliteDetachCommand() {
        return store.sqliteDetachCommand();
    }

    public Optional<byte[]> chartData(SongData song) {
        return store.getBundle(song.getBackbeatBundleId()).map(bundle -> {
            try (bundle) {
                return bundle.chartData();
            }
        });
    }

    public Optional<Resource> resolveAsset(SongData song, String relativePath) {
        return store.resolveAsset(song.getBackbeatBundleId(), relativePath).map(asset -> {
            if (asset instanceof AssetData.File file) return Resource.file(file.path(), relativePath);
            AssetData.Bytes bytes = (AssetData.Bytes) asset;
            return Resource.bytes("backbeat:" + song.getBackbeatBundleId() + ":" + relativePath,
                    relativePath, bytes.data());
        });
    }

    public List<PackContents> installedPacks() {
        List<PackContents> packs = new ArrayList<>();
        for (var metadata : store.listPacks(GAMEMODES_WE_ACKNLOWLEDGE)) {
            store.getPack(metadata.url()).ifPresent(packs::add);
        }
        return List.copyOf(packs);
    }

    public List<CourseContents> installedCourses() {
        List<CourseContents> courses = new ArrayList<>();
        for (var metadata : store.listCourses(GAMEMODES_WE_ACKNLOWLEDGE)) {
            store.getCourse(metadata.url()).ifPresent(courses::add);
        }
        return List.copyOf(courses);
    }

    public List<CatalogTable> installedTables() {
        List<CatalogTable> tables = new ArrayList<>();
        for (var metadata : store.listTables(GAMEMODES_WE_ACKNLOWLEDGE)) {
            installedTable(metadata.url()).ifPresent(tables::add);
        }
        return List.copyOf(tables);
    }

    public Optional<CatalogTable> installedTable(String url) {
        return store.getTable(url).map(table -> toCatalogTable(url, table));
    }

    private CatalogTable toCatalogTable(String url, TableContents table) {
        Function<TableContentsChart, CatalogTableChart> convertChart = chart -> new CatalogTableChart(
                chart.id(), chart.desc());

        List<CatalogTableSection> sections = new ArrayList<>();
        for (TableContentsLevel level : table.levels()) {
            List<CatalogTableChart> charts = level.charts().stream()
                    .map(convertChart).toList();
            if (!charts.isEmpty()) {
                sections.add(new CatalogTableSection(table.symbol() + level.level(), charts));
            }
        }
        for (TableContentsFolder folder : table.folders()) {
            List<CatalogTableChart> charts = folder.charts().stream()
                    .map(convertChart).toList();
            if (!charts.isEmpty()) {
                sections.add(new CatalogTableSection(folder.name(), charts));
            }
        }
        return new CatalogTable(url, table.name(), table.symbol(), table.gamemode(), sections);
    }

    public record CatalogChart(String bundleId, String filename, String sha256, String preview, byte[] data) {
        public CatalogChart {
            data = data.clone();
        }

        @Override
        public byte[] data() {
            return data.clone();
        }
    }

    public record CatalogTable(String url, String name, String symbol, String gamemode,
                               List<CatalogTableSection> sections) {
        public CatalogTable { sections = List.copyOf(sections); }
    }

    public record CatalogTableSection(String name, List<CatalogTableChart> charts) {
        public CatalogTableSection { charts = List.copyOf(charts); }
    }

    public record CatalogTableChart(String id, String description) {}

    @Override
    public void close() {
        store.close();
    }
}
