param([int]$Number = 0, [int]$Port = 8080)
$ErrorActionPreference = "Stop"
$projects = @{}
$labs = @(
    @{number=1; folder="Lab1_AnonymousClasses_V12"; title="Анонимные классы"}
    @{number=2; folder="Lab2_LambdaExpressions_V12"; title="Лямбда-выражения"}
    @{number=3; folder="Lab3_JavaFX_ProjectStructure_V12"; title="Структура проекта и интерфейс"}
    @{number=4; folder="Lab4_JavaFX_MVC_V12"; title="Компоненты MVC"}
    @{number=5; folder="Lab5_JavaFX_Functionality_V12"; title="Функционал приложения"}
    @{number=6; folder="Lab6_JavaFX_CSS_V12"; title="Стилизация CSS"}
    @{number=7; folder="Lab7_JavaFX_XML_V12"; title="Работа с XML"}
    @{number=8; folder="Lab8_JavaFX_NativePackage_V12"; title="Нативный пакет"}
    @{number=9; folder="Lab9_JavaEE_MainPage_V12"; title="Главная страница Java EE"}
    @{number=10; folder="Lab10_JavaEE_ServletTemplates_V12"; title="Шаблоны сервлетов"}
    @{number=11; folder="Lab11_JavaEE_Domain_V12"; title="Домен данных"}
    @{number=12; folder="Lab12_JavaEE_Views_V12"; title="Представления"}
    @{number=13; folder="Lab13_JavaEE_Database_V12"; title="Подключение базы данных"}
    @{number=14; folder="Lab14_JavaEE_Insert_V12"; title="Добавление записей"}
    @{number=15; folder="Lab15_JavaEE_EditDelete_V12"; title="Редактирование и удаление"}
)
foreach ($lab in $labs) { $projects[[int]$lab.number] = $lab.folder }
if ($Number -eq 0) {
    foreach ($lab in $labs) { Write-Host "1.$($lab.number)  $($lab.title)" }
    $choice = Read-Host "Номер лабораторной (например, 3 или 1.3)"
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
