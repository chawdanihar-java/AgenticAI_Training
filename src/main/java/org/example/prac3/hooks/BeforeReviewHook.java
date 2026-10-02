package org.example.prac3.hooks;


import org.example.prac3.model.CodeReviewResult;
import org.springframework.stereotype.Component;

@Component
public class BeforeReviewHook implements Hook {

    @Override
    public String getName() {
        return "Before Review";
    }

    @Override
    public boolean execute(CodeReviewResult result) {

        return result != null
                && result.code() != null
                && !result.code().isBlank();
    }
}