package com.esprit.Services;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service to generate text content (title, summary, content) from an image using Gemini API.
 */
public class GeminiImageToTextService {

    private static final String GEMINI_KEY = com.esprit.config.Secrets.get("GEMINI_API_KEY");
    private static final String MODEL = "gemini-1.5-flash"; // Using the recommended model as per the error message
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL + ":generateContent?key=" + GEMINI_KEY;
    
    // Configure OkHttpClient with longer timeouts
    private final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * Generates text fields (title, summary, content) from the given image data (base64 encoded).
     * @param base64Image Base64 encoded image string
     * @return Map with keys "title", "summary", "content" and their generated text values
     * @throws IOException on network or API errors
     */
    public Map<String, String> generateTextFromImage(String base64Image) throws IOException {
        return generateTextFromImage(base64Image, true);
    }
    
    /**
     * Generates text fields (title, summary, content) from the given image data (base64 encoded).
     * @param base64Image Base64 encoded image string
     * @param resizeIfNeeded Whether to resize the image if it's too large
     * @return Map with keys "title", "summary", "content" and their generated text values
     * @throws IOException on network or API errors
     */
    public Map<String, String> generateTextFromImage(String base64Image, boolean resizeIfNeeded) throws IOException {
        Map<String, String> result = new HashMap<>();

        // Make sure the base64 image doesn't contain the data URL prefix
        if (base64Image.startsWith("data:image")) {
            base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
        }
        
        // Log the first few characters of the base64 string for debugging
        System.out.println("Base64 image data (first 20 chars): " + 
                          (base64Image.length() > 20 ? base64Image.substring(0, 20) + "..." : base64Image));
        System.out.println("Original base64 image data length: " + base64Image.length());
        
        // If the image is too large, resize it
        if (resizeIfNeeded && base64Image.length() > 200000) { // 200KB threshold
            try {
                base64Image = resizeBase64Image(base64Image);
                System.out.println("Resized base64 image data length: " + base64Image.length());
            } catch (Exception e) {
                System.err.println("Failed to resize image: " + e.getMessage());
                // Continue with the original image
            }
        }
        
        // Construct the request body according to Gemini API specifications
        JSONObject body = new JSONObject()
                .put("contents", new JSONArray().put(
                        new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject()
                                            .put("inlineData", new JSONObject()
                                                .put("mimeType", "image/jpeg")
                                                .put("data", base64Image)))
                                        .put(new JSONObject()
                                            .put("text", "Generate a title, summary, and content for a blog post based on this image. Format your response as: Title\nSummary\nContent"))
                                )
                ))
                .put("generationConfig", new JSONObject()
                        .put("temperature", 0.4)
                        .put("topK", 32)
                        .put("topP", 1.0)
                        .put("maxOutputTokens", 800))
                .put("safetySettings", new JSONArray()
                        .put(new JSONObject()
                            .put("category", "HARM_CATEGORY_DANGEROUS_CONTENT")
                            .put("threshold", "BLOCK_NONE"))
                );

        System.out.println("Sending request to Gemini API...");
        Request request = new Request.Builder()
                .url(ENDPOINT)
                .post(RequestBody.create(body.toString(),
                        MediaType.get("application/json")))
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response resp = http.newCall(request).execute()) {
            if (!resp.isSuccessful()) {
                String errorBody = resp.body() != null ? resp.body().string() : "No response body";
                System.err.println("Gemini API error: " + resp.code() + " - " + errorBody);
                throw new IOException("Gemini API error " + resp.code() + " - " + errorBody);
            }
            
            if (resp.body() == null) {
                throw new IOException("Gemini API returned empty response body");
            }

            String responseBody = resp.body().string();
            System.out.println("Received response from Gemini API: " + 
                             (responseBody.length() > 100 ? responseBody.substring(0, 100) + "..." : responseBody));
            
            JSONObject json = new JSONObject(responseBody);
            JSONArray candidates = json.optJSONArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new IOException("No candidates returned from Gemini API: " + responseBody);
            }

            // Extract the text response from the API result
            String textResponse;
            try {
                textResponse = candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            } catch (Exception e) {
                System.err.println("Error parsing Gemini API response: " + e.getMessage());
                System.err.println("Response body: " + responseBody);
                throw new IOException("Failed to parse Gemini API response: " + e.getMessage());
            }

            // Parse the response into title, summary, and content
            System.out.println("Full text response from Gemini API: " + textResponse);
            
            // Try to identify title, summary, and content based on common patterns
            String title = "";
            String summary = "";
            String content = "";
            
            // Split by newlines
            String[] lines = textResponse.split("\\n");
            
            // Check for explicit labels with various formats like "**Title:**" or "Title:" 
            boolean foundTitle = false;
            boolean foundSummary = false;
            boolean foundContent = false;
            boolean inContent = false;
            
            for (String line : lines) {
                String trimmedLine = line.trim();
                
                // Skip empty lines
                if (trimmedLine.isEmpty()) continue;
                
                // Check for labeled sections with various formats
                if (trimmedLine.matches("(?i)\\*\\*Title:\\*\\*.*")) {
                    // Format: **Title:** Some title
                    title = trimmedLine.replaceAll("(?i)\\*\\*Title:\\*\\*", "").trim();
                    foundTitle = true;
                } else if (trimmedLine.matches("(?i)\\*\\*Summary:\\*\\*.*")) {
                    // Format: **Summary:** Some summary
                    summary = trimmedLine.replaceAll("(?i)\\*\\*Summary:\\*\\*", "").trim();
                    foundSummary = true;
                    inContent = false;
                } else if (trimmedLine.matches("(?i)\\*\\*Content:\\*\\*.*")) {
                    // Format: **Content:** Some content
                    String contentStart = trimmedLine.replaceAll("(?i)\\*\\*Content:\\*\\*", "").trim();
                    if (!contentStart.isEmpty()) {
                        content += contentStart + "\n";
                    }
                    foundContent = true;
                    inContent = true;
                } else if (trimmedLine.toLowerCase().startsWith("title:")) {
                    // Format: Title: Some title
                    title = trimmedLine.substring("title:".length()).trim();
                    foundTitle = true;
                } else if (trimmedLine.toLowerCase().startsWith("summary:")) {
                    // Format: Summary: Some summary
                    summary = trimmedLine.substring("summary:".length()).trim();
                    foundSummary = true;
                    inContent = false;
                } else if (trimmedLine.toLowerCase().startsWith("content:")) {
                    // Format: Content: Some content
                    String contentStart = trimmedLine.substring("content:".length()).trim();
                    if (!contentStart.isEmpty()) {
                        content += contentStart + "\n";
                    }
                    foundContent = true;
                    inContent = true;
                } else if (foundContent || inContent) {
                    // Add to content if we're in the content section
                    content += trimmedLine + "\n";
                }
            }
            
            // If we didn't find labeled sections, use a simpler approach
            if (!foundTitle && !foundSummary && !foundContent) {
                if (lines.length >= 3) {
                    // Assume first line is title, second is summary, rest is content
                    title = lines[0].trim();
                    summary = lines[1].trim();
                    StringBuilder contentBuilder = new StringBuilder();
                    for (int i = 2; i < lines.length; i++) {
                        contentBuilder.append(lines[i].trim()).append("\n");
                    }
                    content = contentBuilder.toString().trim();
                } else if (lines.length == 2) {
                    // Just title and summary
                    title = lines[0].trim();
                    summary = lines[1].trim();
                } else if (lines.length == 1) {
                    // Just title
                    title = lines[0].trim();
                    summary = "Generated from image";
                }
            }
            
            // Store the extracted parts
            result.put("title", title.isEmpty() ? "Generated Title" : title);
            result.put("summary", summary.isEmpty() ? "Generated Summary" : summary);
            result.put("content", content.isEmpty() ? textResponse.trim() : content.trim());
            
            System.out.println("Extracted title: " + result.get("title"));
            System.out.println("Extracted summary: " + result.get("summary"));
            System.out.println("Extracted content length: " + result.get("content").length() + " characters");
            
            System.out.println("Successfully parsed Gemini API response");
        } catch (Exception e) {
            System.err.println("Exception in Gemini API call: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error processing Gemini API request: " + e.getMessage(), e);
        }

        return result;
    }
    
    /**
     * Resize a base64 encoded image to reduce its size
     * @param base64Image The base64 encoded image string
     * @return A resized base64 encoded image string
     * @throws IOException If the image cannot be processed
     */
    private String resizeBase64Image(String base64Image) throws IOException {
        // Decode base64 to byte array
        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
        
        // Read the image
        BufferedImage originalImage = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));
        if (originalImage == null) {
            throw new IOException("Failed to read image data");
        }
        
        // Calculate new dimensions (max 800px width or height while maintaining aspect ratio)
        int maxDimension = 800;
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        int newWidth, newHeight;
        if (originalWidth > originalHeight) {
            newWidth = maxDimension;
            newHeight = (int) (originalHeight * ((double) maxDimension / originalWidth));
        } else {
            newHeight = maxDimension;
            newWidth = (int) (originalWidth * ((double) maxDimension / originalHeight));
        }
        
        // Create a new resized image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        // Convert back to base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", outputStream);
        byte[] resizedImageBytes = outputStream.toByteArray();
        
        return java.util.Base64.getEncoder().encodeToString(resizedImageBytes);
    }
}
