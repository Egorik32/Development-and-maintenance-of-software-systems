$ErrorActionPreference = "Stop"
& (Join-Path $PSScriptRoot "build.ps1")
$course = Split-Path $PSScriptRoot -Parent
. (Join-Path $course "runtime\java17_env.ps1")
$check = Join-Path $PSScriptRoot "verification"
New-Item -ItemType Directory -Force -Path $check | Out-Null
& "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -cp (Join-Path $PSScriptRoot 'out') -d $check (Join-Path $course 'tools\DomainCheck.java')
if ($LASTEXITCODE -ne 0) { throw "Domain check compilation failed." }
& "$env:JAVA_HOME\bin\java.exe" '-Dfile.encoding=UTF-8' -cp "$check;$PSScriptRoot\out" DomainCheck
if ($LASTEXITCODE -ne 0) { throw "Domain check failed." }
