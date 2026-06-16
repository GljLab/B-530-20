#!/bin/bash
# =============================================
# 酒店管理系统自动化部署脚本
# 用法: ./deploy.sh [dev|test|prod] [版本号] [--rollback]
# =============================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"
BACKUP_FILE="$SCRIPT_DIR/.docker-compose.backup.yml"

ENV="${1:-dev}"
VERSION="${2:-}"
ROLLBACK=false

if [[ "${3:-}" == "--rollback" ]]; then
    ROLLBACK=true
fi

if [ -z "$VERSION" ]; then
    VERSION="latest"
fi

# =============================================
# 日志函数
# =============================================
log() {
    local level="$1"
    shift
    local message="$*"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "[$timestamp] [$level] $message"
}

log_info() {
    log "INFO" "$@"
}

log_success() {
    log "SUCCESS" "$@"
}

log_warn() {
    log "WARN" "$@"
}

log_error() {
    log "ERROR" "$@"
}

# =============================================
# 检查环境
# =============================================
check_prerequisites() {
    log_info "检查前置条件..."

    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi

    if command -v docker-compose &> /dev/null; then
        DOCKER_COMPOSE="docker-compose"
    elif docker compose version &> /dev/null; then
        DOCKER_COMPOSE="docker compose"
    else
        log_error "Docker Compose 未安装"
        exit 1
    fi

    log_success "前置条件检查通过"
}

# =============================================
# 检查 .env 文件
# =============================================
check_env_file() {
    log_info "检查环境配置文件..."

    if [ ! -f "$ENV_FILE" ]; then
        log_warn ".env 文件不存在，将从 .env.example 创建默认配置"
        local example_file="$SCRIPT_DIR/.env.example"
        if [ -f "$example_file" ]; then
            cp "$example_file" "$ENV_FILE"
            log_warn "请编辑 $ENV_FILE 文件配置敏感信息后重新运行"
            return 1
        else
            log_error ".env.example 模板文件不存在"
            exit 1
        fi
    fi

    log_success ".env 文件存在"
    return 0
}

# =============================================
# 设置环境变量
# =============================================
set_environment() {
    local target_env="$1"
    log_info "设置部署环境: $target_env"

    export SPRING_PROFILES_ACTIVE="$target_env"
    export APP_VERSION="$VERSION"

    log_success "环境设置完成"
}

# =============================================
# 备份当前配置
# =============================================
backup_current_state() {
    log_info "备份当前状态..."

    if $DOCKER_COMPOSE ps -q &> /dev/null; then
        $DOCKER_COMPOSE config > "$BACKUP_FILE" 2>/dev/null || true
        if [ -f "$BACKUP_FILE" ] && [ -s "$BACKUP_FILE" ]; then
            log_success "当前配置已备份到 $BACKUP_FILE"
            return 0
        fi
    fi
    log_warn "没有运行中的容器，跳过备份"
    return 1
}

# =============================================
# 构建镜像
# =============================================
build_images() {
    log_info "构建 Docker 镜像 (版本: $VERSION)..."

    $DOCKER_COMPOSE build \
        --build-arg APP_VERSION="$VERSION" \
        backend frontend

    log_success "镜像构建完成"
}

# =============================================
# 启动服务
# =============================================
start_services() {
    log_info "启动服务..."

    $DOCKER_COMPOSE up -d --remove-orphans

    log_success "服务启动命令已执行"
}

# =============================================
# 验证服务健康状态
# =============================================
test_service_health() {
    local timeout="${1:-180}"
    log_info "验证服务健康状态（超时: ${timeout}秒）..."

    local elapsed=0
    local interval=10

    while [ $elapsed -lt $timeout ]; do
        local health=$(docker inspect --format='{{.State.Health.Status}}' label-530-backend 2>/dev/null || echo "starting")

        if [ "$health" == "healthy" ]; then
            log_success "后端服务健康检查通过"

            local db_health=$(docker inspect --format='{{.State.Health.Status}}' label-530-db 2>/dev/null || echo "unknown")
            if [ "$db_health" == "healthy" ]; then
                log_success "数据库服务健康检查通过"
            fi

            return 0
        elif [ "$health" == "unhealthy" ]; then
            log_warn "后端服务状态: unhealthy"
        else
            log_info "后端服务状态: $health (等待中...)"
        fi

        sleep $interval
        elapsed=$((elapsed + interval))
    done

    log_error "服务健康检查超时（${timeout}秒）"
    return 1
}

# =============================================
# 回滚机制
# =============================================
rollback() {
    log_warn "执行回滚操作..."

    if [ -f "$BACKUP_FILE" ] && [ -s "$BACKUP_FILE" ]; then
        log_info "从备份恢复..."
        $DOCKER_COMPOSE -f "$BACKUP_FILE" up -d --remove-orphans && {
            log_success "回滚成功"
            return 0
        }
    else
        log_warn "没有找到备份文件，尝试使用现有配置重启..."
        $DOCKER_COMPOSE restart && {
            log_success "服务已重启"
            return 0
        }
    fi

    log_error "回滚失败"
    return 1
}

# =============================================
# 回滚模式主流程
# =============================================
rollback_mode() {
    log_info "===== 回滚模式 ====="

    if [ ! -f "$BACKUP_FILE" ] || [ ! -s "$BACKUP_FILE" ]; then
        log_error "没有找到备份文件 $BACKUP_FILE，无法回滚"
        exit 1
    fi

    backup_current_state || true
    rollback

    if test_service_health 120; then
        log_success "回滚验证通过"
    else
        log_error "回滚后服务仍然不健康，请手动排查"
        exit 1
    fi
}

# =============================================
# 主部署流程
# =============================================
deploy() {
    log_info "============================================="
    log_info "  酒店管理系统部署脚本"
    log_info "  环境: $ENV"
    log_info "  版本: $VERSION"
    log_info "============================================="

    check_prerequisites

    if ! check_env_file; then
        log_warn "请先配置 .env 文件后重新运行"
        exit 1
    fi

    set_environment "$ENV"

    local has_backup=false
    if backup_current_state; then
        has_backup=true
    fi

    build_images
    start_services

    if test_service_health 180; then
        log_success "部署成功！所有服务运行正常"
    else
        log_error "部署失败"

        if $has_backup; then
            log_warn "正在执行自动回滚..."
            if rollback; then
                log_warn "已回滚到上一版本"
            fi
        else
            log_warn "没有可用的备份，无法自动回滚"
        fi

        exit 1
    fi

    echo ""
    log_info "============================================="
    log_info "  部署完成！"
    log_info "  后端服务: http://localhost:8000"
    log_info "  前端页面: http://localhost:3000"
    log_info "  健康检查: http://localhost:8000/actuator/health"
    log_info "============================================="
}

# =============================================
# 入口
# =============================================
check_prerequisites

if $ROLLBACK; then
    rollback_mode
else
    deploy
fi
