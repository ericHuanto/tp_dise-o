# COMPILACIÓN (imagen con Maven + JDK 17)
FROM maven:3.9-eclipse-temurin-17 AS compilacion
WORKDIR /build

COPY pom.xml .
COPY shared/pom.xml shared/
COPY microservicio-donacion/pom.xml microservicio-donacion/
COPY microservicio-logistica/pom.xml microservicio-logistica/
COPY bff-usuario/pom.xml bff-usuario/

COPY shared/src shared/src
COPY bff-usuario/src bff-usuario/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl bff-usuario -am package -Dmaven.test.skip=true

# EJECUCIÓN (solo el runtime de Java, sin Maven ni código fuente)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=compilacion /build/bff-usuario/target/donatrack-bff-usuario.jar app.jar
EXPOSE 8080
# MaxRAMPercentage: el heap de Java usa como máximo ese % del límite de memoria del contenedor.
# Es CMD (no ENTRYPOINT) para que los jobs de cron puedan reemplazarlo por otro "java ... Clase".
CMD ["java", "-XX:MaxRAMPercentage=60", "-jar", "app.jar"]
