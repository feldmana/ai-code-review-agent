package com.agentic.codereview.mcp;

import java.net.*;
import java.io.*;

public class SimpleOllamaRouter {

    public String chooseTool(String userInput) throws Exception {

        String prompt =
                "You are a reasoning tool router.\n" +
                "Choose ONE tool only:\n" +
                "- review_code\n" +
                "- analyze_code_type\n" +
                "- get_rules\n" +
                "- both\n\n" +
                "User request:\n" + userInput + "\n\n" +
                "Return ONLY tool name.";

        URL url = new URL("http://localhost:11434/api/generate");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String body = """
        {
          "model": "llama3",
          "prompt": "%s",
          "stream": false
        }
        """.formatted(prompt.replace("\"", "\\\""));

        try (OutputStreamWriter w = new OutputStreamWriter(conn.getOutputStream())) {
            w.write(body);
        }

        BufferedReader r = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            sb.append(line);
        }

        String response = sb.toString();

        // extract raw response safely
        if (response.contains("review_code")) return "review_code";
        if (response.contains("analyze_code_type")) return "analyze_code_type";
        if (response.contains("get_rules")) return "get_rules";
        if (response.contains("both")) return "both";

        return "review_code"; // fallback
    }
}