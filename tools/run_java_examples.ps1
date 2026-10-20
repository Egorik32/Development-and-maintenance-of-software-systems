param([Parameter(Mandatory = $true)][string]$ExamplesRoot, [int]$Number = 0)
$ErrorActionPreference = "Stop"
$course = Split-Path $PSScriptRoot -Parent
. (Join-Path $course "runtime\java_env.ps1")
$manifest = Get-Content -LiteralPath (Join-Path $ExamplesRoot "examples.json") -Raw -Encoding UTF8 | ConvertFrom-Json
$selected = @($manifest | Where-Object { $Number -eq 0 -or $_.number -eq $Number })
if (!$selected.Count) { throw "This example number does not exist." }
$records = @()
foreach ($example in $selected) {
    $source = Join-Path $ExamplesRoot $example.folder
    $out = Join-Path $ExamplesRoot ("out\" + $example.folder)
    New-Item -ItemType Directory -Force -Path $out | Out-Null
    $files = @(Get-ChildItem -LiteralPath $source -Filter "*.java" -File | Select-Object -ExpandProperty FullName)
    & "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -d $out @files
    if ($LASTEXITCODE -ne 0) { throw "Compilation failed: $($example.folder)" }
    $output = @(& "$env:JAVA_HOME\bin\java.exe" "-Dfile.encoding=UTF-8" -cp $out $example.class)
    if ($LASTEXITCODE -ne 0) { throw "Execution failed: $($example.folder)" }
    if (($output -join "`n") -cne (@($example.expected) -join "`n")) {
        throw "Unexpected output: $($example.folder): $($output -join '; ')"
    }
    Write-Output "=== Example $($example.number): $($example.folder) ==="
    $output | Write-Output
    $records += [pscustomobject]@{number=$example.number;class=$example.class;output=$output;passed=$true}
}
$verification = Join-Path (Split-Path $ExamplesRoot -Parent) "verification"
New-Item -ItemType Directory -Force -Path $verification | Out-Null
$filename = if ($Number -eq 0) { "examples_result.json" } else { "example-$Number.json" }
ConvertTo-Json -InputObject @($records) -Depth 5 | Set-Content -LiteralPath (Join-Path $verification $filename) -Encoding UTF8
Write-Output "RESULT: ALL $($selected.Count) SELECTED EXAMPLES PASSED"
