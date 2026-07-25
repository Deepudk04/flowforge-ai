$ErrorActionPreference = "Stop"

$projectDirectory = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$propertiesPath = Join-Path $PSScriptRoot "maven-wrapper.properties"
$distributionUrl = (Get-Content $propertiesPath |
    Where-Object { $_ -match "^distributionUrl=" } |
    Select-Object -First 1) -replace "^distributionUrl=", ""

if (-not $distributionUrl) {
    throw "distributionUrl is missing from $propertiesPath"
}

$archiveName = Split-Path $distributionUrl -Leaf
$distributionName = $archiveName -replace "-bin\.zip$", ""
$wrapperHome = Join-Path $env:USERPROFILE ".m2\wrapper\dists\$distributionName"
$mavenHome = Join-Path $wrapperHome $distributionName
$mavenCommand = Join-Path $mavenHome "bin\mvn.cmd"

if (-not (Test-Path $mavenCommand)) {
    New-Item -ItemType Directory -Path $wrapperHome -Force | Out-Null
    $archivePath = Join-Path $wrapperHome $archiveName
    Invoke-WebRequest -Uri $distributionUrl -OutFile $archivePath
    Expand-Archive -Path $archivePath -DestinationPath $wrapperHome -Force
    Remove-Item -LiteralPath $archivePath
}

Push-Location $projectDirectory
try {
    & $mavenCommand @args
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
