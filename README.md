# API

Edita el archivo api-specs/src/main/resources/api-spec.yaml

Generar código en base a ese archivo

Código de Servidor:

```bash
./gradlew :api-specs:generate-server
```

Código para clientes:

```bash
./gradlew :api-specs:generate-client
```

Para generar el jar de cliente:

```bash
cd api-specs/build/generated/client && mvn clean package
```

# Generar entidades, modelo, servicios, repos, etc.

```bash
./gradlew generate -Pentities=clase1,clase2,...,claseN
```

Eso genera clases entidad, clase de dominio, mappers, repositorios, ports, adapters y servicios usando como base el(los)
nombre(s) de las clases argumentos
