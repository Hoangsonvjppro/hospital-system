#!/usr/bin/env bash
# ============================================================
# seed.sh — Seed hoàn chỉnh database cho Phòng Mạch Tư
# Chạy: bash scripts/seed.sh
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# ── Cấu hình ──────────────────────────────────────────────
DB_CONTAINER="clinic-mysql"
DB_USER="root"
DB_PASS="123456"
SQL_FILE="$PROJECT_DIR/src/main/resources/sql/seed_demo_users.sql"

echo "╔══════════════════════════════════════════════╗"
echo "║   SEED DATABASE — Phòng Mạch Tư             ║"
echo "╚══════════════════════════════════════════════╝"
echo ""

# ── Bước 1: Chạy SQL seed ─────────────────────────────────
echo "[1/3] Chạy SQL seed (tạo user demo trong DB)..."
if ! podman exec -i "$DB_CONTAINER" mysql -u "$DB_USER" -p"$DB_PASS" < "$SQL_FILE" 2>/dev/null; then
    echo "  ❌ Lỗi: Không thể chạy SQL seed."
    echo "     Kiểm tra container '$DB_CONTAINER' đang chạy: podman ps"
    exit 1
fi
echo "  ✅ SQL seed hoàn tất."
echo ""

# ── Bước 2: Compile project ───────────────────────────────
echo "[2/3] Compile project..."
if ! mvn compile -q -f "$PROJECT_DIR/pom.xml" 2>/dev/null; then
    echo "  ❌ Lỗi compile! Chạy 'mvn compile' để xem chi tiết."
    exit 1
fi
echo "  ✅ Compile thành công."
echo ""

# ── Bước 3: Chạy DataSeeder (cập nhật BCrypt hash) ────────
echo "[3/3] Chạy DataSeeder (cập nhật password hash BCrypt)..."
mvn exec:java -Dexec.mainClass="com.hospital.util.DataSeeder" -f "$PROJECT_DIR/pom.xml" 2>/dev/null
echo ""

echo "╔══════════════════════════════════════════════╗"
echo "║   ✅ SEED HOÀN TẤT                          ║"
echo "║   Chạy app:  mvn exec:java                  ║"
echo "║   Login:     ketoan / password               ║"
echo "╚══════════════════════════════════════════════╝"
