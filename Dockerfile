FROM amazoncorretto:17 AS builder
WORKDIR /app

# Install Gradle
RUN yum update -y && \
    yum install -y wget unzip && \
    wget https://services.gradle.org/distributions/gradle-8.7-bin.zip && \
    unzip gradle-8.7-bin.zip -d /opt/gradle && \
    ln -s /opt/gradle/gradle-8.7/bin/gradle /usr/bin/gradle

COPY . .
RUN gradle clean build -x test

# Stage 2: Run with Amazon Corretto
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]