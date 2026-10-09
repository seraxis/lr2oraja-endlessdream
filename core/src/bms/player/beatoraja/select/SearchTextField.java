package bms.player.beatoraja.select;

import bms.player.beatoraja.MainState;
import bms.player.beatoraja.Resolution;
import bms.player.beatoraja.SpriteBatchHelper;
import bms.player.beatoraja.input.KeyBoardInputProcesseor.ControlKeys;
import bms.player.beatoraja.select.bar.SearchWordBar;

import bms.player.beatoraja.skin.Skin;
import bms.player.beatoraja.skin.SkinText;
import bms.player.beatoraja.skin.SkinTextImage;
import bms.player.beatoraja.skin.lr2.LR2BitmapFontConverter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.FitViewport;

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
			searchfont = createSkinFont(selector, r);

			if (searchfont == null) {
				searchfont = createDefaultFont(selector, r);
			}

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
			search.setBounds(r.x, r.y, r.width, r.height);
			search.setMaxLength(50);
			search.setFocusTraversal(false);

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
					if (getKeyboardFocus() != null && !r.contains(x, y)) {
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

		if (getBatch() != null) {
			getBatch().dispose();
		}
	}

	public Rectangle getSearchBounds() {
		return search != null ? new Rectangle(search.getX(), search.getY(), search.getWidth(), search.getHeight()) : null;
	}

	public Skin getSkin() {
		return skin;
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
	private BitmapFont createSkinFont(MusicSelector selector, Rectangle r) {
		SkinText st = ((MusicSelectSkin) selector.getSkin()).searchText;
		if (!(st instanceof SkinTextImage)) {
			return null;
		}

		SkinTextImage.SkinTextImageSource src = ((SkinTextImage) st).getSource();
		if (src == null) {
			return null;
		}

		try {
			return LR2BitmapFontConverter.create(src, r.height);
		} catch (Exception e) {
			logger.error("Failed to convert LR2's font into bitmap font: ", e);
			return null;
		}
	}

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
