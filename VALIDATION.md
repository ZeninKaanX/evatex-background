# EVATEX validation — 1.8.13

The 1.8.12 Fabric release's 24 exact-version builds were compiled and their metadata, Java bytecode targets, embedded libraries, bundled engines, archive CRC and SHA-256 sidecars checked. Decoder/audio regression tests include real MP4/GIF playback, native PNG/JPEG/BMP decoding, error classification, bounded buffers, focus behavior, custom-widget bounds and menu ownership. Current 1.8.13 expansion results are recorded separately below.

## Historical 1.8.11 runtime coverage

Actual Linux Minecraft 1.21.11 with Fabric Loader 0.18.4 completed native home actions, settings return, compact layouts, foreign-menu ownership and focus pause/resume using the supplied Lost Soul 1080p60 video. Loader 0.19.3's original bundled Mixin Extras 0.5.4 reproduced a startup failure; the corrected 0.5.5 library allowed the real GUI to run.

Minecraft 26.1.2 native Options was opened successfully in a clean profile. The supplied rendering-mod subset also passed on Linux; see the linked runtime record. Runtime results and limits are recorded in docs/client-playback-1.8.11-validation.md.

The desktop is locked. GUI tests render a real hidden window assigned to the secondary display; captures are game framebuffers, not screenshots of a mapped desktop window. Sound is muted at 10% volume. No 4K/8K video playback stress was performed.

The supplied Windows game log proves bundled FFmpeg execution was denied, followed by missing system FFmpeg. No fatal settings-crash stack appears in the supplied logs. No claim of real Windows or Lunar runtime testing is made for this Linux test session.

## 1.8.12 review validation

All 24 targets compile with the new authenticated video/PCM transport and diagnostic action. The previous 24 targets were built and packaged, not individually exercised in Minecraft during this review. Existing historical runtime records remain applicable only to their named configurations.

Real FFmpeg regression tests cover MP4/GIF loops, single-decode photos, frame timing, software fallback after failed hardware initialization, pause/resume, bounded frame ownership and shutdown. Audio tests use fake output devices and emit no host sound. Handshake tests reject a wrong secret and a stalled partial request, accept the legitimate sender afterward, preserve exact chunk payload, close the listening port after success and interrupt a pending connection on shutdown.

Physical macOS execution has not been performed. Bundling Intel/Apple Silicon engines is a packaging check, not macOS runtime validation. Physical NVIDIA/Intel/new AMD GPU decode results are not available here; the historical physical RX 580 evidence is documented in [Windows results](docs/WINDOWS_2026_10_12.md). Linux exercises the real software decode path and Windows hardware opt-out selection; it does not prove a Windows driver's behavior.

The archive verifier checks FFmpeg license/provenance files and engine hashes. This is a packaging verification, not a legal assessment of redistribution obligations.

Minecraft 1.21.11 on the secondary display passed the real diagnostic button/clipboard action, English translated feedback, report privacy checks and software playback. The previous clipboard contents were restored. A 1920×1080 vanilla title-menu framebuffer was captured for the gallery.

Both supplied 1080p media files passed 540 software-decoded frames over authenticated video/PCM streams using host system FFmpeg, with hardwareAcceleration=false and audio muted at 10%. Reze playback crossed its 16.6-second source duration.


## Native Forge/NeoForge integration — 13 October 2026

Minecraft 1.21.11 was compiled against the official Forge 61.2.3 (ForgeGradle 7) and NeoForge 21.11.45 (ModDevGradle) APIs using Java 21. Shared playback/configuration now reaches game paths and mod metadata through `EvatexPlatform`; native jars contain native client entrypoints and TOML, with no Fabric entrypoint, nested Fabric modules or Mod Menu API dependency. Mixin injection selectors retain complete descriptors when intermediary overloads are translated to Mojang names. Native font/glyph accessor names are translated too.

Real clients ran on Linux with Mesa Intel UHD Graphics. Final QA used isolated profiles under `loaders/<loader>/run`, hidden GL windows positioned on the secondary display, muted audio at 10%, and the supplied 1080p60 Reze/NBSPLV videos. Tested stages: vanilla title screen; EVATEX settings; Minecraft options; world creation; multiplayer server selection; focus loss/resume; optional Astral home; native Mods screen; and the native Mods configuration factory opening EVATEX settings. Each menu published advancing video frames. Focus loss reported `paused`, and playback resumed without restarting the menu session. Native Mods screens obey the options-menu background policy. The configuration extension is registered through `ConfigScreenHandler.ConfigScreenFactory` on Forge and `IConfigScreenFactory` on NeoForge.

`tests/native` and `scripts/smoke_native.py` are QA-only. The runner reserves test JVMs by PID before allowing window creation, disables the loader's early splash in the isolated profile, and adds a KWin guard that places only those test windows on the secondary screen. It never targets the user's Prism instance. Release packaging rejects any QA helper classes/configuration. Both native packages passed metadata, dependency-exclusion, unique-entry, checksum and five-platform engine-payload checks. Forge also extracted and played through the bundled Linux engine. Test profiles now mute Minecraft master/music separately from the disabled video sound.

Fabric 1.21.11 and the Java 17 legacy adapter for 1.20.1 compiled after the platform refactor. Decoder/audio/layout/menu-ownership/custom-widget/authenticated-loopback regressions passed, including local TCP focus pause/resume with zero FFmpeg CPU time during the paused interval and temporary audio-device recovery with no sound emitted.

Limits: these are native-loader integration tests on Linux, not real Windows/macOS driver tests. Native builds currently target Minecraft 1.21.11 only; the existing 24-version matrix remains Fabric-specific. Atlas compatibility remains Fabric-specific because the referenced Atlas client is a Fabric mod. Native APIs are pinned to the tested loader revisions; minimum metadata allows Forge 61.2.0+, but the real Forge run recorded here used 61.2.3.


## Optional in-game menu policy (1.8.13)

The 1.21.11 and legacy 1.20.1 Fabric builds compiled. Decoder, audio, focus, authenticated loopback and translation regressions passed. MenuPlaybackRegression verified the default-off setting, copied configuration, supported-menu opt-in, gameplay/overlay exclusion and 1,000 exit transitions. A real NeoForge 21.11.45 Linux client ran the supplied 1080p60 Reze video through the existing menus with master audio muted. An actual loaded-world GUI measurement has not yet been recorded; no zero-FPS-loss claim is made.

## Native version expansion (in progress)

`loaders/targets.json` records available upstream targets for Fabric's 24 Minecraft releases. It contains 22 Forge and 23 NeoForge targets; Forge 1.20.5/1.21.2 and NeoForge 1.20 do not exist upstream. A listed target alone does not establish EVATEX compatibility. Compilation receipts, including failed attempts, are written to `build/native-loaders/matrix-build.json`; publication receipts include exact loader/version and the remote SHA-512. These local reports are build output, not shipped source documentation.

Real Minecraft integration coverage remains the named 1.21.11 configurations above. Extended-version compilation is not an individual runtime test. Native packaging checks reject Fabric dependencies and QA helpers, verify exact Minecraft/loader requirements and Java bytecode targets, check production refmaps where SRG is used, and compare bundled engine hashes. Builds share a low CPU quota, Gradle has one worker, and binary development bundles avoid decompilation where supported.
