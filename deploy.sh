#!/usr/bin/env bash
# meadow 一键部署（cachyos.lan）
# 前置：postgres/redis/rustfs 已在宿主机运行；.env 已配置（见下方说明）
# 用法：./deploy.sh
set -euo pipefail

cd "$(dirname "$0")"

# ── .env 检查 ──
if [[ ! -f .env ]]; then
  echo "❌ 缺少 .env，请先创建（参考：）"
  cat <<'EOF'
MEADOW_DB_URL=jdbc:postgresql://host.docker.internal:5432/meadow
MEADOW_DB_USER=username
MEADOW_DB_PASSWORD=password
MEADOW_JWT_SECRET=<随机长密钥>
MEADOW_ENCRYPTION_SECRET=<32位hex>
MEADOW_REDIS_HOST=host.docker.internal
MEADOW_REDIS_PASSWORD=<redis密码>
MEADOW_S3_HOST=host.docker.internal
MEADOW_S3_ACCESS_KEY=<s3 key>
MEADOW_S3_SECRET_KEY=<s3 secret>
MEADOW_RESEND_API_KEY=<resend key>
WEB_PORT=8080
EOF
  exit 1
fi

# ── 前端目录检查（nginx build context 引用 ../meadow-web）──
if [[ ! -d ../meadow-web ]]; then
  echo "❌ 缺少前端目录 ../meadow-web，请先 clone meadow-web"
  exit 1
fi

# ── 构建并启动 ──
echo "🚀 构建并启动 backend + frontend ..."
docker compose up -d --build

# ── 健康检查 ──
echo "⏳ 等待 backend 就绪 ..."
for i in $(seq 1 30); do
  if curl -sf http://127.0.0.1:23333/healthz >/dev/null 2>&1; then
    echo "✅ backend 就绪 (healthz ok)"
    break
  fi
  [[ $i -eq 30 ]] && { echo "❌ backend 未就绪，查看日志: docker compose logs backend"; exit 1; }
  sleep 2
done

# ── 状态 ──
echo ""
echo "✅ 部署完成："
echo "   backend : http://cachyos.lan:23333/healthz"
echo "   frontend: http://cachyos.lan:${WEB_PORT:-8080}"
echo ""
echo "查看日志: docker compose logs -f"
echo "停止:     docker compose down"
