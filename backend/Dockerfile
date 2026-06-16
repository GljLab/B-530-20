# =============================================
# 构建阶段
# =============================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# 拷贝 Maven 配置和依赖描述文件，利用 Docker 缓存
COPY settings.xml /usr/share/maven/ref/settings.xml
COPY pom.xml .

# 预下载依赖（只有 pom.xml 变化时才重新下载
RUN --mount=type=cache,target=/root/.m2 \
    mvn -s /usr/share/maven/ref/settings.xml dependency:go-offline -B

# 拷贝源码
COPY src ./src

# 打包
RUN --mount=type=cache,target=/root/.m2 \
    mvn -s /usr/share/maven/ref/settings.xml package -DskipTests -B

# =============================================
# 运行阶段
# =============================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 安装必要工具（用于健康检查）
RUN apk add --no-cache wget curl

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 创建日志目录并设置权限
RUN mkdir -p /app/logs /app/uploads && \
    chown -R appuser:appgroup /app/logs /app

# 拷贝 jar 包
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8000

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=5 --start-period=60s \
    CMD wget -q --spider http://localhost:8000/actuator/health || exit 1

# 切换到非 root 用户
USER appuser

# 启动命令
ENTRYPOINT ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "app.jar"]
