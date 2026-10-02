package org.example.prac3.hooks;


import org.springframework.stereotype.Component;

@Component
public class AfterFixHook {

    public boolean execute(String fixedCode) {

        return fixedCode != null
                && !fixedCode.isBlank();
    }
}