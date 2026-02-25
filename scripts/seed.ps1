# ============================================================
# seed.ps1 — Seed hoàn chỉnh database cho Phòng Mạch Tư
# Chạy: pwsh scripts/seed.ps1
# ============================================================

$ErrorActionPreference = "Stop"

$ScriptDir  = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectDir = Split-Path -Parent $ScriptDir

# ── Cấu hình ──────────────────────────────────────────────
$DbContainer = "clinic-mysql"
$DbUser      = "root"
$DbPass      = "123456"
$SqlFile     = Join-Path $ProjectDir "src/main/resources/sql/seed_demo_users.sql"

Write-Host ""
Write-Host "╔══════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   SEED DATABASE — Phòng Mạch Tư             ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# ── Bước 1: Chạy SQL seed ─────────────────────────────────
Write-Host "[1/3] Chạy SQL seed (tạo user demo trong DB)..." -ForegroundColor Yellow
try {
    Get-Content $SqlFile | podman exec -i $DbContainer mysql -u $DbUser -p"$DbPass" 2>$null
    Write-Host "  ✅ SQL seed hoàn tất." -ForegroundColor Green
} catch {
    Write-Host "  ❌ Lỗi: Không thể chạy SQL seed." -ForegroundColor Red
    Write-Host "     Kiểm tra container '$DbContainer' đang chạy: podman ps" -ForegroundColor Red
    exit 1
}
Write-Host ""

# ── Bước 2: Compile project ───────────────────────────────
Write-Host "[2/3] Compile project..." -ForegroundColor Yellow
try {
    mvn compile -q -f "$ProjectDir/pom.xml" 2>$null
    Write-Host "  ✅ Compile thành công." -ForegroundColor Green
} catch {
    Write-Host "  ❌ Lỗi compile! Chạy 'mvn compile' để xem chi tiết." -ForegroundColor Red
    exit 1
}
Write-Host ""

# ── Bước 3: Chạy DataSeeder (cập nhật BCrypt hash) ────────
Write-Host "[3/3] Chạy DataSeeder (cập nhật password hash BCrypt)..." -ForegroundColor Yellow
mvn exec:java "-Dexec.mainClass=com.hospital.util.DataSeeder" -f "$ProjectDir/pom.xml" 2>$null
Write-Host ""

Write-Host "╔══════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║   ✅ SEED HOÀN TẤT                          ║" -ForegroundColor Green
Write-Host "║   Chạy app:  mvn exec:java                  ║" -ForegroundColor Green
Write-Host "║   Login:     ketoan / password               ║" -ForegroundColor Green
Write-Host "╚══════════════════════════════════════════════╝" -ForegroundColor Green
