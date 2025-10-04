package bms.player.beatoraja.skin;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;

import bms.player.beatoraja.PixmapResourcePool;
import bms.player.beatoraja.skin.json.JsonSkin;
import bms.player.beatoraja.skin.BitmapFontCache.CacheableBitmapFont;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Array;

// Parallelized bitmap font preloader
// largely adopted from SkinTextBitmap.java

public class BitmapFontBatchLoader {
    public BitmapFontBatchLoader(JsonSkin.Skin skin, Path skinPath, boolean usecim,
                                 boolean useMipMaps) {
        this.usecim = usecim;
		this.useMipMaps = useMipMaps;

        for (JsonSkin.Font font : skin.font) {
            Path path = skinPath.getParent().resolve(font.path);
			boolean validPath = path.toString().toLowerCase().endsWith(".fnt");
            boolean alreadyCached = BitmapFontCache.Has(path);
            if (!validPath || alreadyCached) continue;
            fontPaths.put(path, font.type);
        }
    }

    private boolean usecim;
    private boolean useMipMaps;
    private HashMap<Path, Integer> fontPaths = new HashMap<Path, Integer>();
    private HashMap<Path, BitmapFont.BitmapFontData> fontData =
        new HashMap<Path, BitmapFont.BitmapFontData>();

    public void load() {
        int parallelism = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(parallelism);

        LinkedBlockingQueue<BitmapFont.BitmapFontData> parsedFontData =
            new LinkedBlockingQueue<BitmapFont.BitmapFontData>();
        // parse each font description file, queue up the resulting data
        Future<Boolean> parseTask = pool.submit(() -> {
            for (Path path : fontPaths.keySet()) {
                BitmapFont.BitmapFontData fontData =
                    new BitmapFont.BitmapFontData(new FileHandle(path.toFile()), false);
                this.fontData.put(path, fontData);
                try {
                    parsedFontData.put(fontData);
                }
                catch (InterruptedException e) {
                }
            }
            return true;
        });

        // each font data object contains a list of 'page' image file paths
        // we read the parse queue and load all the required images in parallel
        LinkedBlockingQueue<String> loadedImages = new LinkedBlockingQueue<String>();
        HashMap<String, TextureRegion> loadedTextures = new HashMap<String, TextureRegion>();

        // once for each path in the set
        // no particular exception handling because it's okay if we drop some images,
        // they'll be missing from the cache later and get loaded anyway
        final PixmapResourcePool resource = SkinLoader.getResource();

        while (!parseTask.isDone() || !parsedFontData.isEmpty()) {
            try {
                BitmapFont.BitmapFontData nextFontData =
                    parsedFontData.poll(1, TimeUnit.MILLISECONDS);
                if (nextFontData == null) continue;
                for (String imagePath : nextFontData.imagePaths) {
                    pool.submit(() -> {
                        resource.get(imagePath);
                        try {
                            loadedImages.put(imagePath);
                        }
                        catch (InterruptedException e) {
                        }
                    });
                }
            }
            catch (InterruptedException e) {
            }
        }

        pool.shutdown();

        // textures need to be created on the main thread due to GL calls
        // this loop should ensure that all image paths are read from the queue
        // after the thread pool terminates
        while (!pool.isTerminated() || !loadedImages.isEmpty()) {
            try {
                String imagePath = loadedImages.poll(1, TimeUnit.MILLISECONDS);
                if (imagePath == null) continue;
                Texture texture = SkinLoader.getTexture(imagePath, this.usecim, this.useMipMaps);
                loadedTextures.put(imagePath, new TextureRegion(texture));
            }
            catch (InterruptedException e) {
            }
        }

        for (Path path : fontPaths.keySet()) {
            var fontData = this.fontData.get(path);

            Array<TextureRegion> imageRegions = new Array<>(fontData.imagePaths.length);
            for (String imagePath : fontData.imagePaths) {
                imageRegions.add(loadedTextures.get(imagePath));
            }

            float size = fontData.lineHeight;
            float scaleW = 0;
            float scaleH = 0;
            fontSizes sizes = readFontSizes(path);
            if (sizes != null) {
                size = sizes.size();
                scaleW = sizes.scaleW();
                scaleH = sizes.scaleH();
            }
            else if (imageRegions.size > 0) {
                scaleW = (float)imageRegions.get(0).getRegionWidth();
                scaleH = (float)imageRegions.get(0).getRegionHeight();
            }

            CacheableBitmapFont fontCache = new CacheableBitmapFont();
            fontCache.font = new BitmapFont(fontData, imageRegions, true);
            fontCache.fontData = fontData;
            fontCache.regions = imageRegions;
            fontCache.type = fontPaths.get(path);
            fontCache.originalSize = size;
            fontCache.pageWidth = scaleW;
            fontCache.pageHeight = scaleH;
            BitmapFontCache.Set(path, fontCache);
        }
    }

    record fontSizes(float size, float scaleW, float scaleH){}

    private fontSizes readFontSizes(Path fontPath) {
        // size が BitmapFont から取得できないので、独自に取得する
        try (BufferedReader reader = new BufferedReader(
                 new InputStreamReader(new FileHandle(fontPath.toFile()).read()), 512)) {
            String line = reader.readLine();
            float size =
                (float)Integer.parseInt(line.substring(line.indexOf("size=") + 5).split(" ")[0]);
            line = reader.readLine();
            float scaleW =
                (float)Integer.parseInt(line.substring(line.indexOf("scaleW=") + 7).split(" ")[0]);
            float scaleH =
                (float)Integer.parseInt(line.substring(line.indexOf("scaleH=") + 7).split(" ")[0]);
            return new fontSizes(size, scaleW, scaleH);
        }
        catch (Exception e) {
            return null;
        }
    }
}
