param(
    [string]$ApiBase = "http://127.0.0.1:8081",
    [switch]$SkipReset,
    [switch]$SkipStaticChecks,
    [switch]$KeepBackendRunning
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$backendDir = Join-Path $repoRoot "backend"
$frontendDir = Join-Path $repoRoot "frontend"
$resetScript = Join-Path $PSScriptRoot "reset-demo-db.ps1"
$verifyBaselineScript = Join-Path $PSScriptRoot "verify-demo-baseline.ps1"
$startBackendScript = Join-Path $PSScriptRoot "start-backend.ps1"
$uri = [Uri]$ApiBase
$port = $uri.Port
$tempDir = Join-Path ([System.IO.Path]::GetTempPath()) ("grain-acceptance-smoke-" + [Guid]::NewGuid().ToString("N"))
$backendStdout = Join-Path $tempDir "backend.stdout.log"
$backendStderr = Join-Path $tempDir "backend.stderr.log"
$grainTemplatePath = Join-Path $tempDir "grain-temp-fixed-template.xlsx"
$legacyCsvPath = Join-Path $tempDir "grain-temp-legacy.csv"
$legacyExcelPath = Join-Path $tempDir "grain-temp-legacy.xlsx"
$backendLauncher = $null
$createdUserId = $null
$userMavenRepo = Join-Path $env:USERPROFILE ".m2\repository"
$mavenRepo = if (Test-Path -LiteralPath $userMavenRepo) { $userMavenRepo } else { Join-Path $backendDir ".m2\repository" }

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

function Get-ShellPath {
    $pwsh = Get-Command pwsh -ErrorAction SilentlyContinue
    if ($null -ne $pwsh) {
        return $pwsh.Source
    }

    $powershell = Get-Command powershell -ErrorAction SilentlyContinue
    if ($null -ne $powershell) {
        return $powershell.Source
    }

    throw "Neither pwsh nor powershell was found in PATH."
}

function Invoke-ExternalCommand {
    param(
        [string]$Command,
        [string[]]$Arguments,
        [string]$WorkingDirectory,
        [string]$Label
    )

    Push-Location $WorkingDirectory
    try {
        & $Command @Arguments
        if ($LASTEXITCODE -ne 0) {
            throw "$Label failed with exit code $LASTEXITCODE"
        }
    }
    finally {
        Pop-Location
    }
}

function Invoke-ApiRequest {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body
    )

    $requestUri = if ($Path.StartsWith("http")) { $Path } else { "$ApiBase$Path" }
    if ($PSBoundParameters.ContainsKey("Body")) {
        return Invoke-RestMethod -Uri $requestUri -Method $Method -ContentType "application/json" -Body ($Body | ConvertTo-Json -Depth 8)
    }

    return Invoke-RestMethod -Uri $requestUri -Method $Method
}

function Assert-ApiSuccess {
    param(
        [object]$Response,
        [string]$Context
    )

    Assert-True ($null -ne $Response) "$Context did not return a response."
    Assert-True ($Response.code -eq 200) "$Context returned code $($Response.code)."
    return $Response.data
}

function Invoke-FileUpload {
    param(
        [string]$RequestUri,
        [string]$FilePath
    )

    Add-Type -AssemblyName System.Net.Http

    $client = [System.Net.Http.HttpClient]::new()
    $form = [System.Net.Http.MultipartFormDataContent]::new()
    $stream = [System.IO.File]::OpenRead($FilePath)
    $fileContent = [System.Net.Http.StreamContent]::new($stream)
    $fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("application/octet-stream")
    $form.Add($fileContent, "file", [System.IO.Path]::GetFileName($FilePath))

    try {
        $response = $client.PostAsync($RequestUri, $form).GetAwaiter().GetResult()
        $raw = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
        Assert-True $response.IsSuccessStatusCode "File upload failed: $($response.StatusCode) $raw"
        return ($raw | ConvertFrom-Json)
    }
    finally {
        $fileContent.Dispose()
        $stream.Dispose()
        $form.Dispose()
        $client.Dispose()
    }
}

function Wait-BackendReady {
    param([int]$TimeoutSeconds = 90)

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-ApiRequest -Method "GET" -Path "/api/users"
            if ($response.code -eq 200) {
                return
            }
        }
        catch {
        }

        Start-Sleep -Seconds 2
    }

    $stdout = if (Test-Path -LiteralPath $backendStdout) { Get-Content -LiteralPath $backendStdout -Tail 120 | Out-String } else { "" }
    $stderr = if (Test-Path -LiteralPath $backendStderr) { Get-Content -LiteralPath $backendStderr -Tail 120 | Out-String } else { "" }
    throw "Backend did not become ready on $ApiBase.`nSTDOUT:`n$stdout`nSTDERR:`n$stderr"
}

function Stop-Backend {
    $listenerPids = @(Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique)

    foreach ($processId in $listenerPids) {
        try {
            Stop-Process -Id $processId -Force -ErrorAction Stop
        }
        catch {
        }
    }

    if ($null -ne $backendLauncher -and -not $backendLauncher.HasExited) {
        try {
            Stop-Process -Id $backendLauncher.Id -Force -ErrorAction Stop
        }
        catch {
        }
    }
}

function Convert-ToDateText {
    param([object]$Value)

    if ($Value -is [DateTime]) {
        return $Value.ToString('yyyy-MM-dd HH:mm:ss')
    }

    return [string]$Value
}

function New-LegacyGrainCsv {
    param(
        [string]$FilePath,
        [string]$CollectedAt
    )

    $lines = @(
        "warehouseId,collectedAt,zoneCode,layerNo,pointNo,temperatureValue,probeCode,remark",
        "1,$CollectedAt,A,1,1,24.3,CABLE-A,legacy smoke row 1",
        "1,$CollectedAt,A,1,2,24.5,CABLE-A,legacy smoke row 2",
        "1,$CollectedAt,A,2,1,24.8,CABLE-A,legacy smoke row 3"
    )
    [System.IO.File]::WriteAllLines($FilePath, $lines, [System.Text.UTF8Encoding]::new($false))
}

function New-LegacyGrainExcel {
    param(
        [string]$FilePath,
        [string]$CollectedAt
    )

    Add-Type -AssemblyName System.IO.Compression
    Add-Type -AssemblyName System.IO.Compression.FileSystem

    $rows = @(
        @("warehouseId", "collectedAt", "zoneCode", "layerNo", "pointNo", "temperatureValue", "probeCode", "remark"),
        @("1", $CollectedAt, "A", "1", "1", "24.3", "CABLE-A", "legacy excel row 1"),
        @("1", $CollectedAt, "A", "1", "2", "24.5", "CABLE-A", "legacy excel row 2"),
        @("1", $CollectedAt, "A", "2", "1", "24.8", "CABLE-A", "legacy excel row 3")
    )

    $columns = @("A", "B", "C", "D", "E", "F", "G", "H")
    $sheetRows = foreach ($rowIndex in 0..($rows.Count - 1)) {
        $excelRow = $rowIndex + 1
        $cells = foreach ($columnIndex in 0..($rows[$rowIndex].Count - 1)) {
            $cellRef = "$($columns[$columnIndex])$excelRow"
            $cellValue = [System.Security.SecurityElement]::Escape([string]$rows[$rowIndex][$columnIndex])
            ('<c r="{0}" t="inlineStr"><is><t>{1}</t></is></c>' -f $cellRef, $cellValue)
        }
        ('<row r="{0}">{1}</row>' -f $excelRow, ($cells -join ''))
    }

    $entries = @{
        '[Content_Types].xml' = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>'
        '_rels/.rels' = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>'
        'xl/workbook.xml' = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Sheet1" sheetId="1" r:id="rId1"/></sheets></workbook>'
        'xl/_rels/workbook.xml.rels' = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>'
        'xl/worksheets/sheet1.xml' = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>' + ($sheetRows -join '') + '</sheetData></worksheet>'
    }

    if (Test-Path -LiteralPath $FilePath) {
        Remove-Item -LiteralPath $FilePath -Force
    }

    $archive = [System.IO.Compression.ZipFile]::Open($FilePath, [System.IO.Compression.ZipArchiveMode]::Create)
    try {
        foreach ($entryPath in $entries.Keys) {
            $entry = $archive.CreateEntry($entryPath)
            $writer = [System.IO.StreamWriter]::new($entry.Open(), [System.Text.UTF8Encoding]::new($false))
            try {
                $writer.Write($entries[$entryPath])
            }
            finally {
                $writer.Dispose()
            }
        }
    }
    finally {
        $archive.Dispose()
    }
}

function Assert-OverviewFields {
    param([object]$Overview)

    $requiredFields = @(
        "warehouseCount",
        "grainSummaryCount",
        "realAlertCount",
        "predictionAlertCount",
        "archivedPredictionCount",
        "latestAlerts",
        "latestGrainSummaries",
        "warehouseHealthList"
    )

    foreach ($field in $requiredFields) {
        Assert-True ($Overview.PSObject.Properties.Name -contains $field) "Dashboard overview is missing field '$field'."
    }
}

function Assert-SummaryContainsCollectedAt {
    param(
        [object[]]$Summaries,
        [string]$CollectedAt,
        [string]$Context
    )

    $matched = @($Summaries | Where-Object { (Convert-ToDateText $_.collectedAt) -eq $CollectedAt })
    Assert-True ($matched.Count -ge 1) "$Context was not found in grain summaries: $CollectedAt"
}

New-Item -ItemType Directory -Path $tempDir -Force | Out-Null
if (-not (Test-Path -LiteralPath $mavenRepo)) {
    New-Item -ItemType Directory -Path $mavenRepo -Force | Out-Null
}

try {
    if (-not $SkipStaticChecks) {
        Write-Step "Running backend compile"
        $mvn = Get-Command mvn -ErrorAction Stop
        Invoke-ExternalCommand -Command $mvn.Source -Arguments @("-Dmaven.repo.local=$mavenRepo", "-q", "-DskipTests", "compile") -WorkingDirectory $backendDir -Label "backend compile"
        Write-Pass "Backend compile passed"

        Write-Step "Running frontend build"
        Push-Location $frontendDir
        try {
            npm run build
            if ($LASTEXITCODE -ne 0) {
                throw "frontend build failed with exit code $LASTEXITCODE"
            }
        }
        finally {
            Pop-Location
        }
        Write-Pass "Frontend build passed"
    }

    if (-not $SkipReset) {
        Write-Step "Resetting demo database"
        $npm = Get-Command npm -ErrorAction Stop
        Invoke-ExternalCommand -Command $npm.Source -Arguments @("run", "reset-demo-db") -WorkingDirectory $repoRoot -Label "demo database reset"
        Write-Pass "Demo database reset passed"

        Write-Step "Verifying Phase 11 baseline"
        $shellPath = Get-ShellPath
        Invoke-ExternalCommand -Command $shellPath -Arguments @("-ExecutionPolicy", "Bypass", "-File", $verifyBaselineScript) -WorkingDirectory $repoRoot -Label "Phase 11 baseline verification"
        Write-Pass "Phase 11 baseline verification passed"
    }

    Write-Step "Starting backend on port $port"
    $backendLauncher = Start-Process -FilePath (Get-ShellPath) `
        -ArgumentList @("-ExecutionPolicy", "Bypass", "-File", $startBackendScript) `
        -WorkingDirectory $repoRoot `
        -RedirectStandardOutput $backendStdout `
        -RedirectStandardError $backendStderr `
        -PassThru `
        -WindowStyle Hidden
    Wait-BackendReady
    Write-Pass "Backend is ready"

    Write-Step "Checking Phase 11 baseline APIs"
    $sensorHumidityPage = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/sensor-data?warehouseId=2&metricCode=humidity&pageNum=1&pageSize=150") "sensor humidity baseline"
    Assert-True ($sensorHumidityPage.total -eq 120) "Sensor humidity baseline should expose 120 Jan-Apr rows for warehouse 2."
    Assert-True (@($sensorHumidityPage.list | Where-Object { (Convert-ToDateText $_.collectedAt) -like '2026-09*' }).Count -eq 0) "Sensor humidity baseline still contains September rows."
    Assert-True (@($sensorHumidityPage.list | Where-Object { (Convert-ToDateText $_.collectedAt) -like '2026-04*' }).Count -ge 1) "Sensor humidity baseline does not surface April rows."

    $sensorTrend = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/sensor-data/trend?warehouseId=6&metricCode=co2") "sensor co2 trend baseline"
    Assert-True (@($sensorTrend.points).Count -eq 120) "Sensor trend baseline should expose 120 Jan-Apr points for warehouse 6 co2."
    Assert-True (@($sensorTrend.points | Where-Object { (Convert-ToDateText $_.time) -like '2026-09*' }).Count -eq 0) "Sensor trend baseline still contains September points."

    $grainSummaries = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/grain-temp/summaries?warehouseId=2") "grain summary baseline"
    Assert-True (@($grainSummaries).Count -eq 120) "Grain summary baseline should expose 120 Jan-Apr rows for warehouse 2."
    Assert-True (@($grainSummaries | Where-Object { (Convert-ToDateText $_.collectedAt) -like '2026-09*' }).Count -eq 0) "Grain summary baseline still contains September rows."
    Assert-True (@($grainSummaries | Where-Object { $_.warningFlag -eq $true }).Count -ge 1) "Risk warehouse grain summaries should contain alert rows near April end."

    $phase11Tasks = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/predictions/tasks") "Phase 11 prediction task list"
    Assert-True (@($phase11Tasks).Count -eq 3) "Prediction task archive should contain exactly three Phase 11 tasks after reset."
    Assert-True (@($phase11Tasks | Where-Object { $_.taskNo -like 'TASK-ROLLING-*' }).Count -eq 0) "Legacy rolling prediction tasks are still present after reset."
    Assert-True (@($phase11Tasks | Where-Object { (Convert-ToDateText $_.forecastStartTime) -notlike '2026-05*' }).Count -eq 0) "Prediction tasks are not anchored to post-Apr baseline forecast windows."
    $riskTask = @($phase11Tasks | Where-Object { $_.warehouseId -eq 2 })[0]
    Assert-True ($null -ne $riskTask) "Phase 11 prediction archive is missing the warehouse 2 risk task."
    $riskTaskDetail = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/predictions/tasks/$($riskTask.taskId)") "Phase 11 risk task detail"
    Assert-True ((Convert-ToDateText $riskTaskDetail.trainEndTime) -like '2026-04-30*') "Risk task is not trained against the Jan-Apr baseline."
    $futureRiskPoints = @($riskTaskDetail.resultList | Where-Object { $_.phaseType -eq 'FUTURE' })
    Assert-True ($futureRiskPoints.Count -ge 5) "Risk task detail should retain May future prediction points."
    Assert-True (@($futureRiskPoints | Where-Object { (Convert-ToDateText $_.resultTime) -notlike '2026-05*' }).Count -eq 0) "Risk task future predictions still contain non-May results."
    Write-Pass "Phase 11 baseline API checks passed"

    Write-Step "Checking dashboard overview"
    $overviewBefore = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/dashboard/overview") "dashboard overview"
    Assert-OverviewFields -Overview $overviewBefore
    Assert-True ([int]$overviewBefore.realAlertCount -ge 1) "Dashboard overview realAlertCount should be positive after reset."
    Assert-True ([int]$overviewBefore.predictionAlertCount -ge 1) "Dashboard overview predictionAlertCount should be positive after reset."
    $alertPage = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/dashboard/alerts?pageNum=1&pageSize=10") "dashboard alerts page"
    Assert-True (@($alertPage.list).Count -ge 1) "Dashboard alerts page is empty."
    $alertSourceTypes = @($alertPage.list | ForEach-Object { $_.sourceType })
    Assert-True ($alertSourceTypes -contains "REAL") "Dashboard alerts page does not include REAL alerts."
    Assert-True ($alertSourceTypes -contains "PREDICTION") "Dashboard alerts page does not include PREDICTION alerts."
    $grainSummaryCountBefore = [int]$overviewBefore.grainSummaryCount
    Write-Pass "Dashboard overview passed"

    Write-Step "Checking prediction archive read APIs"
    $taskList = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/predictions/tasks") "prediction task list"
    Assert-True (@($taskList).Count -ge 1) "Prediction task list is empty."
    $firstTaskId = [long]$taskList[0].taskId
    $taskDetail = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/predictions/tasks/$firstTaskId") "prediction task detail"
    Assert-True ($taskDetail.taskId -eq $firstTaskId) "Prediction task detail does not match the requested task id."
    Write-Pass "Prediction archive checks passed"

    Write-Step "Running user CRUD smoke"
    $username = "acceptance_smoke_" + [DateTimeOffset]::Now.ToUnixTimeSeconds()
    $createdUser = Assert-ApiSuccess (Invoke-ApiRequest -Method "POST" -Path "/api/users" -Body @{
        username = $username
        password = "654321"
        displayName = "Acceptance Smoke User"
        phone = "13900009999"
        warehouseId = 1
        status = "ACTIVE"
        roleCodes = @("VIEWER")
    }) "create user"
    $createdUserId = [long]$createdUser.id

    Assert-ApiSuccess (Invoke-ApiRequest -Method "PUT" -Path "/api/users/$createdUserId" -Body @{
        displayName = "Acceptance Smoke User Updated"
        phone = "13900008888"
        warehouseId = 2
        status = "ACTIVE"
        roleCodes = @("VIEWER", "WAREHOUSE_MANAGER")
    }) "update user" | Out-Null

    Assert-ApiSuccess (Invoke-ApiRequest -Method "PUT" -Path "/api/users/$createdUserId/password" -Body @{
        newPassword = "abcdef"
    }) "reset user password" | Out-Null

    $users = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/users") "user list"
    $matchedUser = @($users | Where-Object { $_.id -eq $createdUserId })
    Assert-True ($matchedUser.Count -eq 1) "Created user was not found in user list."
    Assert-True ($matchedUser[0].displayName -eq "Acceptance Smoke User Updated") "Updated display name was not persisted."
    Assert-True ([long]$matchedUser[0].warehouseId -eq 2) "Updated warehouseId was not persisted."
    Assert-True ($matchedUser[0].phone -eq "13900008888") "Updated phone was not persisted."

    Assert-ApiSuccess (Invoke-ApiRequest -Method "DELETE" -Path "/api/users/$createdUserId") "delete user" | Out-Null
    $createdUserId = $null

    $usersAfterDelete = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/users") "user list after delete"
    Assert-True (@($usersAfterDelete | Where-Object { $_.username -eq $username }).Count -eq 0) "Temporary user still exists after delete."
    Write-Pass "User CRUD smoke passed"

    Write-Step "Downloading fixed grain template"
    Invoke-WebRequest -UseBasicParsing -Uri "$ApiBase/api/grain-temp/import/template" -OutFile $grainTemplatePath | Out-Null
    Assert-True (Test-Path -LiteralPath $grainTemplatePath) "Fixed grain template was not downloaded."
    Assert-True ((Get-Item -LiteralPath $grainTemplatePath).Length -gt 0) "Fixed grain template is empty."
    Write-Pass "Template download passed"

    Write-Step "Importing fixed grain template"
    $fixedTemplateImport = Assert-ApiSuccess (Invoke-FileUpload -RequestUri "$ApiBase/api/grain-temp/import" -FilePath $grainTemplatePath) "fixed grain template import"
    Assert-True ([bool]$fixedTemplateImport.summaryGenerated) "Fixed grain template import did not generate a summary."
    $summariesAfterFixed = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/grain-temp/summaries?warehouseId=$($fixedTemplateImport.warehouseId)") "grain summaries after fixed template import"
    Assert-SummaryContainsCollectedAt -Summaries $summariesAfterFixed -CollectedAt $fixedTemplateImport.collectedAt -Context "Fixed template import summary"
    Write-Pass "Fixed template import passed"

    Write-Step "Importing legacy grain CSV"
    $legacyCollectedAt = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    New-LegacyGrainCsv -FilePath $legacyCsvPath -CollectedAt $legacyCollectedAt
    $legacyCsvImport = Assert-ApiSuccess (Invoke-FileUpload -RequestUri "$ApiBase/api/grain-temp/import" -FilePath $legacyCsvPath) "legacy grain CSV import"
    Assert-True ([bool]$legacyCsvImport.summaryGenerated) "Legacy grain CSV import did not generate a summary."
    $summariesAfterCsv = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/grain-temp/summaries?warehouseId=$($legacyCsvImport.warehouseId)") "grain summaries after legacy CSV import"
    Assert-SummaryContainsCollectedAt -Summaries $summariesAfterCsv -CollectedAt $legacyCsvImport.collectedAt -Context "Legacy CSV import summary"
    Write-Pass "Legacy CSV import passed"

    Write-Step "Importing legacy row-based Excel sample"
    New-LegacyGrainExcel -FilePath $legacyExcelPath -CollectedAt ((Get-Date).AddMinutes(1).ToString("yyyy-MM-dd HH:mm:ss"))
    Assert-True (Test-Path -LiteralPath $legacyExcelPath) "Legacy Excel sample was not generated."
    $legacyXlsImport = Assert-ApiSuccess (Invoke-FileUpload -RequestUri "$ApiBase/api/grain-temp/import" -FilePath $legacyExcelPath) "legacy row-based Excel import"
    Assert-True ([bool]$legacyXlsImport.summaryGenerated) "Legacy row-based Excel import did not generate a summary."
    Write-Pass "Legacy row-based Excel import passed"

    Write-Step "Checking dashboard overview after imports"
    $overviewAfter = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/dashboard/overview") "dashboard overview after imports"
    Assert-OverviewFields -Overview $overviewAfter
    Assert-True ([int]$overviewAfter.grainSummaryCount -gt $grainSummaryCountBefore) "grainSummaryCount did not increase after imports."
    $dashboardSummaryPage = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/dashboard/grain-summaries?pageNum=1&pageSize=10") "dashboard grain summary page"
    $warehouseHealthPage = Assert-ApiSuccess (Invoke-ApiRequest -Method "GET" -Path "/api/dashboard/warehouse-health?pageNum=1&pageSize=10") "dashboard warehouse health page"
    Assert-True (@($dashboardSummaryPage.list).Count -ge 1) "Dashboard grain summary page is empty after imports."
    Assert-True (@($warehouseHealthPage.list).Count -ge 1) "Dashboard warehouse health page is empty after imports."
    Write-Pass "Dashboard post-import checks passed"

    Write-Host ""
    Write-Host "Acceptance smoke completed successfully." -ForegroundColor Green
    Write-Host "Temporary files: $tempDir" -ForegroundColor DarkGray
}
finally {
    if ($null -ne $createdUserId) {
        try {
            Invoke-ApiRequest -Method "DELETE" -Path "/api/users/$createdUserId" | Out-Null
        }
        catch {
        }
    }

    if (-not $KeepBackendRunning) {
        Stop-Backend
    }
}


