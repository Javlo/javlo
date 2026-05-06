package org.javlo.utils.captcha;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.javlo.context.GlobalContext;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CloudflareTurnstileVerifier {
    private static final String VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    public static boolean verify(GlobalContext globalContext, String captchaResponse) {
        try {
            String postData = "secret=" + globalContext.getSpecialConfig().getCloudflareTurnstileSecretKey() + "&response=" + captchaResponse;

            URL url = new URL(VERIFY_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                    return jsonResponse.get("success").getAsBoolean();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

