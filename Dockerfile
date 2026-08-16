# meadow-backend (multi-stage: gradle build -> jre run, non-root)
FROM gradle:8.14-jdk21 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle installDist --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S meadow && adduser -S meadow -G meadow
WORKDIR /app
COPY --from=build --chown=meadow:meadow /app/build/install/Meadow /app

USER meadow
EXPOSE 23333
HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
  CMD wget -qO- http://127.0.0.1:23333/healthz >/dev/null || exit 1

ENTRYPOINT ["/app/bin/Meadow"]
