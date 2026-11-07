param([int]$Size = 512)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName PresentationCore,WindowsBase
$sproutRoot = Split-Path -Parent $PSScriptRoot
[xml]$sproutVector = Get-Content -LiteralPath (Join-Path $sproutRoot 'app/src/main/res/drawable/ic_sprout.xml')
$sproutVisual = New-Object Windows.Media.DrawingVisual
$sproutDrawing = $sproutVisual.RenderOpen()
$sproutDrawing.PushTransform((New-Object Windows.Media.ScaleTransform ($Size / 108), ($Size / 108)))
foreach ($sproutPath in $sproutVector.SelectNodes('//*[local-name()="path"]')) {
    $sproutFill = $sproutPath.GetAttribute('fillColor', 'http://schemas.android.com/apk/res/android')
    $sproutStroke = $sproutPath.GetAttribute('strokeColor', 'http://schemas.android.com/apk/res/android')
    $sproutData = $sproutPath.GetAttribute('pathData', 'http://schemas.android.com/apk/res/android')
    $sproutBrush = if ($sproutFill) { [Windows.Media.BrushConverter]::new().ConvertFromString($sproutFill) } else { $null }
    $sproutPen = $null
    if ($sproutStroke) {
        $sproutPen = [Windows.Media.Pen]::new([Windows.Media.BrushConverter]::new().ConvertFromString($sproutStroke), [double]$sproutPath.GetAttribute('strokeWidth', 'http://schemas.android.com/apk/res/android'))
        $sproutPen.StartLineCap = 'Round'; $sproutPen.EndLineCap = 'Round'
    }
    $sproutDrawing.DrawGeometry($sproutBrush, $sproutPen, [Windows.Media.Geometry]::Parse($sproutData))
}
$sproutDrawing.Pop(); $sproutDrawing.Close()
$sproutBitmap = [Windows.Media.Imaging.RenderTargetBitmap]::new($Size, $Size, 96, 96, [Windows.Media.PixelFormats]::Pbgra32)
$sproutBitmap.Render($sproutVisual)
$sproutEncoder = [Windows.Media.Imaging.PngBitmapEncoder]::new()
$sproutEncoder.Frames.Add([Windows.Media.Imaging.BitmapFrame]::Create($sproutBitmap))
$sproutDestination = Join-Path $sproutRoot 'delivery'
New-Item -ItemType Directory -Force -Path $sproutDestination | Out-Null
$sproutFile = [IO.File]::Create((Join-Path $sproutDestination 'app-icon.png'))
try { $sproutEncoder.Save($sproutFile) } finally { $sproutFile.Dispose() }
