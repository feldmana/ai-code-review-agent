package com.agentic.codereview.agent;

import com.agentic.codereview.model.Action;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * PlannerAgent breaks down tasks into actionable steps
 */
public class PlannerAgent {
    private static final Logger logger = LoggerFactory.getLogger(PlannerAgent.class);
    private static final Gson gson = new Gson();

    /**
     * Creates an execution plan for a given task
     */
    public List<Action> createPlan(String taskDescription) {
        logger.info("Creating plan for task: {}", taskDescription);

        List<Action> actions = new ArrayList<>();

        String lower = taskDescription.toLowerCase();

        // Standard code review flow
        if (lower.contains("review") || lower.contains("code")) {
            actions.add(new Action(Action.ActionType.SCAN_FILES, "Scan project directory for source files", 1));
            actions.add(new Action(Action.ActionType.REVIEW_FILES, "Send files to LLM for review", 2));
            actions.add(new Action(Action.ActionType.SUMMARIZE, "Aggregate all reviews into summary", 3));
            actions.add(new Action(Action.ActionType.WRITE_REPORT, "Generate markdown report", 4));

            if (lower.contains("email")) {
                actions.add(new Action(Action.ActionType.SEND_EMAIL, "Send report via email", 5));
            }
        }

        // Email only flow
        if (lower.contains("email") && !lower.contains("review")) {
            actions.add(new Action(Action.ActionType.SEND_EMAIL, "Send report via email", 1));
        }

        logger.info("Created plan with {} actions", actions.size());
        for (Action action : actions) {
            logger.debug("Action {}: {}", action.getSequenceNumber(), action.getAction());
        }

        return actions;
    }

    /**
     * Generates JSON representation of the plan
     */
    public String planToJson(List<Action> actions) {
        JsonArray actionsArray = new JsonArray();

        for (Action action : actions) {
            JsonObject actionObj = new JsonObject();
            actionObj.addProperty("sequence", action.getSequenceNumber());
            actionObj.addProperty("action", action.getAction().toString());
            actionObj.addProperty("description", action.getDescription());
            actionsArray.add(actionObj);
        }

        return gson.toJson(actionsArray);
    }

    /**
     * Validates the feasibility of a plan
     */
    public boolean validatePlan(List<Action> actions) {
        if (actions.isEmpty()) {
            logger.warn("Plan is empty");
            return false;
        }

        // Check that actions are sequentially numbered
        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i).getSequenceNumber() != i + 1) {
                logger.warn("Invalid action sequence at position {}", i);
                return false;
            }
        }

        logger.info("Plan validation successful");
        return true;
    }
}

