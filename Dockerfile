# Etapa 1: compila el proyecto con Maven. Las dependencias se bajan en una capa aparte
# para que Docker las reutilice mientras no cambie el pom.xml.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# Etapa 2: solo el JRE y el .jar, sin Maven ni el código fuente (imagen más chica)
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /app/target/logistica-api-*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=America/Argentina/Buenos_Aires", "-jar", "app.jar"]
