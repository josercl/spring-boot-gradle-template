# Inicialización

Editar el archivo build.gradle de la raíz del proyecto y cambiar la variable "group"
al paquete de java que se quiere usar y después ejecutar:

```bash
./gradlew initProject
```

# API

Edita el archivo `api-specs/src/main/resources/api-spec.yaml`

Generar código en base a ese archivo

Código de Servidor:

```bash
./gradlew :api-specs:generateServer
```

Código para clientes:

```bash
./gradlew :api-specs:generateClient
```

Para generar el jar de cliente:

```bash
cd api-specs/build/generated/client
chmod +x ./gradlew
./gradlew jar
./gradlew publishMavenPublicationToMavenLocal
```

# Generar entidades, modelo, servicios, repos, etc.

```bash
./gradlew generateCrud -Pentities=clase1,clase2,...,claseN [-Ponly=domain|app|infra] [-PbasePackage=xxx.yyy.zzz]
```

Eso genera clases entidad, clase de dominio, mappers, repositorios, ports, adapters y servicios usando como base el(los)
nombre(s) de las clases argumentos

# Para correr el servicio

```bash
./gradlew bootRun
```