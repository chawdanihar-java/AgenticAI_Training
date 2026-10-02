package org.example.prac3.rag;


import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class RagService {

    public List<KnowledgeDocument> retrieve(String code) {

        return List.of(
                load("knowledge/java-guidelines.md"),
                load("knowledge/spring-guidelines.md"),
                load("knowledge/sql-guidelines.md")
        );
    }

    private KnowledgeDocument load(String path) {

        try {
            ClassPathResource resource = new ClassPathResource(path);

            String content = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            return new KnowledgeDocument(path, content);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to load knowledge file: " + path, e);
        }
    }
}