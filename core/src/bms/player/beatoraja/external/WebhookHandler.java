package bms.player.beatoraja.external;

import bms.model.Mode;
import bms.player.beatoraja.MainState;
import bms.player.beatoraja.ReplayData;
import bms.player.beatoraja.ScoreData;
import bms.player.beatoraja.modmenu.ImGuiNotify;
import bms.player.beatoraja.result.AbstractResult;
import bms.player.beatoraja.result.CourseResult;
import bms.player.beatoraja.result.MusicResult;
import bms.player.beatoraja.skin.property.IntegerPropertyFactory;
import bms.player.beatoraja.skin.property.StringPropertyFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

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

    public Map<String, Object> createWebhookPayload(MainState currentState) {
        Map<String, Object> payload = new HashMap<>();

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
                AbstractResult resultState = ((AbstractResult) currentState);
                ScoreData newScore = resultState.getNewScore();
                ScoreData oldScore = resultState.getOldScore();
                int maxScore = IntegerPropertyFactory.getIntegerProperty(NUMBER_MAXSCORE).get(currentState);

                StringBuilder description = new StringBuilder();
                description.append(String.format("**DJ LEVEL:** %s \n",
                        formatRank(currentState, newScore, maxScore))
                );
                description.append(String.format("**EX SCORE: %s** %s\n",
                        newScore.getExscore(),
                        formatDiff(newScore.getExscore(), oldScore != null ? oldScore.getExscore() : 0))
                );
                description.append(String.format("**BAD/POOR: %s** %s\n",
                        getBPCount(newScore),
                        formatDiff(getBPCount(newScore), getBPCount(oldScore)))
                );
                if (resultState.getIRRank() !=0) {
                    description.append(String.format("**IR RANK: %d/%d** %s\n",
                            resultState.getIRRank(),
                            resultState.getIRTotalPlayer(),
                            formatDiff(resultState.getIRRank(), resultState.getOldIRRank()))
                    );
                }
                if (currentState.resource.getOriginalMode().equals(Mode.BEAT_7K)) {
                    ReplayData rd = currentState.resource.getReplayData();
                    description.append(String.format("**PATTERN: %s** \n",
                            formatRandom(rd))
                    );
                }
                description.append(formatLinks(currentState));

                Map<String, String> footer = new HashMap<>();
                embed.put("title", createTitle(currentState));
                embed.put("color", ScreenShotExporter.getClearTypeColour(currentState));
                author.put("name", StringPropertyFactory.getStringProperty(STRING_TABLE_NAME).get(currentState));
                embed.put("author", author);
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

    // 7/9 == 14/18 == 77.77% == AA
    // exact ex matches on the grade boundary are by convention "[GRADE]-0"
    public enum GradeRank {
        MAX_MINUS(17.0f, "MAX-"),
        AAA_PLUS(16.0f,"AAA+"),
        AAA_MINUS(15.0f, "AAA-"),
        AA_PLUS(14.0f, "AA+"),
        AA_MINUS(13.0f, "AA-"),
        A_PLUS(12.0f, "A+"),
        A_MINUS(11.0f, "A-"),
        B_PLUS(10.0f, "B+"),
        C(8.0f, "C+"),
        D(6.0f, "D+"),
        E(4.0f, "E+"),
        F(0.0f, "F+");


        private final float numerator;
        private final String text;

        GradeRank(float numerator, String text) {
            this.numerator = numerator;
            this.text = text;
        }
        
        public float getPercent() { return (this.numerator/18.0f) * 100.0f; }
        public String getText() { return this.text; }
        public float getNumerator() { return this.numerator; }
    }

    // BAD + POOR + EPOOR
    private static int getBPCount(ScoreData newScore) {
        return newScore.getJudgeCount(3) + newScore.getJudgeCount(4) + newScore.getJudgeCount(5);
    }

    // Calculates the number used in rank deltas. e.g. AA+76 MAX-133
    private static int rankRelativeExDiff(int ex, int max, float rankNumerator) {
        // Even numerators produce [GRADE]+ and odd produces [GRADE]-
        if (rankNumerator % 2 == 0) {
            double gradeExTarget = Math.ceil(Float.valueOf(max) * (rankNumerator)/18.0f);
            return (int) (ex - gradeExTarget);
        } else {
            double gradeExTarget = Math.ceil(Float.valueOf(max) * (rankNumerator + 1.0f)/18.0f);
            return (int) (gradeExTarget - ex);
        }
    }

    // Integer difference + emoji
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
    
    private static String formatLinks(MainState currentState) {
        var song = currentState.resource.getSongdata();
        String ss = "";
        var md5 = song.getMd5();
        String lr2ir =
                "http://www.dream-pro.info/~lavalse/LR2IR/search.cgi?mode=ranking&bmsmd5=";
        if (md5 != null) { ss += " [LR2IR](" + lr2ir + md5 + ")"; }
        String charturl = "https://bms-score-viewer.pages.dev/view?md5=";
        if (md5 != null) ss += " |";
        ss += " [Chart](" + charturl + md5 + ")";

        var levels = currentState.resource.getReverseLookupLevels();
        for (var level : levels) { ss += " | " + level; }
        return ss;
    }

    private static String formatPercent(ScoreData newScore, int maxScore) {
        float percent = 100.0f * (Float.valueOf(newScore.getExscore()) / Float.valueOf(maxScore));
        return String.format("(%.2f%%)", percent);
    }

    // Makes rank string in "[GRADE][+/-][Relative diff] ([percent]) [emoji]" format. e.g "AAA-53 (86.53%) :arrow_up:"
    private static String formatRank(MainState currentState, ScoreData newScore, int maxScore) {
        int ex = newScore.getExscore();
        float percent = 100.0f * (Float.valueOf(ex) / Float.valueOf(maxScore));
        StringBuilder sb = new StringBuilder();
        int currentRank = 0;
        int oldRank = 0;

        for (GradeRank rank : GradeRank.values()) {
            if (percent > rank.getPercent()) {
                currentRank = (int) (Math.floor(rank.getNumerator()/2.0f) * 2.0f);
                sb.append(String.format("**%s%d**", rank.getText(), rankRelativeExDiff(ex, maxScore, rank.getNumerator())));
                break;
            }
        }

        float oldPercent = 100.0f * Float.valueOf(((AbstractResult) currentState).getOldScore().getExscore()) / Float.valueOf(maxScore);
        for (GradeRank rank : GradeRank.values()) {
            if (oldPercent > rank.getPercent()) {
                oldRank  = (int) (Math.floor(rank.getNumerator()/2.0f) * 2.0f);
                break;
            }
        }
        sb.append(String.format(" %s", formatPercent(newScore, maxScore)));

        if (currentRank > oldRank) {
            sb.append(String.format(" :arrow_up:"));
        } else if (currentRank < oldRank) {
            sb.append(String.format(" :arrow_down:"));
        } else {
            sb.append(String.format(" :arrow_right:"));
        }
        return sb.toString();
    }

    // magic numbers identified in Random.java
    private static String formatRandom(ReplayData rd) {
        StringBuilder sb = new StringBuilder();

        switch (rd.randomoption) {
            case 0: // IDENTITY
                sb.append("1234567");
                break;
            case 1: // MIRROR
                sb.append("7654321");
                break;
            case 2, 3: // RANDOM, R-RAN
                IntStream.of(rd.laneShufflePattern[0])
                        .limit(7) // 7K only, skip 7th index which contains scr
                        .map(i -> i + 1)
                        .mapToObj(String::valueOf)
                        .forEach(sb::append);
                break;
            case 4:
                sb.append("SRAN");
                break;
            case 5:
                sb.append("SPIRAL");
                break;
            case 6:
                sb.append("HRAN");
                break;
            case 7:
                sb.append("ALLSCR");
                break;
            default:
                sb.append("N/A");
        }
        return sb.toString();
    }
}