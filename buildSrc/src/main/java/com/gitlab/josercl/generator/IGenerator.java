package com.gitlab.josercl.generator;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.text.CaseUtils;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.List;

public interface IGenerator {
    void generate(String entityName) throws IOException;

    default TypeSpec getEntitySpec(String entityName, List<Class<?>> annotations, List<FieldSpec> idFieldSpecs) {
        TypeSpec.Builder builder = TypeSpec
            .classBuilder(CaseUtils.toCamelCase(entityName, true))
            .addModifiers(Modifier.PUBLIC)
            .addFields(idFieldSpecs);
        annotations.forEach(builder::addAnnotation);
        return builder.build();
    }

    default String portName(String name) {
        return String.format("%s%s", CaseUtils.toCamelCase(name, true), Constants.Domain.PORT_SUFFIX);
    }
}
