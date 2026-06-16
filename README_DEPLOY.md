# 部署说明文档

## 主要改进点

1. **多环境配置**：支持 dev（开发）、test（测试）、prod（生产）三套环境，通过 `SPRING_PROFILES_ACTIVE` 环境变量切换
2. **敏感信息管理**：数据库密码、JWT密钥等敏感信息从 `.env` 文件读取，不硬编码在代码中
3. **Dockerfile 优化**：采用多阶段构建 + Maven 缓存分层，非 root 用户运行，内置健康检查
4. **健康检查机制**：集成 Spring Boot Actuator，Docker Compose 配置 healthcheck，确保服务就绪后才接收流量
5. **日志持久化**：日志按日期滚动，保留7天，挂载到宿主机 `./logs/backend` 目录
6. **自动化部署**：提供 PowerShell / Shell 脚本，支持一键部署、版本管理和自动回滚

## 环境变量配置说明

复制 `.env.example` 为 `.env`，根据实际环境修改以下变量：

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 激活的环境 (dev/test/prod) | `dev` |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 | `root` |
| `MYSQL_DATABASE` | 数据库名 | `permission_system` |
| `SPRING_DATASOURCE_URL` | 数据库连接URL | - |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | `root` |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | `root` |
| `JWT_SECRET` | JWT 密钥（至少32字符） | - |
| `JWT_EXPIRATION` | JWT 过期时间（毫秒） | `86400000` (24h) |
| `CORS_ALLOWED_ORIGINS` | 允许跨域的域名（逗号分隔） | `http://localhost:3000` |
| `LOG_FILE_PATH` | 日志文件路径 | `/app/logs/app.log` |
| `APP_VERSION` | 应用版本号 | `1.0.0` |

## 部署流程

### Windows 环境（PowerShell）

```powershell
# 1. 复制环境变量模板
copy .env.example .env

# 2. 编辑 .env 配置敏感信息
notepad .env

# 3. 开发环境部署
.\deploy.ps1 -Env dev

# 4. 生产环境部署（指定版本）
.\deploy.ps1 -Env prod -Version 1.2.0

# 5. 回滚到上一版本
.\deploy.ps1 -Rollback
```

### Linux/Mac 环境（Shell）

```bash
# 1. 复制环境变量模板
cp .env.example .env

# 2. 编辑 .env 配置敏感信息
vim .env

# 3. 添加执行权限
chmod +x deploy.sh

# 4. 开发环境部署
./deploy.sh dev

# 5. 生产环境部署（指定版本）
./deploy.sh prod 1.2.0

# 6. 回滚到上一版本
./deploy.sh "" "" --rollback
```

## 目录结构

```
logs/
  backend/           # 后端日志（持久化）
.env                 # 环境变量配置（不提交Git）
.env.example         # 环境变量模板
deploy.ps1           # Windows 部署脚本
deploy.sh            # Linux/Mac 部署脚本
docker-compose.yml   # Docker Compose 配置
```

## 健康检查

- 端点：`http://localhost:8000/actuator/health`
- 正常返回：`{"status":"UP"}`
- 验证数据库连接状态

## 不同环境差异

| 配置项 | dev | test | prod |
|--------|-----|------|------|
| 数据库连接池 | 5-10 | 10-20 | 20-50 |
| 日志级别 | DEBUG | INFO | WARN |
| CORS | 允许所有 | 指定域名 | 指定域名 |
| JWT 过期时间 | 24h | 12h | 2h |
| 健康检查详情 | 显示全部 | 授权后可见 | 不显示 |
