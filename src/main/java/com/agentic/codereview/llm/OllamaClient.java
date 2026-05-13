package com.agentic.codereview.llm;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP Client for communicating with Ollama local LLM API
 */
public class OllamaClient {

    private static final Logger logger = LoggerFactory.getLogger(OllamaClient.class);

    private final String baseUrl;
    private final String model;
    private final OkHttpClient httpClient;

    public OllamaClient(String host, int port, String model) {
        //this.baseUrl = host + ":" + port;
        this.baseUrl = "http://127.0.0.1:11434";

        this.model = model;

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    /**
     * MAIN FIX: uses /api/chat instead of /api/generate
     */
    public String generateResponse(String prompt) throws IOException {

        // 🔴 FIX 1: force IPv4 (prevents localhost -> IPv6 weird routing issues)
        String url = "http://127.0.0.1:11434/api/chat";

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);

        // Optional: keep false explicitly (safe for debugging)
        requestBody.addProperty("stream", false);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);

        requestBody.add("messages", messages);

        logger.debug("Calling Ollama URL: {}, model: {}", url, model);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.parse("application/json")
                ))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

            String responseText = response.body() != null ? response.body().string() : "";

            // 🔴 FIX 3: log full failure details (important for Ollama debugging)
            if (!response.isSuccessful()) {
                logger.error("Ollama API error: {} body: {}", response.code(), responseText);
                throw new IOException("Failed to get response from Ollama: " + response.code());
            }

            logger.debug("Ollama raw response: {}", responseText);

            String cleaned = extractJson(responseText);
            JsonObject json = JsonParser.parseString(cleaned).getAsJsonObject();
            // 🔴 FIX 4: safer parsing (avoid NullPointer crashes)
            if (json.has("message") && json.getAsJsonObject("message").has("content")) {
                return json.getAsJsonObject("message")
                        .get("content")
                        .getAsString();
            }

            throw new IOException("Invalid Ollama response format: " + responseText);
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start == -1 || end == -1 || end <= start) {
            throw new RuntimeException("No JSON found in response: " + text);
        }

        return text.substring(start, end + 1);
    }

    /**
     * Test connection
     */
    public boolean testConnection() {
        try {
            Request request = new Request.Builder()
                    .url(baseUrl + "/api/tags")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            logger.error("Failed to connect to Ollama at {}", baseUrl, e);
            return false;
        }
    }
}