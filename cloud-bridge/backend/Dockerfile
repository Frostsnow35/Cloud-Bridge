# Build stage
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first to cache this layer
RUN mvn dependency:go-offline -B

COPY src ./src
# Skip tests to speed up build
RUN mvn clean package -DskipTests

# Run stage
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Copy data for initialization
COPY data ./data

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
