package com.gitlab.josercl.generator;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import lombok.Data;
import org.mapstruct.Mapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.lang.model.element.Modifier;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class InfraGenerator implements IGenerator{
    public static final String INFRA_ENTITY_PACKAGE = "persistence.entity";
    public static final String INFRA_MAPPER_PACKAGE = "persistence.entity.mapper";
    public static final String INFRA_REPOSITORY_PACKAGE = "persistence.repository";
    public static final String INFRA_ADAPTER_PACKAGE = "persistence.adapter";

    private static final String REPOSITORY_SUFFIX = "Repository";
    private static final String ADAPTER_SUFFIX = "Adapter";

    private final Path infrastructurePath = Path.of("infrastructure/src/main/java");

    @Override
    public void generate(String entityName) throws IOException {
        FieldSpec idFieldSpec = FieldSpec.builder(Long.class, "id", Modifier.PRIVATE)
            .addAnnotation(Id.class)
            .build();

        TypeSpec entitySpec = getEntitySpec(entityName, List.of(Data.class, Entity.class), List.of(idFieldSpec));
        TypeSpec repositorySpec = getRepositorySpec(entitySpec, idFieldSpec);
        TypeSpec mapperSpec = getMapperSpec(entitySpec);
        TypeSpec adapterSpec = getAdapterSpec(entitySpec, repositorySpec, mapperSpec);

        JavaFile.builder(INFRA_ENTITY_PACKAGE, entitySpec)
            .build()
            .writeToPath(infrastructurePath);
        JavaFile.builder(INFRA_MAPPER_PACKAGE, mapperSpec)
            .build()
            .writeToPath(infrastructurePath);
        JavaFile.builder(INFRA_REPOSITORY_PACKAGE, repositorySpec)
            .build()
            .writeToPath(infrastructurePath);
        JavaFile.builder(INFRA_ADAPTER_PACKAGE, adapterSpec)
            .build()
            .writeToPath(infrastructurePath);
    }

    private TypeSpec getRepositorySpec(TypeSpec entitySpec, FieldSpec idFieldSpec) {
        return TypeSpec
            .interfaceBuilder(String.format("%s%s", entitySpec.name, REPOSITORY_SUFFIX))
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Repository.class)
            .addSuperinterface(
                ParameterizedTypeName.get(
                    ClassName.get(CrudRepository.class),
                    ClassName.get(INFRA_ENTITY_PACKAGE, entitySpec.name),
                    idFieldSpec.type
                )
            )
            .addSuperinterface(
                ParameterizedTypeName.get(
                    ClassName.get(JpaRepository.class),
                    ClassName.get(INFRA_ENTITY_PACKAGE, entitySpec.name),
                    idFieldSpec.type
                )
            )
            .build();
    }

    private TypeSpec getMapperSpec(TypeSpec entitySpec) {
        return TypeSpec.interfaceBuilder(String.format("%sMapper", entitySpec.name))
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(
                AnnotationSpec.builder(Mapper.class)
                    .addMember("injectionStrategy", CodeBlock.of("org.mapstruct.InjectionStrategy.CONSTRUCTOR"))
                    .build()
            )
            .addMethods(List.of(
                MethodSpec.methodBuilder("toDomain")
                    .returns(ClassName.get(DomainGenerator.DOMAIN_MODEL_PACKAGE, domainName(entitySpec.name)))
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(
                        ParameterSpec.builder(
                            ClassName.get(INFRA_ENTITY_PACKAGE, entitySpec.name),
                            entitySpec.name.toLowerCase()
                        ).build()
                    )
                    .build()
            ))
            .build();
    }

    private TypeSpec getAdapterSpec(TypeSpec entitySpec, TypeSpec repositorySpec, TypeSpec mapperSpec) {
        return TypeSpec.classBuilder(String.format("%s%s", entitySpec.name, ADAPTER_SUFFIX))
            .addSuperinterface(ClassName.get(DomainGenerator.DOMAIN_SPI_PACKAGE, portName(entitySpec.name)))
            .addField(
                FieldSpec.builder(
                        ClassName.get(INFRA_REPOSITORY_PACKAGE, repositorySpec.name),
                        "repository",
                        Modifier.PRIVATE, Modifier.FINAL
                    )
                    .build()
            )
            .addField(
                FieldSpec.builder(
                        ClassName.get(INFRA_MAPPER_PACKAGE, mapperSpec.name),
                        "mapper",
                        Modifier.PRIVATE, Modifier.FINAL
                    )
                    .build()
            )
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addParameter(
                        ClassName.get(INFRA_REPOSITORY_PACKAGE, repositorySpec.name),
                        "repository"
                    )
                    .addParameter(
                        ClassName.get(INFRA_MAPPER_PACKAGE, mapperSpec.name),
                        "mapper"
                    )
                    .addStatement("this.$N = $N", "mapper", "mapper")
                    .addStatement("this.$N = $N", "repository", "repository")
                    .build()
            )
            .build();
    }
}
