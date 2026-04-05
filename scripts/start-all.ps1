$ErrorActionPreference = "Stop"

$scriptRoot = $PSScriptRoot

& (Join-Path $scriptRoot "check-env.ps1")

Write-Host "准备启动前后端..." -ForegroundColor Cyan

$backendScript = Join-Path $scriptRoot "start-backend.ps1"
$frontendScript = Join-Path $scriptRoot "start-frontend.ps1"

Start-Process -FilePath "powershell" -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", $backendScript
Start-Process -FilePath "powershell" -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", $frontendScript

Write-Host "如果启动成功，可访问：" -ForegroundColor Green
Write-Host "前端: http://localhost:5173"
Write-Host "后端: http://localhost:8080"
Write-Host "如果后端窗口提示缺少 Maven，请改用 IDEA 直接运行 backend。" -ForegroundColor Yellow
