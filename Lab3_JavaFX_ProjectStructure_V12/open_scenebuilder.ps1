param([ValidateSet("RootLayout", "CityOverview")][string]$View = "CityOverview")
$ErrorActionPreference = "Stop"
$course = Split-Path $PSScriptRoot -Parent
$builder = Join-Path $course "runtime\scenebuilder\SceneBuilder\SceneBuilder.exe"
$viewFile = Join-Path $PSScriptRoot "src\main\resources\com\example\cityparks\view\$View.fxml"
if (!(Test-Path -LiteralPath $builder)) { throw "Portable Scene Builder is missing." }
& $builder $viewFile
