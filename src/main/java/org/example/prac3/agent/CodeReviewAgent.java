package org.example.prac3.agent;


import org.example.prac3.approval.HumanApprovalService;
import org.example.prac3.hooks.AfterFixHook;
import org.example.prac3.hooks.BeforeFixHook;
import org.example.prac3.hooks.BeforeReviewHook;
import org.example.prac3.model.CodeReviewResult;
import org.example.prac3.model.Finding;
import org.example.prac3.model.ReviewStatus;
import org.example.prac3.rag.RagService;
import org.example.prac3.service.CodeFixService;
import org.example.prac3.service.CodeReviewService;
import org.springframework.stereotype.Service;

@Service
public class CodeReviewAgent {

    private final CodeReviewService reviewService;
    private final CodeFixService fixService;
    private final RagService ragService;
    private final HumanApprovalService approvalService;

    private final BeforeReviewHook beforeReviewHook;
    private final BeforeFixHook beforeFixHook;
    private final AfterFixHook afterFixHook;

    public CodeReviewAgent(
            CodeReviewService reviewService,
            CodeFixService fixService,
            RagService ragService,
            HumanApprovalService approvalService,
            BeforeReviewHook beforeReviewHook,
            BeforeFixHook beforeFixHook,
            AfterFixHook afterFixHook) {

        this.reviewService = reviewService;
        this.fixService = fixService;
        this.ragService = ragService;
        this.approvalService = approvalService;
        this.beforeReviewHook = beforeReviewHook;
        this.beforeFixHook = beforeFixHook;
        this.afterFixHook = afterFixHook;
    }

    public CodeReviewResult execute(String code) {

        CodeReviewResult initialResult =
                new CodeReviewResult(
                        ReviewStatus.ANALYZING,
                        "Starting code review",
                        code,
                        java.util.List.of()
                );

        // 1. Before Review Hook
        if (!beforeReviewHook.execute(initialResult)) {
            return new CodeReviewResult(
                    ReviewStatus.FAILED,
                    "Before review hook blocked the request.",
                    code,
                    java.util.List.of()
            );
        }

        // 2. RAG
        ragService.retrieve(code);

        // 3. Skills
        CodeReviewResult reviewResult =
                reviewService.review(code);

        if (reviewResult.findings().isEmpty()) {
            return reviewResult;
        }

        // 4. Human Review
        Finding finding = reviewResult.findings().get(0);

        boolean approved = approvalService.requestApproval(
                finding.description()
                        + "\n\nSuggested Fix:\n"
                        + finding.suggestedFix()
        );

        if (!approved) {
            return new CodeReviewResult(
                    ReviewStatus.REJECTED,
                    "Human rejected the suggested fix.",
                    code,
                    reviewResult.findings()
            );
        }

        // 5. Before Fix Hook
        CodeReviewResult approvedResult =
                new CodeReviewResult(
                        ReviewStatus.APPROVED,
                        "Fix approved.",
                        code,
                        reviewResult.findings()
                );

        if (!beforeFixHook.execute(approvedResult, true)) {
            return new CodeReviewResult(
                    ReviewStatus.FAILED,
                    "Before fix hook blocked the fix.",
                    code,
                    reviewResult.findings()
            );
        }

        // 6. Apply Fix
        String fixedCode =
                fixService.applyFix(code, finding);

        System.out.println("\n===== CODE CHANGE =====");
        System.out.println("Original Code:");
        System.out.println(code);

        System.out.println("\nFixed Code:");
        System.out.println(fixedCode);

        // 7. After Fix Hook
        if (!afterFixHook.execute(fixedCode)) {
            return new CodeReviewResult(
                    ReviewStatus.FAILED,
                    "After fix validation failed.",
                    fixedCode,
                    reviewResult.findings()
            );
        }

        return new CodeReviewResult(
                ReviewStatus.COMPLETED,
                "Fix applied successfully.",
                fixedCode,
                reviewResult.findings()
        );
    }
}