# EVATEX Background Video 1.8.1 — Astral

## Release notes

This update adds live video sound controls, a safer default decode size, responsive Astral settings and portable folder opening.

Sound is muted by default and can be enabled, disabled or adjusted from 0–100% without restarting playback. The Astral volume slider affects only the video. Video continues if the audio device is unavailable. The interface follows the game language, uses separate library/settings pages at large GUI scales, and switches to tabs on narrow screens.

High-resolution source files are downscaled to a safe 1080p output by default. Adjustable limits and a memory bound control Java-side frame storage; FFmpeg worker counts are limited to reduce resource pressure. Large source files still require suitable hardware.

FFmpeg is included for Windows x64, Linux x64/ARM64 and macOS Intel/Apple Silicon. Folder opening uses native operating-system commands outside the render thread, with Linux fallbacks and inherited native-library environment cleanup.

Twenty custom title splash messages are added to the existing Minecraft/resource-pack pool, including the requested creator, motivational and anime messages.

All 24 exact-version Fabric builds from Minecraft 1.20 through 26.3 remain supported. Atlas Client 3.0.3 remains a separate optional mod on 1.21.11. Tests use the supplied 1080p clip on Linux, including live audio toggling, looping and menu continuity. No 4K or 8K videos are played during validation; high-resolution bounds are checked using dimensions only. Windows and macOS binaries are packaged and checksum-verified, but their runtime behavior is not tested on this Linux computer.

## Upload metadata

Publish 24 separate Fabric versions. Each version must select only its matching Minecraft release; do not attach all game versions to a single jar.

| Version number | Game version | File |
| --- | --- | --- |
| 1.8.1+mc1.20 | 1.20 | evatex-background-video-1.8.1-mc1.20.jar |
| 1.8.1+mc1.20.1 | 1.20.1 | evatex-background-video-1.8.1-mc1.20.1.jar |
| 1.8.1+mc1.20.2 | 1.20.2 | evatex-background-video-1.8.1-mc1.20.2.jar |
| 1.8.1+mc1.20.3 | 1.20.3 | evatex-background-video-1.8.1-mc1.20.3.jar |
| 1.8.1+mc1.20.4 | 1.20.4 | evatex-background-video-1.8.1-mc1.20.4.jar |
| 1.8.1+mc1.20.5 | 1.20.5 | evatex-background-video-1.8.1-mc1.20.5.jar |
| 1.8.1+mc1.20.6 | 1.20.6 | evatex-background-video-1.8.1-mc1.20.6.jar |
| 1.8.1+mc1.21 | 1.21 | evatex-background-video-1.8.1-mc1.21.jar |
| 1.8.1+mc1.21.1 | 1.21.1 | evatex-background-video-1.8.1-mc1.21.1.jar |
| 1.8.1+mc1.21.2 | 1.21.2 | evatex-background-video-1.8.1-mc1.21.2.jar |
| 1.8.1+mc1.21.3 | 1.21.3 | evatex-background-video-1.8.1-mc1.21.3.jar |
| 1.8.1+mc1.21.4 | 1.21.4 | evatex-background-video-1.8.1-mc1.21.4.jar |
| 1.8.1+mc1.21.5 | 1.21.5 | evatex-background-video-1.8.1-mc1.21.5.jar |
| 1.8.1+mc1.21.6 | 1.21.6 | evatex-background-video-1.8.1-mc1.21.6.jar |
| 1.8.1+mc1.21.7 | 1.21.7 | evatex-background-video-1.8.1-mc1.21.7.jar |
| 1.8.1+mc1.21.8 | 1.21.8 | evatex-background-video-1.8.1-mc1.21.8.jar |
| 1.8.1+mc1.21.9 | 1.21.9 | evatex-background-video-1.8.1-mc1.21.9.jar |
| 1.8.1+mc1.21.10 | 1.21.10 | evatex-background-video-1.8.1-mc1.21.10.jar |
| 1.8.1+mc1.21.11 | 1.21.11 | evatex-background-video-1.8.1-mc1.21.11.jar |
| 1.8.1+mc26.1 | 26.1 | evatex-background-video-1.8.1-mc26.1.jar |
| 1.8.1+mc26.1.1 | 26.1.1 | evatex-background-video-1.8.1-mc26.1.1.jar |
| 1.8.1+mc26.1.2 | 26.1.2 | evatex-background-video-1.8.1-mc26.1.2.jar |
| 1.8.1+mc26.2 | 26.2 | evatex-background-video-1.8.1-mc26.2.jar |
| 1.8.1+mc26.3 | 26.3 | evatex-background-video-1.8.1-mc26.3.jar |

Release channel: release. Environment: client only. Loader: Fabric 0.19.5+. Java: 17+ through 1.20.4, 21+ for 1.20.5 through 1.21.11, 25+ for 26.x. Full Fabric API is not required; the resource modules are embedded. Atlas 3.0.3 and its dependencies are optional, separate, and supported on 1.21.11 only.

Files and SHA256 checksums are in `build/dist`. Windows x64, Linux x64/ARM64 and macOS Intel/Apple Silicon engines and their license/provenance are bundled. This document prepares the update; it does not indicate that anything has already been uploaded to Modrinth.
