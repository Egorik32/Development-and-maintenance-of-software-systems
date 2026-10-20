$ErrorActionPreference = "Stop"
$course = Split-Path $PSScriptRoot -Parent
$git = (Get-Command git -ErrorAction Stop).Source
$labs = Get-Content -LiteralPath (Join-Path $PSScriptRoot "lab_conditions.json") -Raw | ConvertFrom-Json
$previous = $null
foreach ($lab in $labs | Where-Object number -ge 9) {
    $folder = Join-Path $course $lab.folder
    $repository = Join-Path $folder '.git'
    if (Test-Path -LiteralPath $repository) { throw "Repository already exists; do not overwrite it: $folder" }
    if ($previous) {
        & robocopy.exe (Join-Path $previous '.git') $repository /E /R:0 /W:0 /NFL /NDL /NJH /NJS /NP | Out-Null
        if ($LASTEXITCODE -ge 8) { throw "Cannot copy local stage history." }
    } else {
        & $git -C $folder init -b master | Out-Null
        & $git -C $folder config user.name "Egorik32"
        & $git -C $folder config user.email "kuznecovegor280@gmail.com"
        & $git -C $folder commit --allow-empty -m "Start Java EE laboratory cycle" | Out-Null
    }
    $branch = "dev$($lab.number - 8)"
    & $git -C $folder switch -c $branch
    if ($LASTEXITCODE -ne 0) { throw "Cannot create stage branch." }
    $paths = @('src', '.project', '.classpath', '.settings', '.gitignore', 'build.ps1', 'build_war.ps1', 'run.ps1', 'sql', 'setup_db.ps1') | Where-Object { Test-Path -LiteralPath (Join-Path $folder $_) }
    & $git -C $folder add -A -- $paths
    & $git -C $folder commit -m "Lab 1.$($lab.number): $($lab.title)" | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "Cannot record stage code." }
    & $git -C $folder switch master
    & $git -C $folder merge --no-ff $branch -m "Merge verified lab 1.$($lab.number)" | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "Cannot merge stage." }
    & $git -C $folder log --graph --oneline --all | Out-File -LiteralPath (Join-Path $folder 'verification\git-history.txt') -Encoding utf8
    $previous = $folder
}
