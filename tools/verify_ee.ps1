param([int[]]$Numbers = @(9,10,11,12,13,14,15), [switch]$CheckLab15Browser, [string]$NodePath = 'node')
$ErrorActionPreference = "Stop"
$course = Split-Path $PSScriptRoot -Parent
$labs = Get-Content -LiteralPath (Join-Path $PSScriptRoot "lab_conditions.json") -Raw | ConvertFrom-Json
$shell = (Get-Process -Id $PID).Path
$psql = "C:\Program Files\PostgreSQL\17\bin\psql.exe"
$env:PGPASSWORD = "postgres"
$env:PGCLIENTENCODING = "UTF8"
function Read-Db([int]$Number, [string]$Sql) {
    $value = & $psql -h localhost -U postgres -d "cityparks_lab$Number" -v ON_ERROR_STOP=1 -tAc $Sql
    if ($LASTEXITCODE -ne 0) { throw "Database assertion query failed." }
    return (($value -join "`n").Trim())
}
function Assert-Lab([bool]$Condition, [string]$Message) {
    if (!$Condition) { throw $Message }
    Write-Output "PASS: $Message"
}
foreach ($lab in $labs | Where-Object { $_.number -ge 9 -and $Numbers.Contains([int]$_.number) }) {
    $n = $lab.number
    $folder = Join-Path $course $lab.folder
    $verification = Join-Path $folder "verification"
    New-Item -ItemType Directory -Force -Path $verification | Out-Null
    $port = 8100 + $n
    if (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue) { throw "Test port occupied: $port" }
    $launcher = Start-Process -FilePath $shell -ArgumentList @('-NoProfile','-ExecutionPolicy','Bypass','-File',('"' + (Join-Path $folder 'run.ps1') + '"'),'-Port', $port) -WindowStyle Hidden -RedirectStandardOutput (Join-Path $verification 'server.log') -RedirectStandardError (Join-Path $verification 'server-error.log') -PassThru
    $base = "http://localhost:$port/lab1-$n"
    try {
        $ready = $false
        for ($attempt = 0; $attempt -lt 45; $attempt++) {
            try { $homePage = Invoke-WebRequest -NoProxy -Uri "$base/" -TimeoutSec 2; $ready = $homePage.StatusCode -eq 200; if ($ready) { break } }
            catch { Start-Sleep -Milliseconds 500 }
        }
        if (!$ready) { throw "Tomcat failed to serve lab 1.$n; inspect verification logs." }
        $result = & {
            Assert-Lab ($homePage.Content.Contains('Города и парки') -and $homePage.Content.Contains('Главное меню')) "Home page 200 and UTF-8 content"
            if ($n -eq 9) {
                & $NodePath (Join-Path $PSScriptRoot 'verify_bootstrap.cjs') "$base/" $verification
                if ($LASTEXITCODE -ne 0) { throw 'Bootstrap browser verification failed.' }
            }
            if ($n -eq 15 -and $CheckLab15Browser) {
                & $NodePath (Join-Path $PSScriptRoot 'verify_lab15_utf8.cjs') $base $verification
                if ($LASTEXITCODE -ne 0) { throw 'Lab 1.15 browser UTF-8 verification failed.' }
            }
            if ($n -ge 10) {
                foreach ($entity in @('city','park')) {
                    $page = Invoke-WebRequest -NoProxy -Uri "$base/$entity" -TimeoutSec 10
                    Assert-Lab ($page.StatusCode -eq 200) "Servlet $entity returns 200"
                    if ($n -lt 12) { Assert-Lab ($page.Content.Contains('Привет')) "Template servlet responds" }
                    else { Assert-Lab ($page.Content.Contains('Парк имени Горького')) "JSP displays model data" }
                    if ($n -eq 15) {
                        Assert-Lab (($page.Headers.'Content-Type' -join '').Contains('charset=UTF-8')) "HTTP $entity response declares UTF-8"
                        $expectedText = if ($entity -eq 'city') { @('Ростов-на-Дону','Шахты','Сочи','областной центр','город областного значения','курортный город') } else { @('Парк имени Горького','Парк Революции','Олимпийский парк','городской','культурный','спортивный') }
                        foreach ($value in $expectedText) { Assert-Lab ($page.Content.Contains($value)) "Correct Cyrillic in $entity data: $value" }
                    }
                }
            }
            if ($n -ge 13) {
                Assert-Lab ((Read-Db $n 'SELECT count(*) FROM cities') -eq '3') "Database has three seed cities"
                Assert-Lab ((Read-Db $n 'SELECT count(*) FROM parks') -eq '3') "Database has three seed parks"
            }
            if ($n -ge 14) {
                $testPark = "Проверочный парк ЛР$n"
                $testCity = "Проверочный город ЛР$n"
                $parkForm = @{name=$testPark;type='городской';area='12.75';hasWater='on';buildDate='2020-02-29'}
                $added = Invoke-WebRequest -NoProxy -Uri "$base/park" -Method Post -Body $parkForm -ContentType 'application/x-www-form-urlencoded; charset=UTF-8'
                $parkId = Read-Db $n 'SELECT id FROM parks ORDER BY id DESC LIMIT 1'
                Assert-Lab ($parkId -match '^\d+$' -and $added.Content.Contains($testPark)) "POST inserts park with generated key and UTF-8"
                $cityForm = @{name=$testCity;status='город';population='12345';parkId=$parkId}
                $added = Invoke-WebRequest -NoProxy -Uri "$base/city" -Method Post -Body $cityForm -ContentType 'application/x-www-form-urlencoded; charset=UTF-8'
                $cityId = Read-Db $n 'SELECT id FROM cities ORDER BY id DESC LIMIT 1'
                Assert-Lab ($cityId -match '^\d+$' -and $added.Content.Contains($testCity)) "POST inserts linked city"
                if ($n -eq 15) {
                    $edit = Invoke-WebRequest -NoProxy -Uri "$base/editcity?id=$cityId"
                    Assert-Lab ($edit.Content.Contains($testCity)) "City edit page contains selected record"
                    $cityForm.id = $cityId
                    $cityForm.name = $testCity + ' изменен'
                    $cityForm.population = '54321'
                    Invoke-WebRequest -NoProxy -Uri "$base/editcity" -Method Post -Body $cityForm -ContentType 'application/x-www-form-urlencoded; charset=UTF-8' | Out-Null
                    Assert-Lab ((Read-Db $n "SELECT population FROM cities WHERE id=$cityId") -eq '54321') "City UPDATE persisted"
                    $updated = Invoke-WebRequest -NoProxy -Uri "$base/city"
                    Assert-Lab ($updated.Content.Contains($cityForm.name)) "City UPDATE preserves Cyrillic text"
                    $edit = Invoke-WebRequest -NoProxy -Uri "$base/editpark?id=$parkId"
                    Assert-Lab ($edit.Content.Contains($testPark)) "Park edit page contains selected record"
                    $parkForm.id = $parkId
                    $parkForm.area = '33.50'
                    Invoke-WebRequest -NoProxy -Uri "$base/editpark" -Method Post -Body $parkForm -ContentType 'application/x-www-form-urlencoded; charset=UTF-8' | Out-Null
                    Assert-Lab ((Read-Db $n "SELECT area FROM parks WHERE id=$parkId") -eq '33.50') "Park UPDATE persisted"
                    $updated = Invoke-WebRequest -NoProxy -Uri "$base/park"
                    Assert-Lab ($updated.Content.Contains($parkForm.name) -and $updated.Content.Contains($parkForm.type)) "Park UPDATE preserves Cyrillic text"
                    $list = Invoke-WebRequest -NoProxy -Uri "$base/park"
                    Assert-Lab ($list.Content.Contains("confirm('Удалить запись?')")) "Delete confirmation present"
                    $deleteStatus = 0
                    try { Invoke-WebRequest -NoProxy -Uri "$base/deletepark?id=1" | Out-Null }
                    catch { $deleteStatus = [int]$_.Exception.Response.StatusCode }
                    Assert-Lab ($deleteStatus -eq 409 -and (Read-Db $n 'SELECT count(*) FROM parks WHERE id=1') -eq '1') "Related park deletion rejected with a clear conflict response"
                    Invoke-WebRequest -NoProxy -Uri "$base/deletecity?id=$cityId" | Out-Null
                    Assert-Lab ((Read-Db $n "SELECT count(*) FROM cities WHERE id=$cityId") -eq '0') "City DELETE persisted"
                    Invoke-WebRequest -NoProxy -Uri "$base/deletepark?id=$parkId" | Out-Null
                    Assert-Lab ((Read-Db $n "SELECT count(*) FROM parks WHERE id=$parkId") -eq '0') "Park DELETE persisted"
                } else {
                    Read-Db $n "DELETE FROM cities WHERE id=$cityId; DELETE FROM parks WHERE id=$parkId;" | Out-Null
                }
            }
            Write-Output "RESULT: ALL CHECKS PASSED FOR LAB 1.$n"
        }
        $result | Tee-Object -FilePath (Join-Path $verification 'result.txt')
    } finally {
        $serverBase = Join-Path $folder '.tomcat'
        Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" | Where-Object { $_.CommandLine -and $_.CommandLine.Contains($serverBase) } | ForEach-Object { Stop-Process -Id $_.ProcessId -ErrorAction SilentlyContinue }
        if (!$launcher.HasExited) { Stop-Process -Id $launcher.Id -ErrorAction SilentlyContinue }
    }
}
