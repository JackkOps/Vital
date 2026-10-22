# ========================================================
# Estágio 1: Build da aplicação e execução dos testes (CI)
# ========================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copia arquivos do Maven Wrapper e POM primeiro para cache eficiente de camadas
COPY backend/rotavital/.mvn/ .mvn/
COPY backend/rotavital/mvnw backend/rotavital/pom.xml ./
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B || true

# Copia o código-fonte da aplicação
COPY backend/rotavital/src/ src/

# Compila o projeto e roda a suíte de testes automatizados
RUN ./mvnw clean package -DskipTests=false

# ========================================================
# Estágio 2: Runtime enxuto e seguro (Imagem Final)
# ========================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Criação de usuário não-root por segurança (princípio de menor privilégio)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copia estritamente o artefato compilado do primeiro estágio
COPY --from=builder /build/target/*.jar /app/app.jar

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
