$ErrorActionPreference = "Stop"
$course = Split-Path $PSScriptRoot -Parent
$lab = Join-Path $course "Lab8_JavaFX_NativePackage_V12"
$bundle = Join-Path $lab "native-build\deploy\bundles\CityParks"
$destination = Join-Path $env:LOCALAPPDATA "CityParksLab8"
New-Item -ItemType Directory -Force -Path $destination | Out-Null
& robocopy.exe $bundle $destination /E /R:0 /W:0 /NFL /NDL /NJH /NJS /NP | Out-Null
if ($LASTEXITCODE -ge 8) { throw "Native application copy failed." }
$exe = Join-Path $destination "CityParks.exe"
$appFolder = Split-Path $exe -Parent
if (!(Test-Path -LiteralPath (Join-Path $appFolder "runtime"))) { throw "Bundled runtime missing." }
$checkLog = Join-Path $lab "verification\native-startup.log"
$process = Start-Process -FilePath $exe -WorkingDirectory $appFolder -WindowStyle Hidden -PassThru -RedirectStandardOutput $checkLog -RedirectStandardError (Join-Path $lab "verification\native-stderr.log")
try {
    $ready = $false
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Milliseconds 300
        $process.Refresh()
        if ($process.HasExited) { throw "Native launcher exited before showing the application." }
        if ((Test-Path -LiteralPath $checkLog) -and ((Get-Content -LiteralPath $checkLog -Raw) -match 'READY: window=true; view=true')) { $ready = $true; break }
    }
    if (!$ready) { throw "Native application window was not detected." }
    "PASS: Native executable started with its own runtime; JavaFX window and populated view verified by startup log." | Tee-Object -FilePath (Join-Path $lab "verification\native.txt")
} finally { if (!$process.HasExited) { Stop-Process -Id $process.Id } }

. (Join-Path $course "runtime\java_env.ps1")
$verification = Join-Path $lab "verification"
$jar = Join-Path $destination "app\CityParks.jar"
& "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -cp $jar -d $verification (Join-Path $PSScriptRoot "FxCheck.java")
if ($LASTEXITCODE) { throw "Packaged application test compilation failed." }
Push-Location $lab
try {
    & "$env:JAVA_HOME\bin\java.exe" '-Dfile.encoding=UTF-8' '-Dcityparks.lab=test1.8.native' -cp "$verification;$jar" FxCheck 8 2>&1 | Tee-Object -FilePath (Join-Path $verification "native-theme.txt")
    if ($LASTEXITCODE) { throw "Packaged JAR theme and functionality check failed." }
} finally { Pop-Location }
