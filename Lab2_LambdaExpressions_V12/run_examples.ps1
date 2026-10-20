param([int]$Number = 0)
$ErrorActionPreference = "Stop"
$course = Split-Path $PSScriptRoot -Parent
& (Join-Path $course "tools\run_java_examples.ps1") -ExamplesRoot (Join-Path $PSScriptRoot "examples") -Number $Number
