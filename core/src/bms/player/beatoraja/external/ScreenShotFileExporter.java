package bms.player.beatoraja.external;

import bms.player.beatoraja.MainState;
import bms.player.beatoraja.config.KeyConfiguration;
import bms.player.beatoraja.decide.MusicDecide;
import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.player.beatoraja.play.BMSPlayer;
import bms.player.beatoraja.result.CourseResult;
import bms.player.beatoraja.result.MusicResult;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.skin.property.IntegerPropertyFactory;
import bms.player.beatoraja.skin.property.StringPropertyFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

import static bms.player.beatoraja.skin.SkinProperty.*;

public class ScreenShotFileExporter implements ScreenShotExporter {
    private static final Logger logger = LoggerFactory.getLogger(ScreenShotFileExporter.class);

    @Override
    public boolean send(MainState currentState, byte[] pixels) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String stateName = "";
        if (currentState instanceof MusicSelector) {
            stateName = "_Music_Select";
        } else if (currentState instanceof MusicDecide) {
            stateName = "_Decide";
        }
        if (currentState instanceof BMSPlayer) {
            final String tablelevel = StringPropertyFactory.getStringProperty(STRING_TABLE_LEVEL).get(currentState);
            if (!tablelevel.isEmpty()) {
                stateName = "_Play_" + tablelevel;
            } else {
                stateName = "_Play_LEVEL" + IntegerPropertyFactory.getIntegerProperty(NUMBER_PLAYLEVEL).get(currentState);
            }
            final String fulltitle = StringPropertyFactory.getStringProperty(STRING_FULLTITLE).get(currentState);
            if (!fulltitle.isEmpty()) {
                stateName += " " + fulltitle;
            }
        } else if (currentState instanceof MusicResult || currentState instanceof CourseResult) {
            if (currentState instanceof MusicResult) {
                final String tablelevel = StringPropertyFactory.getStringProperty(STRING_TABLE_LEVEL).get(currentState);
                if (tablelevel.length() > 0) {
                    stateName += "_" + tablelevel + " ";
                } else {
                    stateName += "_LEVEL" + IntegerPropertyFactory.getIntegerProperty(NUMBER_PLAYLEVEL).get(currentState) + " ";
                }
            } else {
                stateName += "_";
            }
            final String fulltitle = StringPropertyFactory.getStringProperty(STRING_FULLTITLE).get(currentState);
            if (fulltitle.length() > 0) stateName += fulltitle;
            stateName += " " + ScreenShotExporter.getClearTypeName(currentState);
            stateName += " " + ScreenShotExporter.getRankTypeName(currentState);
        } else if (currentState instanceof KeyConfiguration) {
            stateName = "_Config";
        }
        stateName = stateName.replace("\\", "￥").replace("/", "／").replace(":", "：").replace("*", "＊").replace("?", "？").replace("\"", "”").replace("<", "＜").replace(">", "＞").replace("|", "｜").replace("\t", " ");
        stateName = "_LR2oraja" + stateName;

        Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGBA8888);
        try {
            String path = "screenshot/" + sdf.format(Calendar.getInstance().getTime()) + stateName + ".png";
            BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
            PixmapIO.writePNG(new FileHandle(path), pixmap);
            logger.info("スクリーンショット保存:" + path);
            pixmap.dispose();
            ImGuiNotify.info(String.format("Screen shot saved: %s", path), 2000);

            this.sendClipboard(currentState, path);
            this.sendWebhook(currentState, path);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        pixmap.dispose();
        return false;
    }

    private void sendClipboard(MainState currentState, String path) {
        if (!currentState.resource.getConfig().isSetClipboardWhenScreenshot()) {
            // スクショのクリップボードコピーが有効でないなら終わる
            return;
        }
        try {
            // バイナリ一致させるためにファイルからデータ取得
            BufferedImage image = ImageIO.read(new File(path));

            // ARGBからRGBへ
            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            int px[] = new int[width * height];
            image.getRGB(0, 0, width, height, px, 0, width);
            output.setRGB(0, 0, width, height, px, 0, width);

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            ImageTransferable imageTransferable = new ImageTransferable(output);
            clipboard.setContents(imageTransferable, null);
            logger.info("スクリーンショット保存: Clipboard");
            ImGuiNotify.info("Screen shot saved : Clipboard", 2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendWebhook(MainState currentState, String path) {
        if (currentState.resource.getConfig().getWebhookOption() == 0 || currentState.resource.getConfig().getWebhookUrl().length == 0) {
            // Webhook action not enabled or missing URL
            return;
        }

        try {
            WebhookHandler handler = new WebhookHandler();
            Map<String, Object> payload = handler.createWebhookPayload(currentState);
            ObjectMapper om = new ObjectMapper();
            String payloadAsString = om.writeValueAsString(payload);

            List<URL> webhookUrls = Arrays.stream(currentState.resource.getConfig().getWebhookUrl()).map(url -> {
                try {
                    return new URL(url);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }).toList();

            for (URL webhookUrl : webhookUrls) {
                handler.sendWebhookWithImage(
                        payloadAsString,
                        Paths.get(path),
                        webhookUrl
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ImageTransferable implements Transferable {
        private Image image;

        public ImageTransferable(Image image) {
            this.image = image;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for (DataFlavor f : getTransferDataFlavors()) {
                if (f.equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(DataFlavor.imageFlavor)) {
                return image;
            }
            throw new UnsupportedFlavorException(flavor);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }
    }
}
