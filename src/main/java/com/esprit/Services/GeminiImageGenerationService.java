package com.esprit.Services;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service to generate images using Gemini 2.0 Flash Preview Image Generation model.
 * Uses OkHttpClient to send requests to the Google Generative Language API.
 */
public class GeminiImageGenerationService {

    private static final String GEMINI_KEY = "AIzaSyA0GmmWDPsVB_5PSH8sIyxEFqMg55qoC2c"; // Ensure this env var is set
    private static final String MODEL = "gemini-2.0-flash-preview-image-generation";
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL + ":generateContent?key=" + GEMINI_KEY;

    private final OkHttpClient http = new OkHttpClient();

    /**
     * Generates an image based on the given prompt using Gemini API.
     * Saves the image to a temporary file and returns the file URI.
     */
    public Map<String, Object> generateImage(String prompt) throws IOException {
        Map<String, Object> result = new HashMap<>();

        String fullPrompt = "Generate an image illustrating the concept of " + prompt +
                " in a modern flat-vector style conveying energy and community spirit — no text anywhere, and minimal text";

        JSONObject body = new JSONObject()
                .put("model", MODEL)
                .put("contents", new JSONArray().put(
                        new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject().put("text", fullPrompt))
                                )
                ))
                .put("generationConfig", new JSONObject()
                        .put("responseModalities", new JSONArray().put("TEXT").put("IMAGE"))
                );

        Request request = new Request.Builder()
                .url(ENDPOINT)
                .post(RequestBody.create(body.toString(),
                        MediaType.get("application/json")))
                .addHeader("Accept", "application/json")
                .build();

        try (Response resp = http.newCall(request).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) {
                result.put("error", "Gemini error " + resp.code() + " – no body");
                return result;
            }

            String responseBody = resp.body().string();
            JSONObject json = new JSONObject(responseBody);
            JSONArray candidates = json.optJSONArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                result.put("error", "No candidates returned");
                return result;
            }

            JSONArray parts = candidates
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts");

            JSONObject inlineData = null;
            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.getJSONObject(i);
                if (part.has("inlineData")) {
                    inlineData = part.getJSONObject("inlineData");
                    break;
                }
            }
            if (inlineData == null) {
                result.put("error", "No inlineData (image) found in parts");
                return result;
            }

            String b64 = inlineData.getString("data");
            byte[] imgBytes = Base64.getMimeDecoder().decode(b64);

            Path dir = Files.createTempDirectory("gemini_img_");
            Path target = dir.resolve("generated_" + UUID.randomUUID() + ".png");
            try (FileOutputStream out = new FileOutputStream(target.toFile())) {
                out.write(imgBytes);
            }

            result.put("image_uri", target.toUri().toString());
        }

        return result;
    }
}
