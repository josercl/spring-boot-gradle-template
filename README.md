Editar el archivo api-specs/src/main/resources/api-spec.yaml

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