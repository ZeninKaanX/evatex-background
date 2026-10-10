# EVATEX Background Video

Use your own video as the background of Minecraft's title screen and multiplayer server selection. Choose a clip and adjust playback from the in-game Video Menu.

Windows automatically attempts GPU video decoding and GPU downscaling. Unsupported drivers or video formats fall back to software decoding. Performance depends on the video, graphics card and driver.

## Features

- Custom black-and-white Astral interface with optional transitions, stars and falling meteors.
- Continuous playback across the title screen, server selection and video settings.
- Resolution and frame-rate controls, fill/fit framing, darkening and vignette.
- Safe 1080p decoded output by default, with adjustable scale and decode-size limits.
- Turkish and English translations following Minecraft's selected language, with English fallback.
- Live sound on/off and volume controls, muted by default.
- Responsive library and settings pages, with tabs on narrow screens.
- Automatic saving and an option to disable video or interface animations.

## Installation

1. Install Fabric Loader **0.19.5 or newer**.
2. Download the EVATEX file matching your exact Minecraft version.
3. Place it in your game directory's `mods` folder and remove any previous EVATEX jar.
4. Add videos to the `videos` folder in your game directory. Existing `videolar` folders also work.
5. Open **Video Menu** from the title screen and select a video.

Common formats include MP4, WebM, MKV, MOV and AVI. Sound is muted by default; enable it in Video Menu. Settings are saved automatically. Lower resolution or frame rate can reduce resource use on slower computers.

## Compatibility

EVATEX 1.8.2 provides separate Fabric builds for all 24 stable Minecraft Java Edition releases from **1.20 through 26.3**. Install one matching jar at a time.

| Minecraft | Java |
| --- | --- |
| 1.20–1.20.4 | 17 or newer |
| 1.20.5–1.21.11 | 21 or newer |
| 26.1–26.3 | 25 or newer |

Full Fabric API is not required by EVATEX; the necessary resource-loading modules are included. FFmpeg is bundled for Windows x64, Linux x64/ARM64 and macOS Intel/Apple Silicon. These platforms do not require a separate FFmpeg installation.

**Atlas Client 3.0.3** is supported as a separate mod on **Minecraft 1.21.11**, in both custom and vanilla menu modes. Atlas requires its own dependencies. Earlier Minecraft releases, snapshots and other mod loaders are outside this release's support range.

## Atlas Client integration

EVATEX includes a dedicated integration for **Atlas Client 3.0.3 on Minecraft 1.21.11**. Your video plays behind Atlas’s custom title menu and multiplayer server selection. A compact Astral **Video Menu** button sits at the lower right, with centred text and its own black-and-white styling.

Atlas’s menu layout and settings are preserved. EVATEX remains a separate mod with its own video controls; the two mods are not combined. Atlas’s vanilla menu mode also works. In the normal Minecraft title screen, the EVATEX shortcut is centred near the bottom.

Install Atlas and its required dependencies separately. This integration targets the version listed above; compatibility with every custom menu or launcher has not been verified.

## Links and license

[Source code](https://github.com/ZeninKaanX/evatex-background-video-1.6.0) · [Report an issue](https://github.com/ZeninKaanX/evatex-background-video-1.6.0/issues)

EVATEX is developed by EverVerity and released under the MIT license. Bundled FFmpeg binaries use GPLv3; license and provenance files are included in the jar. [FFmpeg source](https://ffmpeg.org/download.html) and build information are available from [BtbN](https://github.com/BtbN/FFmpeg-Builds) [Gyan](https://www.gyan.dev/ffmpeg/builds/) and [ffmpeg-static](https://github.com/eugeneware/ffmpeg-static).
