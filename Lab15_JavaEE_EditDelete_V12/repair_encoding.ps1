param([switch]$CheckOnly)
$ErrorActionPreference = "Stop"
$course = Split-Path $PSScriptRoot -Parent
. (Join-Path $course "runtime\java17_env.ps1")
$verification = Join-Path $PSScriptRoot "verification"
New-Item -ItemType Directory -Force -Path $verification | Out-Null
$backup = $null
if (!$CheckOnly) {
    $pgDump = Get-ChildItem -Path "C:\Program Files\PostgreSQL\*\bin\pg_dump.exe" | Sort-Object FullName -Descending | Select-Object -First 1 -ExpandProperty FullName
    if (!$pgDump) { throw "pg_dump.exe is required for a backup before repair." }
    $backup = Join-Path $verification ("before-encoding-repair-" + (Get-Date -Format 'yyyyMMdd-HHmmss-fff') + ".dump")
    $previousPassword = $env:PGPASSWORD
    try {
        $env:PGPASSWORD = "postgres"
        & $pgDump -h localhost -U postgres -d cityparks_lab15 --format=custom --encoding=UTF8 --file $backup
        if ($LASTEXITCODE -ne 0 -or (Get-Item -LiteralPath $backup).Length -eq 0) { throw "Database backup failed; repair cancelled." }
    } finally {
        $env:PGPASSWORD = $previousPassword
    }
    Write-Output "Backup: $backup"
}
& (Join-Path $PSScriptRoot "build.ps1")
$classpath = "$PSScriptRoot\out;$PSScriptRoot\src\main\webapp\WEB-INF\lib\*"
& "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -cp $classpath -d $verification (Join-Path $course "tools\RepairLab15Encoding.java")
if ($LASTEXITCODE -ne 0) { throw "Recovery tool compilation failed." }
$classpath = "$verification;$classpath"
& "$env:JAVA_HOME\bin\java.exe" -cp $classpath RepairLab15Encoding --self-test
if ($LASTEXITCODE -ne 0) { throw "Recovery self-test failed; database was not changed." }
$mode = if ($CheckOnly) { "--check" } else { "--apply" }
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
$result = @(& "$env:JAVA_HOME\bin\java.exe" "-Dfile.encoding=UTF-8" "-Dsun.stdout.encoding=UTF-8" -cp $classpath RepairLab15Encoding $mode)
if ($LASTEXITCODE -ne 0) { throw "Recovery failed; transaction was rolled back. Backup: $backup" }
$result | Write-Output
$logName = if ($CheckOnly) { 'encoding-check-result.txt' } else { 'encoding-repair-result.txt' }
$result | Set-Content -LiteralPath (Join-Path $verification $logName) -Encoding UTF8
