package com.agentic.codereview.agent;

import com.agentic.codereview.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RouterAgent classifies user requests and determines task type
 */
public class RouterAgent {
    private static final Logger logger = LoggerFactory.getLogger(RouterAgent.class);

    /**
     * Routes a task to the appropriate handler based on its type
     */
    public Task.TaskType routeTask(String userInput, String projectPath) {
        logger.info("Routing task: {}", userInput);

        String lowerInput = userInput.toLowerCase();

        if (lowerInput.contains("review") || lowerInput.contains("code") || lowerInput.contains("analyze")) {
            logger.debug("Routed to: REVIEW_CODE");
            return Task.TaskType.REVIEW_CODE;
        } else if (lowerInput.contains("summary") || lowerInput.contains("summarize")) {
            logger.debug("Routed to: SUMMARIZE");
            return Task.TaskType.SUMMARIZE;
        } else if (lowerInput.contains("email") || lowerInput.contains("send")) {
            logger.debug("Routed to: SEND_EMAIL");
            return Task.TaskType.SEND_EMAIL;
        }

        // Default to code review
        logger.debug("Defaulting to: REVIEW_CODE");
        return Task.TaskType.REVIEW_CODE;
    }

    /**
     * Validates if a task is executable
     */
    public boolean validateTask(Task task) {
        if (task.getProjectPath() == null || task.getProjectPath().isEmpty()) {
            logger.warn("Task validation failed: empty project path");
            return false;
        }

        logger.info("Task validation passed: {}", task);
        return true;
    }

    /**
     * Determines priority of a task
     */
    public int calculatePriority(Task task) {
        // Higher number = higher priority
        return switch (task.getType()) {
            case REVIEW_CODE -> 3;
            case SUMMARIZE -> 2;
            case SEND_EMAIL -> 1;
        };
    }
}

