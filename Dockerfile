FROM gradle:jdk8 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:8-jre-slim

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/telegram-bot-1.0-SNAPSHOT.jar /app/telegram-bot-1.0-SNAPSHOT.jar

ENV FU_TG_BOT_KEY=$FU_TG_BOT_KEY

EXPOSE 443 80 88 8443

ENTRYPOINT ["java", "-jar","/app/telegram-bot-1.0-SNAPSHOT.jar"]