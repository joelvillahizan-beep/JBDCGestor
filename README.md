# GestorRecetas - Caso práctico UD02

Aplicación de consola en Java que guarda las recetas en MySQL mediante JDBC.
Permite listar, buscar por id, añadir, modificar el tiempo y las raciones,
eliminar y buscar por nombre con el procedimiento `buscarReceta`.

Las altas y las bajas actualizan `estadisticas` en la misma transacción.
Las consultas con datos de entrada utilizan parámetros. La búsqueda por nombre
utiliza `CallableStatement`, que hereda de `PreparedStatement`.

## Requisitos

- JDK 17 o posterior. Verificado con OpenJDK 21.0.13.
- Maven. Verificado con Maven 3.9.9.
- MySQL 8.0 o posterior. Verificado con MySQL 8.4.6 e InnoDB.
- MySQL Connector/J 9.1.0, descargado por Maven mediante `pom.xml`.

## Preparar la base

Abre `src/main/resources/recetario.sql` en MySQL Workbench y ejecuta el script.
El script elimina y vuelve a crear la base `recetario`, como en el enunciado:
úsalo solo para la base de esta práctica, sin otros datos que quieras conservar.
Se crean las dos tablas, las ocho recetas iniciales y el procedimiento almacenado.

## Configurar la conexión

`ConexionBD` lee las variables de entorno `DB_URL`, `DB_USER` y `DB_PASSWORD`.
Si no existen, utiliza `localhost:3306`, la base `recetario`, el usuario `root`
y una contraseña vacía. Debes indicar la contraseña real de tu MySQL local.
El proyecto no incluye contraseñas.

En IntelliJ, abre `pom.xml` como proyecto Maven. Elige un JDK 17 o posterior,
recarga Maven y crea una configuración de aplicación para `GestionRecetas`.
En sus variables de entorno añade, con tus datos:

```text
DB_URL=jdbc:mysql://localhost:3306/recetario?connectionTimeZone=Europe/Madrid
DB_USER=root
DB_PASSWORD=tu_contraseña_local
```

La contraseña del ejemplo es un texto que debes sustituir, no una credencial.
Si MySQL usa otro puerto, cambia el `3306` de la URL.
En el entorno de pruebas se utilizó el puerto `3307` y la cuenta local
`recetario_app`; esos datos no obligan a cambiar tu instalación.

## Ejecutar

Ejecuta `GestionRecetas.main()` desde IntelliJ. También puedes utilizar una
terminal con las variables de entorno configuradas:

```text
mvn compile exec:java
```

El menú ofrece las operaciones numeradas del 1 al 7. La opción 0 termina.
La cabecera muestra el usuario del sistema, la fecha, la hora y la conexión.

## Comprobaciones de integración

`src/test/java/VerificarRecetas.java` comprueba 20 resultados sobre MySQL real.
Este comprobador se ejecuta de forma explícita; `mvn test` no lo lanza.
Utiliza una base de prácticas recién creada y sin otros usuarios conectados:
durante la prueba de error se modifica temporalmente el contador de recetas.

```text
mvn test-compile exec:java -Dexec.mainClass=VerificarRecetas -Dexec.classpathScope=test -Dexec.args=--base-de-pruebas
```

Para comprobar el `rollback`, el comprobador prepara el contador con el máximo
valor de un `INT`, `2147483647`. El alta inserta una receta y después intenta
sumar uno al contador. Esa segunda operación produce el error 1264 en modo SQL
estricto. Se verifica que la receta no existe y que el contador mantiene su valor
anterior a la transacción. Un bloque `finally` restaura el total de ocho recetas.
El id autoincremental puede dejar un hueco después de una baja o un rollback.

El registro `evidencias/verificacion.txt` recoge la ejecución realizada el
23/11/2026 en el entorno de pruebas de esta conversación. El usuario del sistema
es `agent`; no es una ejecución en el ordenador personal del alumno.

## Organización

| Archivo | Función |
| --- | --- |
| `ConexionBD.java` | Abre las conexiones JDBC. |
| `Receta.java` | Representa una fila de la tabla recetas. |
| `GestionRecetas.java` | Contiene el menú y los métodos de acceso a datos. |
| `recetario.sql` | Crea la base, las tablas, los datos y el procedimiento. |
| `VerificarRecetas.java` | Comprueba el comportamiento con MySQL real. |

Cada operación abre y cierra su conexión con `try-with-resources`. En una
transacción, las dos sentencias comparten la misma conexión. Se confirma con
`commit()` y se intenta deshacer con `rollback()` ante un error SQL. No se
reutiliza la conexión después de cerrarla.

## Referencias

- Enunciado facilitado: `Caso_practico_UD02 (1).pdf`.
- [Oracle: PreparedStatement](https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html)
- [Oracle: transacciones JDBC](https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html)
- [MySQL: procedimientos con CallableStatement](https://dev.mysql.com/doc/connector-j/en/connector-j-usagenotes-statements-callable.html)
- [MySQL: dependencia Maven de Connector/J](https://dev.mysql.com/doc/connector-j/en/connector-j-installing-maven.html)
