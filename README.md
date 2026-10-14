[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=santisr05_UDEA_LAB2V_ARQSW_2026&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=santisr05_UDEA_LAB2V_ARQSW_2026)
[![Coverage Status](https://coveralls.io/repos/github/santisr05/UDEA_LAB2V_ARQSW_2026/badge.svg?branch=main)](https://coveralls.io/github/santisr05/UDEA_LAB2V_ARQSW_2026?branch=main)
[![Cobertura](https://sonarcloud.io/api/project_badges/measure?project=santisr05_UDEA_LAB2V_ARQSW_2026&metric=coverage)](https://sonarcloud.io/summary/new_code?id=santisr05_UDEA_LAB2V_ARQSW_2026)
[![Líneas duplicadas](https://sonarcloud.io/api/project_badges/measure?project=santisr05_UDEA_LAB2V_ARQSW_2026&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=santisr05_UDEA_LAB2V_ARQSW_2026)
[![Líneas de código](https://sonarcloud.io/api/project_badges/measure?project=santisr05_UDEA_LAB2V_ARQSW_2026&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=santisr05_UDEA_LAB2V_ARQSW_2026)
[![Fiabilidad](https://sonarcloud.io/api/project_badges/measure?project=santisr05_UDEA_LAB2V_ARQSW_2026&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=santisr05_UDEA_LAB2V_ARQSW_2026)
[![Seguridad](https://sonarcloud.io/api/project_badges/measure?project=santisr05_UDEA_LAB2V_ARQSW_2026&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=santisr05_UDEA_LAB2V_ARQSW_2026)
[![Mantenibilidad](https://sonarcloud.io/api/project_badges/measure?project=santisr05_UDEA_LAB2V_ARQSW_2026&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=santisr05_UDEA_LAB2V_ARQSW_2026)
[![Deuda técnica](https://sonarcloud.io/api/project_badges/measure?project=santisr05_UDEA_LAB2V_ARQSW_2026&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=santisr05_UDEA_LAB2V_ARQSW_2026)

# UDEA_LAB2V_ARQSW_2026 - Banco (Laboratorio 2: CI/CD)

Aplicación bancaria (Spring Boot + MySQL) del curso de Arquitectura de Software, Universidad de Antioquia.
Permite crear clientes, transferir dinero entre cuentas y consultar el histórico de transacciones, desde una
interfaz web o directamente por la API REST. Incluye un pipeline de CI/CD con GitHub Actions, SonarQube Cloud,
Coveralls, Docker Hub y Render.

## Enlaces

| Recurso | URL |
|---|---|
| Servicio desplegado (Render) | https://banco-latest-qqm7.onrender.com/ |
| Imagen Docker (Docker Hub) | https://hub.docker.com/r/santisr05/banco |
| Análisis de calidad (SonarQube Cloud) | https://sonarcloud.io/dashboard?id=santisr05_UDEA_LAB2V_ARQSW_2026 |
| Cobertura (Coveralls) | https://coveralls.io/github/santisr05/UDEA_LAB2V_ARQSW_2026 |

El servicio usa el plan gratuito de Render: se suspende tras unos minutos sin tráfico y la primera petición puede
tardar en responder.

## Arquitectura

Arquitectura por capas dentro de `com.udea.lab1v2026`:

| Capa | Paquete | Responsabilidad |
|---|---|---|
| Controlador | `controller` | Endpoints REST y estado (`/health`, `/version`) |
| Servicio | `service` | Lógica de negocio y transacciones |
| Repositorio | `repository` | Acceso a datos con Spring Data JPA |
| Modelo | `entity`, `DTO`, `mapper` | Entidades, objetos de transferencia y mappers (MapStruct) |
| Errores | `exception` | Traducción de excepciones a códigos HTTP (400, 404, 409) |
| Interfaz web | `resources/static` | HTML, CSS y JavaScript servidos por Spring Boot |

Flujo de un cambio hasta la nube:

```
rama feature -> push -> GitHub Actions (tests -> sonar -> build -> docker -> deploy)
                                                         |             |
                                                   JAR (artefacto)  Docker Hub -> Render -> MySQL en Aiven
```

Estructura del proyecto:

```
.github/workflows/build.yml      Pipeline de CI/CD
Dockerfile, .dockerignore        Imagen de la aplicación
pom.xml                          Dependencias, JaCoCo y propiedades de Sonar
src/main/java/com/udea/lab1v2026 Código de la aplicación
src/main/resources               application.properties e interfaz web (static/)
src/test/java                    Pruebas unitarias (JUnit y Mockito)
```

## Tecnologías

JDK 17, Maven 3.9, Spring Boot 4.0.8, Spring Data JPA / Hibernate, MySQL, MapStruct, Lombok, JUnit y Mockito,
JaCoCo, GitHub Actions, SonarQube Cloud, Coveralls, Docker, Render y Aiven.

## API REST

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/` | Interfaz web |
| GET | `/health` | Estado del servicio |
| GET | `/version` | Versión de la aplicación |
| GET | `/api/customers` | Listar clientes |
| GET | `/api/customers/{id}` | Obtener un cliente |
| POST | `/api/customers` | Crear un cliente |
| POST | `/api/transactions` | Transferir dinero entre cuentas |
| GET | `/api/transactions/{accountNumber}` | Histórico de una cuenta |

Ejemplos de cuerpo:

```json
POST /api/customers
{ "firstName": "Ana", "lastName": "Perez", "accountNumber": "1001", "balance": 1000 }

POST /api/transactions
{ "senderAccountNumber": "1001", "receiverAccountNumber": "1002", "amount": 200 }
```

Errores: `400` datos inválidos (monto no positivo, cuentas iguales, cuenta inexistente, saldo insuficiente),
`404` cliente no encontrado, `409` violación de restricciones (por ejemplo, cuenta duplicada).

## Interfaz web

Al abrir la raíz del servicio se muestra una interfaz con tres vistas: **Clientes** (lista y creación),
**Transferir** (cuenta de origen, destino y monto) e **Historial** (transacciones enviadas y recibidas de una cuenta).
Son archivos estáticos que viajan dentro del mismo JAR, por lo que no requieren CORS.

## Ejecutar en local

Requisitos: JDK 17, Maven 3.6+ y una base MySQL llamada `lab1v2026banco`.

```shell
mvn spring-boot:run
```

Por defecto usa `root/root` sobre `localhost:3306` y el puerto `8088`. Para otra base se definen variables de entorno
(no se guardan credenciales reales en el repositorio):

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `DB_URL` | URL JDBC de la base de datos | MySQL en `localhost:3306/lab1v2026banco` |
| `DB_USERNAME` | Usuario | `root` |
| `DB_PASSWORD` | Contraseña | `root` (solo para desarrollo local) |
| `PORT` | Puerto HTTP | `8088` |

Ejemplo en PowerShell contra una base en la nube (Aiven exige SSL):

```powershell
$env:DB_URL="jdbc:mysql://HOST:PUERTO/defaultdb?sslMode=REQUIRED"
$env:DB_USERNAME="USUARIO"
$env:DB_PASSWORD="CONTRASEÑA"
mvn spring-boot:run
```

## Pruebas y cobertura

```shell
mvn clean verify
```

Ejecuta 26 pruebas unitarias (sin base de datos) y genera el reporte de JaCoCo en `target/site/jacoco/jacoco.xml`,
que leen SonarQube Cloud y Coveralls. Coveralls no calcula cobertura: solo la muestra a partir de ese reporte.
JaCoCo y Sonar excluyen las clases sin lógica propia (`entity`, `DTO`, `config`, `mapper` y la clase `Application`),
de modo que ambas herramientas miden el mismo alcance (72 líneas).

## Docker

```shell
mvn -B package -DskipTests
docker build -t banco .
docker run -p 8088:8088 -e DB_URL=... -e DB_USERNAME=... -e DB_PASSWORD=... banco
```

La imagen toma el JAR `target/banco.jar` que construye el pipeline, corre con un usuario sin privilegios y expone el
puerto 8088.

## Pipeline de CI/CD

El archivo `.github/workflows/build.yml` define cinco jobs encadenados:

| Job | Qué hace | Cuándo corre |
|---|---|---|
| `tests` | `mvn verify` y envío de la cobertura a Coveralls | Siempre |
| `sonar` | Análisis en SonarQube Cloud | En pull requests y en `main` |
| `build` | Genera `target/banco.jar` y lo sube como artefacto | Siempre que `tests` pase |
| `docker` | Construye la imagen y la publica en Docker Hub (`latest` y SHA del commit) | Solo en `main` |
| `deploy` | Avisa a Render por deploy hook y hace una prueba de humo sobre `/health` | Solo en `main` |

Qué ocurre según el evento:

| Evento | tests | sonar | build | docker | deploy |
|---|---|---|---|---|---|
| Push a una rama `feature/**` | Corre | Se omite | Corre | Se omite | Se omite |
| Pull request hacia `main` | Corre | Corre | Corre | Se omite | Se omite |
| Merge o push a `main` | Corre | Corre | Corre | Corre | Corre |
| Ejecución manual sobre `main` | Corre | Corre | Corre | Corre | Corre |

Un job omitido aparece en gris en GitHub y no es un fallo. Con un pull request abierto hay dos ejecuciones por commit:
la del push a la rama (sin Sonar) y la del pull request (con Sonar).

## Estrategia de ramas (trunk-based)

`main` es el tronco y siempre debe estar en verde. Los cambios entran por ramas cortas `feature/**` y pull requests:

```shell
git checkout main
git pull origin main
git checkout -b feature/mi-cambio
# ...cambios...
mvn clean verify
git add .
git commit -m "feat: descripción del cambio"
git push -u origin feature/mi-cambio
# abrir el pull request hacia main y fusionarlo cuando las verificaciones pasen
```

El merge a `main` publica la imagen y despliega automáticamente.

## Configuración de servicios externos

1. **SonarQube Cloud:** importar el repositorio, definir el código nuevo (en clase se usó 1 día), **desactivar
   Automatic Analysis** (Administration, Analysis Method) y generar un token. Verificar que `sonar.projectKey` y
   `sonar.organization` del `pom.xml` coincidan con los del proyecto.
2. **Coveralls:** iniciar sesión con GitHub y activar el repositorio (gratis solo para repos públicos; en uno privado
   hace falta el secreto `COVERALLS_REPO_TOKEN`).
3. **Docker Hub:** crear el repositorio público `banco` y un access token con permisos de lectura y escritura.
4. **Aiven (MySQL):** crear un servicio MySQL y copiar host, puerto, usuario y contraseña. La URL JDBC lleva
   `?sslMode=REQUIRED`.
5. **Render:** crear un servicio web desde una imagen existente (`docker.io/<usuario>/banco:latest`), instancia gratuita,
   con las variables `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` (sin `PORT`, lo define Render). Copiar el Deploy Hook.

Secretos y variables en GitHub (Settings, Secrets and variables, Actions):

| Nombre | Tipo | Uso |
|---|---|---|
| `SONAR_TOKEN` | Secreto | Token de SonarQube Cloud |
| `DOCKER_USERNAME` | Secreto | Usuario de Docker Hub |
| `DOCKER_PASSWORD` | Secreto | Access token de Docker Hub |
| `RENDER_DEPLOY_HOOK_URL` | Secreto | Deploy hook del servicio en Render |
| `COVERALLS_REPO_TOKEN` | Secreto (opcional) | Solo si el repositorio es privado |
| `RENDER_SERVICE_URL` | Variable | URL pública del servicio, para la prueba de humo |

## Notas importantes

- **Identificadores y Aiven:** Aiven rechaza tablas sin clave primaria. Con `GenerationType.AUTO`, Hibernate crea en
  MySQL tablas auxiliares de secuencia (`customers_seq`) sin clave primaria y la creación de registros falla. Por eso
  las entidades usan `GenerationType.IDENTITY`. Si se cambia el modo de generación de ids sobre tablas ya creadas, hay
  que recrearlas una vez (por ejemplo, ejecutando con `SPRING_JPA_HIBERNATE_DDL_AUTO=create`) y volver a `update`;
  esa variable nunca debe quedar definida en producción porque borra los datos en cada arranque.
- **Credenciales:** no se guardan en el repositorio. Los valores por defecto `root/root` son solo para desarrollo
  local.
- **Rama principal:** el pipeline asume que la rama principal se llama `main`.
- **Render gratuito:** la instancia se suspende por inactividad; la interfaz reintenta la carga inicial.
- **Hora de las transacciones:** se muestra tal como la entrega el servidor, sin convertir zonas horarias.
- **Sin autenticación:** cualquier persona con la URL puede crear clientes y transferir; es una aplicación de
  laboratorio.

## Autor

Santiago Sepúlveda - Ingeniería de Sistemas, Universidad de Antioquia. Curso de Arquitectura de Software, 2026-2.