package bms.player.beatoraja.stream.command;

import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.select.bar.HashBar;
import bms.player.beatoraja.song.SongData;
import com.badlogic.gdx.graphics.Color;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * reqコマンドの処理
 */
public class StreamRequestCommand extends StreamCommand {
    MusicSelector selector;
    int maxLength = 30;
    Thread updaterThread;
    UpdateBar updater;

    public StreamRequestCommand(MusicSelector selector) {
        COMMAND_STRING = "!!req";
        this.selector = selector;
        maxLength = this.selector.main.getPlayerConfig().getMaxRequestCount();
        updater = new UpdateBar();
        updaterThread = new Thread(updater);
        updaterThread.start();
    }

    @Override
    public void run(String data) {
        if (data.length() != 64) {
            return;
        }

        // is sha256
        updater.set(data);
    }

    @Override
    public void dispose() {
        if (updaterThread != null) {
            updaterThread.interrupt();
        }
    }

    class UpdateBar implements Runnable {

        HashBar bar;
        List<SongData> songDatas = new ArrayList<SongData>();

        // sha256 stack
        Stack<String> stack = new Stack<>();
        // lock obj
        private final Object lock = new Object();

        UpdateBar() {
            this.bar = new HashBar(selector, "Stream Request", new SongData[0]);
            this.bar.setSortable(false);
        }

        void set(String sha256) {
            synchronized (lock) {
                stack.add(sha256);
                addMessage(sha256);
            }
        }
		
        void addMessage(String sha256) {
                SongData[] _songDatas = selector.getSongDatabase().getSongDatas(new String[] { escape(sha256) });
                if (_songDatas.length > 0) {
                    SongData data = _songDatas[0];
                    if (songDatas.stream().filter(song -> song.getSha256().equals(sha256)).count() > 0 ||
                        stack.stream().filter(hash -> hash.equals(sha256)).count() > 1) { // stackの中身には自身を含めるため、1個の場合は通す
                        // すでに追加済みならスキップ
                        ImGuiNotify.warning(String.format("%s has already been added", data.getFullTitle()));
                    }
                    ImGuiNotify.info(String.format("Added %s to stream request list", data.getFullTitle()));
                } else {
                    ImGuiNotify.warning("Doesn't have requested song in collection");
                }
		}

        void update() {
            synchronized (lock) {
                // 選曲画面でないなら更新しない
                if (selector.main.getCurrentState() instanceof MusicSelector) {
                    // 溜まってるぶんを順に取得
                    while (stack.size() != 0) {
                        String sha256 = stack.pop();
                        if (songDatas.stream().filter(song -> song.getSha256().equals(sha256)).count() > 0) {
                            // すでに追加済みならスキップ
                            continue;
                        }
                        SongData[] _songDatas = selector.getSongDatabase().getSongDatas(new String[] { escape(sha256) });
                        if (_songDatas.length > 0) {
                            songDatas.add(_songDatas[0]);
                        }
                        if (songDatas.size() > maxLength) {
                            songDatas.remove(0);
                        }
                    }

                    if (songDatas.size() > 0) {
                        bar.setElements(songDatas.toArray(new SongData[0]));
                        try {
                            selector.getBarManager().setAppendDirectoryBar("Stream Request", bar);
                            selector.getBarManager().updateBar();
                        } catch (Exception e) {
                        } // continue
                    }
                }
            }
        }

        private String escape(String before) {
            // とりあえずSQLに渡すのでエスケープする
            StringBuilder after = new StringBuilder();
            for (int i = 0; i < before.length(); i++) {
                char c = before.charAt(i);
                if (c == '_' || c == '%' || c == '\\') {
                    after.append('\\');
                }
                after.append(c);
            }
            return after.toString();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (stack.size() != 0) {
                        update();
                    }
                } catch (Exception e) {
                    break;
                }
            }
        }
    }
}
