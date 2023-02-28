package com.gitlab.josercl.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class DomainGenerator implements IGenerator{
    public static final String DOMAIN_MODEL_PACKAGE = "model";
    public static final String DOMAIN_SPI_PACKAGE = "spi";
    public static final String DOMAIN_API_PACKAGE = "api";
    public static final String DOMAIN_API_IMPL_PACKAGE = "api.impl";
    public static final String SERVICE_SUFFIX = "Service";

    private final Path domainPath = Path.of("domain/src/main/java");

    @Override
    public void generate(String entityName) throws IOException {
        TypeSpec entityDomainSpec = getEntitySpec(domainName(entityName), List.of(), List.of());
        TypeSpec portSpec = getPortSpec(entityName);
        TypeSpec serviceSpec = getServiceSpec(entityName);
        TypeSpec serviceImplSpec = getServiceImplSpec(serviceSpec);

        JavaFile.builder(DOMAIN_MODEL_PACKAGE, entityDomainSpec)
            .build()
            .writeToPath(domainPath);
        JavaFile.builder(DOMAIN_SPI_PACKAGE, portSpec)
            .build()
            .writeToPath(domainPath);
        JavaFile.builder(DOMAIN_API_PACKAGE, serviceSpec)
            .build()
            .writeToPath(domainPath);
        JavaFile.builder(DOMAIN_API_IMPL_PACKAGE, serviceImplSpec)
            .build()
            .writeToPath(domainPath);
    }

    private TypeSpec getPortSpec(String entityName) {
        return TypeSpec.interfaceBuilder(portName(entityName))
            .addModifiers(Modifier.PUBLIC)
            .build();
    }

    private TypeSpec getServiceSpec(String entityName) {
        return TypeSpec.interfaceBuilder(String.format("%s%s", entityName, SERVICE_SUFFIX))
            .addModifiers(Modifier.PUBLIC)
            .build();
    }

    private TypeSpec getServiceImplSpec(TypeSpec serviceSpec) {
        return TypeSpec.classBuilder(String.format("%s%s", serviceSpec.name, "Impl"))
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ClassName.get(DOMAIN_API_PACKAGE, serviceSpec.name))
            .build();
    }
}
