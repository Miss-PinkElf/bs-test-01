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

Write-Host "Default startup does not rebuild the demo database automatically." -ForegroundColor Cyan
Write-Host "Run npm run reset-demo-db first if you need fresh demo data." -ForegroundColor Yellow

$projectMavenSettings = Join-Path $backendPath ".mvn\settings.xml"
$userMavenRepo = Join-Path $env:USERPROFILE ".m2\repository"
$projectMavenRepo = if (Test-Path -LiteralPath $userMavenRepo) { $userMavenRepo } else { Join-Path $backendPath ".m2\repository" }
if (-not (Test-Path -LiteralPath $projectMavenRepo)) {
    New-Item -ItemType Directory -Force -Path $projectMavenRepo | Out-Null
}
if (Test-Path -LiteralPath $projectMavenSettings) {
    Write-Host "Using project Maven settings: $projectMavenSettings" -ForegroundColor Cyan
}
Write-Host "Using project Maven repository: $projectMavenRepo" -ForegroundColor Cyan

$runArgs = @(
    "-Dmaven.repo.local=$projectMavenRepo",
    "clean",
    "spring-boot:run",
    "-Dspring-boot.run.jvmArguments=-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8",
    "-Dspring-boot.run.arguments=--server.port=8081"
)

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
