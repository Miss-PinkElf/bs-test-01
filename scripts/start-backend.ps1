$ErrorActionPreference = "Stop"

function Clear-Port {
    param(
        [int]$Port
    )

    $connections = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue |
        Where-Object { $_.State -in @("Listen", "Established") } |
        Select-Object -ExpandProperty OwningProcess -Unique

    if (-not $connections) {
        Write-Host "端口 $Port 当前空闲。" -ForegroundColor Green
        return
    }

    foreach ($pid in $connections) {
        try {
            $process = Get-Process -Id $pid -ErrorAction Stop
            Write-Host "检测到端口 $Port 被进程占用，正在关闭 PID=$pid ($($process.ProcessName)) ..." -ForegroundColor Yellow
            Stop-Process -Id $pid -Force
            Write-Host "已释放端口 $Port。" -ForegroundColor Green
        }
        catch {
            Write-Host "关闭占用端口 $Port 的进程失败：PID=$pid" -ForegroundColor Red
            throw
        }
    }
}

Clear-Port -Port 8081

$backendPath = Join-Path $PSScriptRoot "..\backend"
Set-Location $backendPath

$runArgs = @("spring-boot:run", "-Dspring-boot.run.arguments=--server.port=8081")

if (Test-Path -LiteralPath ".\mvnw.cmd") {
    & ".\mvnw.cmd" @runArgs
    exit $LASTEXITCODE
}

$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if ($null -ne $mvn) {
    & $mvn.Source @runArgs
    exit $LASTEXITCODE
}

Write-Host "未找到 mvn 或 mvnw.cmd，无法直接启动后端。" -ForegroundColor Yellow
Write-Host "请安装 Maven，或在 IDEA 中打开 backend 并运行 GrainPlatformApplication。" -ForegroundColor Yellow
exit 1
