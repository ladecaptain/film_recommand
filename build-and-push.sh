#!/bin/bash
# === 构建并推送 Docker 镜像到仓库 ===
# 使用前请修改 .env.docker 中的 REGISTRY 配置

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# 加载配置
if [ -f .env.docker ]; then
  source .env.docker
fi

if [ -z "$REGISTRY" ] || [ "$REGISTRY" = "registry.cn-hangzhou.aliyuncs.com/your-namespace" ]; then
  echo "❌ 请先编辑 .env.docker，将 REGISTRY 改为你的仓库地址"
  echo "   阿里云示例：registry.cn-hangzhou.aliyuncs.com/你的命名空间"
  echo "   Docker Hub 示例：docker.io/你的用户名"
  exit 1
fi

echo "📦 构建镜像..."
echo "   Registry: $REGISTRY"
echo "   Tag:      ${TAG:-latest}"
echo ""

# 构建
docker compose build

# 打标签
docker tag film-recommend-backend-backend:latest   "$REGISTRY/film-backend:${TAG:-latest}"
docker tag film-recommend-frontend-frontend:latest "$REGISTRY/film-frontend:${TAG:-latest}"

echo ""
echo "📤 推送镜像..."

docker push "$REGISTRY/film-backend:${TAG:-latest}"
docker push "$REGISTRY/film-frontend:${TAG:-latest}"

echo ""
echo "✅ 推送完成！"
echo ""
echo "服务器上运行："
echo "  1. 把 docker-compose.prod.yml 和 schema.sql 复制到服务器"
echo "  2. 创建 .env 文件："
echo "     REGISTRY=$REGISTRY"
echo "     TAG=${TAG:-latest}"
echo "     DB_PASSWORD=你的数据库密码"
echo "     JWT_SECRET=你的JWT密钥"
echo "     TMDB_ACCESS_TOKEN=你的TMDb令牌"
echo "  3. docker compose -f docker-compose.prod.yml up -d"
