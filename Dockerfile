# meadow-backend (multi-stage: gradle build -> jre run, non-root)
FROM gradle:8.14-jdk21 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
# application.yaml 被 .gitignore 排除（防止真实密钥入库），容器构建时用占位符模板生成；
# 真实密钥/连接信息由 compose 的环境变量（MEADOW_*）在运行时注入。
RUN cp src/main/resources/application.example.yaml src/main/resources/application.yaml
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
