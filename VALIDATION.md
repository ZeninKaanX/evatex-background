# Astral 1.8.0 validation

Test environment: Linux x64, Java 21, Fabric Loader 0.19.5, real Minecraft client, real bundled FFmpeg. Test harnesses run in isolated game directories and are excluded from release jars.

## Automated regressions

`scripts/test.py` passes config normalization, invalid file handling, RGBA/ABGR channel ordering, frame ownership, native-resolution and 4K bounds, English/Turkish key parity, real FFmpeg playback at 15 and 60 FPS, looping and shutdown. The timing fixture publishes 45 frames at 15 FPS in about 2.93 seconds and 180 frames at 60 FPS in about 2.98 seconds. These checks test playback speed, not a hardware performance benchmark.

## In-game checks

| Minecraft | Installed mods | Result |
| --- | --- | --- |
| 1.20.1 | EVATEX with embedded Fabric resource modules | Title, Turkish Astral UI, multiplayer video, fallback and clean exit |
| 1.21.11 | EVATEX with embedded Fabric resource modules | Title, Turkish Astral UI, multiplayer video, fallback and clean exit |
| 1.21.11 | EVATEX + Atlas Client 3.0.3 + Fabric API + Player Animation Library | Atlas custom and vanilla UI, translated shortcut, video settings, multiplayer video, fallback and clean exit |

GUI capture paths and launch logs are in `build/smoke-<version>-standalone` and `build/smoke-1.21.11-atlas`. The GUI test checks decoder continuity, loaded Turkish translations and graceful exit. Screenshots are also visually inspected; an active decoder alone does not prove video visibility. GUI sessions share the desktop and allow normal user interaction.

The first incomplete 1.20.1 language test had missing Minecraft asset packs in its isolated asset cache. After downloading those official resources, the same release jar passed. Offline Realms authentication and unavailable narrator/audio warnings are unrelated to EVATEX.

## Scope and remaining limits

Only 1.20.1 and 1.21.11 are declared compatible. Exact-version metadata prevents loading incompatible mixins on other Minecraft APIs. The current Minecraft 26.x named APIs and Java 25 need a separate port; they are not supported by these jars. No claim is made for every Minecraft version or every hardware configuration.

Atlas integration is optional and restricted to Atlas 3.0.3. It uses Atlas's existing ImGui context and the existing video GPU texture; Atlas code, settings and binaries remain separate. Its dependencies must still be installed.

Meteor effects have a fixed 48-star field and at most 3 meteors (2 on small GUI dimensions), rendered only in EVATEX settings. Disabling animations stops moving meteors and hover/screen fades. Decoder startup runs off the render thread, uploads occur only when a new frame is available, and leaving supported menus closes the decoder. High-resolution video still consumes CPU, RAM and upload bandwidth; reduce resolution/FPS on slower computers. No per-device performance guarantee is made.

Bundled FFmpeg playback was verified on Linux x64. Windows x64 binaries are packaged but were not executed here. macOS and ARM require a suitable external system FFmpeg. Video audio is intentionally disabled. Only Turkish and English translations are provided.
