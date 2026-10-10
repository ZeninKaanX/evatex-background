# EVATEX Background Video 1.8.2 — Clean menu shortcut

## Release notes

Video Menu now sits at the lower centre of the title screen in both Minecraft and Atlas Client 3.0.3, with a smaller button. Native windows below 220 GUI pixels high use the top fallback to avoid the crowded vanilla controls. The Atlas shortcut scales with the game window and has explicit local padding and centred text, keeping it clear of the account card. Minecraft buttons use the actual font height for vertical label alignment.

Placement depends on the game window and GUI scale rather than the launcher or installation path. All 24 exact-version Fabric builds remain available. Custom menus provided by other mods may need their own compatibility bridge; no claim is made that every launcher or menu replacement has been tested.

## Upload metadata

Publish 24 separate Fabric versions. Each version must select only its matching Minecraft release; do not attach all game versions to a single jar.

| Version number | Game version | File |
| --- | --- | --- |
| 1.8.2+mc1.20 | 1.20 | evatex-background-video-1.8.2-mc1.20.jar |
| 1.8.2+mc1.20.1 | 1.20.1 | evatex-background-video-1.8.2-mc1.20.1.jar |
| 1.8.2+mc1.20.2 | 1.20.2 | evatex-background-video-1.8.2-mc1.20.2.jar |
| 1.8.2+mc1.20.3 | 1.20.3 | evatex-background-video-1.8.2-mc1.20.3.jar |
| 1.8.2+mc1.20.4 | 1.20.4 | evatex-background-video-1.8.2-mc1.20.4.jar |
| 1.8.2+mc1.20.5 | 1.20.5 | evatex-background-video-1.8.2-mc1.20.5.jar |
| 1.8.2+mc1.20.6 | 1.20.6 | evatex-background-video-1.8.2-mc1.20.6.jar |
| 1.8.2+mc1.21 | 1.21 | evatex-background-video-1.8.2-mc1.21.jar |
| 1.8.2+mc1.21.1 | 1.21.1 | evatex-background-video-1.8.2-mc1.21.1.jar |
| 1.8.2+mc1.21.2 | 1.21.2 | evatex-background-video-1.8.2-mc1.21.2.jar |
| 1.8.2+mc1.21.3 | 1.21.3 | evatex-background-video-1.8.2-mc1.21.3.jar |
| 1.8.2+mc1.21.4 | 1.21.4 | evatex-background-video-1.8.2-mc1.21.4.jar |
| 1.8.2+mc1.21.5 | 1.21.5 | evatex-background-video-1.8.2-mc1.21.5.jar |
| 1.8.2+mc1.21.6 | 1.21.6 | evatex-background-video-1.8.2-mc1.21.6.jar |
| 1.8.2+mc1.21.7 | 1.21.7 | evatex-background-video-1.8.2-mc1.21.7.jar |
| 1.8.2+mc1.21.8 | 1.21.8 | evatex-background-video-1.8.2-mc1.21.8.jar |
| 1.8.2+mc1.21.9 | 1.21.9 | evatex-background-video-1.8.2-mc1.21.9.jar |
| 1.8.2+mc1.21.10 | 1.21.10 | evatex-background-video-1.8.2-mc1.21.10.jar |
| 1.8.2+mc1.21.11 | 1.21.11 | evatex-background-video-1.8.2-mc1.21.11.jar |
| 1.8.2+mc26.1 | 26.1 | evatex-background-video-1.8.2-mc26.1.jar |
| 1.8.2+mc26.1.1 | 26.1.1 | evatex-background-video-1.8.2-mc26.1.1.jar |
| 1.8.2+mc26.1.2 | 26.1.2 | evatex-background-video-1.8.2-mc26.1.2.jar |
| 1.8.2+mc26.2 | 26.2 | evatex-background-video-1.8.2-mc26.2.jar |
| 1.8.2+mc26.3 | 26.3 | evatex-background-video-1.8.2-mc26.3.jar |

Release channel: release. Environment: client only. Loader: Fabric 0.19.5+. Java: 17+ through 1.20.4, 21+ for 1.20.5 through 1.21.11, 25+ for 26.x. Full Fabric API is not required; the resource modules are embedded. Atlas 3.0.3 and its dependencies are optional, separate, and supported on 1.21.11 only.

Files and SHA256 checksums are in `build/dist`. Windows x64, Linux x64/ARM64 and macOS Intel/Apple Silicon engines and their license/provenance are bundled. This document prepares the update; it does not indicate that anything has already been uploaded to Modrinth.
