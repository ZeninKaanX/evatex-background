# Video engine on CurseForge

The CurseForge JAR contains Java code and media resources, **no FFmpeg executable or compressed executable**. Videos and animated GIFs require FFmpeg; photographs do not.

Select a video in My Library and press Apply. If FFmpeg is missing, EVATEX offers **Download engine**. The download starts only after pressing that button, runs outside the game thread, and shows progress. Alternatively, open Settings → Playback → Video engine. Returning without downloading leaves the game usable. If the connection fails, retry there.

EVATEX downloads the platform-specific engine directly from its pinned upstream release on GitHub: Gyan's FFmpeg 9.0.2 Windows build, or ffmpeg-static 6.1.1 for Linux/macOS. The archive and extracted executable must both match SHA-256 values in `assets/evatexbackgroundvideo/engine-manifest.json`. Downloads are HTTPS-only with an exact host allowlist, redirect checks and bounded size/time. Extraction writes only the named FFmpeg file into the instance's `.evatex-ffmpeg/engines` directory; it does not unpack archive paths or run an installer. Setup does not need administrator rights or modify PATH. Verified engines are reused without downloading on later launches.

You may instead install FFmpeg yourself and put `ffmpeg.exe` (Windows) or `ffmpeg` (Linux/macOS) in `<game directory>/.evatex-ffmpeg/bin`, or make it available on your existing system PATH. Those user-managed installations are trusted as user-selected tools; the pinned download hashes do not apply to them. Compatible engines extracted by older EVATEX versions are reused only when their recorded hash matches.

Modrinth's normal bundled-engine distribution retains its existing installation behavior. The CurseForge package is explicitly marked `curseforge-external-engine`; its bytes differ from the Modrinth distribution. This is a packaging difference, not an automatic updater for mod code.

## Credits, source and license

FFmpeg: https://ffmpeg.org/ — FFmpeg contributors. The downloaded builds are GPLv3 builds; EVATEX's Java source remains MIT licensed. The GPL text is included in `assets/evatexbackgroundvideo/engine-licenses/COPYING.GPLv3`.

Windows binaries and build configuration: https://github.com/GyanD/codexffmpeg/releases/tag/9.0.2 and https://www.gyan.dev/ffmpeg/builds/ . FFmpeg source: https://ffmpeg.org/releases/ffmpeg-9.0.2.tar.xz . Build component/configuration details are supplied in Gyan's release archive.

Linux/macOS binaries and build recipes: https://github.com/eugeneware/ffmpeg-static/releases/tag/b6.1.1 and https://github.com/eugeneware/ffmpeg-static . Source and license references for each pinned build are recorded in the engine manifest.

A submitted file is not a published file: CurseForge approval is tracked separately from upload and hash verification. Moderator approval is not guaranteed by these package checks.
