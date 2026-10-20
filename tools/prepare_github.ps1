param([Parameter(Mandatory = $true)][string]$Destination)
$ErrorActionPreference = 'Stop'
$course = Split-Path $PSScriptRoot -Parent
$target = [IO.Path]::GetFullPath($Destination)
if ($target.StartsWith($course, [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Use a publication directory outside the source course folder.'
}
New-Item -ItemType Directory -Force -Path $target | Out-Null
if (Get-ChildItem -LiteralPath $target -Force | Where-Object Name -ne '.git') {
    throw 'Publication directory must be empty except for its .git directory.'
}

function Copy-SourceTree([string]$Source, [string]$Target) {
    New-Item -ItemType Directory -Force -Path $Target | Out-Null
    foreach ($item in Get-ChildItem -LiteralPath $Source -Force) {
        $to = Join-Path $Target $item.Name
        if ($item.PSIsContainer) {
            if ($item.Name -in @('.git','.tomcat','out','build','dist','__pycache__','deploy','project','projectRefs','externalLibs')) { continue }
            if ($item.Name -eq 'verification') {
                New-Item -ItemType Directory -Force -Path $to | Out-Null
                Get-ChildItem -LiteralPath $item.FullName -File | Where-Object {
                    $_.Name -match '^(result\.txt|examples_result\.json|native\.txt|native-theme\.txt|scenebuilder\.txt|bootstrap_result\.json|utf8-browser-result\.json|main(-light|-dark)?\.png|dialog\.png|bootstrap-(desktop|mobile)\.png|utf8-(cities|parks)\.png)$'
                } | ForEach-Object { Copy-Item -LiteralPath $_.FullName -Destination $to }
                continue
            }
            Copy-SourceTree $item.FullName $to
        } elseif ($item.Name -ne '.gitignore' -and $item.Extension -notin @('.class','.log','.dump','.war','.zip','.msi')) {
            Copy-Item -LiteralPath $item.FullName -Destination $to
        }
    }
}

Get-ChildItem -LiteralPath $course -Directory | Where-Object Name -match '^Lab\d+_.*_V12$' | ForEach-Object {
    Copy-SourceTree $_.FullName (Join-Path $target $_.Name)
}
Copy-SourceTree $PSScriptRoot (Join-Path $target 'tools')
Copy-SourceTree (Join-Path $course 'materials') (Join-Path $target 'materials')
foreach ($name in @('README.md','.gitignore','ЗАПУСТИТЬ_ЛАБУ.ps1','КАК_ЗАПУСТИТЬ_НА_ПОКАЗЕ.md','ЧЕК_ЛИСТ_ДЛЯ_ЗАЩИТЫ.md','ПРОВЕРКА_2026_10_20.md')) {
    Copy-Item -LiteralPath (Join-Path $course $name) -Destination (Join-Path $target $name)
}
New-Item -ItemType Directory -Force -Path (Join-Path $target 'runtime'),(Join-Path $target 'reports') | Out-Null
foreach ($name in @('README.md','java_env.ps1','java17_env.ps1')) {
    Copy-Item -LiteralPath (Join-Path $course "runtime\$name") -Destination (Join-Path $target 'runtime')
}
Copy-SourceTree (Join-Path $course 'runtime\ant\lib') (Join-Path $target 'runtime\ant\lib')
Copy-SourceTree (Join-Path $course 'runtime\efx-generator\lib') (Join-Path $target 'runtime\efx-generator\lib')
Get-ChildItem -LiteralPath (Join-Path $course 'reports') -File | Where-Object Name -match '^Отчет_ЛР_1_[12]_.*\.docx$' | ForEach-Object {
    Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $target 'reports')
}
$files = @(Get-ChildItem -LiteralPath $target -File -Recurse -Force | Where-Object FullName -notlike "$target\.git\*")
if ($files | Where-Object Length -ge 50MB) { throw 'An unexpectedly large file was included; inspect the publication directory.' }
Write-Output "Prepared $($files.Count) files for review; JDKs, Tomcat, local Git directories and database backups are excluded."
