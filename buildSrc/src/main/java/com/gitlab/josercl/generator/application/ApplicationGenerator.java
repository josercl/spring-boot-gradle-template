package com.gitlab.josercl.generator.application;

import com.gitlab.josercl.generator.Constants;
import com.gitlab.josercl.generator.IGenerator;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.text.CaseUtils;
import org.mapstruct.Mapper;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;

public class ApplicationGenerator implements IGenerator {

    private final Path applicationPath = Path.of("application/src/main/java");

    @Override
    public void generate(String entityName) throws IOException {
        TypeSpec mapperSpec = getMapperSpec(entityName);
        JavaFile mapperFile = JavaFile.builder(Constants.Application.MAPPER_PACKAGE, mapperSpec).build();
        mapperFile.writeToPath(applicationPath);

        TypeSpec controllerSpec = getControllerSpec(entityName, mapperFile);
        JavaFile controllerFile = JavaFile.builder(Constants.Application.CONTROLLER_PACKAGE, controllerSpec).build();
        controllerFile.writeToPath(applicationPath);
    }

    private TypeSpec getControllerSpec(String entityName, JavaFile mapperFile) {
        ClassName serviceType = ClassName.get(
            Constants.Domain.API_PACKAGE,
            String.format("%s%s", CaseUtils.toCamelCase(entityName, true), Constants.Domain.SERVICE_SUFFIX)
        );
        ClassName mapperType = ClassName.get(
            mapperFile.packageName,
            String.format("%s%s", CaseUtils.toCamelCase(entityName, true), Constants.MAPPER_SUFFIX)
        );

        FieldSpec serviceField = FieldSpec.builder(serviceType, "service")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();
        FieldSpec mapperField = FieldSpec.builder(mapperType, "mapper")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build();

        ParameterSpec serviceParam = ParameterSpec.builder(serviceType, "service").build();
        ParameterSpec mapperParam = ParameterSpec.builder(mapperType, "mapper").build();

        return TypeSpec.classBuilder(String.format("%s%s", entityName, Constants.Application.CONTROLLER_SUFFIX))
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(RestController.class)
            .addField(serviceField)
            .addField(mapperField)
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addParameter(serviceParam)
                    .addParameter(mapperParam)
                    .addStatement("this.$N = $N", serviceField, serviceParam)
                    .addStatement("this.$N = $N", mapperField, mapperParam)
                    .build()
            )
            .build();
    }

    private TypeSpec getMapperSpec(String entityName) {
        return TypeSpec.interfaceBuilder(String.format("%s%s", entityName, "Mapper"))
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(
                AnnotationSpec.builder(Mapper.class)
                    .addMember("injectionStrategy", "$L", "org.mapstruct.InjectionStrategy.CONSTRUCTOR")
                    .addMember("componentModel", "$L", "org.mapstruct.MappingConstants.ComponentModel.SPRING")
                    .addMember("uses", "{$L}", "PagedMapper.class")
                    .build()
            )
            .build();
    }
}
