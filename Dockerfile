FROM openjdk:17-jdk

RUN groupadd spring && useradd spring -g spring

USER spring:spring

WORKDIR /usr/src/app

COPY boot/build/libs/boot.jar .

EXPOSE 8080

ENTRYPOINT ["java","-jar", "boot.jar"]
