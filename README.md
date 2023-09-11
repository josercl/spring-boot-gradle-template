# Inicialization

Edit the project level build.gradle file and change the "group" variable to the java package
you will use and then execute:

```bash
./gradlew initProject -PbasePackage=xxx.yyy.zzz -PprojectName=project_name_here
```

# API

Edit the API spec file (`api-specs/src/main/resources/api-spec.yaml`)

Generate code using based on that file

Server-side Code:

```bash
./gradlew :api-specs:generateServer
```

Client-side code:

```bash
./gradlew :api-specs:generateClient
```

To generate the client-side jar:

```bash
cd api-specs/build/generated/client
chmod +x ./gradlew
./gradlew jar
./gradlew publishMavenPublicationToMavenLocal
```

# Generate entities, model, services, repositories, etc.

```bash
./gradlew generateCrud -Pentities=class1,class2,...,classN [-Ponly=domain|app|infra] [-PbasePackage=xxx.yyy.zzz]
```

This will generate entity classes, domain models, mappers, repositories, ports/adapters and services using
as base the name of the entities specified

# To run the project

```bash
./gradlew bootRun
```