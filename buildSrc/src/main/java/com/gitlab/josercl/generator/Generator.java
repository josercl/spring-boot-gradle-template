package com.gitlab.josercl.generator;

import java.io.IOException;

public class Generator implements IGenerator {

    private final InfraGenerator infraGenerator;
    private final DomainGenerator domainGenerator;

    public Generator(InfraGenerator infraGenerator, DomainGenerator domainGenerator) {
        this.infraGenerator = infraGenerator;
        this.domainGenerator = domainGenerator;
    }

    @Override
    public void generate(String entityName) throws IOException {
        infraGenerator.generate(entityName);
        domainGenerator.generate(entityName);
    }

    public static void main(String[] args) throws IOException {
        String entityName = args[0];

        new Generator(new InfraGenerator(), new DomainGenerator())
            .generate(entityName);
    }
}
