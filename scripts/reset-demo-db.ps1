param(
    [string]$DbHost = "localhost",
    [int]$DbPort = 3306,
    [string]$Username = "root",
    [string]$Password = "123456",
    [string]$Database = "grain_env_predict"
)

$ErrorActionPreference = "Stop"

$scriptRoot = $PSScriptRoot
$repoRoot = Resolve-Path (Join-Path $scriptRoot "..")
$schemaPath = Join-Path $repoRoot "backend\src\main\resources\db\schema.sql"

if (-not (Test-Path -LiteralPath $schemaPath)) {
    throw "Schema file was not found: $schemaPath"
}

$mysql = Get-Command mysql -ErrorAction SilentlyContinue
if ($null -eq $mysql) {
    throw "mysql CLI was not found in PATH. Install MySQL client tools first."
}

Write-Host "Resetting demo database from: $schemaPath" -ForegroundColor Cyan
Write-Host "Target: $Username@$DbHost`:$DbPort / $Database" -ForegroundColor Cyan

$mysqlArgs = @(
    "--default-character-set=utf8mb4",
    "--host=$DbHost",
    "--port=$DbPort",
    "--user=$Username"
)

if ($Password -ne "") {
    $mysqlArgs += "--password=$Password"
}

Get-Content -Raw -Encoding UTF8 $schemaPath | & $mysql.Source @mysqlArgs

if ($LASTEXITCODE -ne 0) {
    throw "mysql exited with code $LASTEXITCODE"
}

Write-Host "Demo database reset completed successfully." -ForegroundColor Green
