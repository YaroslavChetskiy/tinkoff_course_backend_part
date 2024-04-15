FROM eclipse-temurin:21

COPY target/bot.jar bot.jar

ENV APP_TELEGRAM_TOKEN=${APP_TELEGRAM_TOKEN}

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "-Dapp.telegram-token=$APP_TELEGRAM_TOKEN", "/bot.jar"]
