package bms.player.beatoraja.skin.lr2;

import bms.player.beatoraja.skin.SkinTextImage;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IdentityMap;
import com.badlogic.gdx.utils.IntMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Convert LR2's picture based font to bitmap font from LibGDX
 */
public class LR2BitmapFontConverter {

	public static BitmapFont create(SkinTextImage.SkinTextImageSource src, float targetHeight) {
		final int size = src.getSize();
		final int margin = src.getMargin();

		// NOTE: Page definition in LR2font is not guaranteed to be serialized, but must be in libgdx's.
		//  Hence we must re-index the indexes
		IdentityMap<Texture, Integer> pageIndex = new IdentityMap<>();
		Array<LR2FontBuilder.Page> pages = new Array<>();
		Array<TextureRegion> pageRegions = new Array<>();
        Array<LR2FontBuilder.Glyph> glyphs = new Array<>();

		for (IntMap.Keys it = src.getCodes(); it.hasNext; ) {
			int code = it.next();
			TextureRegion img = src.getImage(code);
			if (img == null || img.getRegionWidth() <= 0 || img.getRegionHeight() <= 0) {
				continue;
			}
			if (code < 0 || code > 0xFFFF) {
				continue;
			}
			Texture tex = img.getTexture();
			Integer idx = pageIndex.get(tex);
			if (idx == null) {
				idx = pages.size;
				pageIndex.put(tex, idx);
				pageRegions.add(new TextureRegion(tex));
				pages.add(new LR2FontBuilder.Page("page" + idx, tex.getWidth(), tex.getHeight()));
			}

            glyphs.add(new LR2FontBuilder.Glyph(
                    code,
                    idx,
                    img.getRegionX(),
                    img.getRegionY(),
                    img.getRegionWidth(),
                    img.getRegionHeight()
            ));
		}

		final String fnt = LR2FontBuilder.build(size, margin, pages, glyphs);
		BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData();
		data.load(new FileHandle("lr2font.fnt") {
			@Override
			public InputStream read() {
				return new ByteArrayInputStream(fnt.getBytes(StandardCharsets.UTF_8));
			}
		}, false);

		BitmapFont font = new BitmapFont(data, pageRegions, true);

		float rectH = data.capHeight;
		data.setScale(targetHeight / size, targetHeight / rectH);
		data.cursorX = 0f;

		return font;
	}

	private LR2BitmapFontConverter() {

	}
}
