# Use a imagem base do OpenJDK
FROM openjdk:17-jdk-slim

# Define o diretório de trabalho
WORKDIR /app

# Copia o arquivo JAR para o container
COPY target/src-1.0-SNAPSHOT.jar /app/conferencia.jar

# Comando para rodar o JAR
CMD ["java", "-jar", "conferencia.jar"]