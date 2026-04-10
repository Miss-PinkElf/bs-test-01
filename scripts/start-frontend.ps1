$ErrorActionPreference = "Stop"

function Clear-Port {
    param(
        [int]$Port
    )

    $connections = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue |
        Where-Object { $_.State -in @("Listen", "Established") } |
        Select-Object -ExpandProperty OwningProcess -Unique

    if (-not $connections) {
        Write-Host "Port $Port is available." -ForegroundColor Green
        return
    }

    foreach ($processId in $connections) {
        try {
            $process = Get-Process -Id $processId -ErrorAction Stop
            Write-Host "Port $Port is occupied. Stopping PID=$processId ($($process.ProcessName)) ..." -ForegroundColor Yellow
            Stop-Process -Id $processId -Force
            Write-Host "Port $Port has been released." -ForegroundColor Green
        }
        catch {
            Write-Host "Failed to stop PID=$processId on port $Port." -ForegroundColor Red
            throw
        }
    }
}

Clear-Port -Port 5174

$frontendPath = Join-Path $PSScriptRoot "..\frontend"
Set-Location $frontendPath

if (-not (Test-Path -LiteralPath ".\node_modules")) {
    Write-Host "首次启动，正在执行 npm install ..." -ForegroundColor Cyan
    npm install
}

$env:VITE_API_BASE = "http://localhost:8081"
Write-Host "当前前端接口地址：$env:VITE_API_BASE" -ForegroundColor Cyan
npm run dev -- --host 0.0.0.0
