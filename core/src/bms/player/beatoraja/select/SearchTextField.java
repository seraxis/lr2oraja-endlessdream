package bms.player.beatoraja.select;

import bms.player.beatoraja.Resolution;
import bms.player.beatoraja.SpriteBatchHelper;
import bms.player.beatoraja.input.KeyBoardInputProcesseor.ControlKeys;
import bms.player.beatoraja.select.bar.SearchWordBar;
import bms.player.beatoraja.skin.Skin;
import bms.player.beatoraja.skin.SkinObject;
import bms.player.beatoraja.skin.SkinText;
import bms.player.beatoraja.skin.SkinTextImage;
import bms.player.beatoraja.skin.lr2.LR2BitmapFontConverter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * 楽曲検索用テキストフィールド
 *
 * @author exch
 */
public class SearchTextField extends Stage {
	private static final Logger logger = LoggerFactory.getLogger(SearchTextField.class);
	
	/**
	 * フォント生成用クラス
	 */
	private FreeTypeFontGenerator generator;

	private BitmapFont searchfont;

	private TextField search;

	private Rectangle skinBounds;

	/**
	 * A reference to the skin who creates this search text field, only used to check if the skin has disposed or not
	 */
	private Skin skin;

	/**
	 * 画面クリック感知用Actor
	 */
	private Group screen;

	public SearchTextField(MusicSelector selector, Resolution resolution) {
		super(new FitViewport(resolution.width, resolution.height), SpriteBatchHelper.createSpriteBatch());

		skin = selector.getSkin();

		final Rectangle r = ((MusicSelectSkin) selector.getSkin()).getSearchTextRegion();

		try {
			Optional<BitmapFontConfig> searchFontConf = createSkinFont(selector, r);
			searchfont = searchFontConf.map(it -> it.bitmapFont)
					.orElse(createDefaultFont(selector, r));

			final TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle(); // background
			textFieldStyle.font = searchfont;
			textFieldStyle.fontColor = Color.WHITE;
			
			Pixmap cursorp = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
			cursorp.setColor(Color.toIntBits(255, 255, 255, 255));
			cursorp.fill();
			textFieldStyle.cursor = new TextureRegionDrawable(new TextureRegion(new Texture(cursorp)));
			cursorp.dispose();

			Pixmap selectionp = new Pixmap(2, 8, Pixmap.Format.RGBA8888);
			selectionp.setColor(Color.toIntBits(255, 255, 255, 255));
			selectionp.fill();
			textFieldStyle.selection = new TextureRegionDrawable(new TextureRegion(new Texture(selectionp)));
			selectionp.dispose();
			
			textFieldStyle.messageFont = searchfont;
			textFieldStyle.messageFontColor = Color.GRAY;

			search = new TextField("", textFieldStyle);
			search.setMessageText("search song");
			int align = SkinText.ALIGN_LEFT;
			if (searchFontConf.isPresent()) {
				search.getStyle().fontColor = searchFontConf.get().fontColor;
				search.getStyle().messageFontColor = searchFontConf.get().messageFontColor;
				align = searchFontConf.get().align;
			}
			search.setTextFieldListener(new TextFieldListener() {

				public void keyTyped(TextField textField, char key) {
					// Emergency exit to avoid npe
					if (searchfont == null) {
						return ;
					}

					if (key == '\n' || key == 13) {
						if (textField.getText().length() > 0) {
							SearchWordBar swb = new SearchWordBar(selector, textField.getText());
							int count = swb.getChildren().length;
							if (count > 0) {
								selector.getBarManager().addSearch(swb);
								selector.getBarManager().updateBar(null);
								selector.getBarManager().setSelected(swb);
								textField.setText("");
								textField.setMessageText(count + " song(s) found");
								textFieldStyle.messageFontColor = Color.valueOf("00c0c0");
							} else {
								textField.setText("");
								textField.setMessageText("no song found");
								textFieldStyle.messageFontColor = Color.DARK_GRAY;
								selector.main.getInputProcessor().isControlKeyPressed(ControlKeys.ENTER);
							}
						}
						
						textField.getOnscreenKeyboard().show(false);
						setKeyboardFocus(null);
					}
					BitmapFont.Glyph glyph = searchfont.getData().getGlyph(key);
					if (key >= 32 && key != 127 && glyph == null) {
						if (generator == null) {
							generator = createDefaultFontGenerator(selector);
						}
						FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
						parameter.size = (int) r.height;
						parameter.characters += textField.getText() + key;
						BitmapFont newsearchfont = generator.generateFont(parameter);
						textFieldStyle.font = newsearchfont;
						textFieldStyle.messageFont = newsearchfont;
						searchfont.dispose();
						searchfont = newsearchfont;
						textField.appendText(String.valueOf(key));
					}
				}
			});
			float boundX = align == SkinText.ALIGN_CENTER ? r.x - r.width / 2F
					: align == SkinText.ALIGN_RIGHT ? r.x - r.width
					: r.x;
			Rectangle actualSearchBound = new Rectangle(
					boundX,
					r.y,
					r.width,
					r.height
			);
			skinBounds = r;
			search.setBounds(boundX, r.y, r.width, r.height);
			search.setMaxLength(50);
			search.setFocusTraversal(false);
			search.setAlignment(
					align == SkinText.ALIGN_CENTER ? Align.center
					: align == SkinText.ALIGN_LEFT ? Align.left
					: Align.right
			);

			search.setVisible(true);
			search.addListener((e) -> {
				if (e.isHandled()) {
					selector.main.getInputProcessor().getKeyBoardInputProcesseor()
							.setTextInputMode(getKeyboardFocus() != null);
				}
				return false;
			});			

			screen = new Group();
			screen.setBounds(0, 0, resolution.width, resolution.height);
			screen.addListener(new ClickListener() {
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					if (getKeyboardFocus() != null && !actualSearchBound.contains(x, y)) {
						unfocus(selector);
					}
					return false;
				}
			});
			screen.addActor(search);
			addActor(screen);
		} catch (GdxRuntimeException e) {
			logger.warn("Search Text読み込み失敗");
		}
	}

	public void unfocus(MusicSelector selector) {
		if(search != null) {
			search.setText("");
			search.setMessageText("search song");
			search.getStyle().messageFontColor = Color.GRAY;
			search.getOnscreenKeyboard().show(false);			
		}
		setKeyboardFocus(null);
		selector.main.getInputProcessor().getKeyBoardInputProcesseor().setTextInputMode(false);
	}

	public void dispose() {
//		super.dispose();
		if (generator != null) {
			generator.dispose();
			generator = null;
		}
		if (searchfont != null) {
			searchfont.dispose();
			searchfont = null;
		}

		if (search != null) {
			disposeTextureRegionDrawable(search.getStyle().cursor);
			disposeTextureRegionDrawable(search.getStyle().selection);
		}
	}

	public Skin getSkin() {
		return skin;
	}

	public Rectangle getSkinBounds() {
		return skinBounds;
	}

	private FreeTypeFontGenerator createDefaultFontGenerator(MusicSelector selector) {
		return new FreeTypeFontGenerator(Gdx.files.internal(selector.main.getConfig().getSystemfontpath()));
	}

	private BitmapFont createDefaultFont(MusicSelector selector, Rectangle r) {
		if (generator == null) {
			generator = createDefaultFontGenerator(selector);
		}
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = (int) r.height;
		parameter.incremental = true;
		return generator.generateFont(parameter);
	}

	/**
	 * Create the bitmap font object from skin's definition
	 */
	private Optional<BitmapFontConfig> createSkinFont(MusicSelector selector, Rectangle r) {
		SkinText st = ((MusicSelectSkin) selector.getSkin()).searchText;
		if (!(st instanceof SkinTextImage)) {
			return Optional.empty();
		}

		SkinTextImage.SkinTextImageSource src = ((SkinTextImage) st).getSource();
		if (src == null) {
			return Optional.empty();
		}

		Color color = null;
		for (SkinObject.SkinObjectDestination dst : st.getAllDestination()) {
			if (dst != null && dst.color != null && (color == null || dst.color.a > color.a)) {
				color = dst.color;
			}
		}

		if (color == null || color.a == 0.0F) {
			color = Color.WHITE;
		}

		int align = st.getAlign();

		try {
			BitmapFont font = LR2BitmapFontConverter.create(src, r.height);
			return Optional.of(new BitmapFontConfig(
					font,
					color,
					new Color(color.r, color.g, color.b, color.a * 0.6F),
					align
			));
		} catch (Exception e) {
			logger.error("Failed to convert LR2's font into bitmap font: ", e);
			return Optional.empty();
		}
	}

	private record BitmapFontConfig(
		BitmapFont bitmapFont,
		Color fontColor,
		Color messageFontColor,
		int align
	) {}

	private void disposeTextureRegionDrawable(Drawable drawable) {
		if (!(drawable instanceof TextureRegionDrawable)) {
			return ;
		}
		TextureRegion region = ((TextureRegionDrawable) drawable).getRegion();
		if (region != null && region.getTexture() != null) {
			region.getTexture().dispose();
		}
	}
}
