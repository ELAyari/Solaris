package com.esprit.Services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;



public class N8nWebhookService {

    private final String webhookUrl;

    public N8nWebhookService(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    /**
     * Sends a message payload to the n8n webhook to trigger WhatsApp chatbot message sending.
     * @param toNumber Recipient phone number in international format, e.g. +21624031727
     * @param messageBody Message content
     * @return true if sent successfully, false otherwise
     */
    public boolean sendMessageToN8n(String toNumber, String messageBody) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonPayload = String.format(
                "{\"to\": \"%s\", \"message\": \"%s\"}",
                toNumber, escapeJson(messageBody)
            );

            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            OutputStream os = conn.getOutputStream();
            os.write(input, 0, input.length);
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();

            BufferedReader br;
            if (responseCode >= 200 && responseCode < 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            System.out.println("N8n webhook response code: " + responseCode);
            System.out.println("N8n webhook response body: " + response.toString());

            return responseCode == 200 || responseCode == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean sendMessageWithPdf(String toNumber, String messageBody, String pdfUrl, String fileName) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
    
            String finalFileName = (fileName != null && !fileName.isEmpty()) ? fileName : "document.pdf";
    
            String jsonPayload = String.format(
                "{ \"to\": \"%s\", \"message\": \"%s\", \"pdfUrl\": \"%s\", \"fileName\": \"%s\" }",
                toNumber,
                escapeJson(messageBody),
                escapeJson(pdfUrl),
                escapeJson(finalFileName)
            );
    
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(input);
                os.flush();
            }
    
            int responseCode = conn.getResponseCode();
            BufferedReader br = (responseCode >= 200 && responseCode < 300)
                    ? new BufferedReader(new InputStreamReader(conn.getInputStream()))
                    : new BufferedReader(new InputStreamReader(conn.getErrorStream()));
    
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
    
            System.out.println("N8n webhook response code: " + responseCode);
            System.out.println("N8n webhook response body: " + response.toString());
    
            return responseCode == 200 || responseCode == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendMessageWithBase64Pdf(String toNumber, String messageBody, String base64PdfContent, String fileName) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String finalFileName = (fileName != null && !fileName.isEmpty()) ? fileName : "document.pdf";

            String jsonPayload = String.format(
                "{ \"to\": \"%s\", \"message\": \"%s\", \"base64Pdf\": \"%s\", \"fileName\": \"%s\" }",
                toNumber,
                escapeJson(messageBody),
                escapeJson(base64PdfContent),
                escapeJson(finalFileName)
            );

            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(input);
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            BufferedReader br = (responseCode >= 200 && responseCode < 300)
                    ? new BufferedReader(new InputStreamReader(conn.getInputStream()))
                    : new BufferedReader(new InputStreamReader(conn.getErrorStream()));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            System.out.println("N8n webhook response code: " + responseCode);
            System.out.println("N8n webhook response body: " + response.toString());

            return responseCode == 200 || responseCode == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
