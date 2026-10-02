package org.example.prac3.service;


import org.example.prac3.model.CodeReviewResult;
import org.example.prac3.model.Finding;
import org.example.prac3.model.ReviewStatus;
import org.example.prac3.skills.Skill;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CodeReviewService {

    private final List<Skill> skills;

    public CodeReviewService(List<Skill> skills) {
        this.skills = skills;
    }

    public CodeReviewResult review(String code) {

        List<Finding> findings = new ArrayList<>();

        for (Skill skill : skills) {
            findings.addAll(skill.analyze(code));
        }

        return new CodeReviewResult(
                findings.isEmpty()
                        ? ReviewStatus.COMPLETED
                        : ReviewStatus.WAITING_FOR_APPROVAL,
                findings.isEmpty()
                        ? "No issues found."
                        : "Code review completed. Human approval required.",
                code,
                findings
        );
    }
}