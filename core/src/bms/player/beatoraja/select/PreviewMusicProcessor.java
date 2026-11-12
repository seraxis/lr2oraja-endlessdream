package bms.player.beatoraja.select;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.Config.SongPreview;
import bms.player.beatoraja.audio.AudioDriver;
import bms.player.beatoraja.song.Resource;
import bms.player.beatoraja.song.SongData;

/**
 * プレビュー再生管理用クラス
 *
 * @author exch
 */
public class PreviewMusicProcessor {
    private static final Logger logger = LoggerFactory.getLogger(PreviewMusicProcessor.class);
    /**
     * 音源読み込みタスク
     */
    private Deque<PreviewCommand> commands = new ConcurrentLinkedDeque<>();

    private PreviewThread preview;

    private String defaultMusic = "";

    private SongData current;

    private final AudioDriver audio;

    private final Config config;

    public PreviewMusicProcessor(AudioDriver audio, Config config) {
        this.audio = audio;
        this.config = config;
    }

    public void setDefault(String path) {
        defaultMusic = (path != null ? path : "");
    }

    public void start(SongData song) {
        if(preview == null) {
            preview = new PreviewThread();
            preview.start();
        }
        current = song;

        PreviewCommand command = PreviewCommand.DEFAULT;
        if (song != null && song.getPreview() != null && song.getPreview().length() > 0) {
            try {
                command = song.resolveAssetWithExtensions(song.getPreview(), ".wav", ".flac", ".ogg", ".mp3")
                        .map(PreviewCommand::resource).orElse(PreviewCommand.DEFAULT);
            } catch (RuntimeException e) {
                logger.warn(e.getMessage());
            }
        }
        commands.add(command);
    }

    public SongData getSongData() {
        return current;
    }

    public void stop() {
        preview.stop = true;
        preview = null;
    }

    class PreviewThread extends Thread {

        private boolean stop;
        private PreviewCommand playing;
        private float currentVolume;

        public void run() {
            audio.play(defaultMusic, config.getAudioConfig().getSystemvolume(), true);
            playing = PreviewCommand.file(defaultMusic);
            currentVolume = config.getAudioConfig().getSystemvolume();
            while(!stop) {
                if(!commands.isEmpty()) {
                    PreviewCommand command = commands.removeFirst();
                    if(command == PreviewCommand.DEFAULT) command = PreviewCommand.file(defaultMusic);
                    if(!command.key.equals(playing.key)) {
                        stopPreview(true);
                        if(command.resource != null) {
                            audio.play(command.resource, config.getAudioConfig().getSystemvolume(), config.getSongPreview() == SongPreview.LOOP);
                        } else {
                            audio.setVolume(defaultMusic, config.getAudioConfig().getSystemvolume());
                        }
                        playing = command;
                    }
                } else if(playing.resource != null && !audio.isPlaying(playing.resource)){
                	// プレビュー演奏終了後に選曲BGMに戻す
                    stopPreview(true);
                    audio.setVolume(defaultMusic, config.getAudioConfig().getSystemvolume());
                    playing = PreviewCommand.file(defaultMusic);
                } else if(currentVolume != config.getAudioConfig().getSystemvolume()){
                    if (playing.resource != null) audio.setVolume(playing.resource, config.getAudioConfig().getSystemvolume());
                    else audio.setVolume(defaultMusic, config.getAudioConfig().getSystemvolume());
                    currentVolume = config.getAudioConfig().getSystemvolume();
                } else {
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                    }
                }
            }
            this.stopPreview(false);
        }

        private void stopPreview(boolean pause) {
            if(playing != null && !playing.key.isEmpty()) {
                if(playing.resource != null) {
					audio.stop(playing.resource);
					audio.dispose(playing.resource);
                } else if(pause) {
                	for(int i = 10;i >= 0;i--) {
                		float vol = i * 0.1f * config.getAudioConfig().getSystemvolume();
                        audio.setVolume(defaultMusic, vol);
                        // TODO フェードアウトはAudioDriver側で実装したい
                        try {
							sleep(15);
						} catch (InterruptedException e) {
						}
                	}
                } else {
                    audio.stop(defaultMusic);
                }
            }
        }
    }

    private record PreviewCommand(String key, Resource resource) {
        private static final PreviewCommand DEFAULT = new PreviewCommand("", null);

        private static PreviewCommand file(String path) {
            return new PreviewCommand(path, null);
        }

        private static PreviewCommand resource(Resource resource) {
            return new PreviewCommand(resource.key(), resource);
        }
    }
}
