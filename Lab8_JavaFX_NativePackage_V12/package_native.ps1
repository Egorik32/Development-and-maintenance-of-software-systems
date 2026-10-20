param([ValidateSet("image", "exe")][string]$Format = "image")
$ErrorActionPreference = "Stop"
$course = Split-Path $PSScriptRoot -Parent
. (Join-Path $course "runtime\java_env.ps1")
$generator = Join-Path $course "runtime\efx-generator"
$lib = Join-Path $generator "lib"
$out = Join-Path $generator "out"
New-Item -ItemType Directory -Force -Path $out | Out-Null
& "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -cp "$lib\*" -d $out (Join-Path $course "tools\EfxBuildGenerator.java")
if ($LASTEXITCODE -ne 0) { throw "e(fx)clipse generator compilation failed." }
& "$env:JAVA_HOME\bin\java.exe" "-Dfile.encoding=UTF-8" -cp "$out;$lib\*" EfxBuildGenerator $PSScriptRoot $Format
if ($LASTEXITCODE -ne 0) { throw "e(fx)clipse generation failed." }
$antLib = Join-Path $course "runtime\ant\lib"
& "$env:JAVA_HOME\bin\java.exe" "-Dfile.encoding=UTF-8" -cp "$antLib\*;$env:JAVA_HOME\lib\tools.jar" org.apache.tools.ant.Main -f (Join-Path $PSScriptRoot "native-build\build.xml")
if ($LASTEXITCODE -ne 0) { throw "Native packaging failed." }
if (!(Test-Path -LiteralPath (Join-Path $PSScriptRoot "native-build\deploy\bundles\CityParks\CityParks.exe"))) {
    throw "A successful Ant exit is insufficient: native launcher was not created. Inspect packaging output."
}
Write-Host "Native launcher: native-build\deploy\bundles\CityParks\CityParks.exe"
