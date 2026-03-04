#!/usr/bin/env bash
# ============================================================
# seed.sh — Seed hoàn chỉnh database cho Phòng Mạch Tư
# Chạy: bash scripts/seed.sh
#
# Tự động phát hiện MySQL chạy trên container hay local:
#   - Container (podman/docker): dùng exec vào container
#   - Local: dùng lệnh mysql trực tiếp
#
# Tùy chọn:
#   --local     Ép dùng MySQL local (bỏ qua detect container)
#   --container Ép dùng MySQL container
#   --reset     XÓA database và tạo lại toàn bộ (schema + seed)
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# ── Cấu hình ──────────────────────────────────────────────
DB_CONTAINER="clinic-mysql"
DB_USER="root"
DB_PASS="123456"
DB_NAME="clinic_management"
DB_HOST="localhost"
DB_PORT="3306"

SQL_DIR="$PROJECT_DIR/src/main/resources"
SQL_SCHEMA="$SQL_DIR/schema.sql"
SQL_SEED="$SQL_DIR/seed.sql"

# ── Parse arguments ───────────────────────────────────────
FORCE_MODE=""   # "", "local", "container"
DO_RESET=false
for arg in "$@"; do
    case "$arg" in
        --local)     FORCE_MODE="local" ;;
        --container) FORCE_MODE="container" ;;
        --reset)     DO_RESET=true ;;
        *) echo "⚠️  Tham số không hợp lệ: $arg"; exit 1 ;;
    esac
done

# ── Detect container runtime (podman hoặc docker) ────────
CONTAINER_CMD=""
detect_container_runtime() {
    if command -v podman &>/dev/null && podman ps --format '{{.Names}}' 2>/dev/null | grep -q "^${DB_CONTAINER}$"; then
        CONTAINER_CMD="podman"
    elif command -v docker &>/dev/null && docker ps --format '{{.Names}}' 2>/dev/null | grep -q "^${DB_CONTAINER}$"; then
        CONTAINER_CMD="docker"
    fi
}

# ── Detect MySQL mode ─────────────────────────────────────
MYSQL_MODE=""  # "container" hoặc "local"
detect_mysql() {
    if [[ "$FORCE_MODE" == "local" ]]; then
        MYSQL_MODE="local"
        return
    fi
    if [[ "$FORCE_MODE" == "container" ]]; then
        MYSQL_MODE="container"
        detect_container_runtime
        if [[ -z "$CONTAINER_CMD" ]]; then
            echo "❌ Không tìm thấy container '$DB_CONTAINER' đang chạy."
            echo "   Kiểm tra: podman ps  hoặc  docker ps"
            exit 1
        fi
        return
    fi
    # Auto-detect: ưu tiên container trước, fallback local
    detect_container_runtime
    if [[ -n "$CONTAINER_CMD" ]]; then
        MYSQL_MODE="container"
    elif command -v mysql &>/dev/null; then
        MYSQL_MODE="local"
    else
        echo "❌ Không tìm thấy MySQL!"
        echo "   Cách 1: Chạy container:  podman compose up -d  (hoặc docker compose up -d)"
        echo "   Cách 2: Cài MySQL local:  sudo apt install mysql-client"
        exit 1
    fi
}

# ── Hàm chạy SQL file (với UTF-8 charset) ────────────────
run_sql_file() {
    local sql_file="$1"
    local description="$2"

    if [[ ! -f "$sql_file" ]]; then
        echo "  ❌ Không tìm thấy file: $sql_file"
        return 1
    fi

    if [[ "$MYSQL_MODE" == "container" ]]; then
        # --default-character-set=utf8mb4 để fix lỗi phông chữ tiếng Việt
        $CONTAINER_CMD exec -i "$DB_CONTAINER" \
            mysql --default-character-set=utf8mb4 \
            -u "$DB_USER" -p"$DB_PASS" < "$sql_file" 2>/dev/null
    else
        mysql --default-character-set=utf8mb4 \
            -h "$DB_HOST" -P "$DB_PORT" \
            -u "$DB_USER" -p"$DB_PASS" < "$sql_file" 2>/dev/null
    fi
}

# ── Hàm chạy SQL command (inline) ────────────────────────
run_sql_cmd() {
    local sql="$1"
    if [[ "$MYSQL_MODE" == "container" ]]; then
        echo "$sql" | $CONTAINER_CMD exec -i "$DB_CONTAINER" \
            mysql --default-character-set=utf8mb4 \
            -u "$DB_USER" -p"$DB_PASS" 2>/dev/null
    else
        echo "$sql" | mysql --default-character-set=utf8mb4 \
            -h "$DB_HOST" -P "$DB_PORT" \
            -u "$DB_USER" -p"$DB_PASS" 2>/dev/null
    fi
}

# ── Bắt đầu ──────────────────────────────────────────────
detect_mysql

echo "╔══════════════════════════════════════════════╗"
echo "║   SEED DATABASE — Phòng Mạch Tư             ║"
echo "╚══════════════════════════════════════════════╝"
echo ""
echo "  MySQL mode : $MYSQL_MODE"
if [[ "$MYSQL_MODE" == "container" ]]; then
    echo "  Runtime    : $CONTAINER_CMD"
    echo "  Container  : $DB_CONTAINER"
else
    echo "  Host       : $DB_HOST:$DB_PORT"
fi
echo "  User       : $DB_USER"
echo ""

# ── Bước 1: Drop & tạo lại database (nếu --reset) ───────
if $DO_RESET; then
    echo "[1/4] ⚠️  RESET DATABASE (xóa & tạo lại toàn bộ)..."
    run_sql_cmd "DROP DATABASE IF EXISTS $DB_NAME;"
    echo "  ✅ Database cũ đã xóa."
else
    echo "[1/4] Kiểm tra database..."
fi
echo ""

# ── Bước 2: Chạy schema.sql (tạo bảng) ───────────────────
echo "[2/4] Chạy schema.sql (tạo/cập nhật bảng)..."
if run_sql_file "$SQL_SCHEMA" "schema"; then
    echo "  ✅ Schema hoàn tất."
else
    echo "  ❌ Lỗi tạo schema!"
    exit 1
fi
echo ""

# ── Bước 3: Chạy seed.sql (dữ liệu mẫu) ─────────────────
echo "[3/4] Chạy seed.sql (bệnh nhân, thuốc, bệnh án...)..."
if run_sql_file "$SQL_SEED" "seed"; then
    echo "  ✅ Seed data hoàn tất."
else
    echo "  ⚠️  Lỗi seed data (có thể dữ liệu đã tồn tại). Tiếp tục..."
fi
echo ""

# ── Bước 4: Compile & chạy DataSeeder (BCrypt hash) ──────
echo "[4/4] Compile & cập nhật password hash BCrypt..."
if mvn compile -q -f "$PROJECT_DIR/pom.xml" 2>/dev/null; then
    mvn exec:java -Dexec.mainClass="com.hospital.util.DataSeeder" -f "$PROJECT_DIR/pom.xml" 2>/dev/null
    echo "  ✅ DataSeeder hoàn tất."
else
    echo "  ⚠️  Lỗi compile. Chạy 'mvn compile' để xem chi tiết."
fi
echo ""

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║   ✅ SEED HOÀN TẤT                                        ║"
echo "║                                                            ║"
echo "║   Dữ liệu test:                                           ║"
echo "║   • 10 bệnh nhân  • 2 bác sĩ  • 20 loại thuốc            ║"
echo "║   • 5 lịch hẹn    • 6 bệnh án • 3 đơn thuốc              ║"
echo "║                                                            ║"
echo "║   Tài khoản (password cho tất cả: password):               ║"
echo "║   • admin / doctor / doctor2 / letan / ketoan / duocsi     ║"
echo "║                                                            ║"
echo "║   Cách dùng:                                               ║"
echo "║   • Seed thường:   bash scripts/seed.sh                   ║"
echo "║   • Ép dùng local: bash scripts/seed.sh --local            ║"
echo "║   • Reset toàn bộ: bash scripts/seed.sh --reset            ║"
echo "║   • Chạy app:      mvn exec:java                          ║"
echo "╚══════════════════════════════════════════════════════════════╝"
