# EVATEX Background Video 1.8.0 — Astral

## Release notes

EVATEX now has its own black-and-white Astral interface with custom buttons, smooth hover/screen transitions, stars and falling meteors. No external theme is required. Animation effects can be disabled.

The interface follows Minecraft's selected language, with Turkish and English included. Video playback now continues on multiplayer server selection as well as the title screen. Atlas Client 3.0.3 is supported as a separate mod on Minecraft 1.21.11, in both its custom and vanilla UI modes.

Video quality improvements include full source resolution by default for new settings, Lanczos downscaling, linear sampling and fill/fit framing. FPS conversion preserves playback speed. Fixes include asynchronous video probing, process cleanup, safe frame handoff, config validation, atomic saves and invalid-path handling. Existing `videolar` folders continue to work alongside `videos`.

## Upload metadata

Publish two separate Fabric versions, so each file is attached to its exact supported Minecraft version:

| Version number | Game version | File |
| --- | --- | --- |
| 1.8.0+mc1.21.11 | 1.21.11 | evatex-background-video-1.8.0-mc1.21.11.jar |
| 1.8.0+mc1.20.1 | 1.20.1 | evatex-background-video-1.8.0-mc1.20.1.jar |

Release channel: release. Environment: client only. Loader: Fabric 0.19.5+. Java: 21+ for 1.21.11; 17+ for 1.20.1. Fabric API is not required by EVATEX; its resource modules are embedded. Atlas and its dependencies are optional and separate. No other Minecraft versions should be selected for these files.

Files and SHA256 checksums are in `build/dist`. Windows x64 and Linux x64 FFmpeg binaries and their license/provenance are bundled. macOS and ARM users need system FFmpeg. This document prepares the update; it does not indicate that anything has already been uploaded to Modrinth.
