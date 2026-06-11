# 阶段1：构建
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY domain/pom.xml domain/pom.xml
COPY domain/domain-model/pom.xml domain/domain-model/pom.xml
COPY domain/domain-api/pom.xml domain/domain-api/pom.xml
COPY domain/domain-impl/pom.xml domain/domain-impl/pom.xml
COPY domain/repository-api/pom.xml domain/repository-api/pom.xml
COPY domain/cache-api/pom.xml domain/cache-api/pom.xml
COPY domain/mq-api/pom.xml domain/mq-api/pom.xml
COPY domain/security-api/pom.xml domain/security-api/pom.xml
COPY infrastructure/pom.xml infrastructure/pom.xml
COPY infrastructure/repository/pom.xml infrastructure/repository/pom.xml
COPY infrastructure/repository/mysql-impl/pom.xml infrastructure/repository/mysql-impl/pom.xml
COPY infrastructure/cache/pom.xml infrastructure/cache/pom.xml
COPY infrastructure/cache/redis-impl/pom.xml infrastructure/cache/redis-impl/pom.xml
COPY infrastructure/mq/pom.xml infrastructure/mq/pom.xml
COPY infrastructure/mq/sqs-impl/pom.xml infrastructure/mq/sqs-impl/pom.xml
COPY infrastructure/security/pom.xml infrastructure/security/pom.xml
COPY infrastructure/security/jwt-impl/pom.xml infrastructure/security/jwt-impl/pom.xml
COPY application/pom.xml application/pom.xml
COPY application/application-api/pom.xml application/application-api/pom.xml
COPY application/application-impl/pom.xml application/application-impl/pom.xml
COPY interface/pom.xml interface/pom.xml
COPY interface/interface-http/pom.xml interface/interface-http/pom.xml
COPY interface/interface-consumer/pom.xml interface/interface-consumer/pom.xml
COPY bootstrap/pom.xml bootstrap/pom.xml
# 先下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -q || true
# 再复制源码并构建
COPY . .
RUN mvn clean package -DskipTests -q

# 阶段2：运行（精简镜像）
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/bootstrap/target/*.jar app.jar
EXPOSE 8003
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
