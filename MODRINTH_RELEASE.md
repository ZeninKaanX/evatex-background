# 1.8.11 — Client access and playback diagnostics

- Support Fabric Loader 0.18.4+ on Minecraft 1.20–26.2, and Loader 0.19.3+ on Minecraft 26.3. Include Mixin Extras 0.5.5 to fix the reproduced older-Loader startup failure.
- Improve shortcut placement around public custom controls: visibility, fractional edges, nested children and bounded traversal.
- Open EVATEX settings from Mod Menu as an alternative to the title-screen button. For Lunar Client, use the Fabric add-on and Vanilla (Overlay) menu style to expose native mod buttons. Full proprietary Lunar runtime testing is pending.
- Decode standard PNG/JPEG/BMP photos with Java's bounded image reader, without an external FFmpeg process.
- Show separate messages for blocked and missing playback engines. Preserve the permission-denial cause if system FFmpeg is also missing; avoid a permanently failed system-engine fallback.
- Keep the chosen mod icon, clean Astral menus, video brightness, sharp text, meteors and focus pause/resume behavior.

The supplied Windows game log reports `CreateProcess error=5` for bundled FFmpeg and `error=2` for unavailable system FFmpeg. This release does not override Windows execution policies. The reported settings crash has no crash stack in the supplied files and is not claimed to be resolved.

## Upload metadata

Type: Release. Loader: Fabric. Upload Minecraft 1.21.11 first; use one exact Minecraft version for each jar.
