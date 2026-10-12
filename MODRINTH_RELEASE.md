# 1.8.12 — Authenticated playback and support diagnostics

- Authenticate local FFmpeg video and PCM uploads with a fresh per-session secret. Bind only to IPv4 loopback on ephemeral ports, reject invalid/incomplete handshakes and close the listener after the legitimate connection.
- Add **Copy diagnostics** under **Menus** in EVATEX settings. Reports include OS, Java, GPU renderer, active decoder, decoded resolution and configured FPS, excluding private media paths and connection secrets.
- Provide focused playback-report and volunteer hardware-test issue templates.
- Align current project source/issues links and Loader requirements, clean the README and add the official YouTube showcase plus an English full-HD vanilla-menu gallery image.

Fabric Loader 0.18.4+ is supported; Minecraft 26.3 needs 0.19.3+. Install the jar matching your exact Minecraft version. Existing settings and muted defaults are preserved. This is a stable release.

Physical Windows/macOS GPU behavior and proprietary Lunar runtime remain subject to the documented validation limits. This update does not override Windows execution policies.

## Upload metadata

- Release type: release
- Loader: Fabric
- Client only
- Separate exact-version files; Minecraft 1.21.11 first
