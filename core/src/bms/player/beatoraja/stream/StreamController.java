package bms.player.beatoraja.stream;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bms.player.beatoraja.MessageRenderer;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.stream.command.StreamCommand;
import bms.player.beatoraja.stream.command.StreamRequestCommand;

/**
 * beatoraja パイプで受け取った文字列処理
 */
public class StreamController {
    private static final Logger logger = LoggerFactory.getLogger(StreamController.class);
    StreamCommand[] commands;
    BufferedReader pipeBuffer;
    Thread polling;
    boolean isActive = false;
    MusicSelector selector;

    public StreamController(MusicSelector selector) {
        this.selector = selector;
        commands = new StreamCommand[] { new StreamRequestCommand(this.selector) };
        try {
            pipeBuffer = new BufferedReader(new FileReader("\\\\.\\pipe\\beatoraja"));
            isActive = true;
        } catch (Exception e) {
            e.printStackTrace();
            dispose();
        }
    }

    public void run() {
        if (pipeBuffer == null) {
            return;
        }
        polling = new Thread(() -> {
            try {
                String line = null;
                while (!Thread.interrupted()) {
                    try {
                        line = pipeBuffer.readLine();
                        if (line == null) {
                            break;
                        }
						logger.info("受信:{}", line);
                        execute(line);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            while (!pipeBuffer.ready());
        } catch (IOException e) {
            e.printStackTrace();
        }
        polling.start();
    }

    public void dispose() {
        if (polling != null) {
            try {
                polling.interrupt();
                polling = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (pipeBuffer != null) {
            try {
                pipeBuffer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for(int i = 0; i < commands.length; i++) {
            commands[i].dispose();
        }
        logger.info("パイプリソース破棄完了");
    }

    private void execute(String line) {
        for(int i = 0; i < commands.length; i++) {
            String cmd = commands[i].COMMAND_STRING + " ";
            String[] splitLine = line.split(cmd);
            String data = splitLine.length == 2 ? splitLine[1] : "";
            commands[i].run(data);
        }
    }

}
