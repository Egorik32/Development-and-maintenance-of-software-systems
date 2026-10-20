$ErrorActionPreference = "Stop"
. (Join-Path (Split-Path $PSScriptRoot -Parent) "runtime\java_env.ps1")
$src = Join-Path $PSScriptRoot "src\main\java"
$out = Join-Path $PSScriptRoot "out"
New-Item -ItemType Directory -Force -Path $out | Out-Null
$sources = @(Get-ChildItem -LiteralPath $src -Recurse -Filter *.java | ForEach-Object FullName)
& "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -d $out $sources
if ($LASTEXITCODE -ne 0) { throw "Compilation failed." }
Copy-Item -Path (Join-Path $PSScriptRoot "src\main\resources\*") -Destination $out -Recurse -Force
Write-Host "Compilation completed."
