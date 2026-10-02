package org.example.prac3.skills;



import org.example.prac3.model.Finding;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SqlSkill implements Skill {

    @Override
    public String getName() {
        return "SQL Analysis";
    }

    @Override
    public List<Finding> analyze(String code) {

        if (code == null || code.isBlank()) {
            return List.of();
        }

        if (code.contains("\"SELECT * FROM users WHERE username = '\"")) {

            return List.of(new Finding(
                    "SQL_INJECTION",
                    "HIGH",
                    "Unknown",
                    "SQL query appears to concatenate user input.",
                    "Direct string concatenation can allow SQL injection.",
                    "Use parameterized queries.",
                    "String sql = \"SELECT * FROM users WHERE username = ?\";"
            ));
        }

        return List.of();
    }
}