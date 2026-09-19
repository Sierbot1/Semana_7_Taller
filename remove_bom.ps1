$basePath = "C:\Users\sierb\Documents\Universidad\Desarrollo_Empresarial\proyectos\Taller_Semana_7\loan-api\1. Equipos"
$files = @(
    "Crear equipo - camara.request.yaml",
    "Crear equipo - portatil.request.yaml",
    "Crear equipo - sensor.request.yaml",
    "Listar equipos.request.yaml",
    "Obtener equipo por id.request.yaml",
    "Actualizar equipo.request.yaml"
)
foreach ($f in $files) {
    $path = Join-Path $basePath $f
    $bytes = [System.IO.File]::ReadAllBytes($path)
    if ($bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $content = [System.Text.Encoding]::UTF8.GetString($bytes, 3, $bytes.Length - 3)
        [System.IO.File]::WriteAllText($path, $content, (New-Object System.Text.UTF8Encoding $false))
        Write-Host ("Removed BOM from: " + $f)
    } else {
        Write-Host ("No BOM in: " + $f)
    }
}
Write-Host "Done!"
