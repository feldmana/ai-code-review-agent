package com.agentic.codereview.model;

import java.util.Objects;

/**
 * Represents an action to be executed in the plan
 */
public class Action {
    public enum ActionType {
        SCAN_FILES,
        REVIEW_FILES,
        SUMMARIZE,
        WRITE_REPORT,
        SEND_EMAIL
    }

    private ActionType action;
    private String description;
    private int sequenceNumber;

    public Action(ActionType action, String description, int sequenceNumber) {
        this.action = Objects.requireNonNull(action, "Action type cannot be null");
        this.description = description;
        this.sequenceNumber = sequenceNumber;
    }

    // Getters
    public ActionType getAction() { return action; }
    public String getDescription() { return description; }
    public int getSequenceNumber() { return sequenceNumber; }

    @Override
    public String toString() {
        return "Action{" +
                "action=" + action +
                ", sequence=" + sequenceNumber +
                ", description='" + description + '\'' +
                '}';
    }
}

