$ErrorActionPreference = "Stop"
& (Join-Path $PSScriptRoot "build.ps1")
. (Join-Path (Split-Path $PSScriptRoot -Parent) "runtime\java_env.ps1")
& "$env:JAVA_HOME\bin\java.exe" "-Dfile.encoding=UTF-8" "-Dcityparks.lab=1.3" -cp (Join-Path $PSScriptRoot "out") com.example.cityparks.MainApp
if ($LASTEXITCODE -ne 0) { throw "Application exited with an error." }
