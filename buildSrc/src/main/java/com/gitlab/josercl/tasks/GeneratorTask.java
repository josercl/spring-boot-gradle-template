package com.gitlab.josercl.tasks;

import com.gitlab.josercl.generator.IGenerator;
import com.gitlab.josercl.generator.application.ApplicationGenerator;
import com.gitlab.josercl.generator.domain.DomainGenerator;
import com.gitlab.josercl.generator.infrastructure.InfraGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

abstract class GeneratorTask extends DefaultTask {
    public GeneratorTask() {
    }

    @TaskAction
    public void generate() throws IOException {
        Object entities = getProject().getProperties().getOrDefault("entities", null);

        if (entities == null) return;

        Object only = getProject().getProperties().getOrDefault("only", null);

        List<IGenerator> generatorsToUse = new ArrayList<>();

        if (only == null) {
            generatorsToUse.add(new DomainGenerator());
            generatorsToUse.add(new InfraGenerator());
            generatorsToUse.add(new ApplicationGenerator());
        } else {
            String[] onlies = ((String) only).split(",");

            for (String s : onlies) {
                switch (s) {
                    case "domain" -> generatorsToUse.add(new DomainGenerator());
                    case "infra" -> generatorsToUse.add(new InfraGenerator());
                    case "application" -> generatorsToUse.add(new ApplicationGenerator());
                }
            }
        }

        for (String entity : ((String) entities).split(",")) {
            for (IGenerator iGenerator : generatorsToUse) {
                iGenerator.generate(entity);
            }
        }
    }
}