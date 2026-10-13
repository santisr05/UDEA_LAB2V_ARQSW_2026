# Imagen de ejecucion: toma el JAR que ya construyo el job "build" del pipeline.
# Para probar en local: mvn -B package -DskipTests  y luego  docker build -t banco .
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Usuario sin privilegios (no ejecutar como root)
RUN addgroup --system spring && adduser --system --ingroup spring spring

COPY target/banco.jar app.jar

USER spring:spring

EXPOSE 8088

# SerialGC y limite de memoria para instancias pequenas (Render free: 512 MB)
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-XX:MaxRAMPercentage=70", "-jar", "app.jar"]
