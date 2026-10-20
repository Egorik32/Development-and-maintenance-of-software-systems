$ErrorActionPreference = "Stop"
$runtimeRoot = $PSScriptRoot
$jdk = Join-Path $runtimeRoot "zulu8.96.0.205-ca-fx-jdk8.0.504-win_x64"
if (!(Test-Path -LiteralPath (Join-Path $jdk "bin\javac.exe"))) {
    throw "The bundled JDK is missing: $jdk"
}
$env:JAVA_HOME = $jdk
$env:JRE_HOME = $jdk
$env:PATH = (Join-Path $jdk "bin") + ";" + $env:PATH
