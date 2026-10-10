# EVATEX Background Video — Astral 1.8.0

Replace Minecraft's title screen and multiplayer server selection backgrounds with a looping video. Made by EverVerity.

![Own black-and-white Astral interface in Minecraft](docs/astral-interface.png)

## Installation

Install Fabric Loader **0.19.5 or later** and the jar matching your Minecraft version:

| Build | Minecraft | Java |
| --- | --- | --- |
| `mc1.20.1` | 1.20.1 | 17+ |
| `mc1.21.11` | 1.21.11 | 21+ |

Fabric API is not required. These are separate builds; other game versions are not declared compatible. Windows x64 and Linux x64 FFmpeg binaries are bundled. macOS and ARM devices require an appropriate system `ffmpeg` on PATH.

## Use

1. Place MP4, MOV, WebM, MKV, AVI or M4V files in `videos` inside the game directory. Existing `videolar` folders and saved paths are also supported.
2. Open **Video Menu** on the title screen and select a clip.
3. Adjust resolution, frame rate, framing (fill or fit), darkening and vignette. The mod has its own black-and-white Astral interface, with optional screen fades, hover transitions and falling meteors. Settings save automatically. Refresh rescans files after adding videos.

The Astral interface uses dark panels and black/white accents, with keyboard focus and native narration support. Text follows Minecraft's selected language. Turkish and English are included; other languages use the English fallback.

New configurations decode at 100% source resolution, bounded to 3840×2160 while preserving aspect ratio. Existing settings are preserved. Resolution controls decoded pixel size, not framing. Lanczos downscaling and linear texture sampling improve clarity. FPS conversion preserves playback speed. Playback continues between the title screen, multiplayer server selection and video settings, and stops outside these screens.

If a clip fails, the vanilla panorama remains available. Check `logs/latest.log` for FFmpeg or file errors. Background video has no audio.

## Atlas Client compatibility

On Minecraft 1.21.11, EVATEX supports Atlas Client **3.0.3** as a separate installed mod. Optional mixins display the video behind Atlas’s custom main menu and server selection UI, and expose a translated Video Menu shortcut. Atlas’s vanilla UI mode is also supported. Its jar and configuration are not modified or bundled. When no playable video is selected, Atlas keeps its own background. Other Atlas versions do not load these optional hooks. Atlas still requires its own Fabric API and Player Animation Library dependencies.

## Build

Requires Python 3, network access on the first build, and JDK 21 (`JAVA_HOME` or `java`/`javac` on PATH).

```bash
bash scripts/fetch-ffmpeg.sh
python3 scripts/build.py --minecraft 1.21.11
python3 scripts/build.py --minecraft 1.20.1
```

The build script downloads Minecraft's official client and libraries, Fabric Loader/Mixin and intermediary mappings, remaps the client with Tiny Remapper, and compiles the appropriate source tree. Output jars and SHA256 files are in `build/dist`. Mojang artifacts are checked against published SHA1 hashes.

For a small development jar that requires system FFmpeg, add `--system-ffmpeg`. Do not distribute that build as a bundled release.

## Verification

```bash
python3 scripts/test.py
```

Regression checks cover corrupt config values, pixel channel ordering, frame ownership, playback timing at different FPS settings, looping, resolution bounds and decoder shutdown. In-game checks and remaining limitations are recorded in `VALIDATION.md`.

## License

Mod code: MIT (see `LICENSE`). Bundled FFmpeg executables: GPL v3, with `COPYING.GPLv3` included in the jar. The fetch script records binary origins and upstream build/source links in `assets/videomenu/ffmpeg/PROVENANCE.json`. FFmpeg is developed by the [FFmpeg project](https://ffmpeg.org/).
