param(
    [Parameter(Mandatory=$true)][int]$GamePid,
    [ValidateRange(3,300)][int]$Seconds = 60,
    [string]$OutputDirectory = ".\evatex-measurements"
)
$ErrorActionPreference = 'Stop'
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
$cpu = Get-CimInstance Win32_Processor
$os = Get-CimInstance Win32_OperatingSystem
$adapters = @(Get-CimInstance Win32_VideoController | Select-Object Name,DriverVersion)
$logicalCpus = [int](($cpu | Measure-Object -Property NumberOfLogicalProcessors -Sum).Sum)
[ordered]@{
    WindowsVersion=$os.Version
    CPU=@($cpu | Select-Object Name,NumberOfLogicalProcessors)
    MemoryGiB=[math]::Round($os.TotalVisibleMemorySize / 1MB,2)
    DisplayAdapters=$adapters
    SamplingSeconds=$Seconds
    CpuPercentMeaning='Percentage of all logical CPUs; separate game and ffmpeg processes'
    GpuMeaning='Per-process, per-engine counters; absent counters mean unavailable, not zero'
} | ConvertTo-Json -Depth 6 | Set-Content -Encoding UTF8 (Join-Path $OutputDirectory 'hardware.json')

$previous = @{}
$rows = [System.Collections.Generic.List[object]]::new()
$gpuRows = [System.Collections.Generic.List[object]]::new()
$warnings = [System.Collections.Generic.HashSet[string]]::new()
$clock = [System.Diagnostics.Stopwatch]::StartNew()
while ($clock.Elapsed.TotalSeconds -lt $Seconds) {
    $elapsed = $clock.Elapsed.TotalSeconds
    $children = @(Get-CimInstance Win32_Process -Filter "ParentProcessId = $GamePid" |
        Where-Object { $_.Name -match '^ffmpeg(\.exe)?$' })
    $ids = @($GamePid) + @($children | ForEach-Object { [int]$_.ProcessId })
    foreach ($processId in $ids) {
        try {
            $process = Get-Process -Id $processId -ErrorAction Stop
            $cpuSeconds = $process.TotalProcessorTime.TotalSeconds
            $cpuPercent = $null
            if ($previous.ContainsKey($processId)) {
                $delta = $elapsed - $previous[$processId].Elapsed
                if ($delta -gt 0) {
                    $cpuPercent = [math]::Round(100 * ($cpuSeconds-$previous[$processId].Cpu) / $delta / $logicalCpus,2)
                }
            }
            $rows.Add([pscustomobject]@{
                ElapsedSeconds=[math]::Round($elapsed,3); ProcessId=$processId; Name=$process.ProcessName
                CpuPercent=$cpuPercent; WorkingSetMiB=[math]::Round($process.WorkingSet64/1MB,2)
            })
            $previous[$processId] = @{Elapsed=$elapsed;Cpu=$cpuSeconds}
        } catch {
            [void]$warnings.Add("A sampled process exited or could not be read: $processId")
        }
    }
    try {
        $engines = @(Get-CimInstance Win32_PerfFormattedData_GPUPerformanceCounters_GPUEngine -ErrorAction Stop)
        foreach ($engine in $engines) {
            if ($engine.Name -match '^pid_(\d+)_') {
                $enginePid = [int]$Matches[1]
                if ($ids -contains $enginePid) {
                    $gpuRows.Add([pscustomobject]@{
                        ElapsedSeconds=[math]::Round($elapsed,3); ProcessId=$enginePid
                        Engine=$engine.Name; UtilizationPercent=$engine.UtilizationPercentage
                    })
                }
            }
        }
    } catch { [void]$warnings.Add('GPU performance counters are unavailable on this system') }
    Start-Sleep -Milliseconds 1000
}
$rows | Export-Csv -NoTypeInformation -Encoding UTF8 (Join-Path $OutputDirectory 'processes.csv')
$gpuRows | Export-Csv -NoTypeInformation -Encoding UTF8 (Join-Path $OutputDirectory 'gpu-engines.csv')
@($warnings) | ConvertTo-Json | Set-Content -Encoding UTF8 (Join-Path $OutputDirectory 'warnings.json')
Write-Host "Measurements saved to $OutputDirectory"
