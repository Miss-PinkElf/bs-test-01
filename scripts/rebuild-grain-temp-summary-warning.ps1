param(
    [string]$DbHost = "localhost",
    [int]$DbPort = 3306,
    [string]$Username = "root",
    [string]$Password = "123456",
    [string]$Database = "grain_env_predict",
    [int]$PreviewLimit = 30
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

$mysql = Get-Command mysql -ErrorAction SilentlyContinue
if ($null -eq $mysql) {
    throw "mysql CLI was not found in PATH. Install MySQL client tools first."
}
$script:mysqlPath = $mysql.Source

$limit = [Math]::Max(1, $PreviewLimit)

$mismatchBaseSql = @"
SELECT
    s.id,
    w.warehouse_name,
    DATE_FORMAT(s.collected_at, '%Y-%m-%d %H:%i:%s') AS collected_at,
    s.max_temp,
    s.warning_level AS current_warning_level,
    CASE
        WHEN s.max_temp >= 28.00 THEN 'WARNING'
        WHEN s.max_temp >= 25.00 THEN 'ATTENTION'
        ELSE 'NORMAL'
    END AS expected_warning_level,
    s.warning_flag AS current_warning_flag,
    CASE
        WHEN s.max_temp >= 25.00 THEN 1
        ELSE 0
    END AS expected_warning_flag,
    COALESCE(s.warning_message, '') AS current_warning_message,
    CASE
        WHEN s.max_temp >= 28.00 THEN '检测到高温点，建议立即排查并通风降温'
        WHEN s.max_temp >= 25.00 THEN '最高粮温接近阈值，建议持续关注'
        ELSE ''
    END AS expected_warning_message,
    COALESCE(s.analysis_result, '') AS current_analysis_result,
    CASE
        WHEN s.max_temp >= 25.00 THEN '粮温关注'
        ELSE '粮温正常'
    END AS expected_analysis_result,
    COALESCE(s.analysis_remark, '') AS current_analysis_remark,
    CASE
        WHEN s.max_temp >= 28.00 THEN '检测到高温点，建议立即排查并通风降温'
        WHEN s.max_temp >= 25.00 THEN '粮温接近阈值'
        ELSE '粮温整体平稳'
    END AS expected_analysis_remark
FROM grain_temp_summary s
LEFT JOIN warehouse w ON w.id = s.warehouse_id
WHERE s.warning_level <> CASE
        WHEN s.max_temp >= 28.00 THEN 'WARNING'
        WHEN s.max_temp >= 25.00 THEN 'ATTENTION'
        ELSE 'NORMAL'
    END
    OR s.warning_flag <> CASE
        WHEN s.max_temp >= 25.00 THEN 1
        ELSE 0
    END
    OR COALESCE(s.warning_message, '') <> CASE
        WHEN s.max_temp >= 28.00 THEN '检测到高温点，建议立即排查并通风降温'
        WHEN s.max_temp >= 25.00 THEN '最高粮温接近阈值，建议持续关注'
        ELSE ''
    END
    OR COALESCE(s.analysis_result, '') <> CASE
        WHEN s.max_temp >= 25.00 THEN '粮温关注'
        ELSE '粮温正常'
    END
    OR COALESCE(s.analysis_remark, '') <> CASE
        WHEN s.max_temp >= 28.00 THEN '检测到高温点，建议立即排查并通风降温'
        WHEN s.max_temp >= 25.00 THEN '粮温接近阈值'
        ELSE '粮温整体平稳'
    END
"@

$countSql = "SELECT COUNT(*) AS mismatch_count FROM ($mismatchBaseSql) mismatch_rows;"
$countScalarSql = "SELECT COUNT(*) FROM ($mismatchBaseSql) mismatch_rows;"
$previewSql = "SELECT * FROM ($mismatchBaseSql) mismatch_rows ORDER BY collected_at DESC, id DESC LIMIT $limit;"

Write-Host "Target database: $Username@$DbHost`:$DbPort / $Database" -ForegroundColor Cyan
Write-Host "Rule: >=28 WARNING, >=25 ATTENTION, <25 NORMAL" -ForegroundColor Cyan
Write-Host ""
Write-Host "Checking mismatched summary rows..." -ForegroundColor Cyan

Invoke-MySql -Query $countSql -TableOutput
$mismatchCountText = (Invoke-MySql -Query $countScalarSql -SkipColumnNames | Out-String).Trim()
$mismatchCount = [int]$mismatchCountText

if ($mismatchCount -eq 0) {
    Write-Host ""
    Write-Host "No mismatched rows found. Nothing to rebuild." -ForegroundColor Green
    exit 0
}

Write-Host ""
Invoke-MySql -Query $previewSql -TableOutput

Write-Host ""
$confirmation = Read-Confirmation "Input y to rebuild these grain temp summary warnings"
if ($confirmation -ne "y") {
    Write-Host "Rebuild cancelled." -ForegroundColor Yellow
    exit 0
}

$updateSql = @"
START TRANSACTION;

UPDATE grain_temp_summary
SET warning_level = CASE
        WHEN max_temp >= 28.00 THEN 'WARNING'
        WHEN max_temp >= 25.00 THEN 'ATTENTION'
        ELSE 'NORMAL'
    END,
    warning_flag = CASE
        WHEN max_temp >= 25.00 THEN 1
        ELSE 0
    END,
    warning_message = CASE
        WHEN max_temp >= 28.00 THEN '检测到高温点，建议立即排查并通风降温'
        WHEN max_temp >= 25.00 THEN '最高粮温接近阈值，建议持续关注'
        ELSE NULL
    END,
    analysis_result = CASE
        WHEN max_temp >= 25.00 THEN '粮温关注'
        ELSE '粮温正常'
    END,
    analysis_remark = CASE
        WHEN max_temp >= 28.00 THEN '检测到高温点，建议立即排查并通风降温'
        WHEN max_temp >= 25.00 THEN '粮温接近阈值'
        ELSE '粮温整体平稳'
    END;

SET @affected_rows = ROW_COUNT();

COMMIT;

SELECT @affected_rows AS affected_rows;
"@

Write-Host ""
Write-Host "Rebuilding grain temp summary warnings..." -ForegroundColor Cyan
Invoke-MySql -Query $updateSql -TableOutput

Write-Host ""
Write-Host "Verifying remaining mismatched rows..." -ForegroundColor Cyan
Invoke-MySql -Query $countSql -TableOutput

Write-Host ""
Write-Host "Rebuild finished." -ForegroundColor Green
