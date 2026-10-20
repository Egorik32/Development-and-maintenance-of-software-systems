$ErrorActionPreference = "Stop"
$course = Split-Path $PSScriptRoot -Parent
. (Join-Path $course "runtime\java17_env.ps1")
$scene = Join-Path $course "runtime\scenebuilder\SceneBuilder"
$out = Join-Path $course "runtime\scenebuilder-check"
New-Item -ItemType Directory -Force -Path $out | Out-Null
& "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -d $out (Join-Path $PSScriptRoot "SceneBuilderCheck.java")
if ($LASTEXITCODE -ne 0) { throw "Scene Builder check compilation failed." }
$labs = Get-Content -LiteralPath (Join-Path $PSScriptRoot "lab_conditions.json") -Raw | ConvertFrom-Json
foreach ($lab in $labs | Where-Object number -lt 9) {
    $folder = Join-Path $course $lab.folder
    & "$scene\runtime\bin\java.exe" '--add-modules=javafx.controls,javafx.fxml' '--add-opens=javafx.fxml/javafx.fxml=ALL-UNNAMED' '--enable-native-access=javafx.graphics' '-Dfile.encoding=UTF-8' -cp "$out;$scene\app\*" SceneBuilderCheck $folder 2>&1 | Tee-Object -FilePath (Join-Path $folder "verification\scenebuilder.txt")
    if ($LASTEXITCODE -ne 0) { throw "Scene Builder model check failed: $($lab.folder)" }
}
