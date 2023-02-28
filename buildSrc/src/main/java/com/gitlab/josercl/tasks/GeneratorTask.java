package com.gitlab.josercl.tasks;

import com.gitlab.josercl.generator.DomainGenerator;
import com.gitlab.josercl.generator.Generator;
import com.gitlab.josercl.generator.InfraGenerator;
import org.apache.commons.text.CaseUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.util.Arrays;

abstract class GeneratorTask extends DefaultTask {
    @Input
    @Optional
    abstract Property<String> getEntityName();

    private final Generator generator;

    public GeneratorTask() {
        this.generator = new Generator(
            new InfraGenerator(),
            new DomainGenerator()
        );
    }

    @TaskAction
    public void generate() throws IOException {
        Object entities = getProject().getProperties().getOrDefault("entities", null);

        if (entities == null) return;

        Arrays.stream(((String) entities).split(","))
            .map(String::trim)
            .map(s -> CaseUtils.toCamelCase(s, true))
            .forEach(entity -> {
                try {
                    generator.generate(entity);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }
}