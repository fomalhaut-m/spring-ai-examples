# Spring AI Wiki - 停止本地环境 (PowerShell)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Spring AI Wiki - 停止本地环境" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 停止 Docker Compose 服务
Write-Host "1. 停止 MongoDB 和 Chroma..." -ForegroundColor Yellow
docker compose down

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 服务已停止" -ForegroundColor Green
} else {
    Write-Host "❌ 停止服务失败" -ForegroundColor Red
}
Write-Host ""

Write-Host "提示：" -ForegroundColor Yellow
Write-Host "- 数据已保存在 Docker 卷中，下次启动会恢复" -ForegroundColor Gray
Write-Host "- 如需清除数据，使用: docker compose down -v" -ForegroundColor Gray
Write-Host ""
