# EVATEX Background Video — Astral 1.8.3

Replace Minecraft's title screen and multiplayer server selection backgrounds with a looping video. Made by EverVerity.

![Own black-and-white Astral interface in Minecraft](docs/astral-interface.png)

## Windows playback optimization

EVATEX 1.8.3 requests D3D11 hardware video decoding and GPU downscaling on Windows. Unsupported drivers/codecs fall back to hardware decoding without GPU scaling, then software decoding. Linux and macOS retain their existing decoder selection. Video size, frame-rate and audio settings are preserved.

The log records `EVATEX video decoder active: D3D11_SCALE`, `D3D11` or `SOFTWARE` after actual frames arrive. GPU work appears in Windows Task Manager's **Video Decode** graph; the **3D** graph alone does not measure video decoding. If a driver causes trouble, set `hardwareAcceleration` to `false` in `config/evatexbackgroundvideo.json` while the game is closed.

Physical Windows GPU smoothness and CPU use have not yet been measured. GPU decoding still involves CPU colour conversion, frame copies, audio and Minecraft rendering; this release does not promise zero CPU use.

## Installation

Install Fabric Loader **0.19.5 or later** and the jar matching your exact Minecraft version:

| Minecraft releases | Java |
| --- | --- |
| 1.20, 1.20.1, 1.20.2, 1.20.3, 1.20.4 | 17+ |
| 1.20.5, 1.20.6, 1.21 through 1.21.11 | 21+ |
| 26.1, 26.1.1, 26.1.2, 26.2, 26.3 | 25+ |

There are 24 separate builds covering every stable Java Edition release from 1.20 through 26.3. See `COMPATIBILITY.md` for the complete file list and validation results. Use one EVATEX jar at a time and remove the old EVATEX jar when updating. Exact-version metadata prevents loading the wrong API adapter.

Fabric API is not required by EVATEX; its resource-loading modules are embedded. FFmpeg engines are bundled for Windows x64, Linux x64/ARM64 and macOS Intel/Apple Silicon. No separate FFmpeg installation is required on these targets. A compatible system `ffmpeg` is used as a fallback if the bundled engine cannot run. Minecraft versions before 1.20, snapshots and future versions are outside this release's support range.

## Use

1. Place MP4, MOV, WebM, MKV, AVI or M4V files in `videos` inside the game directory. Existing `videolar` folders and saved paths are also supported.
2. Open **Video Menu** on the title screen and select a clip.
3. Adjust sound, resolution, decode-size limit, frame rate, framing (fill or fit), darkening and vignette. The mod has its own black-and-white Astral interface, with optional screen fades, hover transitions and falling meteors. Separate library and playback pages prevent controls from overlapping at larger GUI scales; narrow screens use tabs. Folder opening runs asynchronously with Windows, Linux and macOS commands. Settings save automatically. Refresh rescans files after adding videos.

The Astral interface uses dark panels and black/white accents, with keyboard focus and native narration support. Text follows Minecraft's selected language. Turkish and English are included; other languages use the English fallback.

New configurations use 100% scale with a safe 1920×1080 output limit, preserving aspect ratio. Higher-resolution sources are downscaled before Java-side frame storage. The decode-size control offers 720p through 4320p; an additional memory bound limits frame allocations. Decoding large source files still requires sufficient hardware. Existing settings are preserved. Resolution controls decoded pixel size, not framing. Lanczos downscaling and linear texture sampling improve clarity. FPS conversion preserves playback speed. Playback continues between the title screen, multiplayer server selection and video settings, and stops outside these screens.

If a clip fails, the vanilla panorama remains available. Check `logs/latest.log` for FFmpeg or file errors. Sound is muted by default and can be toggled live without restarting the video. The Astral slider adjusts video volume from 0–100% independently of the system/game volume. Files without an audio track remain silent. Audio-device failures allow video playback to continue.

## Atlas Client compatibility

On Minecraft 1.21.11, EVATEX supports Atlas Client **3.0.3** as a separate installed mod. Optional mixins display the video behind Atlas’s custom main menu and server selection UI, and expose a translated Video Menu shortcut. Atlas’s vanilla UI mode is also supported. Its jar and configuration are not modified or bundled. When no playable video is selected, Atlas keeps its own background. Other Atlas versions do not load these optional hooks. Atlas still requires its own Fabric API and Player Animation Library dependencies.

## Build

Requires Python 3 and network access on the first build. Use JDK 21+ for 1.20/1.21 builds and JDK 25 for 26.x, via `JAVA_HOME` or `java`/`javac` on PATH.

```bash
bash scripts/fetch-ffmpeg.sh
python3 scripts/build.py --minecraft 1.21.11
python3 scripts/build.py --minecraft 1.20.1
# With JAVA_HOME pointing to JDK 25:
python3 scripts/build.py --all
```

The build script downloads Minecraft's official client and libraries and Fabric Loader/Mixin. Through 1.21.11 it uses intermediary mappings and Tiny Remapper; 26.x uses Minecraft's native names. Shared source trees are adapted to each version's actual GUI/texture APIs by `scripts/ports.py`. Reference Mojang mappings are development inputs and are not packaged. The launch classpath uses Fabric's ASM revision, replacing older vanilla ASM entries. Output jars and SHA256 files are in `build/dist`. Mojang artifacts are checked against published SHA1 hashes.

For a small development jar that requires system FFmpeg, add `--system-ffmpeg`. Do not distribute that build as a bundled release.

## Verification

```bash
python3 scripts/test.py --minecraft 1.21.11
python3 scripts/qa_assets.py 1.21.11
python3 scripts/smoke.py --minecraft 1.21.11 --assets build/qa-assets --bundled --quick
```

Regression checks cover corrupt config values, pixel channel ordering, frame ownership, playback timing at different FPS settings, looping, resolution bounds and decoder shutdown. In-game checks and remaining limitations are recorded in `VALIDATION.md`.

## License

Mod code: MIT (see `LICENSE`). Bundled FFmpeg executables: GPL v3, with `COPYING.GPLv3` included in the jar. The fetch script records binary origins and upstream build/source links in `assets/videomenu/ffmpeg/PROVENANCE.json`. FFmpeg is developed by the [FFmpeg project](https://ffmpeg.org/).

The Video Menu shortcut is centred near the bottom of the title screen. Its position follows the game window and GUI scale, independently of launcher paths. Atlas Client 3.0.3 has a separate matching shortcut; unrelated custom menu replacements are not universally supported.
