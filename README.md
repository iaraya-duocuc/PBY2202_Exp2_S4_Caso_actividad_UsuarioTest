# PETSALUD

## Inicio local

Instala Java 17 y ejecuta desde la carpeta del proyecto:

```sh
sh ./mvnw -Dspring-boot.run.profiles=local spring-boot:run
```

La API queda disponible en [http://127.0.0.1:8080](http://127.0.0.1:8080). El perfil `local` usa H2 en memoria y los datos se borran al cerrar la aplicación. Para detenerla, presiona Ctrl+C.
