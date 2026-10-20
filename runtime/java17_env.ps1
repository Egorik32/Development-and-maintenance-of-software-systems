$ErrorActionPreference = "Stop"
$jdk = Join-Path $PSScriptRoot "zulu17.66.19-ca-jdk17.0.19-win_x64"
if (!(Test-Path -LiteralPath (Join-Path $jdk "bin\javac.exe"))) {
    throw "The bundled JDK 17 is missing: $jdk"
}
$env:JAVA_HOME = $jdk
$env:JRE_HOME = $jdk
$env:PATH = (Join-Path $jdk "bin") + ";" + $env:PATH
