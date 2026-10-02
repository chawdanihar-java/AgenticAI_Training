package org.example.prac3.agent;


import org.example.prac3.model.CodeReviewResult;
import org.springframework.stereotype.Component;

@Component
public class AgentLoop {

    private final CodeReviewAgent agent;

    public AgentLoop(CodeReviewAgent agent) {
        this.agent = agent;
    }

    public CodeReviewResult process(String userInput, String code) {

        if ("bye".equalsIgnoreCase(userInput.trim())) {
            return CodeReviewResult.stopped();
        }

        return agent.execute(code);
    }
}