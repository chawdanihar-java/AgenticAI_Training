package org.example.prac3.controller;


import org.example.prac3.agent.AgentLoop;
import org.example.prac3.model.CodeReviewRequest;
import org.example.prac3.model.CodeReviewResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/code-review")
public class CodeReviewController {

    private final AgentLoop agentLoop;

    public CodeReviewController(AgentLoop agentLoop) {
        this.agentLoop = agentLoop;
    }

    @PostMapping
    public CodeReviewResult review(
            @RequestBody CodeReviewRequest request) {

        return agentLoop.process(
                request.userInput(),
                request.code()
        );
    }
}