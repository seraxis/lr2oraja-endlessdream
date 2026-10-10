package bms.player.beatoraja.skin.lr2;

import com.badlogic.gdx.utils.Array;

import java.util.List;

/**
 * Build the BMFont(.fnt file) from LR2's picture based font(.lr2font) in memory
 *
 * @author Catizard
 * @see <a href="https://www.angelcode.com/products/bmfont/doc/file_format.html">BMFont<a>
 */
public class LR2FontBuilder {

	/**
	 * Build the BMFont data
	 *
	 * @param size each character's size; defined by #S
	 * @param margin each character's margin; defined by #M
	 * @param pages densed pages
	 * @param glyphs utf coded glyphs
	 */
	public static String build(int size, int margin, Array<Page> pages, Array<Glyph> glyphs) {
		if (pages.isEmpty()) {
			throw new IllegalArgumentException("no texture page provided");
		}

		int rectH = 0;
		int spaceAdvance = Math.max(1, size / 2);
		for (Glyph g : glyphs) {
			rectH = Math.max(rectH, g.h);
			if (g.code == ' ') {
				spaceAdvance = g.w + margin;
			}
		}
		if (rectH == 0) {
			throw new IllegalArgumentException("no glyph provided");
		}

		StringBuilder data = new StringBuilder();

		data.append("info face=\"lr2font\" size=").append(size)
				.append(" bold=0 italic=0 charset=\"\" unicode=1 stretchH=100 smooth=0 aa=1")
				.append(" padding=0,0,0,0 spacing=0,0 outline=0\n");

		data.append("common lineHeight=").append(rectH)
				.append(" base=").append(rectH)
				.append(" scaleW=").append(pages.get(0).width)
				.append(" scaleH=").append(pages.get(0).height)
				.append(" pages=").append(pages.size)
				.append(" packed=0\n");

		for (int i = 0; i < pages.size; ++i) {
			data.append("page id=").append(i)
					.append("file=\"").append(pages.get(i).file).append("\"\n");
		}

		for (Glyph g : glyphs) {
            if (g.code < 0 || g.code > 0xFFFF) {
                continue;
            }
            data.append("char id=").append(g.code)
              .append(" x=").append(g.x)
              .append(" y=").append(g.y)
              .append(" width=").append(g.w)
              .append(" height=").append(g.h)
              .append(" xoffset=0")
              .append(" yoffset=0")
              .append(" xadvance=").append(g.w + margin)
              .append(" page=").append(g.page)
              .append(" chnl=15\n");
        }

        data.append("metrics ascent=0 descent=0 down=").append(-rectH)
          .append(" capHeight=").append(rectH)
          .append(" lineHeight=").append(rectH)
          .append(" spaceXAdvance=").append(spaceAdvance)
          .append(" xHeight=").append(rectH)
          .append("\n");

        return data.toString();
	}

	public static final class Page {
		public String file;
		public int width, height;

		public Page(String file, int width, int height) {
			this.file = file;
			this.width = width;
			this.height = height;
		}
	}

	public static final class Glyph {
		public int code;
		public int page;
		public int x, y, w, h;

		public Glyph(int code, int page, int x, int y, int w, int h) {
			this.code = code;
			this.page = page;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}
	}

	private LR2FontBuilder() {

	}
}
