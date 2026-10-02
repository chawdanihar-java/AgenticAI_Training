package org.example.prac3.model;

public record Finding(
        String type,
        String severity,
        String location,
        String description,
        String reason,
        String suggestedFix,
        String suggestedCode) {
}