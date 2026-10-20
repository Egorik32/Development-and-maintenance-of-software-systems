param([ValidateSet("SceneBuilder.msi", "LibreOffice.msi")][string]$MsiName = "SceneBuilder.msi", [ValidateSet("scenebuilder", "libreoffice")][string]$DestinationName = "scenebuilder")
$ErrorActionPreference = "Stop"
$course = Split-Path $PSScriptRoot -Parent
$msiFile = (Resolve-Path (Join-Path $course "runtime\$MsiName")).Path
$destination = Join-Path $course "runtime\$DestinationName"
$cabFiles = Join-Path $destination "cab-files"
New-Item -ItemType Directory -Force -Path $cabFiles | Out-Null
$env:PYTHONPATH = Join-Path $course "runtime\pythonlib"
$python = (Get-Command python -ErrorAction Stop).Source
& $python -X utf8 -c 'import olefile,sys; from pathlib import Path; f=olefile.OleFileIO(sys.argv[1]); target=Path(sys.argv[2]); [target.joinpath("payload"+str(i)+".cab").write_bytes(f.openstream(n).read()) for i,n in enumerate(f.listdir()) if f.openstream(n).read(4)==b"MSCF"]' $msiFile $destination
if ($LASTEXITCODE -ne 0) { throw "MSI archive reading failed." }
foreach ($cab in Get-ChildItem -LiteralPath $destination -Filter '*.cab') {
    & expand.exe $cab.FullName '-F:*' $cabFiles | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "CAB extraction failed." }
}
$installer = New-Object -ComObject WindowsInstaller.Installer
$database = $installer.OpenDatabase($msiFile, 0)
function Read-Msi([string]$Sql, [int]$Columns) {
    $view = $database.OpenView($Sql)
    $view.Execute() | Out-Null
    while ($record = $view.Fetch()) {
        $values = @()
        for ($i = 1; $i -le $Columns; $i++) { $values += $record.StringData($i) }
        ,$values
    }
    $view.Close() | Out-Null
}
$directories = @{}
foreach ($row in Read-Msi 'SELECT Directory, Directory_Parent, DefaultDir FROM Directory' 3) { $directories[$row[0]] = @($row[1], $row[2]) }
$components = @{}
foreach ($row in Read-Msi 'SELECT Component, Directory_ FROM Component' 2) { $components[$row[0]] = $row[1] }
function Directory-Path([string]$Id) {
    $row = $directories[$Id]
    if (!$row -or !$row[0]) { return $destination }
    $parent = Directory-Path $row[0]
    $name = (($row[1] -split ':')[0] -split '\|')[-1]
    if ($name -eq '.') { return $parent }
    return Join-Path $parent $name
}
foreach ($row in Read-Msi 'SELECT File, Component_, FileName FROM File' 3) {
    $folder = Directory-Path $components[$row[1]]
    $name = ($row[2] -split '\|')[-1]
    $target = [System.IO.Path]::GetFullPath((Join-Path $folder $name))
    if (!$target.StartsWith($destination + '\', [System.StringComparison]::OrdinalIgnoreCase)) { throw "Unexpected MSI file path." }
    New-Item -ItemType Directory -Force -Path $folder | Out-Null
    Copy-Item -LiteralPath (Join-Path $cabFiles $row[0]) -Destination $target -Force
}
Write-Host "Portable files extracted without running the installer: $destination"
