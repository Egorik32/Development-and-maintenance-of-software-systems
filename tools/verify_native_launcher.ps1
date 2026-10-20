param([string]$Shell = (Get-Process -Id $PID).Path)
$ErrorActionPreference = 'Stop'
$course = Split-Path $PSScriptRoot -Parent
$lab = Join-Path $course 'Lab8_JavaFX_NativePackage_V12'
$exe = Join-Path $env:LOCALAPPDATA 'CityParksLab8\CityParks.exe'
if (Get-CimInstance Win32_Process -Filter "Name = 'CityParks.exe'" | Where-Object ExecutablePath -eq $exe) {
    throw 'Close the existing lab 1.8 window before testing its launcher.'
}
$label = if ($Shell -match 'WindowsPowerShell') { 'ps5' } else { 'ps7' }
$log = Join-Path $lab "verification\launcher-$label.log"
$errors = Join-Path $lab "verification\launcher-$label-errors.log"
$launcher = Start-Process -FilePath $Shell -ArgumentList @('-NoProfile','-ExecutionPolicy','Bypass','-File',('"' + (Join-Path $lab 'run.ps1') + '"')) -WindowStyle Hidden -PassThru -RedirectStandardOutput $log -RedirectStandardError $errors
$app = $null
try {
    for ($attempt = 0; $attempt -lt 180; $attempt++) {
        if ($launcher.HasExited) { throw 'Launcher exited before the application was ready.' }
        $candidate = Get-CimInstance Win32_Process -Filter "Name = 'CityParks.exe'" | Where-Object ExecutablePath -eq $exe | Select-Object -First 1
        if ($candidate) {
            $app = Get-Process -Id $candidate.ProcessId
            $app.Refresh()
            if ($app.MainWindowHandle -ne 0) { break }
        }
        Start-Sleep -Milliseconds 500
    }
    if (!$app -or !$app.MainWindowHandle) { throw 'Native window was not detected.' }
    if ($launcher.HasExited) { throw 'Launcher did not wait for the GUI.' }
    if (!$app.CloseMainWindow()) { throw 'Could not close the test window normally.' }
    if (!$launcher.WaitForExit(15000)) { throw 'Launcher did not finish after the window closed.' }
    $launcher.Refresh()
    if ($launcher.ExitCode -ne 0) { throw "Launcher failed with code $($launcher.ExitCode); see $errors" }
    if (!(Get-Content -LiteralPath $log -Raw).Contains('Native application closed normally.')) {
        throw 'Normal native exit was not confirmed.'
    }
    "PASS: $label run.ps1 waits for the GUI and exits successfully after normal window closure; robocopy success is not treated as failure."
} finally {
    if ($app -and !$app.HasExited) { Stop-Process -Id $app.Id -ErrorAction SilentlyContinue }
    if (!$launcher.HasExited) { Stop-Process -Id $launcher.Id -ErrorAction SilentlyContinue }
}
