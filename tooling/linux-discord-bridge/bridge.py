#!/usr/bin/env python3
"""Restore Flatpak Discord's missing exported IPC relay, without restarting apps.
Uses the same export location as Discord's own Flatpak launcher. Never replaces
an existing socket/symlink and forwards only a current user's local Unix IPC.
"""
import os
from pathlib import Path
import shutil
import signal
import subprocess
import time

running = True
child = None

def stop(signum, frame):
    global running
    running = False

for sig in (signal.SIGINT, signal.SIGTERM):
    signal.signal(sig, stop)
runtime = Path(os.environ['XDG_RUNTIME_DIR'])
private = runtime / '.flatpak/com.discordapp.Discord/xdg-run/discord-ipc-0'
export = runtime / 'app/com.discordapp.Discord/discord-ipc-0'
socat = shutil.which('socat')
if not socat:
    raise SystemExit('socat is required; no system changes were made.')
try:
    while running:
        if child is not None:
            if child.poll() is not None:
                print('Local relay exited; waiting before retry.', flush=True)
                child = None
            else:
                time.sleep(1)
                continue
        # An official/external export always wins. Do not remove stale foreign paths.
        if not os.path.lexists(export) and private.is_socket():
            export.parent.mkdir(parents=True, exist_ok=True, mode=0o700)
            print('Restoring Discord Flatpak local IPC export.', flush=True)
            child = subprocess.Popen([socat,
                f'UNIX-LISTEN:{export},fork,mode=0600', f'UNIX-CONNECT:{private}'])
        time.sleep(2)
finally:
    if child is not None and child.poll() is None:
        child.terminate()
        try:
            child.wait(timeout=3)
        except subprocess.TimeoutExpired:
            child.kill()
            child.wait()
