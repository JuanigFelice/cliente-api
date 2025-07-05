# API de Gestión de Clientes Bancarios

Esta es una API RESTful desarrollada con Spring Boot para la gestión de clientes bancarios y sus productos asociados. Permite realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) sobre los clientes, además de ofrecer un sistema de autenticación JWT para proteger los endpoints.

---

## 💻 Tecnologías Utilizadas

* **Java 17:** Lenguaje de programación.
* **Spring Boot 3.5.3:** Framework para el desarrollo rápido de APIs REST.
* **Maven:** Herramienta de gestión de proyectos y construcción.
* **MySQL:** Base de datos relacional para persistencia de datos.
* **Spring Data JPA / Hibernate:** Para la interacción con la base de datos.
* **Spring Security:** Para la autenticación y autorización.
* **JWT (JSON Web Tokens):** Para la seguridad de la API.
* **Lombok:** Para reducir el código boilerplate.
* **SonarQube:** Herramienta de análisis estático de código para asegurar la calidad.
* **JUnit:** Para pruebas unitarias y de integración.

---

## 📋 Requisitos del Sistema

Antes de comenzar, asegúrate de tener instalado lo siguiente:

* Java Development Kit (JDK) 17 o superior.
* Maven (generalmente viene con tu IDE o puedes instalarlo aparte).
* MySQL Server (versión 8.0 o superior recomendada).
* Un IDE (por ejemplo, IntelliJ IDEA, Eclipse o Spring Tool Suite).
* Postman o una herramienta similar para probar la API.

---

## ⚙️ Configuración del Entorno y Base de Datos

### 1. Configuración de MySQL

1.  **Crea la base de datos:** Conéctate a tu servidor MySQL (por ejemplo, usando MySQL Workbench o la línea de comandos) y crea una base de datos. La API intentará crearla si no existe, pero es una buena práctica hacerlo manualmente:

    ```sql
    CREATE DATABASE bancodb;
    ```

2.  **Crea un usuario y otorga permisos:** Es recomendable usar un usuario específico para la aplicación. Asegúrate de que el usuario (`jfelice` en este caso) tenga permisos para `bancodb`.

    ```sql
    CREATE USER 'jfelice'@'localhost' IDENTIFIED BY 'Jif2024&';
    GRANT ALL PRIVILEGES ON bancodb.* TO 'jfelice'@'localhost';
    FLUSH PRIVILEGES;
    ```
    *Nota: Si tu usuario de MySQL y contraseña son diferentes a `jfelice` y `Jif2024&`, o tu base de datos se llama distinto, asegúrate de actualizar el archivo `src/main/resources/application.properties`.*

### 2. Archivo `application.properties`

El archivo de configuración principal se encuentra en `src/main/resources/application.properties`. Ya está configurado para MySQL y los datos iniciales:

```properties
spring.application.name=cliente-api

# Configuración de la base de datos MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/bancodb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=jfelice
spring.datasource.password=Jif2024&
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Configuración de JPA/Hibernate
# 'update' creará/actualizará las tablas automáticamente si no existen
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT Configuration (Clave Secreta y Tiempo de Expiracion del Token)
# IMPORTANTE: en producción.
# Debe tener al menos 32 caracteres para HS256, pero se recomienda más.
banco.app.jwtSecret=SuperSecretaJWTKeyParaTuBanco1234567890abcdef
# Tiempo de expiración del token JWT en milisegundos (ej: 86400000 ms = 24 horas)
banco.app.jwtExpirationMs=86400000

logging.level.com.banco.cliente_api=DEBUG

🚀 Cómo Ejecutar la Aplicación

1. Clonar el Repositorio
Primero, clona este repositorio en tu máquina local:

git clone https://github.com/JuanigFelice/cliente-api
cd cliente-api

2. Construir el Proyecto con Maven
Navega al directorio raíz del proyecto (cliente-api) en tu terminal y ejecuta el siguiente comando Maven para construir el proyecto:

mvn clean install

3. Ejecutar la Aplicación

mvn spring-boot:run

o ejecutar el .jar directamente:

java -jar target/cliente-api-0.0.3.jar

La aplicación se iniciará en el puerto 8080.

🔒 Precarga de Datos Iniciales
Al iniciar la aplicación por primera vez, se precargarán automáticamente los siguientes datos en tu base de datos MySQL:
Roles
    • ROLE_USER
    • ROLE_MODERATOR
    • ROLE_ADMIN
Usuarios de Prueba
    • Username: admin
        ◦ Password: adminpass
        ◦ Roles: ROLE_ADMIN
    • Username: user
        ◦ Password: userpass
        ◦ Roles: ROLE_USER
Productos Bancarios
    • PZOF - Plazo Fijo
    • CHEQ - Cuenta Corriente
    • TJCREDITO - Tarjeta de Crédito
    • CJAHRR - Caja de Ahorro
    • CTACORR - Cuenta Corriente
    • PRESTAMO - Préstamo
    • TJDEBITO - Tarjeta de Débito
Estos datos iniciales te permitirán probar la API inmediatamente sin necesidad de inserciones manuales. Esto es útil suponiendo que la aplicación se integra en un entorno bancario donde ya existen tablas de Productos y Usuarios con roles predefinidos.


🧪 Cómo Probar la API (con Postman)
La API estará disponible en http://localhost:8080.
1. Obtener un JWT (Login)
Para interactuar con los endpoints protegidos, primero necesitas un JSON Web Token (JWT).
Opción A: Registrar un Nuevo Usuario (si es la primera vez o necesitas roles específicos)
    • Método: POST
    • URL: http://localhost:8080/api/auth/signup
    • Headers:
        ◦ Content-Type: application/json
    • Body (raw, JSON):
{
    "username": "nuevo_usuario",
    "password": "una_password_segura",
    "role": ["user"]
}


Para un administrador, cambia "role": ["user"] a "role": ["admin"].
    • Respuesta esperada: 200 OK con un mensaje de éxito.

Opción B: Iniciar Sesión con Usuarios Precargados para Obtener el Token (recomendado para pruebas rápidas)
    • Como la aplicación ya precarga usuarios y administradores para facilitar las pruebas, directamente puedes obtener el Token necesario haciendo:
    • Método: POST
    • URL: http://localhost:8080/api/auth/signin
    • Headers:
    • Content-Type: application/json
    • Body (raw, JSON):
      {
          "username": "admin",
          "password": "adminpass"
      }


## 💻 Tecnologías Utilizadas

* **Java 17:** Lenguaje de programación.
* **Spring Boot 3.5.3:** Framework para el desarrollo rápido de APIs REST.
* **Maven:** Herramienta de gestión de proyectos y construcción.
* **MySQL:** Base de datos relacional para persistencia de datos.
* **Spring Data JPA / Hibernate:** Para la interacción con la base de datos.
* **Spring Security:** Para la autenticación y autorización.
* **JWT (JSON Web Tokens):** Para la seguridad de la API.
* **Lombok:** Para reducir el código boilerplate.
* **SonarQube:** Herramienta de análisis estático de código para asegurar la calidad.
* **JUnit:** Para pruebas unitarias y de integración.

---

## 📋 Requisitos del Sistema

Antes de comenzar, asegúrate de tener instalado lo siguiente:

* Java Development Kit (JDK) 17 o superior.
* Maven (generalmente viene con tu IDE o puedes instalarlo aparte).
* MySQL Server (versión 8.0 o superior recomendada).
* Un IDE (por ejemplo, IntelliJ IDEA, Eclipse o Spring Tool Suite).
* Postman o una herramienta similar para probar la API.

---

## ⚙️ Configuración del Entorno y Base de Datos

### 1. Configuración de MySQL

1.  **Crea la base de datos:** Conéctate a tu servidor MySQL (por ejemplo, usando MySQL Workbench o la línea de comandos) y crea una base de datos. La API intentará crearla si no existe, pero es una buena práctica hacerlo manualmente:

    ```sql
    CREATE DATABASE bancodb;
    ```

2.  **Crea un usuario y otorga permisos:** Es recomendable usar un usuario específico para la aplicación. Asegúrate de que el usuario (`jfelice` en este caso) tenga permisos para `bancodb`.

    ```sql
    CREATE USER 'jfelice'@'localhost' IDENTIFIED BY 'Jif2024&';
    GRANT ALL PRIVILEGES ON bancodb.* TO 'jfelice'@'localhost';
    FLUSH PRIVILEGES;
    ```
    *Nota: Si tu usuario de MySQL y contraseña son diferentes a `jfelice` y `Jif2024&`, o tu base de datos se llama distinto, asegúrate de actualizar el archivo `src/main/resources/application.properties`.*

### 2. Archivo `application.properties`

El archivo de configuración principal se encuentra en `src/main/resources/application.properties`. Ya está configurado para MySQL y los datos iniciales:

```properties
spring.application.name=cliente-api

# Configuración de la base de datos MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/bancodb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=jfelice
spring.datasource.password=Jif2024&
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Configuración de JPA/Hibernate
# 'update' creará/actualizará las tablas automáticamente si no existen
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT Configuration (Clave Secreta y Tiempo de Expiracion del Token)
# IMPORTANTE: Cambia esta clave por una cadena larga, aleatoria y MUY SEGURA en producción.
# Debe tener al menos 32 caracteres para HS256, pero se recomienda más.
banco.app.jwtSecret=SuperSecretaJWTKeyParaTuBanco1234567890abcdef
# Tiempo de expiración del token JWT en milisegundos (ej: 86400000 ms = 24 horas)
banco.app.jwtExpirationMs=86400000

logging.level.com.banco.cliente_api=DEBUG
server.port=8081

🚀 Cómo Ejecutar la Aplicación
1. Clonar el Repositorio
Primero, clona este repositorio en tu máquina local:
Bash
git clone [https://github.com/JuanigFelice/cliente-api/](https://github.com/JuanigFelice/cliente-api/) # Reemplaza con la URL real de tu repositorio
cd cliente-api
2. Construir el Proyecto con Maven
Navega al directorio raíz del proyecto (cliente-api) en tu terminal y ejecuta el siguiente comando Maven para construir el proyecto:
Bash
mvn clean install
Este comando descargará todas las dependencias, compilará el código y generará el archivo JAR ejecutable.
3. Ejecutar la Aplicación
Una vez que el proyecto se ha construido con éxito, puedes ejecutar la aplicación Spring Boot desde la terminal:
Bash
mvn spring-boot:run
O bien, puedes ejecutar el archivo JAR directamente:
Bash
java -jar target/cliente-api-0.0.3.jar
La aplicación se iniciará en el puerto 8081.

🔒 Precarga de Datos Iniciales
Al iniciar la aplicación por primera vez, se precargarán automáticamente los siguientes datos en tu base de datos MySQL:
Roles
    • ROLE_USER
    • ROLE_MODERATOR
    • ROLE_ADMIN
Usuarios de Prueba
    • Username: admin
        ◦ Password: adminpass
        ◦ Roles: ROLE_ADMIN
    • Username: user
        ◦ Password: userpass
        ◦ Roles: ROLE_USER
Productos Bancarios
    • PZOF - Plazo Fijo
    • CHEQ - Cuenta Corriente
    • TJCREDITO - Tarjeta de Crédito
    • CJAHRR - Caja de Ahorro
    • CTACORR - Cuenta Corriente
    • PRESTAMO - Préstamo
    • TJDEBITO - Tarjeta de Débito
Estos datos iniciales te permitirán probar la API inmediatamente sin necesidad de inserciones manuales. Esto es útil suponiendo que la aplicación se integra en un entorno bancario donde ya existen tablas de Productos y Usuarios con roles predefinidos.

🧪 Cómo Probar la API (con Postman)
La API estará disponible en http://localhost:8081.
1. Obtener un JWT (Login)
Para interactuar con los endpoints protegidos, primero necesitas un JSON Web Token (JWT).
Opción A: Registrar un Nuevo Usuario (si es la primera vez o necesitas roles específicos)
    • Método: POST
    • URL: http://localhost:8081/api/auth/signup
    • Headers:
        ◦ Content-Type: application/json
    • Body (raw, JSON):
      JSON
      {
          "username": "nuevo_usuario",
          "password": "una_password_segura",
          "role": ["user"]
      }
      Para un administrador, cambia "role": ["user"] a "role": ["admin"].
    • Respuesta esperada: 200 OK con un mensaje de éxito.

Opción B: Iniciar Sesión con Usuarios Precargados para Obtener el Token (recomendado para pruebas rápidas)
    • Como la aplicación ya precarga usuarios y administradores para facilitar las pruebas, directamente puedes obtener el Token necesario haciendo:
    • Método: POST
    • URL: http://localhost:8081/api/auth/signin
    • Headers:
        ◦ Content-Type: application/json
    • Body (raw, JSON):
      JSON
      {
          "username": "admin",
          "password": "adminpass"
      }
      O para el usuario estándar:
      JSON
      {
          "username": "user",
          "password": "userpass"
      }
    • Envía la solicitud. Deberías obtener un 200 OK con un JSON que contiene el token.

Ejemplo de respuesta (el token será diferente en cada ejecución):

{
    "id": 490,
    "username": "admin",
    "roles": ["ROLE_ADMIN"],
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc1MTY1MDc1MiwiZXhwIjoxNzUxNzM3MTUyfQ.4fA2suPlE8I23jaiuLTSIrwVU49YgfDQ0OvN0TJMLU",
    "type": "Bearer"
}

Guarda este token. Lo usarás en el Authorization header para las siguientes solicitudes. <--
2. Acceder a Endpoints Protegidos
Una vez que tengas el token, añádelo en el Authorization header de tus solicitudes HTTP. El formato debe ser Authorization: Bearer <TU_TOKEN_JWT_OBTENIDO_AQUI>.
2.1. Crear Clientes (POST)
Crear un Solo Cliente
    • Endpoint: /api/clientes
    • Permisos requeridos: ADMIN
    • Método: POST
    • URL: http://localhost:8080/api/clientes
    • Headers:
        ◦ Content-Type: application/json
        ◦ Authorization: Bearer <TU_TOKEN_JWT_DE_ADMIN>
    • Body (raw, JSON):

{
    "dni": "12345678",
    "nombre": "Juan",
    "apellido": "Perez",
    "telefono": "1122334455",
    "celular": "1566778899",
    "calle": "Av. Principal",
    "numero": 123,
    "codigoPostal": "C1000",
    "productosBancariosCodigos": ["CHEQ", "CJAHRR", "TJCREDITO"]
}

Respuesta esperada: 201 Created y el objeto del cliente creado


Ejemplo de respuesta:

{
    "id": 110,
    "dni": "12345678",
    "nombre": "Juan",
    "apellido": "Perez",
    "calle": "Av. Principal",
    "numero": 123,
    "codigoPostal": "C1000",
    "telefono": "1122334455",
    "celular": "1566778899",
    "productosBancarios": [
        {
            "codigo": "CJAHRR",
            "descripcion": "Caja de Ahorro"
        },
        {
            "codigo": "TJCREDITO",
            "descripcion": "Tarjeta de Crédito"
        },
        {
            "codigo": "CHEQ",
            "descripcion": "Cheques"
        }
    ]
}

Crear Múltiples Clientes (Batch)
    • Endpoint: /api/clientes/batch
    • Permisos requeridos: ADMIN
    • Método: POST
    • URL: http://localhost:8080/api/clientes/batch
    • Headers:
        ◦ Content-Type: application/json
        ◦ Authorization: Bearer <TU_TOKEN_JWT_DE_ADMIN>
    • Body (raw, JSON):
[
    {
        "dni": "87654321",
        "nombre": "Maria",
        "apellido": "Gomez",
        "telefono": "2233445566",
        "celular": "1577889900",
        "calle": "Calle Falsa",
        "numero": 456,
        "codigoPostal": "B2000",
        "productosBancariosCodigos": ["CJAHRR"]
    },
    {
        "dni": "11223344",
        "nombre": "Carlos",
        "apellido": "Lopez",
        "telefono": "3344556677",
        "celular": "1588990011",
        "calle": "Av. Siempreviva",
        "numero": 789,
        "codigoPostal": "L3000",
        "productosBancariosCodigos": ["PZOF", "CHEQ"]
    }
]

Respuesta esperada: 201 Created y una lista de los clientes creados

2.2. Obtener Clientes (GET)
Obtener Todos los Clientes
    • Endpoint: /api/clientes
    • Permisos requeridos: USER, MODERATOR, ADMIN
    • Método: GET
    • URL: http://localhost:8080/api/clientes
    • Headers:
        ◦ Authorization: Bearer <TU_TOKEN_JWT_DE_USER_O_MODERATOR_O_ADMIN>
    • Body: (None)
    • Respuesta esperada: 200 OK y una lista de todos los clientes.
Ejemplo de respuesta (puede variar según los clientes creados previamente):
[
    {
        "id": 110,
        "dni": "12345678",
        "nombre": "Juan",
        "apellido": "Perez",
        "calle": "Av. Principal",
        "numero": 123,
        "codigoPostal": "C1000",
        "telefono": "1122334455",
        "celular": "1566778899",
        "productosBancarios": [
            {
                "codigo": "CJAHRR",
                "descripcion": "Caja de Ahorro"
            },
            {
                "codigo": "TJCREDITO",
                "descripcion": "Tarjeta de Crédito"
            },
            {
                "codigo": "CHEQ",
                "descripcion": "Cheques"
            }
        ]
    },
    {
        "id": 111,
        "dni": "87654321",
        "nombre": "Maria",
        "apellido": "Gomez",
        "calle": "Calle Falsa",
        "numero": 456,
        "codigoPostal": "B2000",
        "telefono": "2233445566",
        "celular": "1577889900",
        "productosBancarios": [
            {
                "codigo": "CJAHRR",
                "descripcion": "Caja de Ahorro"
            }
        ]
    },
    {
        "id": 112,
        "dni": "11223344",
        "nombre": "Carlos",
        "apellido": "Lopez",
        "calle": "Av. Siempreviva",
        "numero": 789,
        "codigoPostal": "L3000",
        "telefono": "3344556677",
        "celular": "1588990011",
        "productosBancarios": [
            {
                "codigo": "PZOF",
                "descripcion": "Plazo Fijo"
            },
            {
                "codigo": "CHEQ",
                "descripcion": "Cheques"
            }
        ]
    }
]

Obtener Cliente por DNI
    • Endpoint: /api/clientes/{dni}
    • Permisos requeridos: USER, MODERATOR, ADMIN
    • Método: GET
    • URL: http://localhost:8080/api/clientes/12345678 (Usando el DNI del cliente creado en el punto 2.1)
    • Headers:
        ◦ Authorization: Bearer <TU_TOKEN_JWT_DE_USER_O_MODERATOR_O_ADMIN>
    • Body: (None)
    • Respuesta esperada: 200 OK y el objeto del cliente con ese DNI. Si no existe, 404 Not Found.
Obtener Clientes por Código de Producto Bancario
    • Endpoint: /api/clientes/por-producto/{codigoProducto}
    • Permisos requeridos: USER, MODERATOR, ADMIN
    • Método: GET
    • URL: http://localhost:8080/api/clientes/por-producto/CJAHRR (Buscar clientes con Caja de Ahorro)
    • Headers:
        ◦ Authorization: Bearer <TU_TOKEN_JWT_DE_USER_O_MODERATOR_O_ADMIN>
    • Body: (None)
    • Respuesta esperada: 200 OK y una lista de clientes que poseen el producto. Si no hay clientes o el producto no existe, una lista vacía o 404 Not Found (dependiendo de la implementación de tu controlador).
2.3. Actualizar Teléfono de Cliente (PATCH)

Actualizar Teléfono de un Solo Cliente
    • Endpoint: /api/clientes/{dni}/telefono
    • Permisos requeridos: MODERATOR, ADMIN
    • Método: PATCH
    • URL: http://localhost:8080/api/clientes/11223344/telefono (Usando el DNI de un cliente existente)
    • Headers:
        ◦ Content-Type: application/json
        ◦ Authorization: Bearer <TU_TOKEN_JWT_DE_MODERATOR_O_ADMIN>
    • Body (raw, JSON):
{
    "dni": "11223344",
    "nuevoTelefono": "44556677"
}

Nota: Aunque el DNI está en la URL, se recomienda incluirlo también en el body para coherencia, aunque el controlador solo lo use de la URL.
    • Respuesta esperada: 200 OK y el objeto del cliente actualizado. Si el cliente no existe, 404 Not Found.
Ejemplo de respuesta:
{
    "id": 112,
    "dni": "11223344",
    "nombre": "Carlos",
    "apellido": "Lopez",
    "calle": "Av. Siempreviva",
    "numero": 789,
    "codigoPostal": "L3000",
    "telefono": "44556677",
    "celular": "1588990011",
    "productosBancarios": [
        {
            "codigo": "PZOF",
            "descripcion": "Plazo Fijo"
        },
        {
            "codigo": "CHEQ",
            "descripcion": "Cheques"
        }
    ]
}

Actualizar Teléfono de Múltiples Clientes (Batch)
    • Endpoint: /api/clientes/telefono/batch
    • Permisos requeridos: MODERATOR, ADMIN
    • Método: PATCH
    • URL: http://localhost:8081/api/clientes/telefono/batch
    • Headers:
        ◦ Content-Type: application/json
        ◦ Authorization: Bearer <TU_TOKEN_JWT_DE_MODERATOR_O_ADMIN>
    • Body (raw, JSON):
[
    {
        "dni": "87654321",
        "telefono": "5555555555"
    },
    {
        "dni": "11223344",
        "telefono": "6666666666"
    },
    {
        "dni": "99999999",
        "telefono": "7777777777"
    }
]

Respuesta esperada: 200 OK y una lista de resultados, indicando si cada cliente fue actualizado ("status": "actualizado") o si hubo un error ("status": "error", con un mensaje descriptivo).
2.4. Eliminar Clientes (DELETE)
Eliminar un Solo Cliente
    • Endpoint: /api/clientes/{dni}
    • Permisos requeridos: ADMIN
    • Método: DELETE
    • URL: http://localhost:8080/api/clientes/11223344 (Usando el DNI del cliente a eliminar)
    • Headers:
        ◦ Authorization: Bearer <TU_TOKEN_JWT_DE_ADMIN>
    • Body: (None)
    • Respuesta esperada: 200 OK y un objeto JSON indicando el éxito. Si el cliente no existe, 404 Not Found.
Ejemplo de respuesta:
{
    "message": "Cliente eliminado exitosamente",
    "dni": "11223344"
}

Eliminar Múltiples Clientes (Batch)
    • Endpoint: /api/clientes/batch
    • Permisos requeridos: ADMIN
    • Método: DELETE
    • URL: http://localhost:8080/api/clientes/batch
    • Headers:
        ◦ Content-Type: application/json
        ◦ Authorization: Bearer <TU_TOKEN_JWT_DE_ADMIN>
    • Body (raw, JSON):
[
    "12345678",
    "87654321"
]

Respuesta esperada: 200 OK y una lista de resultados, indicando si cada cliente fue eliminado ("status": "eliminado") o si hubo un error ("status": "error", con un mensaje).
Ejemplo de respuesta:
[
    {
        "dni": "12345678",
        "status": "eliminado",
        "message": "Cliente eliminado exitosamente."
    },
    {
        "dni": "87654321",
        "status": "eliminado",
        "message": "Cliente eliminado exitosamente."
    }
]



*********************************************************************

📈 Análisis de Calidad de Código con SonarQube
Este proyecto está configurado para integrarse con SonarQube, una plataforma para la calidad y seguridad del código.
1. Requisitos de SonarQube
    • SonarQube Server: Necesitas tener una instancia de SonarQube corriendo (ej. http://localhost:9000). Puedes descargar y ejecutarlo fácilmente.
    • SonarScanner for Maven: Ya configurado en el pom.xml.
2. Ejecutar Análisis
Asegúrate de que tu servidor SonarQube esté en ejecución. Luego, navega al directorio raíz de tu proyecto en la terminal y ejecuta:
	mvn clean verify sonar:sonar


Este comando construirá el proyecto, ejecutará las pruebas y luego enviará los resultados del análisis de código a tu SonarQube Server.
3. Ver Resultados
Después de que el análisis se complete, podrás ver los resultados detallados en la interfaz web de SonarQube (generalmente http://localhost:9000). Busca tu proyecto por la clave cliente-api-project (o la que hayas configurado en tu pom.xml para sonar.projectKey).
