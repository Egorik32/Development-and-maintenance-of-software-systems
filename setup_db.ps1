$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$schema = Join-Path $projectRoot "sql\schema.sql"
$dbName = "cityparks_lab14"
$dbUser = "postgres"
$dbPassword = "postgres"

$possiblePsql = @(
    "C:\Program Files\PostgreSQL\18\bin\psql.exe",
    "C:\Program Files\PostgreSQL\17\bin\psql.exe",
    "C:\Program Files\PostgreSQL\16\bin\psql.exe",
    "C:\Program Files\PostgreSQL\15\bin\psql.exe"
)

$psql = $possiblePsql | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $psql) {
    $cmd = Get-Command psql -ErrorAction SilentlyContinue
    if ($cmd) {
        $psql = $cmd.Source
    }
}

if (-not $psql) {
    throw "psql.exe is not found. Install PostgreSQL first."
}

$env:PGPASSWORD = $dbPassword

$exists = (& $psql -U $dbUser -h localhost -p 5432 -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$dbName'") -join ""
if ($LASTEXITCODE -ne 0) {
    throw "Cannot connect to PostgreSQL. Check that the service is running and password is '$dbPassword'."
}

if ($exists.Trim() -ne "1") {
    & $psql -U $dbUser -h localhost -p 5432 -d postgres -c "CREATE DATABASE $dbName ENCODING 'UTF8'"
    if ($LASTEXITCODE -ne 0) {
        throw "Cannot create database $dbName."
    }
}

& $psql -U $dbUser -h localhost -p 5432 -d $dbName -v ON_ERROR_STOP=1 -f $schema
if ($LASTEXITCODE -ne 0) {
    throw "Cannot apply schema.sql."
}

Write-Host "Database is ready: $dbName"
