package org.example.prac3.runner;

import org.example.prac3.agent.CodeReviewAgent;
import org.example.prac3.model.CodeReviewResult;
import org.example.prac3.model.Finding;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CodeReviewRunner implements CommandLineRunner {

    private final CodeReviewAgent agent;

    public CodeReviewRunner(CodeReviewAgent agent) {
        this.agent = agent;
    }

    @Override
    public void run(String... args) {

        String code = """
    SELECT *
    FROM mapped_image
    WHERE folder_fk = :folderId And active = true
    ORDER BY md5(CONCAT(:seed, mapped_pk))
    """,
                countQuery = """
    SELECT COUNT(*)
    FROM mapped_image
    WHERE folder_fk = :folderId and active=true
    """;

        CodeReviewResult result = agent.execute(code);

        System.out.println();
        System.out.println("========================================");
        System.out.println("         CODE REVIEW RESULT");
        System.out.println("========================================");

        System.out.println("Status      : " + result.status());
        System.out.println("Message     : " + result.message());

        System.out.println();
        System.out.println("Original Code:");
        System.out.println("----------------------------------------");
        System.out.println(result.code());
        System.out.println("----------------------------------------");

        System.out.println();
        System.out.println("Findings:");
        System.out.println("----------------------------------------");

        for (Finding finding : result.findings()) {

            System.out.println("Type        : " + finding.type());
            System.out.println("Severity    : " + finding.severity());
            System.out.println("Location    : " + finding.location());

            System.out.println();
            System.out.println("Description : " + finding.description());
            System.out.println("Reason      : " + finding.reason());
            System.out.println("Suggested Fix:");
            System.out.println(finding.suggestedFix());

            System.out.println();
            System.out.println("Suggested Code:");
            System.out.println("----------------------------------------");
            System.out.println(finding.suggestedCode());
            System.out.println("----------------------------------------");

            System.out.println();
        }

        System.out.println("========================================");

    }
}