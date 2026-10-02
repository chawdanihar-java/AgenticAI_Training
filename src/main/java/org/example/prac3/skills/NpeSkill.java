package org.example.prac3.skills;


import org.example.prac3.model.Finding;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NpeSkill implements Skill {

    @Override
    public String getName() {
        return "NPE Analysis";
    }

    @Override
    public List<Finding> analyze(String code) {

        if (code == null || code.isBlank()) {
            return List.of();
        }

        if (code.contains(".getProfile().getName()")) {

            return List.of(new Finding(
                    "NPE_RISK",
                    "HIGH",
                    "Unknown",
                    "Potential NullPointerException detected.",
                    "Nested method calls may execute on a null object.",
                    "Validate the object and profile before accessing the name.",
                    "if (user != null && user.getProfile() != null) {\n"
                            + "    return user.getProfile().getName();\n"
                            + "}"
            ));
        }

        return List.of();
    }
}