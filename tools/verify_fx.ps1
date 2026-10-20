$ErrorActionPreference = "Stop"
$course = Split-Path $PSScriptRoot -Parent
. (Join-Path $course "runtime\java_env.ps1")
$labs = Get-Content -LiteralPath (Join-Path $PSScriptRoot "lab_conditions.json") -Raw | ConvertFrom-Json
foreach ($lab in $labs | Where-Object number -lt 9) {
    $folder = Join-Path $course $lab.folder
    & (Join-Path $folder "build.ps1")
    $out = Join-Path $folder "out"
    $check = Join-Path $folder "verification"
    New-Item -ItemType Directory -Force -Path $check | Out-Null
    & "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -cp $out -d $check (Join-Path $PSScriptRoot "FxCheck.java")
    if ($LASTEXITCODE -ne 0) { throw "Test compilation failed: $($lab.folder)" }
    Push-Location $folder
    try {
        & "$env:JAVA_HOME\bin\java.exe" "-Dfile.encoding=UTF-8" "-Dcityparks.lab=test1.$($lab.number)" -cp "$check;$out" FxCheck $lab.number 2>&1 | Tee-Object -FilePath (Join-Path $check "result.txt")
        if ($LASTEXITCODE -ne 0) { throw "Functional check failed: $($lab.folder)" }
    } finally { Pop-Location }
}
