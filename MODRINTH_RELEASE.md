# EVATEX Background Video 1.8.0 — Astral

## Release notes

EVATEX now has its own black-and-white Astral interface with custom buttons, smooth hover/screen transitions, stars and falling meteors. No external theme is required. Animation effects can be disabled.

The interface follows Minecraft's selected language, with Turkish and English included. Video playback now continues on multiplayer server selection as well as the title screen. Atlas Client 3.0.3 is supported as a separate mod on Minecraft 1.21.11, in both its custom and vanilla UI modes.

Video quality improvements include full source resolution by default for new settings, Lanczos downscaling, linear sampling and fill/fit framing. FPS conversion preserves playback speed. Fixes include asynchronous video probing, process cleanup, safe frame handoff, config validation, atomic saves and invalid-path handling. Existing `videolar` folders continue to work alongside `videos`.

## Upload metadata

Publish 24 separate Fabric versions. Each version must select only its matching Minecraft release; do not attach all game versions to a single jar.

| Version number | Game version | File |
| --- | --- | --- |
| 1.8.0+mc1.20 | 1.20 | evatex-background-video-1.8.0-mc1.20.jar |
| 1.8.0+mc1.20.1 | 1.20.1 | evatex-background-video-1.8.0-mc1.20.1.jar |
| 1.8.0+mc1.20.2 | 1.20.2 | evatex-background-video-1.8.0-mc1.20.2.jar |
| 1.8.0+mc1.20.3 | 1.20.3 | evatex-background-video-1.8.0-mc1.20.3.jar |
| 1.8.0+mc1.20.4 | 1.20.4 | evatex-background-video-1.8.0-mc1.20.4.jar |
| 1.8.0+mc1.20.5 | 1.20.5 | evatex-background-video-1.8.0-mc1.20.5.jar |
| 1.8.0+mc1.20.6 | 1.20.6 | evatex-background-video-1.8.0-mc1.20.6.jar |
| 1.8.0+mc1.21 | 1.21 | evatex-background-video-1.8.0-mc1.21.jar |
| 1.8.0+mc1.21.1 | 1.21.1 | evatex-background-video-1.8.0-mc1.21.1.jar |
| 1.8.0+mc1.21.2 | 1.21.2 | evatex-background-video-1.8.0-mc1.21.2.jar |
| 1.8.0+mc1.21.3 | 1.21.3 | evatex-background-video-1.8.0-mc1.21.3.jar |
| 1.8.0+mc1.21.4 | 1.21.4 | evatex-background-video-1.8.0-mc1.21.4.jar |
| 1.8.0+mc1.21.5 | 1.21.5 | evatex-background-video-1.8.0-mc1.21.5.jar |
| 1.8.0+mc1.21.6 | 1.21.6 | evatex-background-video-1.8.0-mc1.21.6.jar |
| 1.8.0+mc1.21.7 | 1.21.7 | evatex-background-video-1.8.0-mc1.21.7.jar |
| 1.8.0+mc1.21.8 | 1.21.8 | evatex-background-video-1.8.0-mc1.21.8.jar |
| 1.8.0+mc1.21.9 | 1.21.9 | evatex-background-video-1.8.0-mc1.21.9.jar |
| 1.8.0+mc1.21.10 | 1.21.10 | evatex-background-video-1.8.0-mc1.21.10.jar |
| 1.8.0+mc1.21.11 | 1.21.11 | evatex-background-video-1.8.0-mc1.21.11.jar |
| 1.8.0+mc26.1 | 26.1 | evatex-background-video-1.8.0-mc26.1.jar |
| 1.8.0+mc26.1.1 | 26.1.1 | evatex-background-video-1.8.0-mc26.1.1.jar |
| 1.8.0+mc26.1.2 | 26.1.2 | evatex-background-video-1.8.0-mc26.1.2.jar |
| 1.8.0+mc26.2 | 26.2 | evatex-background-video-1.8.0-mc26.2.jar |
| 1.8.0+mc26.3 | 26.3 | evatex-background-video-1.8.0-mc26.3.jar |

Release channel: release. Environment: client only. Loader: Fabric 0.19.5+. Java: 17+ through 1.20.4, 21+ for 1.20.5 through 1.21.11, 25+ for 26.x. Full Fabric API is not required; the resource modules are embedded. Atlas 3.0.3 and its dependencies are optional, separate, and supported on 1.21.11 only.

Files and SHA256 checksums are in `build/dist`. Windows x64 and Linux x64 FFmpeg binaries and their license/provenance are bundled. macOS and ARM users need system FFmpeg. This document prepares the update; it does not indicate that anything has already been uploaded to Modrinth.
