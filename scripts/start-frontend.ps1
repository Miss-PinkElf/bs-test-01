$ErrorActionPreference = "Stop"
$frontendPath = Join-Path $PSScriptRoot "..\frontend"
Set-Location $frontendPath

if (-not (Test-Path -LiteralPath ".\node_modules")) {
    Write-Host "首次启动，正在执行 npm install ..." -ForegroundColor Cyan
    npm install
}

npm run dev -- --host 0.0.0.0
