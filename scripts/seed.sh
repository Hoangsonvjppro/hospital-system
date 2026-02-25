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
SQL_USERS="$PROJECT_DIR/src/main/resources/sql/seed_demo_users.sql"
SQL_TEST_DATA="$PROJECT_DIR/src/main/resources/sql/seed_test_data.sql"

echo "╔══════════════════════════════════════════════╗"
echo "║   SEED DATABASE — Phòng Mạch Tư             ║"
echo "╚══════════════════════════════════════════════╝"
echo ""

# ── Bước 1: Chạy SQL seed users ───────────────────────────
echo "[1/4] Chạy SQL seed users (tạo user demo trong DB)..."
if ! podman exec -i "$DB_CONTAINER" mysql -u "$DB_USER" -p"$DB_PASS" < "$SQL_USERS" 2>/dev/null; then
    echo "  ❌ Lỗi: Không thể chạy SQL seed users."
    echo "     Kiểm tra container '$DB_CONTAINER' đang chạy: podman ps"
    exit 1
fi
echo "  ✅ SQL seed users hoàn tất."
echo ""

# ── Bước 2: Chạy SQL seed test data ───────────────────────
echo "[2/4] Chạy SQL seed test data (bệnh nhân, thuốc, bệnh án...)..."
if ! podman exec -i "$DB_CONTAINER" mysql -u "$DB_USER" -p"$DB_PASS" < "$SQL_TEST_DATA" 2>/dev/null; then
    echo "  ⚠️  Lỗi seed test data (có thể dữ liệu đã tồn tại). Tiếp tục..."
fi
echo "  ✅ SQL seed test data hoàn tất."
echo ""

# ── Bước 3: Compile project ───────────────────────────────
echo "[3/4] Compile project..."
if ! mvn compile -q -f "$PROJECT_DIR/pom.xml" 2>/dev/null; then
    echo "  ❌ Lỗi compile! Chạy 'mvn compile' để xem chi tiết."
    exit 1
fi
echo "  ✅ Compile thành công."
echo ""

# ── Bước 4: Chạy DataSeeder (cập nhật BCrypt hash) ────────
echo "[4/4] Chạy DataSeeder (cập nhật password hash BCrypt)..."
mvn exec:java -Dexec.mainClass="com.hospital.util.DataSeeder" -f "$PROJECT_DIR/pom.xml" 2>/dev/null
echo ""

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║   ✅ SEED HOÀN TẤT                                        ║"
echo "║                                                            ║"
echo "║   Dữ liệu test:                                           ║"
echo "║   • 10 bệnh nhân  • 2 bác sĩ  • 15 loại thuốc            ║"
echo "║   • 5 lịch hẹn    • 5 bệnh án • 3 đơn thuốc              ║"
echo "║                                                            ║"
echo "║   Tài khoản:                                               ║"
echo "║   • admin / password      (Quản trị)                      ║"
echo "║   • doctor / password     (Bác sĩ)                        ║"
echo "║   • doctor2 / password    (Bác sĩ Nhi)                    ║"
echo "║   • letan / password      (Lễ tân)                        ║"
echo "║   • ketoan / password     (Kế toán)                       ║"
echo "║                                                            ║"
echo "║   Chạy app:  mvn exec:java                                ║"
echo "╚══════════════════════════════════════════════════════════════╝"
