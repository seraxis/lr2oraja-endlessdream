package bms.player.beatoraja.external;

import bms.player.beatoraja.MainState;
import bms.player.beatoraja.modmenu.ImGuiNotify;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class WebhookHandler {
    URL webhookUrl;

    public WebhookHandler(MainState currentState) {
        try {
            webhookUrl = new URL(currentState.resource.getConfig().getWebhookUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeMultipartField(OutputStream os, String boundary, String name, String value) throws IOException {
        os.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
        os.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n").getBytes("UTF-8"));
        os.write("\r\n".getBytes("UTF-8"));
        os.write(value.getBytes("UTF-8"));
        os.write("\r\n".getBytes("UTF-8"));
    }

    private void writeMultipartFile(OutputStream os, String boundary, String name, Path filePath) throws IOException {
        os.write(("--" + boundary + "\r\n").getBytes("UTF-8"));
        os.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filePath.getFileName() + "\"\r\n").getBytes("UTF-8"));
        os.write("Content-Type: image/png\r\n".getBytes("UTF-8"));
        os.write("\r\n".getBytes("UTF-8"));

        // Write file content
        Files.copy(filePath, os);
        os.write("\r\n".getBytes("UTF-8"));
    }

    public void sendWebhookWithImage(String payload, Path imagePath) {
        try {
            HttpURLConnection webhook = (HttpURLConnection) this.webhookUrl.openConnection();

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
}
