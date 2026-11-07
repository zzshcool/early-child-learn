$ErrorActionPreference = 'Stop'
$sproutRoot = Split-Path -Parent $PSScriptRoot
$sproutSigning = Join-Path $sproutRoot '.signing'
New-Item -ItemType Directory -Force -Path $sproutSigning | Out-Null
$sproutCredentialFile = Join-Path $sproutSigning 'credentials.json'
if (-not (Test-Path -LiteralPath $sproutCredentialFile)) {
    $sproutCredential = @{ password = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32)) }
    $sproutCredential | ConvertTo-Json | Set-Content -LiteralPath $sproutCredentialFile -Encoding utf8
}
$sproutCredential = Get-Content -LiteralPath $sproutCredentialFile -Raw | ConvertFrom-Json
$env:SPROUT_STORE_PASSWORD = $sproutCredential.password
$env:SPROUT_STORE_FILE = Join-Path $sproutSigning 'little-sprout-release.p12'
Push-Location $sproutRoot
try {
    if (-not (Test-Path -LiteralPath $env:SPROUT_STORE_FILE)) {
        & (Join-Path $env:JAVA_HOME 'bin/keytool.exe') -genkeypair -storetype PKCS12 -keystore $env:SPROUT_STORE_FILE -alias little-sprout -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=Little Sprout Island, OU=Android, C=TW' -storepass:env SPROUT_STORE_PASSWORD -keypass:env SPROUT_STORE_PASSWORD
        if ($LASTEXITCODE -ne 0) { throw 'Release signing key generation failed.' }
    }
    & .\gradlew.bat :app:assembleRelease --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Release build failed.' }
    New-Item -ItemType Directory -Force -Path delivery | Out-Null
    $sproutMetadata = Get-Content app/build/outputs/apk/release/output-metadata.json -Raw | ConvertFrom-Json
    $sproutVersion = $sproutMetadata.elements[0].versionName
    Copy-Item -LiteralPath app/build/outputs/apk/release/app-release.apk -Destination "delivery/little-sprout-island-$sproutVersion.apk" -Force
} finally {
    Pop-Location
    Remove-Item Env:SPROUT_STORE_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:SPROUT_STORE_FILE -ErrorAction SilentlyContinue
}
