package org.example.prac3.hooks;


import org.example.prac3.model.CodeReviewResult;

public interface Hook {

    String getName();

    boolean execute(CodeReviewResult result);
}