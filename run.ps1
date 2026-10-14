param([int]$Port = 8080)
$ErrorActionPreference = "Stop"
& (Join-Path $PSScriptRoot "build.ps1")
. (Join-Path (Split-Path $PSScriptRoot -Parent) "runtime\java17_env.ps1")
$tomcat = Join-Path (Split-Path $PSScriptRoot -Parent) "runtime\apache-tomcat-10.0.27"
$base = Join-Path $PSScriptRoot ".tomcat"
New-Item -ItemType Directory -Force -Path "$base\conf","$base\logs","$base\temp","$base\work","$base\webapps" | Out-Null
Copy-Item -Path "$tomcat\conf\*" -Destination "$base\conf" -Force
$server = [xml](Get-Content -LiteralPath "$base\conf\server.xml")
$server.Server.port = "-1"
$http = $server.Server.Service.Connector | Where-Object protocol -eq "HTTP/1.1"
$http.port = [string]$Port
$http.SetAttribute("address", "127.0.0.1")
$server.Save("$base\conf\server.xml")
Copy-Item -LiteralPath (Join-Path $PSScriptRoot "dist\lab1-9.war") -Destination "$base\webapps\lab1-9.war" -Force
$env:CATALINA_HOME = $tomcat
$env:CATALINA_BASE = $base
$env:CATALINA_OPTS = "-Dfile.encoding=UTF-8"
Write-Host "Open http://localhost:$Port/lab1-9/"
Write-Host "Press Ctrl+C to stop Tomcat before opening another lab."
& "$tomcat\bin\catalina.bat" run
