# Evidencia de pruebas de PETSALUD

Ejecutar desde la raíz del proyecto con Java 17:

```sh
sh ./mvnw clean verify
```

JaCoCo genera el informe HTML en [target/site/jacoco/index.html](../target/site/jacoco/index.html). Maven exige un mínimo del 80 % de líneas cubiertas en cada servicio de la tabla.

| Servicio | Línea base | Resultado final |
| --- | ---: | ---: |
| `UsuarioServiceImpl` | 1 de 17 líneas (5,9 %) | 30 de 30 líneas (100 %) |
| `VentaServiceImpl` | 1 de 24 líneas (4,2 %) | 28 de 28 líneas (100 %) |

La [línea base](jacoco-linea-base.csv) se midió con las tres pruebas originales, antes de modificar las reglas de negocio. La [ejecución final](jacoco-final.csv) incluye 35 pruebas aprobadas, con cero fallos, errores u omisiones. También se verificó que ejecutar únicamente `UsuarioTest` hace fallar la comprobación de cobertura en ambos servicios.

Las pruebas unitarias simulan el repositorio de Usuario y, en el servicio de Venta, el servicio de Usuario y los repositorios de Producto y Venta. Las pruebas HTTP usan MockMvc y el perfil local con H2 para comprobar permisos y errores de validación.

El código base contiene la relación Venta–Producto, comprobada en la suite. La relación Producto–Proveedor indicada en la actividad queda pendiente porque el modelo entregado carece de Proveedor y Reserva.
