<#
.SYNOPSIS
    Genera erp-icono.ico (multi-resolucion) a juego con la marca de ERP Tahona.
.DESCRIPTION
    Dibuja un cuadrado marron redondeado (#8B4513) con una "T" dorada (#FFD700),
    los colores del favicon de la aplicacion, en 16/32/48/64/128/256 px y los
    empaqueta en un unico .ico. No requiere dependencias externas (usa System.Drawing).
    Lo ejecuta el desarrollador una vez; el .ico resultante se versiona en el repo.
#>
$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$brown = [System.Drawing.ColorTranslator]::FromHtml("#8B4513")
$gold  = [System.Drawing.ColorTranslator]::FromHtml("#FFD700")
$sizes = 16, 32, 48, 64, 128, 256

function New-IconPng([int]$s) {
    $bmp = New-Object System.Drawing.Bitmap($s, $s)
    $g   = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode     = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit

    # Cuadrado redondeado
    $r    = [Math]::Max(2, [int]($s * 0.19))
    $pad  = [Math]::Max(0, [int]($s * 0.02))
    $w    = $s - 1 - 2 * $pad
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $d    = $r * 2
    $path.AddArc($pad,            $pad,            $d, $d, 180, 90)
    $path.AddArc($pad + $w - $d,  $pad,            $d, $d, 270, 90)
    $path.AddArc($pad + $w - $d,  $pad + $w - $d,  $d, $d,   0, 90)
    $path.AddArc($pad,            $pad + $w - $d,  $d, $d,  90, 90)
    $path.CloseFigure()
    $brush = New-Object System.Drawing.SolidBrush($brown)
    $g.FillPath($brush, $path)

    # Letra "T" dorada, centrada
    $font = New-Object System.Drawing.Font("Segoe UI", [single]($s * 0.62), [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $fmt  = New-Object System.Drawing.StringFormat
    $fmt.Alignment     = [System.Drawing.StringAlignment]::Center
    $fmt.LineAlignment = [System.Drawing.StringAlignment]::Center
    $gbrush = New-Object System.Drawing.SolidBrush($gold)
    $rect   = New-Object System.Drawing.RectangleF(0, [single](-$s * 0.04), [single]$s, [single]$s)
    $g.DrawString("T", $font, $gbrush, $rect, $fmt)

    $g.Dispose()
    $ms = New-Object System.IO.MemoryStream
    $bmp.Save($ms, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    ,$ms.ToArray()
}

# Ensamblar el .ico (cada entrada es un PNG embebido; soportado en Windows Vista+)
$pngs = @{}
foreach ($s in $sizes) { $pngs[$s] = New-IconPng $s }

$out = New-Object System.IO.MemoryStream
$bw  = New-Object System.IO.BinaryWriter($out)
$bw.Write([uint16]0)              # reserved
$bw.Write([uint16]1)              # type: icon
$bw.Write([uint16]$sizes.Count)   # image count

$offset = 6 + 16 * $sizes.Count
foreach ($s in $sizes) {
    $data = $pngs[$s]
    $dim  = if ($s -ge 256) { 0 } else { $s }   # 0 significa 256 en el formato ICO
    $bw.Write([byte]$dim)         # width
    $bw.Write([byte]$dim)         # height
    $bw.Write([byte]0)            # paleta
    $bw.Write([byte]0)            # reserved
    $bw.Write([uint16]1)          # color planes
    $bw.Write([uint16]32)         # bits por pixel
    $bw.Write([uint32]$data.Length)
    $bw.Write([uint32]$offset)
    $offset += $data.Length
}
foreach ($s in $sizes) { $bw.Write($pngs[$s]) }
$bw.Flush()

$icoPath = Join-Path $PSScriptRoot "erp-icono.ico"
[System.IO.File]::WriteAllBytes($icoPath, $out.ToArray())
$bw.Dispose()
Write-Host "Icono generado: $icoPath ($([Math]::Round((Get-Item $icoPath).Length/1KB,1)) KB)" -ForegroundColor Green
