FROM openjdk:17-jdk

RUN groupadd spring && useradd spring -g spring
USER spring:spring
COPY boot/build/libs/boot.jar /

ENTRYPOINT ["java","-jar", "/boot.jar"]
