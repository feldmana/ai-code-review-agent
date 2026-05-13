package com.agentic.codereview.llm;

import com.google.gson.Gson;
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
public class OllamaClientFirst {
    private static final Logger logger = LoggerFactory.getLogger(OllamaClient.class);
    private static final Gson gson = new Gson();

    private final String baseUrl;
    private final String model;
    private final OkHttpClient httpClient;

    public OllamaClientFirst(String host, int port, String model) {
        //this.baseUrl = host + ":" + port;
        this.baseUrl = "http://127.0.0.1:11434";
        this.model = model;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Sends a prompt to Ollama and gets a response
     */
    public String generateResponse(String prompt) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("prompt", prompt);
        requestBody.addProperty("stream", false);

        Request request = new Request.Builder()
                .url(baseUrl + "/api/generate")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Ollama API error: {}", response.code());
                throw new IOException("Failed to get response from Ollama: " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response from Ollama");
            }

            String responseText = body.string();
            logger.debug("Ollama raw response: {}", responseText);

            try {
                JsonObject jsonResponse = JsonParser.parseString(responseText).getAsJsonObject();
                return jsonResponse.get("response").getAsString();
            } catch (Exception e) {
                logger.error("Failed to parse Ollama response: {}", responseText, e);
                return responseText;
            }
        }
    }

    /**
     * Generates a JSON response from Ollama (for structured outputs)
     */
    public JsonObject generateJsonResponse(String prompt) throws IOException {
        String response = generateResponse(prompt);

        try {
            // Try to extract JSON from the response
            int jsonStart = response.indexOf('{');
            int jsonEnd = response.lastIndexOf('}');

            if (jsonStart != -1 && jsonEnd != -1) {
                String jsonString = response.substring(jsonStart, jsonEnd + 1);
                return JsonParser.parseString(jsonString).getAsJsonObject();
            }
        } catch (Exception e) {
            logger.warn("Failed to parse JSON from response: {}", response, e);
        }

        // Return response wrapped in JSON if parsing fails
        JsonObject fallback = new JsonObject();
        fallback.addProperty("response", response);
        fallback.addProperty("error", "Could not parse as JSON");
        return fallback;
    }

    /**
     * Tests the connection to Ollama
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

    /**
     * Lists available models from Ollama
     */
    public JsonArray listModels() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/tags")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to list models from Ollama");
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response from Ollama");
            }

            JsonObject jsonResponse = JsonParser.parseString(body.string()).getAsJsonObject();
            return jsonResponse.getAsJsonArray("models");
        }
    }

    public String getModel() {
        return model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}

