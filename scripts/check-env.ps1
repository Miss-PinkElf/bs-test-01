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

Write-Host "检查开发环境..." -ForegroundColor Cyan

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
    Write-Host "基础环境不完整，无法启动项目。" -ForegroundColor Red
    exit 1
}

if (-not $mvnOk -and -not $mvnwOk) {
    Write-Host "未检测到 Maven 或 Maven Wrapper。后端建议用 IDEA 内置 Maven 或后续补 mvnw。" -ForegroundColor Yellow
}

Write-Host "环境检查完成。" -ForegroundColor Green
