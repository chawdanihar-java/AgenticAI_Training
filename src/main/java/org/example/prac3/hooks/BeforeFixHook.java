package org.example.prac3.hooks;


import org.example.prac3.model.CodeReviewResult;
import org.example.prac3.model.ReviewStatus;
import org.springframework.stereotype.Component;

@Component
public class BeforeFixHook {

    public boolean execute(
            CodeReviewResult result,
            boolean humanApproved) {

        if (result == null) {
            return false;
        }

        if (!humanApproved) {
            return false;
        }

        return result.status() == ReviewStatus.APPROVED;
    }
}