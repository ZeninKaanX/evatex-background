# Astral 1.8.0 validation

Test environment: Linux x64, Java 21/25, Fabric Loader 0.19.5, real Minecraft client, real bundled FFmpeg. Test harnesses run in isolated game directories and are excluded from release jars.

## Automated regressions

`scripts/test.py` passes config normalization, invalid file handling, RGBA/ABGR channel ordering, frame ownership, native-resolution and 4K bounds, English/Turkish key parity, real FFmpeg playback at 15 and 60 FPS, looping and shutdown. The timing fixture publishes 45 frames at 15 FPS in about 2.93 seconds and 180 frames at 60 FPS in about 2.98 seconds. These checks test playback speed, not a hardware performance benchmark.

## In-game checks

All 24 exact-version builds listed in `COMPATIBILITY.md` are exercised in isolated real Minecraft clients on Linux x64. Java 21 runs 1.20/1.21 GUI checks; Java 25 runs 26.x checks. The harness visits the title screen, Turkish Astral settings, settings with animations disabled, multiplayer selection, title/server fallback variants and video-disabled idle shutdown. It checks decoder continuity, loaded Turkish translations, idle status when disabled and clean exit. Screenshots are also inspected: an active decoder alone does not prove video visibility.

Atlas Client 3.0.3 is additionally tested on 1.21.11 with full Fabric API and Player Animation Library, in both custom ImGui and vanilla UI modes. Logs/screenshots are in `build/smoke-<version>-standalone` and `build/smoke-1.21.11-atlas`. QA helper mods are excluded from release jars.

Version-specific failures found and fixed include removed legacy server-list background flags/render callbacks, changed panorama/texture/button APIs, named GUI/shutdown APIs in 26.x, and missing legacy resource-loader support on 1.21.9/1.21.10. The isolated launcher classpath now excludes older vanilla ASM libraries in favor of Fabric's revision. Official non-audio assets are downloaded with checksum verification for repeatable language/font tests. Offline Realms authentication and unavailable narrator/audio warnings are unrelated to EVATEX.

Pure config/decoder regressions pass on representatives 1.20, 1.21.11 and 26.3; GitHub Actions runs these regressions and compilation for all 24 versions. `scripts/check_packages.py` validates all bundled archives and checksums. Bundled decoder startup is also tested separately from system-FFmpeg development builds.

## Scope and remaining limits

Support covers stable Java Edition releases from 1.20 through 26.3, using separate exact-version jars. Earlier releases, snapshots, future releases and non-Fabric loaders are outside this matrix. Compilation and menu checks do not establish compatibility with every other mod or every hardware configuration. No gameplay-world or server-side behavior is added.

Atlas integration is optional and restricted to Atlas 3.0.3. It uses Atlas's existing ImGui context and the existing video GPU texture; Atlas code, settings and binaries remain separate. Its dependencies must still be installed.

Meteor effects have a fixed 48-star field and at most 3 meteors (2 on small GUI dimensions), rendered only in EVATEX settings. Disabling animations stops moving meteors and hover/screen fades. Decoder startup runs off the render thread, uploads occur only when a new frame is available, and leaving supported menus closes the decoder. High-resolution video still consumes CPU, RAM and upload bandwidth; reduce resolution/FPS on slower computers. No per-device performance guarantee is made.

Bundled FFmpeg playback was verified on Linux x64. Windows x64 binaries are packaged but were not executed here. macOS and ARM require a suitable external system FFmpeg. Video audio is intentionally disabled. Only Turkish and English translations are provided.
