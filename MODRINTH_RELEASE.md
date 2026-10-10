# EVATEX Background Video 1.8.3 — Windows playback optimization

## Release notes

Windows playback now requests D3D11VA hardware decoding. Supported GPUs also downscale the video before transferring frames to Minecraft, reducing CPU work and large-frame readback. If GPU scaling is unavailable, EVATEX tries hardware decoding with software scaling; unsupported devices, drivers and codecs automatically fall back to CPU decoding. A first-frame deadline prevents indefinite waits on a broken driver.

Bulk pixel conversion reduces per-frame Java work. Configured frame rate, resolution and live audio controls are preserved. Linux and macOS keep their existing decoder selection. The compact Astral shortcut remains at the lower centre in Minecraft and the lower right in Atlas Client 3.0.3.

The active decoder is recorded in the game log after frames arrive. GPU activity should be checked under **Video Decode** in Windows Task Manager. Colour conversion, frame transfer, sound and Minecraft still use CPU resources. Physical Windows GPU smoothness and CPU reduction depend on hardware, driver and codec; no fixed performance improvement is guaranteed.

## Upload metadata

Publish 24 separate Fabric versions. Each version must select only its matching Minecraft release; do not attach all game versions to a single jar.

| Version number | Game version | File |
| --- | --- | --- |
| 1.8.3+mc1.20 | 1.20 | evatex-background-video-1.8.3-mc1.20.jar |
| 1.8.3+mc1.20.1 | 1.20.1 | evatex-background-video-1.8.3-mc1.20.1.jar |
| 1.8.3+mc1.20.2 | 1.20.2 | evatex-background-video-1.8.3-mc1.20.2.jar |
| 1.8.3+mc1.20.3 | 1.20.3 | evatex-background-video-1.8.3-mc1.20.3.jar |
| 1.8.3+mc1.20.4 | 1.20.4 | evatex-background-video-1.8.3-mc1.20.4.jar |
| 1.8.3+mc1.20.5 | 1.20.5 | evatex-background-video-1.8.3-mc1.20.5.jar |
| 1.8.3+mc1.20.6 | 1.20.6 | evatex-background-video-1.8.3-mc1.20.6.jar |
| 1.8.3+mc1.21 | 1.21 | evatex-background-video-1.8.3-mc1.21.jar |
| 1.8.3+mc1.21.1 | 1.21.1 | evatex-background-video-1.8.3-mc1.21.1.jar |
| 1.8.3+mc1.21.2 | 1.21.2 | evatex-background-video-1.8.3-mc1.21.2.jar |
| 1.8.3+mc1.21.3 | 1.21.3 | evatex-background-video-1.8.3-mc1.21.3.jar |
| 1.8.3+mc1.21.4 | 1.21.4 | evatex-background-video-1.8.3-mc1.21.4.jar |
| 1.8.3+mc1.21.5 | 1.21.5 | evatex-background-video-1.8.3-mc1.21.5.jar |
| 1.8.3+mc1.21.6 | 1.21.6 | evatex-background-video-1.8.3-mc1.21.6.jar |
| 1.8.3+mc1.21.7 | 1.21.7 | evatex-background-video-1.8.3-mc1.21.7.jar |
| 1.8.3+mc1.21.8 | 1.21.8 | evatex-background-video-1.8.3-mc1.21.8.jar |
| 1.8.3+mc1.21.9 | 1.21.9 | evatex-background-video-1.8.3-mc1.21.9.jar |
| 1.8.3+mc1.21.10 | 1.21.10 | evatex-background-video-1.8.3-mc1.21.10.jar |
| 1.8.3+mc1.21.11 | 1.21.11 | evatex-background-video-1.8.3-mc1.21.11.jar |
| 1.8.3+mc26.1 | 26.1 | evatex-background-video-1.8.3-mc26.1.jar |
| 1.8.3+mc26.1.1 | 26.1.1 | evatex-background-video-1.8.3-mc26.1.1.jar |
| 1.8.3+mc26.1.2 | 26.1.2 | evatex-background-video-1.8.3-mc26.1.2.jar |
| 1.8.3+mc26.2 | 26.2 | evatex-background-video-1.8.3-mc26.2.jar |
| 1.8.3+mc26.3 | 26.3 | evatex-background-video-1.8.3-mc26.3.jar |

Release channel: release. Environment: client only. Loader: Fabric 0.19.5+. Java: 17+ through 1.20.4, 21+ for 1.20.5 through 1.21.11, 25+ for 26.x. Full Fabric API is not required; the resource modules are embedded. Atlas 3.0.3 and its dependencies are optional, separate, and supported on 1.21.11 only.

Files and SHA256 checksums are in `build/dist`. Windows x64, Linux x64/ARM64 and macOS Intel/Apple Silicon engines and their license/provenance are bundled. This document prepares the update; it does not indicate that anything has already been uploaded to Modrinth.
