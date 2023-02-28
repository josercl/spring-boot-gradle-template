package com.gitlab.josercl.tasks;

import com.gitlab.josercl.generator.Generator;
import com.gitlab.josercl.generator.application.ApplicationGenerator;
import com.gitlab.josercl.generator.domain.DomainGenerator;
import com.gitlab.josercl.generator.infrastructure.InfraGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

abstract class GeneratorTask extends DefaultTask {
    @Input
    @Optional
    abstract Property<String> getEntityName();

    private final Generator generator;

    public GeneratorTask() {
        this.generator = new Generator(
            new InfraGenerator(),
            new DomainGenerator(),
            new ApplicationGenerator()
        );
    }

    @TaskAction
    public void generate() throws IOException {
        Object entities = getProject().getProperties().getOrDefault("entities", null);

        if (entities == null) return;

        for (String entity : ((String) entities).split(",")) {
            generator.generate(entity);
        }
    }
}