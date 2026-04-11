param(
    [string]$DbHost = "localhost",
    [int]$DbPort = 3306,
    [string]$Username = "root",
    [string]$Password = "123456",
    [string]$Database = "grain_env_predict"
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Write-Pass {
    param([string]$Message)
    Write-Host "[PASS] $Message" -ForegroundColor Green
}

function Assert-True {
    param(
        [bool]$Condition,
        [string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

function Invoke-MySqlScalar {
    param([string]$Query)

    $args = @(
        "--host=$DbHost",
        "--port=$DbPort",
        "--user=$Username",
        "--database=$Database",
        "--default-character-set=utf8mb4",
        "--batch",
        "--skip-column-names",
        "--execute=$Query"
    )

    if ($Password -ne "") {
        $args += "--password=$Password"
    }

    $result = & $script:mysqlPath @args
    if ($LASTEXITCODE -ne 0) {
        throw "mysql query failed: $Query"
    }

    return ($result | Out-String).Trim()
}

function Assert-Equals {
    param(
        [string]$Label,
        [string]$Actual,
        [string]$Expected
    )

    if ($Actual -ne $Expected) {
        throw "$Label failed. Expected '$Expected' but got '$Actual'."
    }

    Write-Pass "${Label}: $Actual"
}

$mysql = Get-Command mysql -ErrorAction SilentlyContinue
if ($null -eq $mysql) {
    throw "mysql CLI was not found in PATH. Install MySQL client tools first."
}
$script:mysqlPath = $mysql.Source

Write-Step "Verifying Phase 11 demo baseline in $Database"

Assert-Equals -Label "sensor_data row count" -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM sensor_data WHERE warehouse_id IN (1,2,6) AND metric_code IN ('humidity','co2');") -Expected "720"
Assert-Equals -Label "sensor_data distinct day count" -Actual (Invoke-MySqlScalar "SELECT COUNT(DISTINCT DATE(collected_at)) FROM sensor_data WHERE warehouse_id IN (1,2,6) AND metric_code IN ('humidity','co2');") -Expected "120"
Assert-Equals -Label "sensor_data Jan-Apr only" -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM sensor_data WHERE warehouse_id IN (1,2,6) AND metric_code IN ('humidity','co2') AND (collected_at < '2025-01-01 00:00:00' OR collected_at > '2025-04-30 23:59:59');") -Expected "0"
Assert-Equals -Label "sensor_data legacy September rows removed" -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM sensor_data WHERE collected_at >= '2025-09-01 00:00:00';") -Expected "0"
Assert-Equals -Label "sensor_data quality flags normalized" -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM sensor_data WHERE quality_flag <> 'NORMAL';") -Expected "0"

Write-Step "Checking grain temperature chain"
Assert-Equals -Label "grain_temp_record row count" -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM grain_temp_record;") -Expected "5760"
Assert-Equals -Label "grain_temp_summary row count" -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM grain_temp_summary;") -Expected "360"
Assert-Equals -Label "grain_temp legacy September rows removed" -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM grain_temp_record WHERE collected_at >= '2025-09-01 00:00:00';") -Expected "0"
Assert-Equals -Label "grain_temp quality flags normalized" -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM grain_temp_record WHERE quality_flag <> 'NORMAL';") -Expected "0"

foreach ($warehouseId in @(1, 2, 6)) {
    Assert-Equals -Label "warehouse $warehouseId grain summary days" -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM grain_temp_summary WHERE warehouse_id = $warehouseId;") -Expected "120"
}

$riskAlertCount = [int](Invoke-MySqlScalar "SELECT COUNT(*) FROM grain_temp_summary WHERE warehouse_id = 2 AND warning_flag = 1;")
Assert-True ($riskAlertCount -gt 0) "Risk warehouse does not contain any warning summaries in Jan-Apr baseline."
Write-Pass "risk warehouse warning summaries: $riskAlertCount"

$stableAlertCount = [int](Invoke-MySqlScalar "SELECT COUNT(*) FROM grain_temp_summary WHERE warehouse_id = 1 AND warning_flag = 1;")
Assert-Equals -Label "stable warehouse warning summaries" -Actual $stableAlertCount -Expected "0"

Write-Step "Checking prediction baseline"
Assert-Equals -Label "prediction_task row count" -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM prediction_task;") -Expected "3"
Assert-Equals -Label "legacy rolling task ids removed" -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM prediction_task WHERE task_no LIKE 'TASK-ROLLING-%';") -Expected "0"
Assert-Equals -Label "prediction train window anchored to Jan-Apr" -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM prediction_task WHERE train_start_time = '2025-01-01 00:00:00' AND train_end_time = '2025-04-30 23:59:59' AND based_on_actual_end_time = '2025-04-30 23:59:59';") -Expected "3"
Assert-Equals -Label "prediction forecast window starts after Apr baseline" -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM prediction_task WHERE forecast_start_time < '2025-05-01 00:00:00';") -Expected "0"
Assert-Equals -Label "prediction_result row count" -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM prediction_result;") -Expected "12"
Assert-Equals -Label "prediction_result legacy September rows removed" -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM prediction_result WHERE result_time >= '2025-09-01 00:00:00';") -Expected "0"

$baselineWindow = Invoke-MySqlScalar "SELECT CONCAT(MIN(collected_at), ' -> ', MAX(collected_at)) FROM grain_temp_summary;"
Write-Pass "grain_temp_summary window: $baselineWindow"
$predictionWindow = Invoke-MySqlScalar "SELECT CONCAT(MIN(forecast_start_time), ' -> ', MAX(forecast_end_time)) FROM prediction_task;"
Write-Pass "prediction window: $predictionWindow"

Write-Host ""
Write-Host "Phase 11 baseline verification passed." -ForegroundColor Green

