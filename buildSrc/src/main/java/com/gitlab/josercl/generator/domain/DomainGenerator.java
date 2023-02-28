package com.gitlab.josercl.generator.domain;

import com.gitlab.josercl.generator.Constants;
import com.gitlab.josercl.generator.IGenerator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class DomainGenerator implements IGenerator {

    private final Path domainPath = Path.of("domain/src/main/java");

    @Override
    public void generate(String entityName) throws IOException {
        TypeSpec entityDomainSpec = getEntitySpec(entityName, List.of(), List.of());
        JavaFile domainModelFile = JavaFile.builder(Constants.Domain.MODEL_PACKAGE, entityDomainSpec).build();
        domainModelFile.writeToPath(domainPath);

        TypeSpec portSpec = getPortSpec(entityName);
        JavaFile portFile = JavaFile.builder(Constants.Domain.SPI_PACKAGE, portSpec).build();
        portFile.writeToPath(domainPath);

        TypeSpec serviceSpec = getServiceSpec(entityName);
        JavaFile serviceFile = JavaFile.builder(Constants.Domain.API_PACKAGE, serviceSpec).build();
        serviceFile.writeToPath(domainPath);

        TypeSpec serviceImplSpec = getServiceImplSpec(serviceFile);
        JavaFile serviceImplFile = JavaFile.builder(Constants.Domain.API_IMPL_PACKAGE, serviceImplSpec).build();
        serviceImplFile.writeToPath(domainPath);
    }

    private TypeSpec getPortSpec(String entityName) {
        return TypeSpec.interfaceBuilder(portName(entityName))
            .addModifiers(Modifier.PUBLIC)
            .build();
    }

    private TypeSpec getServiceSpec(String entityName) {
        return TypeSpec.interfaceBuilder(String.format("%s%s", entityName, Constants.Domain.SERVICE_SUFFIX))
            .addModifiers(Modifier.PUBLIC)
            .build();
    }

    private TypeSpec getServiceImplSpec(JavaFile serviceFile) {
        return TypeSpec.classBuilder(String.format("%s%s", serviceFile.typeSpec.name, "Impl"))
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ClassName.get(serviceFile.packageName, serviceFile.typeSpec.name))
            .build();
    }
}
