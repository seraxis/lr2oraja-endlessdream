package bms.player.beatoraja.external;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.MainState;
import bms.player.beatoraja.ScoreData;
import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.player.beatoraja.result.AbstractResult;
import bms.player.beatoraja.result.CourseResult;
import bms.player.beatoraja.result.MusicResult;
import bms.player.beatoraja.skin.property.IntegerPropertyFactory;
import bms.player.beatoraja.skin.property.StringPropertyFactory;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bms.player.beatoraja.skin.SkinProperty.*;

public class WebhookHandler {
    private void writeMultipartField(OutputStream os, String boundary, String name, String value) throws IOException {
        os.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
        os.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n").getBytes("UTF-8"));
        os.write("\r\n".getBytes("UTF-8"));
        os.write(value.getBytes("UTF-8"));
        os.write("\r\n".getBytes("UTF-8"));
    }

    private void writeMultipartFile(OutputStream os, String boundary, String name, Path filePath) throws IOException {
        os.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
        os.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"screenshot.png\"\r\n").getBytes("UTF-8"));
        os.write("Content-Type: image/png\r\n".getBytes("UTF-8"));
        os.write("\r\n".getBytes("UTF-8"));

        // Write file content
        Files.copy(filePath, os);
        os.write("\r\n".getBytes("UTF-8"));
    }

    public void sendWebhookWithImage(String payload, Path imagePath, URL webhookUrl) {
        try {
            HttpURLConnection webhook = (HttpURLConnection) webhookUrl.openConnection();

            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            webhook.setRequestMethod("POST");
            webhook.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            webhook.setDoOutput(true);

            try (OutputStream os = webhook.getOutputStream()) {
                writeMultipartField(os, boundary, "payload_json", payload);

                writeMultipartFile(os, boundary, "files[0]", imagePath);

                os.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));
            }

            int responseCode = webhook.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                ImGuiNotify.warning("Unexpected http response code when sending webhook: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> createField(String name, String value) {
        Map<String, String> field = new HashMap<>();
        field.put("name", name);
        field.put("value", value);
        return field;
    }

    private static String createTitle(MainState currentState) {
        String titleString = "";

        final String tableLevel = StringPropertyFactory.getStringProperty(STRING_TABLE_LEVEL).get(currentState);
        final String fullTitle = StringPropertyFactory.getStringProperty(STRING_FULLTITLE).get(currentState);
        String rank = ScreenShotExporter.getRankTypeName(currentState);
        final String clearType = ScreenShotExporter.getClearTypeName(currentState);

        if (!tableLevel.isEmpty()) {
            titleString += tableLevel + " ";
        }

        if (!fullTitle.isEmpty()) {
            titleString += fullTitle + " ";
        }

        if (!rank.isEmpty()) {
            titleString += rank + " ";
        }

        if (!clearType.isEmpty()) {
            titleString += clearType;
        }

        return titleString;
    }

    private static String formatDiff(int newScore, int oldScore) {
        int improvement = newScore - oldScore;
        if (improvement > 0) {
            return String.format("(+%d) :arrow_up:", improvement);
        } else if (improvement < 0) {
            return String.format("(%d) :arrow_down:", improvement);
        } else {
            return "(±0) :arrow_right:";
        }
    }

    private static String formatPercent(ScoreData newscore, int maxscore) {
        float percent = 100.0f * (Float.valueOf(newscore.getExscore()) / Float.valueOf(maxscore));
        return String.format("(%.2f%%)", percent);
    }

    private static String formatRank(MainState currentState, ScoreData newscore, int maxscore) {
        
        float percent = 100.0f * (Float.valueOf(newscore.getExscore()) / Float.valueOf(maxscore));
        StringBuilder sb = new StringBuilder();


        return "";
    }

    public Map<String, Object> createWebhookPayload(MainState currentState) {
        Map<String, Object> payload = new HashMap<>();
        Config config = currentState.resource.getConfig();

        String webhookName = currentState.resource.getConfig().getWebhookName();
        payload.put("username", webhookName.isEmpty() ? "Endless Dream" : webhookName);
        String webhookAvatar = currentState.resource.getConfig().getWebhookAvatar();
        payload.put("avatar_url", webhookAvatar.isEmpty() ? "" : webhookAvatar);

        if (currentState.resource.getConfig().getWebhookOption() == 2) {
            Map<String, Object> embed = new HashMap<>();
            Map<String, String> author = new HashMap<>();

            Map<String, String> image = new HashMap<>();
            image.put("url", "attachment://screenshot.png");
            embed.put("image", image);

            // Score specific
            if (currentState instanceof MusicResult || currentState instanceof CourseResult) {
                Map<String, String> footer = new HashMap<>();
                embed.put("title", createTitle(currentState));
                embed.put("color", ScreenShotExporter.getClearTypeColour(currentState));
                author.put("name", StringPropertyFactory.getStringProperty(STRING_TABLE_NAME).get(currentState));
                embed.put("author", author);

                ScoreData newScore = ((AbstractResult) currentState).getNewScore();
                ScoreData oldScore = ((AbstractResult) currentState).getOldScore();
                int maxscore = IntegerPropertyFactory.getIntegerProperty(NUMBER_MAXSCORE).get(currentState);

//                embed.put("fields", Arrays.asList(
//                        createField(
//                                String.format("EX Score: %s", newScore.getExscore()),
//                                formatDiff(newScore.getExscore(), oldScore != null ? oldScore.getExscore() : 0)
//                        )
//                ));

                StringBuilder description = new StringBuilder();
                description.append(String.format("**EX Score: %s/%s** %s",
                        newScore.getExscore(),
                        maxscore,
                        formatDiff(newScore.getExscore(), oldScore != null ? oldScore.getExscore() : 0))
                );
                // Rank AAA-51 (86.7%) UP
                description.append(String.format("**Rank %s %s",
                        formatRank(currentState, newScore, maxscore),
                        formatPercent(newScore, maxscore))
                );

                embed.put("description", description);
                footer.put("text", "LR2oraja ~Endless Dream~ Scorecard");
                embed.put("footer", footer);
            } else {
                author.put("name", "LR2oraja ~Endless Dream~");
                embed.put("author", author);
            }

            payload.put("embeds", Arrays.asList(embed));
        }

        return payload;
    }
}
