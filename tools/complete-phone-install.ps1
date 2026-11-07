param([Parameter(Mandatory=$true)][string]$Serial)
$ErrorActionPreference = 'Stop'
# Only completes the normal scan/confirmation for this project's app or instrumentation APK.
$sproutVerified = $false
for ($sproutAttempt = 0; $sproutAttempt -lt 8; $sproutAttempt++) {
    adb -s $Serial shell uiautomator dump /sdcard/sprout-install.xml | Out-Null
    [xml]$sproutUi = (adb -s $Serial shell cat /sdcard/sprout-install.xml)
    $sproutInstaller = $sproutUi.SelectSingleNode('//node[@package="com.oplus.appdetail"]')
    if (-not $sproutInstaller) { break }
    $sproutName = $sproutUi.SelectSingleNode('//node[@resource-id="com.oplus.appdetail:id/app_name"]')
    $sproutScan = $sproutUi.SelectSingleNode('//node[@text="開始深度掃描"]')
    if ($sproutName -and $sproutName.text -notin @('小芽學習島', 'tw.littlesprout.island.test', 'tw.littlesprout.island.debug.test')) { break }
    if ($sproutName) { $sproutVerified = $true }
    if (-not $sproutVerified) { break }
    if (-not $sproutName -and -not $sproutScan) { break }
    $sproutNode = $null
    if ($sproutScan) { $sproutNode = $sproutScan }
    elseif ($sproutUi.SelectSingleNode('//node[contains(@text,"暫無風險")]')) { $sproutNode = $sproutUi.SelectSingleNode('//node[@text="繼續安裝"]') }
    else { $sproutNode = $sproutUi.SelectSingleNode('//node[@resource-id="com.oplus.appdetail:id/menu_more"]') }
    if ($sproutNode) {
        $sproutBounds = [regex]::Matches($sproutNode.bounds, '\d+') | ForEach-Object { [int]$_.Value }
        adb -s $Serial shell input tap (($sproutBounds[0] + $sproutBounds[2]) / 2) (($sproutBounds[1] + $sproutBounds[3]) / 2)
        Write-Output ('Handled project install action: ' + $sproutNode.text)
    }
}
