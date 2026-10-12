param([Parameter(Mandatory=$true)][string]$GameDirectory)
$ErrorActionPreference = "Continue"
$enginePath = Join-Path $GameDirectory ".evatex-ffmpeg\1.8.0\win64\ffmpeg.exe"
$reportPath = Join-Path $GameDirectory "evatex-engine-diagnostic.txt"
Start-Transcript -Path $reportPath -Force
try {
    Write-Output "EVATEX engine diagnostic: read-only checks; no security settings are changed."
    Write-Output "Engine exists: $(Test-Path -LiteralPath $enginePath -PathType Leaf)"
    if (Test-Path -LiteralPath $enginePath -PathType Leaf) {
        $engineHash = (Get-FileHash -LiteralPath $enginePath -Algorithm SHA256).Hash.ToLowerInvariant()
        Write-Output "Matches bundled engine: $($engineHash -eq '227af0691433b703ffc5725e47f7d06eefc34b4a72e7870e73d30e2cda483ecf')"
        if ($engineHash -ne '227af0691433b703ffc5725e47f7d06eefc34b4a72e7870e73d30e2cda483ecf') { throw "Unexpected engine checksum; launch skipped." }
        Write-Output "Windows Authenticode status:"
        Get-AuthenticodeSignature -LiteralPath $enginePath | Select-Object Status, StatusMessage, SignerCertificate | Format-List
        Write-Output "Engine file permissions:"
        Get-Acl -LiteralPath $enginePath | Select-Object Owner, AccessToString | Format-List
        Write-Output "Bundled engine launch:"
        try { & $enginePath -nostdin -hide_banner -version; Write-Output "Exit code: $LASTEXITCODE" }
        catch { Write-Output $_.Exception.ToString() }
    }
    Write-Output "Windows security status (read-only):"
    if (Get-Command Get-MpComputerStatus -ErrorAction SilentlyContinue) {
        try { Get-MpComputerStatus -ErrorAction Stop | Select-Object AMRunningMode, AntivirusEnabled, RealTimeProtectionEnabled, SmartAppControlState | Format-List }
        catch { Write-Output "Security status unavailable: $($_.Exception.Message)" }
    }
    Write-Output "System FFmpeg lookup:"
    Get-Command ffmpeg -ErrorAction SilentlyContinue | Select-Object Name, Source
    foreach ($logName in @('Microsoft-Windows-CodeIntegrity/Operational', 'Microsoft-Windows-Windows Defender/Operational', 'Microsoft-Windows-Security-Mitigations/KernelMode', 'Microsoft-Windows-Security-Mitigations/UserMode')) {
        Write-Output "Matching engine events: $logName"
        try {
            Get-WinEvent -FilterHashtable @{ LogName=$logName; StartTime=(Get-Date).AddHours(-24) } -MaxEvents 500 -ErrorAction Stop |
                Where-Object { $_.Message -match 'ffmpeg|javaw?\.exe|ModrinthApp' } |
                Select-Object -First 20 TimeCreated, Id, Message | Format-List
        } catch { Write-Output "Event log unavailable or no recent events: $($_.Exception.Message)" }
    }
    Write-Output "For the settings exit, include the game's latest.log and latest crash-report file separately."
} finally { Stop-Transcript }
Write-Output "Report saved to $reportPath"
