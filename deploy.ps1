# =============================================
# 酒店管理系统自动化部署脚本
# 用法: .\deploy.ps1 [-Env dev|test|prod] [-Version <版本号>] [-Rollback]
# =============================================

param(
    [string]$Env = "dev",
    [string]$Version = "",
    [switch]$Rollback
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$EnvFile = Join-Path $ScriptDir ".env"
$BackupFile = Join-Path $ScriptDir ".docker-compose.backup.yml"

# =============================================
# 日志函数
# =============================================
function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] [$Level] $Message"
}

function Write-Success {
    param([string]$Message)
    Write-Log $Message "SUCCESS"
}

function Write-Warn {
    param([string]$Message)
    Write-Log $Message "WARN"
}

function Write-Error {
    param([string]$Message)
    Write-Log $Message "ERROR"
}

# =============================================
# 检查环境
# =============================================
function Test-Prerequisites {
    Write-Log "检查前置条件..."

    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw "Docker 未安装，请先安装 Docker Desktop"
    }

    if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
        if (-not (docker compose version 2>$null)) {
            throw "Docker Compose 未安装"
        }
    }

    Write-Success "前置条件检查通过"
}

# =============================================
# 检查 .env 文件
# =============================================
function Test-EnvFile {
    Write-Log "检查环境配置文件..."

    if (-not (Test-Path $EnvFile)) {
        Write-Warn ".env 文件不存在，将从 .env.example 创建默认配置"
        $exampleFile = Join-Path $ScriptDir ".env.example"
        if (Test-Path $exampleFile) {
            Copy-Item $exampleFile $EnvFile
            Write-Warn "请编辑 $EnvFile 文件配置敏感信息后重新运行"
            return $false
        }
        else {
            throw ".env.example 模板文件不存在"
        }
    }

    Write-Success ".env 文件存在"
    return $true
}

# =============================================
# 设置环境变量
# =============================================
function Set-Environment {
    param([string]$TargetEnv)

    Write-Log "设置部署环境: $TargetEnv"

    $env:SPRING_PROFILES_ACTIVE = $TargetEnv

    if ([string]::IsNullOrEmpty($Version)) {
        $Version = "latest"
    }
    $env:APP_VERSION = $Version

    Write-Success "环境设置完成"
}

# =============================================
# 备份当前配置
# =============================================
function Backup-CurrentState {
    Write-Log "备份当前状态..."

    try {
        $containers = docker compose ps -q 2>$null
        if ($containers) {
            docker compose config > $BackupFile 2>$null
            Write-Success "当前配置已备份到 $BackupFile"
            return $true
        }
    }
    catch {
        Write-Warn "备份失败: $_"
    }
    return $false
}

# =============================================
# 构建镜像
# =============================================
function Build-Images {
    Write-Log "构建 Docker 镜像 (版本: $Version)..."

    docker compose build `
        --build-arg APP_VERSION=$Version `
        backend frontend

    if ($LASTEXITCODE -ne 0) {
        throw "镜像构建失败"
    }

    Write-Success "镜像构建完成"
}

# =============================================
# 启动服务
# =============================================
function Start-Services {
    Write-Log "启动服务..."

    docker compose up -d --remove-orphans

    if ($LASTEXITCODE -ne 0) {
        throw "服务启动失败"
    }

    Write-Success "服务启动命令已执行"
}

# =============================================
# 验证服务健康状态
# =============================================
function Test-ServiceHealth {
    param([int]$Timeout = 180)

    Write-Log "验证服务健康状态（超时: $Timeout 秒）..."

    $elapsed = 0
    $interval = 10

    while ($elapsed -lt $Timeout) {
        try {
            $health = docker inspect --format='{{.State.Health.Status}}' label-530-backend 2>$null

            if ($health -eq "healthy") {
                Write-Success "后端服务健康检查通过"

                try {
                    $dbHealth = docker inspect --format='{{.State.Health.Status}}' label-530-db 2>$null
                    if ($dbHealth -eq "healthy") {
                        Write-Success "数据库服务健康检查通过"
                    }
                }
                catch { }

                return $true
            }
            elseif ($health -eq "unhealthy") {
                Write-Warn "后端服务状态: unhealthy"
            }
            else {
                Write-Log "后端服务状态: $health (等待中...)"
            }
        }
        catch {
            Write-Log "等待服务启动... ($elapsed/$Timeout 秒)"
        }

        Start-Sleep -Seconds $interval
        $elapsed += $interval
    }

    throw "服务健康检查超时（${Timeout}秒）"
}

# =============================================
# 回滚机制
# =============================================
function Invoke-Rollback {
    Write-Warn "执行回滚操作..."

    if (Test-Path $BackupFile) {
        Write-Log "从备份恢复..."
        docker compose -f $BackupFile up -d --remove-orphans

        if ($LASTEXITCODE -eq 0) {
            Write-Success "回滚成功"
            return $true
        }
    }
    else {
        Write-Warn "没有找到备份文件，尝试使用现有配置重启..."
        docker compose restart

        if ($LASTEXITCODE -eq 0) {
            Write-Success "服务已重启"
            return $true
        }
    }

    Write-Error "回滚失败"
    return $false
}

# =============================================
# 回滚模式主流程
# =============================================
function Invoke-RollbackMode {
    Write-Log "===== 回滚模式 ====="

    if (-not (Test-Path $BackupFile)) {
        throw "没有找到备份文件 $BackupFile，无法回滚"
    }

    Backup-CurrentState
    Invoke-Rollback

    try {
        Test-ServiceHealth -Timeout 120
        Write-Success "回滚验证通过"
    }
    catch {
        Write-Error "回滚后服务仍然不健康，请手动排查"
        exit 1
    }
}

# =============================================
# 主部署流程
# =============================================
function Invoke-Deploy {
    Write-Log "============================================="
    Write-Log "  酒店管理系统部署脚本"
    Write-Log "  环境: $Env"
    Write-Log "  版本: $Version"
    Write-Log "============================================="

    try {
        Test-Prerequisites

        if (-not (Test-EnvFile)) {
            Write-Warn "请先配置 .env 文件后重新运行"
            exit 1
        }

        Set-Environment -TargetEnv $Env

        $hasBackup = Backup-CurrentState

        Build-Images
        Start-Services

        try {
            Test-ServiceHealth -Timeout 180
            Write-Success "部署成功！所有服务运行正常"
        }
        catch {
            Write-Error "部署失败: $_"

            if ($hasBackup) {
                Write-Warn "正在执行自动回滚..."
                if (Invoke-Rollback) {
                    Write-Warn "已回滚到上一版本"
                }
            }
            else {
                Write-Warn "没有可用的备份，无法自动回滚"
            }

            exit 1
        }

        Write-Log ""
        Write-Log "============================================="
        Write-Log "  部署完成！"
        Write-Log "  后端服务: http://localhost:8000"
        Write-Log "  前端页面: http://localhost:3000"
        Write-Log "  健康检查: http://localhost:8000/actuator/health"
        Write-Log "============================================="
    }
    catch {
        Write-Error "部署过程出错: $_"
        exit 1
    }
}

# =============================================
# 入口
# =============================================
if ($Rollback) {
    Invoke-RollbackMode
}
else {
    Invoke-Deploy
}
