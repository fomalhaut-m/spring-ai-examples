#!/bin/bash
# Spring AI Wiki 环境启动脚本

echo "=========================================="
echo "  Spring AI Wiki - 启动本地环境"
echo "=========================================="
echo ""

# 检查参数
RESET=false
if [ "$1" = "--reset" ] || [ "$1" = "-r" ]; then
    RESET=true
fi

# 检查 Docker 是否运行
echo "1. 检查 Docker 状态..."
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker 未运行，请先启动 Docker Desktop"
    exit 1
fi
echo "✅ Docker 运行正常"
echo ""

# 清理数据卷（可选）
if [ "$RESET" = true ]; then
    echo "2. 清理旧数据（--reset 模式）..."
    docker-compose down -v 2>/dev/null
    echo "✅ 已清理数据卷"
    echo ""
fi

# 启动 Docker Compose 服务
echo "3. 启动 MongoDB 和 Chroma..."
docker-compose up -d

if [ $? -eq 0 ]; then
    echo "✅ 服务启动成功"
else
    echo "❌ 服务启动失败"
    exit 1
fi
echo ""

# 等待服务就绪
echo "4. 等待服务就绪..."
echo "   等待 MongoDB (10秒)..."
sleep 10

echo "   等待 Chroma (5秒)..."
sleep 5
echo ""

# 检查服务状态
echo "5. 检查服务状态..."
echo ""
echo " 容器状态:"
docker-compose ps
echo ""

# 检查 MongoDB
echo "🔴 MongoDB 连接测试..."
if docker exec spring-ai-mongodb mongosh --username admin --password admin --authenticationDatabase admin --eval "db.adminCommand('ping')" > /dev/null 2>&1; then
    echo "✅ MongoDB 连接正常"
else
    echo "⚠️ MongoDB 可能还在启动中，请稍后重试"
fi
echo ""

# 检查 Chroma
echo " Chroma 连接测试..."
if curl -s http://localhost:8000/api/v1/heartbeat > /dev/null 2>&1; then
    echo "✅ Chroma 连接正常"
else
    echo "⚠️ Chroma 可能还在启动中，请稍后重试"
fi
echo ""

# 显示连接信息
echo "=========================================="
echo "  环境信息"
echo "=========================================="
echo "🔴 MongoDB:"
echo "   - 主机: localhost:27017"
echo "   - 数据库: spring-ai-wiki"
echo "   - 用户名: admin"
echo "   - 密码: admin"
echo ""
echo " Chroma:"
echo "   - 主机: http://localhost:8000"
echo "   - 集合: spring-ai-wiki-collection"
echo ""
echo "=========================================="
echo "  常用命令"
echo "=========================================="
echo "查看日志: docker-compose logs -f"
echo "停止服务: docker-compose down"
echo "重启服务: docker-compose up -d"
echo "清理数据: ./start-env.sh --reset  (删除所有数据)"
echo ""
echo "=========================================="
echo "  启动 Spring Boot 应用"
echo "=========================================="
echo "mvn spring-boot:run"
echo ""
echo "或者设置环境变量后运行:"
echo "export MINIMAX_API_KEY=your-api-key"
echo "mvn spring-boot:run"
echo ""