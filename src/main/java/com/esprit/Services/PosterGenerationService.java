package com.esprit.Services;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

public class PosterGenerationService {

    // 🔑 your Google AI Studio API key
    private static final String GEMINI_KEY = com.esprit.config.Secrets.get("GEMINI_API_KEY");
    private static final String MODEL      = "gemini-2.0-flash-preview-image-generation";
    private static final String ENDPOINT   =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL + ":generateContent?key=" + GEMINI_KEY;

    private final OkHttpClient http = new OkHttpClient();

    /**
     * Génère un poster PNG à partir du prompt, le sauve dans un fichier temporaire
     * et retourne son URI locale.
     */
    public String generateFromPrompt(String prompt, int eventId) throws Exception {
        String fullPrompt = "Generate a poster illustrating the concept of " + prompt +"in a modern flat-vector style" +
                "conveying energy and community spirit — no text anywhere , and minimal text";

        // 1) Build the JSON request body exactly as your spec
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

        // 2) Send the POST
        Request request = new Request.Builder()
                .url(ENDPOINT)
                .post(RequestBody.create(body.toString(),
                        MediaType.get("application/json")))
                .addHeader("Accept", "application/json")
                .build();

        try (Response resp = http.newCall(request).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) {
                throw new RuntimeException("Gemini error " + resp.code()
                        + " – " + (resp.body() != null ? resp.body().string() : "no body"));
            }

            // 3) Parse the response
            JSONObject json     = new JSONObject(resp.body().string());
            JSONArray candidates = json.getJSONArray("candidates");
            if (candidates.isEmpty()) {
                throw new RuntimeException("No candidates returned");
            }

            // 4) Locate the first part object containing inlineData
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
                throw new RuntimeException("No inlineData (image) found in parts");
            }

            // 5) Decode Base64 payload
            String b64       = inlineData.getString("data");
            byte[] imgBytes  = Base64.getMimeDecoder().decode(b64);

            // 6) Save to a temp PNG
            Path dir    = Files.createTempDirectory("poster_evt" + eventId + "_");
            Path target = dir.resolve("poster_" + UUID.randomUUID() + ".png");
            try (FileOutputStream out = new FileOutputStream(target.toFile())) {
                out.write(imgBytes);
            }

            // 7) Return its file:// URI
            return target.toUri().toString();
        }
    }
}
