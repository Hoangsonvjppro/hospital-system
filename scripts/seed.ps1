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
#   -Reset                  Chạy lại init_database.sql trước khi seed
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
$DbPass      = "123456"
$DbHost      = "localhost"
$DbPort      = "3306"

$SqlDir      = Join-Path $ProjectDir "src/main/resources/sql"
$SqlInit     = Join-Path $SqlDir "init_database.sql"
$SqlUsers    = Join-Path $SqlDir "seed_demo_users.sql"
$SqlTestData = Join-Path $SqlDir "seed_test_data.sql"

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

# ── Hàm chạy SQL file ────────────────────────────────────
function Invoke-SqlFile {
    param([string]$SqlFile, [string]$Description)

    if (-not (Test-Path $SqlFile)) {
        Write-Host "  ❌ Không tìm thấy file: $SqlFile" -ForegroundColor Red
        return $false
    }

    try {
        if ($MysqlMode -eq 'container') {
            Get-Content $SqlFile -Raw | & $ContainerCmd exec -i $DbContainer mysql -u $DbUser -p"$DbPass" 2>$null
        } else {
            Get-Content $SqlFile -Raw | mysql -h $DbHost -P $DbPort -u $DbUser -p"$DbPass" 2>$null
        }
        return $true
    } catch {
        return $false
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

# ── Bước 0 (tùy chọn): Reset database ────────────────────
if ($Reset) {
    Write-Host "[0/4] ⚠️  RESET DATABASE (xóa & tạo lại toàn bộ)..." -ForegroundColor Red
    if (Invoke-SqlFile $SqlInit "init_database") {
        Write-Host "  ✅ Reset database hoàn tất." -ForegroundColor Green
    } else {
        Write-Host "  ❌ Lỗi reset database!" -ForegroundColor Red
        exit 1
    }
    Write-Host ""
}

# ── Bước 1: Chạy SQL seed users ───────────────────────────
Write-Host "[1/4] Chạy SQL seed users (tạo user demo)..." -ForegroundColor Yellow
if (Invoke-SqlFile $SqlUsers "seed_demo_users") {
    Write-Host "  ✅ SQL seed users hoàn tất." -ForegroundColor Green
} else {
    Write-Host "  ❌ Lỗi: Không thể chạy SQL seed users." -ForegroundColor Red
    Write-Host "     Kiểm tra kết nối MySQL và database đã tồn tại." -ForegroundColor Red
    exit 1
}
Write-Host ""

# ── Bước 2: Chạy SQL seed test data ───────────────────────
Write-Host "[2/4] Chạy SQL seed test data (bệnh nhân, thuốc, bệnh án...)..." -ForegroundColor Yellow
if (Invoke-SqlFile $SqlTestData "seed_test_data") {
    Write-Host "  ✅ SQL seed test data hoàn tất." -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Lỗi seed test data (có thể dữ liệu đã tồn tại). Tiếp tục..." -ForegroundColor DarkYellow
}
Write-Host ""

# ── Bước 3: Compile project ───────────────────────────────
Write-Host "[3/4] Compile project..." -ForegroundColor Yellow
try {
    mvn compile -q -f "$ProjectDir/pom.xml" 2>$null
    Write-Host "  ✅ Compile thành công." -ForegroundColor Green
} catch {
    Write-Host "  ❌ Lỗi compile! Chạy 'mvn compile' để xem chi tiết." -ForegroundColor Red
    exit 1
}
Write-Host ""

# ── Bước 4: Chạy DataSeeder (cập nhật BCrypt hash) ────────
Write-Host "[4/4] Chạy DataSeeder (cập nhật password hash BCrypt)..." -ForegroundColor Yellow
mvn exec:java "-Dexec.mainClass=com.hospital.util.DataSeeder" -f "$ProjectDir/pom.xml" 2>$null
Write-Host ""

Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║   ✅ SEED HOÀN TẤT                                        ║" -ForegroundColor Green
Write-Host "║                                                            ║" -ForegroundColor Green
Write-Host "║   Dữ liệu test:                                           ║" -ForegroundColor Green
Write-Host "║   • 10 bệnh nhân  • 2 bác sĩ  • 15 loại thuốc            ║" -ForegroundColor Green
Write-Host "║   • 5 lịch hẹn    • 6 bệnh án • 3 đơn thuốc              ║" -ForegroundColor Green
Write-Host "║                                                            ║" -ForegroundColor Green
Write-Host "║   Tài khoản (password cho tất cả: password):               ║" -ForegroundColor Green
Write-Host "║   • admin          (Quản trị)                              ║" -ForegroundColor Green
Write-Host "║   • doctor         (Bác sĩ - Nội tổng quát)               ║" -ForegroundColor Green
Write-Host "║   • doctor2        (Bác sĩ - Nhi khoa)                    ║" -ForegroundColor Green
Write-Host "║   • letan          (Lễ tân)                                ║" -ForegroundColor Green
Write-Host "║   • ketoan         (Kế toán)                               ║" -ForegroundColor Green
Write-Host "║   • duocsi         (Dược sĩ)                               ║" -ForegroundColor Green
Write-Host "║                                                            ║" -ForegroundColor Green
Write-Host "║   Cách dùng:                                               ║" -ForegroundColor Green
Write-Host "║   • Seed thường:  pwsh scripts/seed.ps1                    ║" -ForegroundColor Green
Write-Host "║   • Ép dùng local: pwsh scripts/seed.ps1 -Mode local       ║" -ForegroundColor Green
Write-Host "║   • Reset toàn bộ: pwsh scripts/seed.ps1 -Reset            ║" -ForegroundColor Green
Write-Host "║   • Chạy app:      mvn exec:java                          ║" -ForegroundColor Green
Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Green
