param(
    [string]$DbHost = "localhost",
    [int]$DbPort = 3306,
    [string]$Username = "root",
    [string]$Password = "123456",
    [string]$Database = "grain_env_predict"
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$scriptRoot = $PSScriptRoot
$repoRoot = Resolve-Path (Join-Path $scriptRoot "..")
$schemaPath = Join-Path $repoRoot "backend\src\main\resources\db\schema.sql"

if (-not (Test-Path -LiteralPath $schemaPath)) {
    throw "Schema file was not found: $schemaPath"
}

$schemaContent = Get-Content -Raw -Encoding UTF8 -LiteralPath $schemaPath
$expectedDatabase = [regex]::Match($schemaContent, '(?im)^USE\s+([a-zA-Z0-9_]+);').Groups[1].Value
if ([string]::IsNullOrWhiteSpace($expectedDatabase)) {
    throw "Could not determine target database from schema.sql."
}

if ($Database -ne $expectedDatabase) {
    throw "reset-demo-db.ps1 targets '$expectedDatabase' because schema.sql hardcodes that database. Re-run with -Database $expectedDatabase or update schema.sql first."
}

$mysql = Get-Command mysql -ErrorAction SilentlyContinue
if ($null -eq $mysql) {
    throw "mysql CLI was not found in PATH. Install MySQL client tools first."
}

Write-Host "Resetting demo database from: $schemaPath" -ForegroundColor Cyan
Write-Host "Target: $Username@$DbHost`:$DbPort / $expectedDatabase" -ForegroundColor Cyan

$mysqlArgs = @(
    "--default-character-set=utf8mb4",
    "--host=$DbHost",
    "--port=$DbPort",
    "--user=$Username"
)

if ($Password -ne "") {
    $mysqlArgs += "--password=$Password"
}

$schemaContent | & $mysql.Source @mysqlArgs

if ($LASTEXITCODE -ne 0) {
    throw "mysql exited with code $LASTEXITCODE"
}

Write-Host "Demo database reset completed successfully." -ForegroundColor Green
