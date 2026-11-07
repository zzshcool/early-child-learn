$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Speech
$sproutRoot = Split-Path -Parent $PSScriptRoot
$sproutAssets = Join-Path $sproutRoot 'app/src/main/assets'
$sproutManifest = Get-Content -LiteralPath (Join-Path $sproutAssets 'audio-manifest.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$sproutSpeech = New-Object System.Speech.Synthesis.SpeechSynthesizer
try {
    $sproutSpeech.Rate = -2
    foreach ($sproutItem in $sproutManifest.items) {
        $sproutCulture = if ($sproutItem.lang -eq 'en') { 'en-US' } else { 'zh-TW' }
        $sproutVoice = $sproutSpeech.GetInstalledVoices() | Where-Object { $_.Enabled -and $_.VoiceInfo.Culture.Name -eq $sproutCulture } | Select-Object -First 1
        if (-not $sproutVoice) { throw "Missing offline speech voice: $sproutCulture" }
        $sproutSpeech.SelectVoice($sproutVoice.VoiceInfo.Name)
        $sproutOutput = Join-Path $sproutAssets ('audio/' + $sproutItem.lang)
        New-Item -ItemType Directory -Force -Path $sproutOutput | Out-Null
        $sproutSpeech.SetOutputToWaveFile((Join-Path $sproutOutput ($sproutItem.id + '.wav')))
        if ($sproutItem.kind -eq 'letter') {
            $sproutSpeech.SpeakSsml('<speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis" xml:lang="en-US"><say-as interpret-as="characters">' + $sproutItem.text + '</say-as></speak>')
        } else { $sproutSpeech.Speak($sproutItem.text) }
        $sproutSpeech.SetOutputToNull()
    }
} finally { $sproutSpeech.Dispose() }
