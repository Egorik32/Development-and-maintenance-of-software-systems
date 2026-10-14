$ErrorActionPreference = "Stop"
$serverBase = Join-Path $PSScriptRoot ".tomcat"
$servers = @(Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" | Where-Object { $_.CommandLine -and $_.CommandLine.Contains($serverBase) })
foreach ($server in $servers) { Stop-Process -Id $server.ProcessId -ErrorAction Stop }
Write-Host "Stopped this lab's Tomcat processes: $($servers.Count)"
