$ErrorActionPreference = "Stop"

function Test-Tool {
    param(
        [string]$CommandName
    )

    $command = Get-Command $CommandName -ErrorAction SilentlyContinue
    return $null -ne $command
}

function Get-ToolStatusText {
    param(
        [bool]$Available
    )

    if ($Available) {
        return "OK"
    }

    return "MISSING"
}

Write-Host "Checking development environment..." -ForegroundColor Cyan

$javaOk = Test-Tool "java"
$nodeOk = Test-Tool "node"
$npmOk = Test-Tool "npm"
$mvnOk = Test-Tool "mvn"
$mvnwOk = Test-Path -LiteralPath (Join-Path $PSScriptRoot "..\backend\mvnw.cmd")

Write-Host ("java: " + (Get-ToolStatusText -Available $javaOk))
Write-Host ("node: " + (Get-ToolStatusText -Available $nodeOk))
Write-Host ("npm:  " + (Get-ToolStatusText -Available $npmOk))
Write-Host ("mvn:  " + (Get-ToolStatusText -Available $mvnOk))
Write-Host ("mvnw: " + (Get-ToolStatusText -Available $mvnwOk))

if (-not $javaOk -or -not $nodeOk -or -not $npmOk) {
    Write-Host "Required runtime tools are missing. The project cannot start." -ForegroundColor Red
    exit 1
}

if (-not $mvnOk -and -not $mvnwOk) {
    Write-Host "Maven and Maven Wrapper were not detected. Use IDEA's Maven support or add mvnw." -ForegroundColor Yellow
}

Write-Host "Environment check completed." -ForegroundColor Green
