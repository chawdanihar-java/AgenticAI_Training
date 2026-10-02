package org.example.prac3.service;


import org.example.prac3.model.Finding;
import org.springframework.stereotype.Service;

@Service
public class CodeFixService {

    public String applyFix(String code, Finding finding) {

        if (finding == null || finding.suggestedCode() == null) {
            return code;
        }

        System.out.println("Applying approved fix...");

        // Simple demo implementation.
        // Later this will be handled by the AI model.
        return finding.suggestedCode();
    }
}