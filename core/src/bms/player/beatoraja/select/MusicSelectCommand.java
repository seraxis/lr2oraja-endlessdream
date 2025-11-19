package bms.player.beatoraja.select;

import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.player.beatoraja.select.bar.*;
import bms.player.beatoraja.song.SongData;
import bms.player.beatoraja.BMSPlayerMode;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Queue;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

import static bms.player.beatoraja.SystemSoundManager.SoundType.FOLDER_OPEN;
import static bms.player.beatoraja.SystemSoundManager.SoundType.OPTION_CHANGE;

public enum MusicSelectCommand {

	// TODO 最終的には全てEventFactoryへ移動

	RESET_REPLAY(selector -> {
		if (selector.getBarManager().getSelected() instanceof SelectableBar bar) {
			for (int i = 0; i < MusicSelector.REPLAY; i++) {
				if (bar.existsReplay(i)) {
					selector.setSelectedReplay(i);
					return;
				}
			}
		}
		selector.setSelectedReplay(-1);
	}),
	NEXT_REPLAY(selector -> {
		if (selector.getBarManager().getSelected() instanceof SelectableBar bar) {
			for (int i = 1; i < MusicSelector.REPLAY; i++) {
				final int selectedreplay = selector.getSelectedReplay();
				if (bar.existsReplay((i + selectedreplay) % MusicSelector.REPLAY)) {
					selector.setSelectedReplay((i + selectedreplay) % MusicSelector.REPLAY);
					selector.play(OPTION_CHANGE);
					break;
				}
			}
		}
	}),
	PREV_REPLAY(selector -> {
		if (selector.getBarManager().getSelected() instanceof SelectableBar bar) {
			for (int i = 1; i < MusicSelector.REPLAY; i++) {
				final int selectedreplay = selector.getSelectedReplay();
				if (bar.existsReplay((selectedreplay + MusicSelector.REPLAY - i) % MusicSelector.REPLAY)) {
					selector.setSelectedReplay((selectedreplay + MusicSelector.REPLAY - i) % MusicSelector.REPLAY);
					selector.play(OPTION_CHANGE);
					break;
				}
			}
		}
	}),
	/**
	 * 譜面のMD5ハッシュをクリップボードにコピーする
	 */
	COPY_MD5_HASH(selector -> {
		if (selector.getBarManager().getSelected() instanceof SongBar songbar) {
			final SongData song = songbar.getSongData();
			if (song != null) {
				String hash = song.getMd5();
				if (hash != null && hash.length() > 0) {
                    // NOTE: Previous clipboard management is using the java.awt library
                    // which is broken only on macos.
                    // COPY_SHA256_HASH has the same issue
					Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent clipboardContent = new ClipboardContent();
                    clipboardContent.putString(hash);
                    clipboard.setContent(clipboardContent);
					ImGuiNotify.info(String.format("MD5 hash copied: %s", hash));
				}
			}
		}
	}),
	/**
	 * 譜面のMD5ハッシュをクリップボードにコピーする
	 */
	COPY_SHA256_HASH(selector -> {
		if (selector.getBarManager().getSelected() instanceof SongBar songbar) {
			final SongData song = songbar.getSongData();
			if (song != null) {
				String hash = song.getSha256();
				if (hash != null && hash.length() > 0) {
					Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent clipboardContent = new ClipboardContent();
                    clipboardContent.putString(hash);
                    clipboard.setContent(clipboardContent);
					ImGuiNotify.info(String.format("SHA256 hash copied: %s", hash), 2000);
				}
			}
		}
	}),
	DOWNLOAD_IPFS(selector -> {
        Queue<DirectoryBar> dir = selector.getBarManager().getDirectory();
        boolean startdownload = false;
        for (DirectoryBar d : dir) {
            if (d instanceof TableBar) {
                String selecturl = ((TableBar) d).getUrl();
                if (selecturl == null)
                    break;

                Bar current = selector.getBarManager().getSelected();
                if (current instanceof SongBar) {
                    final SongData song = ((SongBar) current).getSongData();
                    if (song != null && song.getIpfs() != null) {
                        selector.main.getMusicDownloadProcessor().start(song);
                        startdownload = true;
                    }
                }

                if (!startdownload) {
					LoggerFactory.getLogger(MusicSelectCommand.class).info("ダウンロードは開始されませんでした。");
                }
                break;
            }
		}
	}),
	DOWNLOAD_HTTP(selector -> {
		Bar current = selector.getBarManager().getSelected();
		if (current instanceof SongBar) {
			final SongData song = ((SongBar) current).getSongData();
			if (song == null) {
				LoggerFactory.getLogger(MusicSelectCommand.class).info("Not a valid song bar? Skipped...");
				return ;
			}
			LoggerFactory.getLogger(MusicSelectCommand.class).info("Missing song md5: {}", song.getMd5());
			if (song.getMd5() != null && !song.getMd5().isEmpty()) {
				selector.main.getHttpDownloadProcessor().submitMD5Task(song.getMd5(), song.getTitle());
			}
		}
	}),
	/**
	 * 同一フォルダにある譜面を全て表示する．コースの場合は構成譜面を全て表示する
	 */
	SHOW_SONGS_ON_SAME_FOLDER(selector -> {
		final BarManager bar = selector.getBarManager();
		Bar current = selector.getBarManager().getSelected();
		if (current instanceof SongBar && ((SongBar) current).existsSong()
				&& (bar.getDirectory().size == 0 || !(bar.getDirectory().last() instanceof SameFolderBar))) {
			SongData sd = ((SongBar) current).getSongData();
			bar.updateBar(new SameFolderBar(selector, sd.getFullTitle(), sd.getFolder()));
			selector.play(FOLDER_OPEN);
		} else if (current instanceof GradeBar) {
			List<Bar> songbars = Arrays.asList(((GradeBar) current).getSongDatas()).stream().distinct()
					.map(SongBar::new).collect(Collectors.toList());
			bar.updateBar(new ContainerBar(current.getTitle(), songbars.toArray(new Bar[songbars.size()])));
			selector.play(FOLDER_OPEN);
		}
	}),
	/**
	 * Open context menu for the currently selected bar
	 */
    SHOW_CONTEXT_MENU(selector -> {
		final BarManager bar = selector.getBarManager();
		Bar current = selector.getBarManager().getSelected();
        boolean alreadyInContextMenu =
            bar.getDirectory().size > 0 && bar.getDirectory().last() instanceof ContextMenuBar;
        if (current instanceof SongBar) {
            SongData song = ((SongBar)current).getSongData();
            if (!alreadyInContextMenu) {
                bar.updateBar(new ContextMenuBar(selector, song));
                selector.play(FOLDER_OPEN);
            }
            else { selector.selectSong(BMSPlayerMode.PLAY); }
        }
        else if (current instanceof TableBar) {
            if (!alreadyInContextMenu) {
                bar.updateBar(new ContextMenuBar(selector, ((TableBar)current)));
                selector.play(FOLDER_OPEN);
            }
            else if (selector.getBarManager().updateBar(current)) { selector.play(FOLDER_OPEN); }
        }
    });

    public final Consumer<MusicSelector> function;

	private MusicSelectCommand(Consumer<MusicSelector> function) {
		this.function = function;
	}
}
