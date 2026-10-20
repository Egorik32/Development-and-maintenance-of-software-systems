param([int]$Number = 0, [int]$Port = 8080)
$ErrorActionPreference = "Stop"
$projects = @{
    1 = "Lab1_AnonymousClasses_V12"
    2 = "Lab2_LambdaExpressions_V12"
}
$labs = Get-Content -LiteralPath (Join-Path $PSScriptRoot "tools\lab_conditions.json") -Raw -Encoding UTF8 | ConvertFrom-Json
foreach ($lab in $labs) { $projects[[int]$lab.number] = $lab.folder }
if ($Number -eq 0) {
    Write-Host "1.1  Анонимные классы"
    Write-Host "1.2  Лямбда-выражения"
    foreach ($lab in $labs) { Write-Host "1.$($lab.number)  $($lab.title)" }
    $choice = Read-Host "Номер лабораторной (3 или 1.3)"
    $choice = $choice -replace '^1\.', ''
    if (![int]::TryParse($choice, [ref]$Number)) { throw "Введите номер от 1 до 15." }
}
if (!$projects.ContainsKey($Number)) { throw "Лабораторная с таким номером отсутствует." }
$folder = Join-Path $PSScriptRoot $projects[$Number]
Write-Host "ЛР 1.${Number}: $folder"
Push-Location $PSScriptRoot
try {
    if ($Number -ge 9) { & (Join-Path $folder "run.ps1") -Port $Port }
    else { & (Join-Path $folder "run.ps1") }
} finally { Pop-Location }
