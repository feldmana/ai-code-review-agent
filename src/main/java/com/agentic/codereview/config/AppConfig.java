package com.agentic.codereview.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Application configuration loaded from properties file or environment variables
 */
public class AppConfig {
    // Ollama Configuration
    public static final String OLLAMA_HOST = "http://localhost";
    public static final int OLLAMA_PORT = 11434;
    public static final String OLLAMA_MODEL = "llama3"; // Can be overridden
    private static final String CONFIG_FILE = "codereview.properties";
    private static AppConfig instance;
    private final boolean enableParallelProcessing = true;
    // MCP Configuration
    private boolean mcpEnabled;
    private int mcpPort;
    private int mcpRequestTimeout;
    private int mcpConnectionPoolSize;
    // Email Configuration
    private boolean emailEnabled;
    private String emailTo;
    private String smtpHost;
    private int smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private boolean smtpTlsEnabled;
    // Review Configuration
    private int maxRetries = 3;
    private int threadPoolSize = 4;

    private AppConfig() {
        loadConfiguration();
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private void loadConfiguration() {
        Properties props = new Properties();

        // Try to load from file
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("Configuration file not found, using environment variables and defaults");
        }

        // Email Configuration
        this.emailEnabled = Boolean.parseBoolean(
                getProperty(props, "EMAIL_ENABLED", System.getenv("EMAIL_ENABLED"), "false")
        );
        this.emailTo = getProperty(props, "EMAIL_TO", System.getenv("EMAIL_TO"), "");
        this.smtpHost = getProperty(props, "SMTP_HOST", System.getenv("SMTP_HOST"), "smtp.gmail.com");
        this.smtpPort = Integer.parseInt(
                getProperty(props, "SMTP_PORT", System.getenv("SMTP_PORT"), "587")
        );
        this.smtpUsername = getProperty(props, "SMTP_USERNAME", System.getenv("SMTP_USERNAME"), "");
        this.smtpPassword = getProperty(props, "SMTP_PASSWORD", System.getenv("SMTP_PASSWORD"), "");
        this.smtpTlsEnabled = Boolean.parseBoolean(
                getProperty(props, "SMTP_TLS_ENABLED", System.getenv("SMTP_TLS_ENABLED"), "true")
        );

        // MCP Configuration
        this.mcpEnabled = Boolean.parseBoolean(
                getProperty(props, "MCP_ENABLED", System.getenv("MCP_ENABLED"), "false")
        );
        this.mcpPort = Integer.parseInt(
                getProperty(props, "MCP_PORT", System.getenv("MCP_PORT"), "9876")
        );
        this.mcpRequestTimeout = Integer.parseInt(
                getProperty(props, "MCP_REQUEST_TIMEOUT", System.getenv("MCP_REQUEST_TIMEOUT"), "30")
        );
        this.mcpConnectionPoolSize = Integer.parseInt(
                getProperty(props, "MCP_CONNECTION_POOL_SIZE", System.getenv("MCP_CONNECTION_POOL_SIZE"), "10")
        );

        // Review Configuration
        this.maxRetries = Integer.parseInt(
                getProperty(props, "MAX_RETRIES", System.getenv("MAX_RETRIES"), "3")
        );
        this.threadPoolSize = Integer.parseInt(
                getProperty(props, "THREAD_POOL_SIZE", System.getenv("THREAD_POOL_SIZE"), "4")
        );
    }

    private String getProperty(Properties props, String propName, String envValue, String defaultValue) {
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        return props.getProperty(propName, defaultValue);
    }

    // Getters
    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public boolean isSmtpTlsEnabled() {
        return smtpTlsEnabled;
    }

    // MCP Getters
    public boolean isMcpEnabled() {
        return mcpEnabled;
    }

    public int getMcpPort() {
        return mcpPort;
    }

    public int getMcpRequestTimeout() {
        return mcpRequestTimeout;
    }

    public int getMcpConnectionPoolSize() {
        return mcpConnectionPoolSize;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public boolean isParallelProcessingEnabled() {
        return enableParallelProcessing;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                "emailEnabled=" + emailEnabled +
                ", smtpHost='" + smtpHost + '\'' +
                ", smtpPort=" + smtpPort +
                ", mcpEnabled=" + mcpEnabled +
                ", mcpPort=" + mcpPort +
                ", maxRetries=" + maxRetries +
                ", threadPoolSize=" + threadPoolSize +
                '}';
    }
}

