# UDEA_LAB2V_ARQSW_2026 - Banco (Laboratorio 2: CI/CD)

[![CI/CD Pipeline](https://github.com/santisr05/UDEA_LAB2V_ARQSW_2026/actions/workflows/build.yml/badge.svg)](https://github.com/santisr05/UDEA_LAB2V_ARQSW_2026/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=santisr05_UDEA_LAB2V_ARQSW_2026&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=santisr05_UDEA_LAB2V_ARQSW_2026)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=santisr05_UDEA_LAB2V_ARQSW_2026&metric=coverage)](https://sonarcloud.io/summary/new_code?id=santisr05_UDEA_LAB2V_ARQSW_2026)
[![Coverage Status](https://coveralls.io/repos/github/santisr05/UDEA_LAB2V_ARQSW_2026/badge.svg?branch=main)](https://coveralls.io/github/santisr05/UDEA_LAB2V_ARQSW_2026?branch=main)

Aplicacion bancaria (Spring Boot + MySQL) del curso de Arquitectura de Software, UdeA.
Permite crear clientes, transferir dinero entre cuentas y consultar el historico de transacciones,
desde una interfaz web o directamente por la API REST.
Incluye un pipeline de CI/CD con GitHub Actions, SonarCloud, Coveralls, Docker Hub y Render.

> Los badges usan el usuario `santisr05` y el project key `santisr05_UDEA_LAB2V_ARQSW_2026`. Ajustalos si los tuyos son distintos.

## Endpoints

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/` | Interfaz web (clientes, transferencias e historial) |
| GET | `/health` | Health check |
| GET | `/version` | Version de la aplicacion |
| GET | `/api/customers` | Listar clientes |
| GET | `/api/customers/{id}` | Obtener un cliente |
| POST | `/api/customers` | Crear un cliente |
| POST | `/api/transactions` | Transferir dinero entre cuentas |
| GET | `/api/transactions/{accountNumber}` | Historico de una cuenta |

## Interfaz web

Al abrir la raiz del servicio (`http://localhost:8088/` en local) se muestra una interfaz sencilla con tres vistas:

1. **Clientes**: lista de clientes y formulario para crear uno nuevo.
2. **Transferir**: transferencia de dinero entre dos cuentas.
3. **Historial**: transacciones enviadas y recibidas de una cuenta.

Son archivos estaticos (`src/main/resources/static`: HTML, CSS y JavaScript) que Spring Boot sirve junto con la API,
por eso viajan dentro del mismo JAR y de la misma imagen Docker, sin configurar CORS.

## Ejecutar en local

Requisitos: JDK 17, Maven 3.6+ y MySQL con la base `lab1v2026banco`.

```shell
mvn spring-boot:run
```

Por defecto usa `root/root` sobre `localhost:3306`. Para otra base se definen las variables de entorno
`DB_URL`, `DB_USERNAME` y `DB_PASSWORD` (no se guardan credenciales reales en el repositorio).

## Pruebas y cobertura

```shell
mvn clean verify
```

El reporte de cobertura queda en `target/site/jacoco/jacoco.xml` (lo leen SonarCloud y Coveralls).

## Docker

```shell
mvn -B package -DskipTests
docker build -t banco .
docker run -p 8088:8088 -e DB_URL=... -e DB_USERNAME=... -e DB_PASSWORD=... banco
```

## Pipeline CI/CD (`.github/workflows/build.yml`)

1. **tests**: `mvn verify` (JUnit + JaCoCo) y envio de cobertura a Coveralls.
2. **sonar**: analisis estatico en SonarCloud (en pull requests y en `main`).
3. **build**: genera `target/banco.jar` y lo guarda como artefacto.
4. **docker**: construye la imagen con ese JAR y la publica en Docker Hub (solo `main`).
5. **deploy**: avisa a Render para desplegar la imagen y hace una prueba de humo sobre `/health` (solo `main`).

Estrategia trunk-based: `main` es el tronco; los cambios entran por ramas cortas `feature/**` y pull requests.

## Secretos y variables del repositorio

| Nombre | Tipo | Uso |
|---|---|---|
| `SONAR_TOKEN` | Secret | Token de SonarCloud |
| `DOCKER_USERNAME` | Secret | Usuario de Docker Hub |
| `DOCKER_PASSWORD` | Secret | Access token de Docker Hub |
| `RENDER_DEPLOY_HOOK_URL` | Secret | Deploy hook del servicio en Render |
| `COVERALLS_REPO_TOKEN` | Secret (opcional) | Solo si el repo es privado |
| `RENDER_SERVICE_URL` | Variable | URL publica del servicio (para la prueba de humo) |
