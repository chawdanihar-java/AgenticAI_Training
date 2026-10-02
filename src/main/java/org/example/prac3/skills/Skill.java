package org.example.prac3.skills;


import org.example.prac3.model.Finding;

import java.util.List;

public interface Skill {

    String getName();

    List<Finding> analyze(String code);
}