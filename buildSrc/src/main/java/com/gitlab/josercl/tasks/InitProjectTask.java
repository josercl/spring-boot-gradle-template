package com.gitlab.josercl.tasks;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class InitProjectTask extends DefaultTask {
    private final Map<String, List<String>> modulesDirs = Map.of(
        "boot", List.of(),
        "infrastructure", List.of("persistence/adapter", "persistence/config", "persistence/entity/mapper", "persistence/repository"),
        "domain", List.of("domain/api/impl", "domain/exception", "domain/model", "domain/spi"),
        "application", List.of("application/configuration", "application/rest/controller", "application/rest/model/mapper")
    );

    @TaskAction
    public void run() {
        String basePackage = Optional.ofNullable(getProject().getProperties().get("basePackage"))
            .map(String.class::cast)
            .orElse((String) getProject().getGroup());

        String baseFolder = basePackage.replaceAll("\\.", File.separator);

        initDirectories(baseFolder);
        createClasses(basePackage);
    }

    private void initDirectories(String baseFolder) {
        String userDir = System.getProperty("user.dir");

        modulesDirs.entrySet()
            .stream()
            .flatMap(entry -> entry.getValue().stream().map(dir -> Path.of(
                userDir,
                entry.getKey(),
                "src", "main", "java",
                baseFolder,
                dir
            )))
            .toList()
            .forEach(path -> {
                try {
                    Files.createDirectories(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

    private void createClasses(String basePackage) {
        createMainApplicationClass(basePackage);
        createApplicationConfigurationClass(basePackage);
        createPersistenceConfigurationClass(basePackage);
        JavaFile domainPageFile = createDomainPageClass(basePackage);
        createBasePageMapperClass(basePackage, domainPageFile);
        createErrorHandlerClass(basePackage);
    }

    private void createMainApplicationClass(String basePackage) {
        String mainApplicationName = "MainApplication";

        TypeSpec mainApplicationSpec = TypeSpec.classBuilder(mainApplicationName)
            .addModifiers(Modifier.PUBLIC)
            .addMethod(
                MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(String[].class, "args")
                    .addStatement(
                        "$T.run($T.class, args)",
                        ClassName.get(SpringApplication.class),
                        ClassName.get("", mainApplicationName)
                    )
                    .build()
            )
            .addAnnotation(SpringBootApplication.class)
            .build();
        try {
            JavaFile.builder(basePackage, mainApplicationSpec).build()
                .writeToPath(Path.of(
                    System.getProperty("user.dir"),
                    "boot",
                    "src", "main", "java"
                ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createApplicationConfigurationClass(String basePackage) {
        TypeSpec applicationConfigurationSpec = TypeSpec.classBuilder("ApplicationConfiguration")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(
                AnnotationSpec.builder(Configuration.class).build()
            ).build();

        String destPackage = basePackage + ".application.configuration";

        JavaFile appConfigurationFile = JavaFile.builder(destPackage, applicationConfigurationSpec).build();
        try {
            appConfigurationFile.writeToPath(Path.of(
                System.getProperty("user.dir"),
                "application",
                "src", "main", "java"
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createPersistenceConfigurationClass(String basePackage) {
        TypeSpec persistenceConfigurationSpec = TypeSpec.classBuilder("PersistenceConfiguration")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(
                AnnotationSpec.builder(Configuration.class).build()
            ).build();

        String destPackage = basePackage + ".persistence.config";

        JavaFile persistenceConfigurationFile = JavaFile.builder(destPackage, persistenceConfigurationSpec).build();
        try {
            persistenceConfigurationFile.writeToPath(Path.of(
                System.getProperty("user.dir"),
                "infrastructure",
                "src", "main", "java"
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JavaFile createDomainPageClass(String basePackage) {
        TypeVariableName typeVariable = TypeVariableName.get("T");
        TypeSpec domainPageSpec = TypeSpec.classBuilder("DomainPage")
            .addTypeVariable(typeVariable)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Data.class)
            .addAnnotation(Builder.class)
            .addAnnotation(AllArgsConstructor.class)
            .addField(ParameterizedTypeName.get(ClassName.get(List.class), typeVariable), "content", Modifier.PRIVATE)
            .addField(Integer.class, "page", Modifier.PRIVATE)
            .addField(Integer.class, "pageSize", Modifier.PRIVATE)
            .addField(Long.class, "totalElements", Modifier.PRIVATE)
            .addField(Integer.class, "totalPages", Modifier.PRIVATE)
            .build();

        String destPackage = basePackage + ".domain.model";

        JavaFile domainPageFile = JavaFile.builder(destPackage, domainPageSpec).build();
        try {
            domainPageFile.writeToPath(Path.of(
                System.getProperty("user.dir"),
                "domain",
                "src", "main", "java"
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return domainPageFile;
    }

    private void createErrorHandlerClass(String basePackage) {
        TypeSpec errorHandlerSpec = TypeSpec.classBuilder("ErrorHandler")
            .superclass(ResponseEntityExceptionHandler.class)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(ControllerAdvice.class)
            .addMethod(getHandleResponseExceptionMethod(basePackage))
            .addMethod(getHandleMethodArgumentNotValid())
            .build();

        String destPackage = basePackage + ".application";

        JavaFile errorHandlerFile = JavaFile.builder(destPackage, errorHandlerSpec).build();
        try {
            errorHandlerFile.writeToPath(Path.of(
                System.getProperty("user.dir"),
                "application",
                "src", "main", "java"
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createBasePageMapperClass(String basePackage, JavaFile domainPageSpec) {
        TypeVariableName t = TypeVariableName.get("T");
        ParameterSpec page = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(Page.class), t), "page").build();
        ClassName domainPage = ClassName.get(domainPageSpec.packageName, domainPageSpec.typeSpec.name);

        TypeSpec pageMapperSpec = TypeSpec.interfaceBuilder("BasePageMapper")
            .addTypeVariable(t)
            .addModifiers(Modifier.PUBLIC)
            .addMethod(
                MethodSpec.methodBuilder("toDomainPage")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(ParameterizedTypeName.get(domainPage, t))
                    .addParameter(page)
                    .addAnnotation(
                        AnnotationSpec.builder(Mapping.class)
                            .addMember("target", "$S", "pageSize")
                            .addMember("source", "$S", "size")
                            .build()
                    )
                    .addAnnotation(
                        AnnotationSpec.builder(Mapping.class)
                            .addMember("target", "$S", "page")
                            .addMember("source", "$S", "number")
                            .build()
                    )
                    .addAnnotation(
                        AnnotationSpec.builder(Mapping.class)
                            .addMember("target", "$S", "content")
                            .addMember("source", "$S", "content")
                            .addMember("defaultExpression", "$S", "java(java.util.List.of())")
                            .build()
                    )
                    .build()
            )
            .build();

        String destPackage = basePackage + ".persistence.entity.mapper";

        JavaFile mapperFile = JavaFile.builder(destPackage, pageMapperSpec).build();
        try {
            mapperFile.writeToPath(Path.of(
                System.getProperty("user.dir"),
                "infrastructure",
                "src", "main", "java"
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MethodSpec getHandleResponseExceptionMethod(String basePackage) {
        ClassName responseException = ClassName.get("common.exception", "ResponseException");
        ClassName errorDTO = ClassName.get(basePackage + ".rest.server.model", "ErrorDTO");
        ParameterSpec exArgument = ParameterSpec.builder(responseException, "ex").build();
        ClassName responseEntity = ClassName.get(ResponseEntity.class);

        return MethodSpec.methodBuilder("handleResponseException")
            .addAnnotation(
                AnnotationSpec.builder(ExceptionHandler.class)
                    .addMember("value", "{$T.class}", responseException)
                    .build()
            )
            .addModifiers(Modifier.PROTECTED)
            .returns(ParameterizedTypeName.get(responseEntity, errorDTO))
            .addParameter(exArgument)
            .addStatement("""
                return $T.status($N.getCode())
                .body(
                    new $T()
                        .code($N.getCode().value())
                        .message($N.getMessage())
                )""", ResponseEntity.class, exArgument, errorDTO, exArgument, exArgument)
            .build();
    }

    private MethodSpec getHandleMethodArgumentNotValid() {
        Method baseMethod = Arrays.stream(ResponseEntityExceptionHandler.class.getDeclaredMethods())
            .filter(method -> method.getName().equalsIgnoreCase("handleMethodArgumentNotValid"))
            .findFirst()
            .orElseThrow();

        MethodSpec.Builder builder = MethodSpec.methodBuilder(baseMethod.getName());
        builder.addAnnotation(Override.class);
        builder.varargs(baseMethod.isVarArgs());

        int modifiers = baseMethod.getModifiers();
        Set<Modifier> modifierSet = new HashSet<>();

        if (java.lang.reflect.Modifier.isPublic(modifiers)) {
            modifierSet.add(Modifier.PUBLIC);
        }
        if (java.lang.reflect.Modifier.isProtected(modifiers)) {
            modifierSet.add(Modifier.PROTECTED);
        }
        if (java.lang.reflect.Modifier.isPrivate(modifiers)) {
            modifierSet.add(Modifier.PRIVATE);
        }
        if (java.lang.reflect.Modifier.isAbstract(modifiers)) {
            modifierSet.add(Modifier.ABSTRACT);
        }
        if (java.lang.reflect.Modifier.isStatic(modifiers)) {
            modifierSet.add(Modifier.STATIC);
        }
        if (java.lang.reflect.Modifier.isFinal(modifiers)) {
            modifierSet.add(Modifier.FINAL);
        }
        if (java.lang.reflect.Modifier.isTransient(modifiers)) {
            modifierSet.add(Modifier.TRANSIENT);
        }
        if (java.lang.reflect.Modifier.isVolatile(modifiers)) {
            modifierSet.add(Modifier.VOLATILE);
        }
        if (java.lang.reflect.Modifier.isSynchronized(modifiers)) {
            modifierSet.add(Modifier.SYNCHRONIZED);
        }
        if (java.lang.reflect.Modifier.isNative(modifiers)) {
            modifierSet.add(Modifier.NATIVE);
        }
        if (java.lang.reflect.Modifier.isStrict(modifiers)) {
            modifierSet.add(Modifier.STRICTFP);
        }

        builder.addModifiers(modifierSet);

        builder.returns(baseMethod.getGenericReturnType());

        ParameterSpec exParameter = null;
        for (Parameter parameter : baseMethod.getParameters()) {
            ParameterSpec parameterSpec = ParameterSpec.builder(
                TypeName.get(parameter.getParameterizedType()),
                parameter.getName()
            ).build();

            if (BindingResult.class.isAssignableFrom(parameter.getType())) {
                exParameter = parameterSpec;
            }

            builder.addParameter(parameterSpec);
        }

        for (Type thrownType : baseMethod.getExceptionTypes()) {
            builder.addException(thrownType);
        }

        builder.addStatement("""
                    $T<$T> errorList = $N.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .collect($T.groupingBy(
                            $T::getField,
                            Collectors.mapping($T::getDefaultMessage, Collectors.toList())
                        ))
                        .entrySet()
                        .stream()
                        .map(entry -> new ValidationError(entry.getKey(), entry.getValue()))
                        .toList()""",
                List.class,
                ClassName.get("common", "ValidationError"),
                exParameter,
                Collectors.class,
                ClassName.get("org.springframework.validation", "FieldError"),
                ClassName.get("org.springframework.context.support", "DefaultMessageSourceResolvable")
            )
            .addStatement("return $T.unprocessableEntity().body(errorList)", baseMethod.getReturnType());

        return builder.build();
    }
}
