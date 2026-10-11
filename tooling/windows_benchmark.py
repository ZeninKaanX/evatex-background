#!/usr/bin/env python3
"""Run actual host-native 1080p playback measurements, without opening a window."""
import argparse
import json
import os
from pathlib import Path
import platform
import re
import subprocess

ROOT = Path(__file__).resolve().parent.parent
parser = argparse.ArgumentParser()
parser.add_argument('--video', type=Path, help='Use the supplied 1080p-or-smaller video unchanged')
args = parser.parse_args()
work = ROOT / 'build/windows-performance'
work.mkdir(parents=True, exist_ok=True)
java = Path(os.environ['JAVA_HOME']) / 'bin/java'
classpath = os.pathsep.join([str(ROOT/'build/classes-1.21.11'),
                           (ROOT/'build/classpath-1.21.11.txt').read_text()])
subprocess.run([str(java.with_name('javac')), '-proc:none', '-cp', classpath,
                '-d', str(work), str(ROOT/'tooling/WindowsPlaybackBenchmark.java')], check=True)
engine = ROOT / 'assets/videomenu/ffmpeg' / ('win64/ffmpeg.exe' if platform.system() == 'Windows' else 'linux64/ffmpeg')
os.environ['PATH'] = str(engine.parent) + os.pathsep + os.environ['PATH']
if args.video:
    fixture = args.video.resolve(strict=True)
    probe = subprocess.run([str(engine), '-hide_banner', '-i', str(fixture)], capture_output=True, text=True)
    dimensions = next((re.search(r'\b([1-9]\d{0,4})x([1-9]\d{0,4})\b', line)
                       for line in probe.stderr.splitlines() if 'Video:' in line), None)
    if not dimensions:
        raise SystemExit('Cannot determine video dimensions')
    width, height = map(int, dimensions.groups())
    if width > 1920 or height > 1080:
        raise SystemExit('This test intentionally refuses sources above 1920x1080; no 4K/8K playback')
    fixture_description = f'user-supplied video {width}x{height}; unchanged; muted'
else:
    fixture = work / '1080p60.mp4'
    subprocess.run([str(engine), '-hide_banner', '-loglevel', 'error', '-y', '-f', 'lavfi',
                    '-i', 'testsrc2=size=1920x1080:rate=60', '-t', '4', '-threads', '2',
                    '-c:v', 'libx264', '-preset', 'fast', '-crf', '20', '-pix_fmt', 'yuv420p',
                    str(fixture)], check=True)
    fixture_description = 'synthetic H.264 1920x1080 60fps; muted'
report = {'os': platform.platform(), 'fixture': fixture_description,
          'audio_volume': 0.1, 'opengl_tested': False, 'measurements': []}
if platform.system() == 'Windows':
    gpu = subprocess.run(['powershell', '-NoProfile', '-Command',
        'Get-CimInstance Win32_VideoController | Select-Object Name,DriverVersion | ConvertTo-Json -Compress'],
        capture_output=True, text=True, check=True)
    report['display_adapters'] = json.loads(gpu.stdout) if gpu.stdout.strip() else []
    print('::notice title=Windows adapters::'+json.dumps(report['display_adapters']))
failed = False
for hardware, height in [(False,1080),(True,1080),(False,720)]:
    result = subprocess.run([str(java), '-Xmx2G', '-cp', str(work)+os.pathsep+classpath,
        'WindowsPlaybackBenchmark', str(fixture), str(hardware).lower(), str(height)],
        capture_output=True, text=True, timeout=90)
    print(result.stdout+result.stderr, flush=True)
    for line in result.stdout.splitlines():
        if line.startswith('BENCHMARK_JSON '):
            report['measurements'].append(json.loads(line[len('BENCHMARK_JSON '):]))
    failed |= result.returncode != 0
(work/'report.json').write_text(json.dumps(report,indent=2)+'\n')
if failed or len(report['measurements']) != 3:
    raise SystemExit('One or more native playback measurements failed')
