package org.example.prac3.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "ai.mode",
        havingValue = "MOCK",
        matchIfMissing = true
)
public class MockClaudeService implements ClaudeService {

    @Override
    public String analyze(String prompt) {

        return """
                {
                  "findings": [
                    {
                      "type": "NPE_RISK",
                      "severity": "HIGH",
                      "location": "UserService.getUserName()",
                      "description": "Potential NullPointerException detected.",
                      "reason": "user.getProfile() may return null.",
                      "suggestedFix": "Validate user and profile before accessing name."
                    }
                  ]
                }
                """;
    }
}