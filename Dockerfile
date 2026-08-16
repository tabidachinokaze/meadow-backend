# meadow-backend Dockerfile（多阶段构建，非 root 运行）
# 构建：JDK 21 + Gradle wrapper
FROM gradle:8.14-jdk21 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle installDist --no-daemon -x test

# 运行：JRE 21（Alpine 精简）
FROM eclipse-temurin:21-jre-alpine
# 非 root 用户（Alpine 无 shadow，用 addgroup/adduser）
RUN addgroup -S meadow && adduser -S meadow -G meadow
WORKDIR /app
COPY --from=build --chown=meadow:meadow /app/build/install/Meadow /app

USER meadow
EXPOSE 23333
# 健康检查探针
HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
  CMD wget -qO- http://127.0.0.1:23333/healthz >/dev/null || exit 1

ENV KTOR_DEVELOPMENT=false
ENTRYPOINT ["/app/bin/Meadow"]
