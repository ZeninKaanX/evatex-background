# EVATEX-Background — Astral 1.8.11

Replace Minecraft's title screen, multiplayer server selection, world selection, resource packs and supported main-menu options backgrounds with a video, looping GIF or photo. Made by EverVerity.

![Own black-and-white Astral interface in Minecraft](docs/astral-interface.png)

## Backgrounds and Windows playback

Photos are decoded once; the decoder then exits while the texture remains visible. Animated GIFs loop at their source timing, sampled at the configured presentation frame rate. PNG/JPEG/BMP are treated as static photos; animated PNG/WebP are not advertised as supported.

The production frame pipeline reuses a bounded pool and a persistent render staging buffer instead of allocating a full pixel array every frame. Windows transports decoded pixels over a buffered loopback TCP connection to avoid small anonymous-process-pipe buffers. The connection is local and closes with the decoder.

EVATEX 1.8.4 requests D3D11 hardware video decoding and GPU downscaling on Windows. Unsupported drivers/codecs fall back to hardware decoding without GPU scaling, then software decoding. Linux and macOS retain their existing decoder selection. Video size, frame-rate and audio settings are preserved.

The log records `EVATEX video decoder active: D3D11_SCALE`, `D3D11` or `SOFTWARE` after actual frames arrive. GPU work appears in Windows Task Manager's **Video Decode** graph; the **3D** graph alone does not measure video decoding. If a driver causes trouble, set `hardwareAcceleration` to `false` in `config/evatexbackgroundvideo.json` while the game is closed.

A returned physical Windows capture confirms D3D11 decoding on an RX 580. Full Minecraft/Atlas rendering smoothness and before/after gains remain unverified; the latest returned archive repeats the original capture. GPU decoding still involves CPU colour conversion, frame copies, audio and Minecraft rendering; this release does not promise zero CPU use.

## Installation

Install Fabric Loader **0.18.4 or later** (Minecraft 26.3 requires **0.19.3 or later**) and the jar matching your exact Minecraft version:

| Minecraft releases | Java |
| --- | --- |
| 1.20, 1.20.1, 1.20.2, 1.20.3, 1.20.4 | 17+ |
| 1.20.5, 1.20.6, 1.21 through 1.21.11 | 21+ |
| 26.1, 26.1.1, 26.1.2, 26.2, 26.3 | 25+ |

There are 24 separate builds covering every stable Java Edition release from 1.20 through 26.3. See `COMPATIBILITY.md` for the complete file list and validation results. Use one EVATEX jar at a time and remove the old EVATEX jar when updating. Exact-version metadata prevents loading the wrong API adapter.

Fabric API is not required by EVATEX; its resource-loading modules are embedded. FFmpeg engines are bundled for Windows x64, Linux x64/ARM64 and macOS Intel/Apple Silicon. No separate FFmpeg installation is required on these targets. A compatible system `ffmpeg` is used as a fallback if the bundled engine cannot run. Minecraft versions before 1.20, snapshots and future versions are outside this release's support range.

## Use

1. Place videos (MP4, MOV, WebM, MKV, AVI, M4V), animated GIFs or photos (PNG, JPG/JPEG, BMP) in `videos` or `backgrounds` inside the game directory. Existing `videolar` folders and saved paths are also supported.
2. Open **Backgrounds** on the title screen and select a background.
3. Adjust sound, resolution, decode-size limit, frame rate, framing (fill or fit), darkening and vignette. The mod has its own black-and-white Astral interface, with optional screen fades, hover transitions and falling meteors. Separate library and playback pages prevent controls from overlapping at larger GUI scales; narrow screens use tabs. Folder opening runs asynchronously with Windows, Linux and macOS commands. Settings save automatically. Refresh rescans files after adding videos.

The Astral interface uses dark panels and black/white accents, with keyboard focus and native narration support. Text follows Minecraft's selected language. Turkish and English are included; other languages use the English fallback.

New configurations use 100% scale with a safe 1920×1080 output limit, preserving aspect ratio. Higher-resolution sources are downscaled before Java-side frame storage. The decode-size control offers 720p through 4320p; an additional memory bound limits frame allocations. Decoding large source files still requires sufficient hardware. Existing settings are preserved. Resolution controls decoded pixel size, not framing. Linear texture sampling improves clarity. Moving Windows backgrounds use bicubic resampling; static photos and other operating systems retain Lanczos. FPS conversion preserves playback speed. Playback continues between the title screen, multiplayer server selection, world selection, main-menu options/submenus and background settings. In EVATEX settings, **World selection**, **Options menus** and **Menu meteors** are separate switches. These menus add a light dark overlay and optional Astral stars/meteors. Disabling a menu restores its vanilla background. Options opened with a world loaded keep the normal in-game background, and decoding stops outside enabled menu screens.

If a clip fails, the vanilla panorama remains available. Check `logs/latest.log` for FFmpeg or file errors. Sound is muted by default and can be toggled live without restarting the video. The Astral slider adjusts video volume from 0–100% independently of the system/game volume. Files without an audio track remain silent. Audio-device failures allow video playback to continue.

The optional Astral style also presents native multiplayer, world selection and main options with translucent monochrome panels, clear text and original Minecraft actions. Selected servers and worlds have a details card. Small windows keep the native layout; if another mod adds a control, EVATEX restores the original layout. Third-party custom screens retain their own presentation.

## Atlas Client compatibility

On Minecraft 1.21.11, EVATEX supports Atlas Client **3.0.3** as a separate installed mod. Optional mixins display the video behind Atlas’s custom main menu and server selection UI, and expose a translated Backgrounds shortcut. Atlas’s vanilla UI mode is also supported. Its jar and configuration are not modified or bundled. When no playable video is selected, Atlas keeps its own background. Other Atlas versions do not load these optional hooks. Atlas still requires its own Fabric API and Player Animation Library dependencies.

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

The Backgrounds shortcut prefers the lower centre and automatically avoids visible native widgets and custom widgets exposing public bounds, including controls added after menu initialization. It recalculates when controls, window dimensions or GUI scale change. A slim vertical side shortcut with rotated text is available when horizontal placement would be cramped. Atlas Client 3.0.3 keeps its separate matching lower-right shortcut. Clients that draw menus without exposed widget bounds or bypass native widget rendering still require a compatible overlay; universal third-party menu support is not claimed. Lunar provides a [Vanilla Overlay](https://support.lunarclient.com/solutions/enabling-vanilla-overlay) for external mod menu buttons.

## Optional Astral home

EVATEX includes its own black-and-white home menu with large singleplayer and multiplayer tiles, profile information, EVATEX background controls and native game settings. Other native/mod title actions are retained in the footer, with pagination when needed. The selected background and optional meteors continue behind the menu.

Enable **EVATEX main menu** under **Menus** in EVATEX settings. It is **disabled by default**, including when updating an existing configuration. Disabling it restores the normal or installed client title screen. The menu is an original EVATEX implementation inspired by spacious client layouts and does not bundle Atlas code, themes or assets.

The development story behind this release is recorded in the [EVATEX 1.8.6 changelog](CHANGELOG.md).
