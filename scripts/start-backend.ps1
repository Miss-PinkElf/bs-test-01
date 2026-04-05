$ErrorActionPreference = "Stop"
$backendPath = Join-Path $PSScriptRoot "..\backend"
Set-Location $backendPath

if (Test-Path -LiteralPath ".\mvnw.cmd") {
    & ".\mvnw.cmd" spring-boot:run
    exit $LASTEXITCODE
}

$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if ($null -ne $mvn) {
    & $mvn.Source spring-boot:run
    exit $LASTEXITCODE
}

Write-Host "未找到 mvn 或 mvnw.cmd，无法直接启动后端。" -ForegroundColor Yellow
Write-Host "请安装 Maven，或在 IDEA 中打开 backend 并运行 GrainPlatformApplication。" -ForegroundColor Yellow
exit 1
