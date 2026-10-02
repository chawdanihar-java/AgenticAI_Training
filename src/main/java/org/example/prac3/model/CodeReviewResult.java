package org.example.prac3.model;

import java.util.List;

public record CodeReviewResult(
        ReviewStatus status,
        String message,
        String code,
        List<Finding> findings) {

    public static CodeReviewResult stopped() {
        return new CodeReviewResult(
                ReviewStatus.STOPPED,
                "Agent stopped by developer.",
                null,
                List.of());
    }
}