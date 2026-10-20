param([ValidateSet(13,14,15)][int]$Number = 15)
$ErrorActionPreference = "Stop"
$course = Split-Path $PSScriptRoot -Parent
$folder = switch ($Number) { 13 { 'Lab13_JavaEE_Database_V12' } 14 { 'Lab14_JavaEE_Insert_V12' } 15 { 'Lab15_JavaEE_EditDelete_V12' } }
$lab = Join-Path $course $folder
$psql = "C:\Program Files\PostgreSQL\17\bin\psql.exe"
$tokens = $null
$errorsFound = $null
$ast = [System.Management.Automation.Language.Parser]::ParseFile((Join-Path $lab 'setup_db.ps1'),[ref]$tokens,[ref]$errorsFound)
if ($errorsFound) { throw 'Cannot parse setup_db.ps1' }
$definition = $ast.Find({param($node) $node -is [System.Management.Automation.Language.FunctionDefinitionAst] -and $node.Name -eq 'Invoke-Utf8Psql'},$true)
if (!$definition) { throw 'UTF-8 import function is missing' }
. ([scriptblock]::Create($definition.Extent.Text))
$testDb = "cityparks_lab${Number}_encoding_test_" + [Guid]::NewGuid().ToString('N').Substring(0,8)
if ($testDb -notmatch '^cityparks_lab(13|14|15)_encoding_test_[a-f0-9]{8}$') { throw 'Unexpected temporary database name' }
$created = $false
$previousEncoding = $env:PGCLIENTENCODING
$previousPassword = $env:PGPASSWORD
try {
    $env:PGPASSWORD = 'postgres'
    $env:PGCLIENTENCODING = 'WIN1251'
    Invoke-Utf8Psql -Arguments @('-h','localhost','-U','postgres','-d','postgres','-v','ON_ERROR_STOP=1','-c',"CREATE DATABASE $testDb ENCODING 'UTF8'") | Out-Null
    if ($LASTEXITCODE) { throw 'Cannot create isolated encoding test database' }
    $created = $true
    $encoding = Invoke-Utf8Psql -Arguments @('-h','localhost','-U','postgres','-d',$testDb,'-tAc','SHOW client_encoding')
    if (($encoding -join '').Trim() -ne 'UTF8' -or $env:PGCLIENTENCODING -ne 'WIN1251') { throw 'Client encoding override or environment restoration failed' }
    Invoke-Utf8Psql -Arguments @('-h','localhost','-U','postgres','-d',$testDb,'-v','ON_ERROR_STOP=1','-f',(Join-Path $lab 'sql\schema.sql')) | Out-Null
    if ($LASTEXITCODE) { throw 'Schema import failed' }
    $hex = Invoke-Utf8Psql -Arguments @('-h','localhost','-U','postgres','-d',$testDb,'-tAc',"SELECT encode(convert_to(name,'UTF8'),'hex') FROM cities WHERE id=1")
    $expected = -join ([Text.Encoding]::UTF8.GetBytes('Ростов-на-Дону') | ForEach-Object { $_.ToString('x2') })
    if (($hex -join '').Trim() -ne $expected) { throw 'UTF-8 seed data was corrupted by the import' }
    Write-Output 'PASS: SQL imports as UTF-8 even when the parent client encoding is WIN1251'
    Write-Output 'PASS: parent environment is restored; the real lab database was not reset'
} finally {
    if ($created) {
        Invoke-Utf8Psql -Arguments @('-h','localhost','-U','postgres','-d','postgres','-v','ON_ERROR_STOP=1','-c',"DROP DATABASE $testDb") | Out-Null
        if ($LASTEXITCODE) { throw "Cannot remove test database $testDb" }
    }
    $env:PGCLIENTENCODING = $previousEncoding
    $env:PGPASSWORD = $previousPassword
}
