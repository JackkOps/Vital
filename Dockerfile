# ========================================================
# Estágio 1: Build da aplicação e execução dos testes (CI)
# ========================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /repo

# Copia arquivos do Maven Wrapper e POM preservando a hierarquia
COPY backend/rotavital/.mvn/ backend/rotavital/.mvn/
COPY backend/rotavital/mvnw backend/rotavital/pom.xml backend/rotavital/
RUN chmod +x backend/rotavital/mvnw && cd backend/rotavital && ./mvnw dependency:go-offline -B || true

# Copia código-fonte e especificações Gherkin (usadas por ../../docs/bdd no POM)
COPY backend/rotavital/src/ backend/rotavital/src/
COPY docs/bdd/ docs/bdd/

# Compila o projeto e roda a suíte de testes (unitários, integração e BDD)
WORKDIR /repo/backend/rotavital
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
COPY --from=builder /repo/backend/rotavital/target/*.jar /app/app.jar

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
