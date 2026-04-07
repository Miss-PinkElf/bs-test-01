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

Write-Host "mvn or mvnw.cmd was not found, cannot start backend directly." -ForegroundColor Yellow
Write-Host "Please install Maven or run GrainPlatformApplication in IDEA." -ForegroundColor Yellow
exit 1
