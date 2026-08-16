# meadow-backend (multi-stage: gradle build -> jre run, non-root)
# 注意：必须用 glibc 基础镜像（eclipse-temurin:*-jre，基于 Ubuntu）——
# Argon2 的 JNA 原生库在 Alpine(musl) 上会 SIGSEGV 崩溃。
FROM gradle:8.14-jdk21 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
# application.yaml 被 .gitignore 排除（防止真实密钥入库），容器构建时用占位符模板生成；
# 真实密钥/连接信息由 compose 的环境变量（MEADOW_*）在运行时注入。
RUN cp src/main/resources/application.example.yaml src/main/resources/application.yaml
RUN gradle installDist --no-daemon -x test

FROM eclipse-temurin:21-jre
RUN groupadd -r meadow && useradd -r -g meadow meadow
WORKDIR /app
COPY --from=build --chown=meadow:meadow /app/build/install/Meadow /app

USER meadow
EXPOSE 23333
HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
  CMD wget -qO- http://127.0.0.1:23333/healthz >/dev/null || exit 1

ENTRYPOINT ["/app/bin/Meadow"]
