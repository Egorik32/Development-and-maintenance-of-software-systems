$ErrorActionPreference = "Stop"
& (Join-Path $PSScriptRoot "package_native.ps1")
$bundle = Join-Path $PSScriptRoot "native-build\deploy\bundles\CityParks"
$destination = Join-Path $env:LOCALAPPDATA "CityParksLab8"
# The JavaFX 8 native launcher needs an ASCII installation path on Windows.
New-Item -ItemType Directory -Force -Path $destination | Out-Null
& robocopy.exe $bundle $destination /E /R:0 /W:0 /NFL /NDL /NJH /NJS /NP | Out-Null
if ($LASTEXITCODE -ge 8) { throw "Cannot copy the self-contained native application." }
Write-Host "Native app with bundled Java: $destination\CityParks.exe"
# A GUI executable invoked with & may leave robocopy's success code in LASTEXITCODE.
$application = Start-Process -FilePath (Join-Path $destination "CityParks.exe") -WorkingDirectory $destination -Wait -PassThru
$global:LASTEXITCODE = $application.ExitCode
if ($application.ExitCode -ne 0) { throw "Native launcher exited with code $($application.ExitCode)." }
Write-Host "Native application closed normally."
