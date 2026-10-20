$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
. (Join-Path (Split-Path $projectRoot -Parent) "runtime\java_env.ps1")
Set-Location $projectRoot

$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8"
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
chcp.com 65001 | Out-Null

if (!(Test-Path "out")) {
    New-Item -ItemType Directory -Force -Path "out" | Out-Null
}

javac -encoding UTF-8 -d out src\Lab1AnonymousClassesV12.java
if ($LASTEXITCODE -ne 0) {
    throw "Compilation failed."
}

java -cp out Lab1AnonymousClassesV12
if ($LASTEXITCODE -ne 0) { throw "Application execution failed." }
