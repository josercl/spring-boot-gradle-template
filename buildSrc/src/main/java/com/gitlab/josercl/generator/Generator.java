package com.gitlab.josercl.generator;

import java.io.IOException;

public class Generator implements IGenerator {
    private final IGenerator[] generators;

    public Generator(IGenerator... generators) {
        this.generators = generators;
    }

    @Override
    public void generate(String entityName) throws IOException {
        for (IGenerator generator : this.generators) {
            generator.generate(entityName);
        }
    }
}
