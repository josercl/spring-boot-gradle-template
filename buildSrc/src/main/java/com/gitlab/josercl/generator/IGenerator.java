package com.gitlab.josercl.generator;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.List;

public interface IGenerator {
    void generate(String entityName) throws IOException;

    default TypeSpec getEntitySpec(String entityName, List<Class<?>> annotations, List<FieldSpec> idFieldSpecs) {
        TypeSpec.Builder builder = TypeSpec
            .classBuilder(entityName)
            .addModifiers(Modifier.PUBLIC)
            .addFields(idFieldSpecs);
        annotations.forEach(builder::addAnnotation);
        return builder.build();
    }

    default String domainName(String name) {
        return String.format("%sDomain", name);
    }

    default String portName(String name) {
        return String.format("%sPort", name);
    }
}
