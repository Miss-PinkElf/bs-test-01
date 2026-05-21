param(
    [string]$DbHost = "localhost",
    [int]$DbPort = 3306,
    [string]$Username = "root",
    [string]$Password = "123456",
    [string]$Database = "grain_env_predict",
    [int]$PreviewLimit = 50,
    [string]$DirtyPattern = "答辩|用于展示|用于说明|重点巡检|主要风险样本|形成对照|故事线|重点风险走势|整体弱于风险仓"
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)

function Read-Confirmation {
    param([string]$Prompt)

    if ([Console]::IsInputRedirected) {
        Write-Host -NoNewline "${Prompt}: "
        $value = [Console]::In.ReadLine()
        Write-Host $value
        return $value
    }

    return Read-Host $Prompt
}

function Get-MySqlArgs {
    param(
        [string]$Query,
        [switch]$TableOutput,
        [switch]$SkipColumnNames
    )

    $args = @(
        "--host=$DbHost",
        "--port=$DbPort",
        "--user=$Username",
        "--database=$Database",
        "--default-character-set=utf8mb4",
        "--execute=$Query"
    )

    if ($Password -ne "") {
        $args += "--password=$Password"
    }

    if ($TableOutput) {
        $args += "--table"
    } else {
        $args += "--batch"
    }

    if ($SkipColumnNames) {
        $args += "--skip-column-names"
    }

    return $args
}

function Invoke-MySql {
    param(
        [string]$Query,
        [switch]$TableOutput,
        [switch]$SkipColumnNames
    )

    $args = Get-MySqlArgs -Query $Query -TableOutput:$TableOutput -SkipColumnNames:$SkipColumnNames
    & $script:mysqlPath @args

    if ($LASTEXITCODE -ne 0) {
        throw "mysql exited with code $LASTEXITCODE"
    }
}

function Escape-SqlLiteral {
    param([string]$Value)
    return $Value.Replace("\", "\\").Replace("'", "''")
}

$mysql = Get-Command mysql -ErrorAction SilentlyContinue
if ($null -eq $mysql) {
    throw "mysql CLI was not found in PATH. Install MySQL client tools first."
}
$script:mysqlPath = $mysql.Source

$pattern = Escape-SqlLiteral $DirtyPattern
$limit = [Math]::Max(1, $PreviewLimit)

$candidateRowsSql = @"
SELECT 'grain_temp_summary.warning_message' AS source_name,
       CAST(id AS CHAR) AS row_id,
       DATE_FORMAT(collected_at, '%Y-%m-%d %H:%i:%s') AS business_time,
       warning_message AS dirty_text
FROM grain_temp_summary
WHERE warning_message REGEXP '$pattern'
UNION ALL
SELECT 'grain_temp_summary.analysis_remark',
       CAST(id AS CHAR),
       DATE_FORMAT(collected_at, '%Y-%m-%d %H:%i:%s'),
       analysis_remark
FROM grain_temp_summary
WHERE analysis_remark REGEXP '$pattern'
UNION ALL
SELECT 'prediction_task.summary',
       CAST(id AS CHAR),
       DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s'),
       summary
FROM prediction_task
WHERE summary REGEXP '$pattern'
UNION ALL
SELECT 'prediction_result.warning_message',
       CAST(id AS CHAR),
       DATE_FORMAT(result_time, '%Y-%m-%d %H:%i:%s'),
       warning_message
FROM prediction_result
WHERE warning_message REGEXP '$pattern'
"@

$countSql = "SELECT COUNT(*) AS dirty_count FROM ($candidateRowsSql) dirty_rows;"
$countScalarSql = "SELECT COUNT(*) FROM ($candidateRowsSql) dirty_rows;"
$previewSql = "SELECT source_name, row_id, business_time, dirty_text FROM ($candidateRowsSql) dirty_rows ORDER BY source_name, business_time, row_id LIMIT $limit;"

Write-Host "Target database: $Username@$DbHost`:$DbPort / $Database" -ForegroundColor Cyan
Write-Host "Dirty pattern: $DirtyPattern" -ForegroundColor Cyan
Write-Host ""
Write-Host "Checking dirty text rows..." -ForegroundColor Cyan

Invoke-MySql -Query $countSql -TableOutput
$dirtyCountText = (Invoke-MySql -Query $countScalarSql -SkipColumnNames | Out-String).Trim()
$dirtyCount = [int]$dirtyCountText
if ($dirtyCount -eq 0) {
    Write-Host ""
    Write-Host "No dirty text rows found. Nothing to clean." -ForegroundColor Green
    exit 0
}

Write-Host ""
Invoke-MySql -Query $previewSql -TableOutput

Write-Host ""
$confirmation = Read-Confirmation "Input y to clean these rows"
if ($confirmation -ne "y") {
    Write-Host "Cleanup cancelled." -ForegroundColor Yellow
    exit 0
}

$cleanupSql = @"
START TRANSACTION;

UPDATE grain_temp_summary
SET warning_message = CASE
    WHEN warning_level IN ('ATTENTION', 'WARNING') THEN '最高粮温接近阈值，建议持续关注'
    ELSE NULL
END
WHERE warning_message REGEXP '$pattern';
SET @grain_summary_warning_rows = ROW_COUNT();

UPDATE grain_temp_summary
SET analysis_remark = CASE
    WHEN analysis_result = '粮温关注' THEN '粮温接近阈值'
    WHEN analysis_result = '轻微波动' THEN '粮温轻微波动'
    WHEN analysis_result = '维护观察' THEN '维护状态历史记录'
    ELSE '粮温整体平稳'
END
WHERE analysis_remark REGEXP '$pattern';
SET @grain_summary_remark_rows = ROW_COUNT();

UPDATE prediction_task
SET summary = CASE
    WHEN risk_level IN ('ATTENTION', 'WARNING') THEN '短期预测粮温接近阈值'
    ELSE '短期预测保持低风险'
END
WHERE summary REGEXP '$pattern';
SET @prediction_task_summary_rows = ROW_COUNT();

UPDATE prediction_result
SET warning_message = CASE
    WHEN warning_level IN ('ATTENTION', 'WARNING') THEN '预计粮温接近阈值，建议持续关注'
    ELSE NULL
END
WHERE warning_message REGEXP '$pattern';
SET @prediction_result_warning_rows = ROW_COUNT();

COMMIT;

SELECT 'grain_temp_summary.warning_message' AS target_name, @grain_summary_warning_rows AS affected_rows
UNION ALL
SELECT 'grain_temp_summary.analysis_remark', @grain_summary_remark_rows
UNION ALL
SELECT 'prediction_task.summary', @prediction_task_summary_rows
UNION ALL
SELECT 'prediction_result.warning_message', @prediction_result_warning_rows;
"@

Write-Host ""
Write-Host "Cleaning dirty text rows..." -ForegroundColor Cyan
Invoke-MySql -Query $cleanupSql -TableOutput

Write-Host ""
Write-Host "Verifying remaining dirty text rows..." -ForegroundColor Cyan
Invoke-MySql -Query $countSql -TableOutput

Write-Host ""
Write-Host "Cleanup finished." -ForegroundColor Green
