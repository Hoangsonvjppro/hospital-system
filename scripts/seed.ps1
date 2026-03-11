# ============================================================
# seed.ps1 — Seed hoàn chỉnh database cho Phòng Mạch Tư
# Chạy: pwsh scripts/seed.ps1
#
# Tự động phát hiện MySQL chạy trên container hay local:
#   - Container (podman/docker): dùng exec vào container
#   - Local: dùng lệnh mysql trực tiếp
#
# Tùy chọn:
#   -Mode local|container   Ép dùng MySQL local hoặc container
#   -Reset                  XÓA database và tạo lại toàn bộ
# ============================================================
param(
    [ValidateSet('auto','local','container')]
    [string]$Mode = 'auto',
    [switch]$Reset
)

$ErrorActionPreference = "Stop"

$ScriptDir  = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectDir = Split-Path -Parent $ScriptDir

# ── Cấu hình ──────────────────────────────────────────────
$DbContainer = "clinic-mysql"
$DbUser      = "root"
$DbPass      = ""
$DbName      = "clinic_management"
$DbHost      = "localhost"
$DbPort      = "3306"

$SqlDir      = Join-Path $ProjectDir "src/main/resources"
$SqlSchema   = Join-Path $SqlDir "schema.sql"
$SqlSeed     = Join-Path $SqlDir "seed.sql"

# ── Detect container runtime (podman hoặc docker) ────────
$ContainerCmd = $null
function Find-ContainerRuntime {
    if (Get-Command podman -ErrorAction SilentlyContinue) {
        $names = podman ps --format '{{.Names}}' 2>$null
        if ($names -contains $DbContainer) {
            $script:ContainerCmd = "podman"
            return
        }
    }
    if (Get-Command docker -ErrorAction SilentlyContinue) {
        $names = docker ps --format '{{.Names}}' 2>$null
        if ($names -contains $DbContainer) {
            $script:ContainerCmd = "docker"
            return
        }
    }
}

# ── Detect MySQL mode ─────────────────────────────────────
$MysqlMode = ""  # "container" hoặc "local"
function Find-MysqlMode {
    if ($Mode -eq 'local') {
        $script:MysqlMode = "local"
        return
    }
    if ($Mode -eq 'container') {
        Find-ContainerRuntime
        if (-not $ContainerCmd) {
            Write-Host "❌ Không tìm thấy container '$DbContainer' đang chạy." -ForegroundColor Red
            Write-Host "   Kiểm tra: podman ps  hoặc  docker ps" -ForegroundColor Red
            exit 1
        }
        $script:MysqlMode = "container"
        return
    }
    # Auto-detect: ưu tiên container, fallback local
    Find-ContainerRuntime
    if ($ContainerCmd) {
        $script:MysqlMode = "container"
    } elseif (Get-Command mysql -ErrorAction SilentlyContinue) {
        $script:MysqlMode = "local"
    } else {
        Write-Host "❌ Không tìm thấy MySQL!" -ForegroundColor Red
        Write-Host "   Cách 1: Chạy container:  podman compose up -d" -ForegroundColor Yellow
        Write-Host "   Cách 2: Cài MySQL local" -ForegroundColor Yellow
        exit 1
    }
}

# ── Hàm chạy SQL file (với UTF-8 charset) ────────────────
function Invoke-SqlFile {
    param([string]$SqlFile, [string]$Description)

    if (-not (Test-Path $SqlFile)) {
        Write-Host "  ❌ Không tìm thấy file: $SqlFile" -ForegroundColor Red
        return $false
    }

    try {
        # Đọc file với encoding UTF-8
        $content = Get-Content $SqlFile -Raw -Encoding UTF8
        if ($MysqlMode -eq 'container') {
            # --default-character-set=utf8mb4 để fix lỗi phông chữ tiếng Việt
            $content | & $ContainerCmd exec -i $DbContainer mysql --default-character-set=utf8mb4 -u $DbUser -p"$DbPass" 2>$null
        } else {
            $passArg = if ($DbPass) { "-p$DbPass" } else { $null }
            $args = @('--default-character-set=utf8mb4', '-h', $DbHost, '-P', $DbPort, '-u', $DbUser)
            if ($passArg) { $args += $passArg }
            $content | mysql @args 2>$null
        }
        return $true
    } catch {
        return $false
    }
}

# ── Hàm chạy SQL command (inline) ────────────────────────
function Invoke-SqlCmd {
    param([string]$Sql)
    try {
        if ($MysqlMode -eq 'container') {
            $Sql | & $ContainerCmd exec -i $DbContainer mysql --default-character-set=utf8mb4 -u $DbUser -p"$DbPass" 2>$null
        } else {
            $passArg = if ($DbPass) { "-p$DbPass" } else { $null }
            $sqlArgs = @('--default-character-set=utf8mb4', '-h', $DbHost, '-P', $DbPort, '-u', $DbUser)
            if ($passArg) { $sqlArgs += $passArg }
            $Sql | mysql @sqlArgs 2>$null
        }
    } catch {
        # ignore
    }
}

# ── Bắt đầu ──────────────────────────────────────────────
Find-MysqlMode

Write-Host ""
Write-Host "╔══════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   SEED DATABASE — Phòng Mạch Tư             ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "  MySQL mode : $MysqlMode" -ForegroundColor Gray
if ($MysqlMode -eq 'container') {
    Write-Host "  Runtime    : $ContainerCmd" -ForegroundColor Gray
    Write-Host "  Container  : $DbContainer" -ForegroundColor Gray
} else {
    Write-Host "  Host       : ${DbHost}:${DbPort}" -ForegroundColor Gray
}
Write-Host "  User       : $DbUser" -ForegroundColor Gray
Write-Host ""

# ── Bước 1: Drop & tạo lại database (nếu -Reset) ────────
if ($Reset) {
    Write-Host "[1/4] ⚠️  RESET DATABASE (xóa & tạo lại toàn bộ)..." -ForegroundColor Red
    Invoke-SqlCmd "DROP DATABASE IF EXISTS $DbName;"
    Write-Host "  ✅ Database cũ đã xóa." -ForegroundColor Green
} else {
    Write-Host "[1/4] Kiểm tra database..." -ForegroundColor Yellow
}
Write-Host ""

# ── Bước 2: Chạy schema.sql (tạo bảng) ───────────────────
Write-Host "[2/4] Chạy schema.sql (tạo/cập nhật bảng)..." -ForegroundColor Yellow
if (Invoke-SqlFile $SqlSchema "schema") {
    Write-Host "  ✅ Schema hoàn tất." -ForegroundColor Green
} else {
    Write-Host "  ❌ Lỗi tạo schema!" -ForegroundColor Red
    exit 1
}
Write-Host ""

# ── Bước 3: Chạy seed.sql (dữ liệu mẫu) ─────────────────
Write-Host "[3/4] Chạy seed.sql (bệnh nhân, thuốc, bệnh án...)..." -ForegroundColor Yellow
if (Invoke-SqlFile $SqlSeed "seed") {
    Write-Host "  ✅ Seed data hoàn tất." -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Lỗi seed data (có thể dữ liệu đã tồn tại). Tiếp tục..." -ForegroundColor DarkYellow
}
Write-Host ""

# ── Bước 4: Compile & chạy DataSeeder (BCrypt hash) ──────
Write-Host "[4/4] Compile & cập nhật password hash BCrypt..." -ForegroundColor Yellow
try {
    mvn compile -q -f "$ProjectDir/pom.xml" 2>$null
    mvn exec:java "-Dexec.mainClass=com.hospital.util.DataSeeder" -f "$ProjectDir/pom.xml" 2>$null
    Write-Host "  ✅ DataSeeder hoàn tất." -ForegroundColor Green
} catch {
    Write-Host "  ⚠️  Lỗi compile. Chạy 'mvn compile' để xem chi tiết." -ForegroundColor DarkYellow
}
Write-Host ""

Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║   ✅ SEED HOÀN TẤT                                        ║" -ForegroundColor Green
Write-Host "║                                                            ║" -ForegroundColor Green
Write-Host "║   Dữ liệu test:                                           ║" -ForegroundColor Green
Write-Host "║   • 10 bệnh nhân  • 2 bác sĩ  • 20 loại thuốc            ║" -ForegroundColor Green
Write-Host "║   • 5 lịch hẹn    • 6 bệnh án • 3 đơn thuốc              ║" -ForegroundColor Green
Write-Host "║                                                            ║" -ForegroundColor Green
Write-Host "║   Tài khoản (password cho tất cả: password):               ║" -ForegroundColor Green
Write-Host "║   • admin / doctor / doctor2 / letan / ketoan / duocsi     ║" -ForegroundColor Green
Write-Host "║                                                            ║" -ForegroundColor Green
Write-Host "║   Cách dùng:                                               ║" -ForegroundColor Green
Write-Host "║   • Seed thường:  pwsh scripts/seed.ps1                    ║" -ForegroundColor Green
Write-Host "║   • Ép dùng local: pwsh scripts/seed.ps1 -Mode local       ║" -ForegroundColor Green
Write-Host "║   • Reset toàn bộ: pwsh scripts/seed.ps1 -Reset            ║" -ForegroundColor Green
Write-Host "║   • Chạy app:      mvn exec:java                          ║" -ForegroundColor Green
Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Green
