$ErrorActionPreference = "Stop"
$frontendPath = Join-Path $PSScriptRoot "..\frontend"
Set-Location $frontendPath

if (-not (Test-Path -LiteralPath ".\node_modules")) {
    Write-Host "首次启动，正在执行 npm install ..." -ForegroundColor Cyan
    npm install
}

$env:VITE_API_BASE = "http://localhost:8081"
Write-Host "当前前端接口地址：$env:VITE_API_BASE" -ForegroundColor Cyan
npm run dev -- --host 0.0.0.0
