package org.example.prac3.skills;



import org.example.prac3.model.Finding;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PerformanceSkill implements Skill {

    @Override
    public String getName() {
        return "Performance Analysis";
    }

    @Override
    public List<Finding> analyze(String code) {

        if (code == null || code.isBlank()) {
            return List.of();
        }

        if (code.contains("for (") &&
                code.contains("repository.findById")) {

            return List.of(new Finding(
                    "PERFORMANCE",
                    "MEDIUM",
                    "Unknown",
                    "Database call detected inside a loop.",
                    "Repeated database calls can significantly increase execution time.",
                    "Use a batch query instead of querying once per item.",
                    "repository.findAllById(ids);"
            ));
        }

        return List.of();
    }
}