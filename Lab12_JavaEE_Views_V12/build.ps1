$ErrorActionPreference = "Stop"
. (Join-Path (Split-Path $PSScriptRoot -Parent) "runtime\java17_env.ps1")
$out = Join-Path $PSScriptRoot "out"
$build = Join-Path $PSScriptRoot "build"
$dist = Join-Path $PSScriptRoot "dist"
New-Item -ItemType Directory -Force -Path $out,$build,$dist | Out-Null
$web = Join-Path $PSScriptRoot "src\main\webapp"
Copy-Item -Path "$web\*" -Destination $build -Recurse -Force
$lib = Join-Path $web "WEB-INF\lib"
$sources = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot "src\main\java") -Recurse -Filter *.java | ForEach-Object FullName)
if ($sources.Count -gt 0) {
    & "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -cp "$lib\*" -d $out $sources
    if ($LASTEXITCODE -ne 0) { throw "Compilation failed." }
    $classes = Join-Path $build "WEB-INF\classes"
    New-Item -ItemType Directory -Force -Path $classes | Out-Null
    Copy-Item -Path "$out\*" -Destination $classes -Recurse -Force
    $config = Join-Path $PSScriptRoot "src\main\java\config"
    if (Test-Path -LiteralPath $config) { Copy-Item -LiteralPath $config -Destination $classes -Recurse -Force }
}
$servletApi = Join-Path $build "WEB-INF\lib\jakarta.servlet-api-5.0.0.jar"
if (Test-Path -LiteralPath $servletApi) { Remove-Item -LiteralPath $servletApi }
& "$env:JAVA_HOME\bin\jar.exe" --create --file (Join-Path $dist "lab1-12.war") -C $build .
if ($LASTEXITCODE -ne 0) { throw "WAR packaging failed." }
Write-Host "WAR created: dist\lab1-12.war"
