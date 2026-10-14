## Preview queue hotfix (1.8.16)

Rapid navigation could leave completed off-screen thumbnail futures occupying every pending slot. The fix cancels/removes requests outside the visible page and detail card each frame; texture caching remains bounded. Pagination selects the new page's first item. A regression exercises 1,000 page changes with both completed and pending requests, verifying current-page preservation and close cancellation. This fixes a newly discovered browsing edge case in 1.8.15; existing published files are preserved and the corrected release is 1.8.16. The 1.8.15 remaining-upload service was stopped before expansion continued.

## Astral Store and local previews (1.8.15)

Minecraft 1.21.11 Fabric ran on the secondary display with audio muted. Actual screenshots cover Store, a verified direct GIF download and Downloaded/Apply state, My Library with both supplied 1080p60 Reze and NBSPLV video posters, local images/GIFs, and a compact 960×540 window. Original Astral click resources resolved in Minecraft. No 4K/8K video test ran. The library regression suite checks 250 entries, pinned personal-source metadata, valid/invalid Git blob checksums and lengths, cancellation/partial cleanup, offline originals, schema/path/address rejection and locale parity. The catalogue has 238 images and 12 GIFs; all 250 artworks have not been visually reviewed. Source collections do not grant EVATEX ownership or redistribution rights.

Full release compiler and package results are recorded separately after final-source CI completes; prior-version coverage below must not be read as 1.8.15 runtime coverage. No new physical Windows GPU, macOS or proprietary Lunar runtime claim is made.

## Discover library validation (1.8.14)

The new library was opened in a real, isolated Minecraft 1.21.11 Fabric client on Linux, using a hidden window confined to the secondary display. Audio was muted at 10% and rendering capped at 15 FPS. Captures cover Turkish/English views, anime search, Astral originals and a compact 960×720 window. A locally generated original was applied from the actual UI; its saved path and discovery by My Library were verified. This is not a Windows/GPU or all-version runtime test.

`tests/LibraryRegression.java` and `scripts/test_library.py` passed schema/version/duplicate/path/HTTPS rejection, third-party source permissions, successful checksum-verified transfer, failed checksum/size cleanup, offline 1920×1080 generation, cancellation and partial-file cleanup. The network transfer fixture uses controlled test data. Catalogue copies and English/Turkish translation keys match. Public DNS/IP validation rejects local/private network destinations on background workers.

The first catalogue has 50 entries: 13 generated EVATEX originals and 37 external source pages. These are not 50 bundled or one-click downloadable videos. Provider media and resolutions have not all been independently viewed or verified. All 24 Fabric API targets compiled in private development packages containing the new library and 50-entry seed. Forge and NeoForge 1.21.11 also compiled. A real NeoForge 21.11.45 client passed the existing video/menu/focus/configuration-factory stages plus Discover entry/exit: the decoder was inactive in Discover and resumed on returning to settings. The run stayed on the secondary screen with muted audio at 10%. These compile checks do not claim runtime coverage for the other 23 Minecraft versions. Final GitHub runs 38520402295 and 38520402263 at source commit 29dbab4 passed all 24 Fabric and 45 native compiler targets. Three host decoder checks also passed. Final 1.21.11 Fabric screenshots passed widget-bound checks after the compact-layout change; the native runtime tests above preceded that layout adjustment. Release packaging verifies pinned cloud artifact digests, Java/loader metadata, QA exclusion, engine payloads and file checksums. Publication proceeds with 1.21.11 Fabric first and reconciles remote hashes per file.

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

Limits: these are native-loader integration tests on Linux, not real Windows/macOS driver tests. This runtime record concerns Minecraft 1.21.11; expansion builds below have their own compilation and package checks and are not individually covered by this game test. Atlas compatibility remains Fabric-specific because the referenced Atlas client is a Fabric mod. Native APIs are pinned to the tested loader revisions; minimum metadata allows Forge 61.2.0+, but the real Forge run recorded here used 61.2.3.


## Optional in-game menu policy (1.8.13)

The 1.21.11 and legacy 1.20.1 Fabric builds compiled. Decoder, audio, focus, authenticated loopback and translation regressions passed. MenuPlaybackRegression verified the default-off setting, copied configuration, supported-menu opt-in, gameplay/overlay exclusion and 1,000 exit transitions. A real NeoForge 21.11.45 Linux client ran the supplied 1080p60 Reze video through the existing menus with master audio muted. An actual loaded-world GUI measurement has not yet been recorded; no zero-FPS-loss claim is made.

## Native version expansion — completed compilation and publication

`loaders/targets.json` records available upstream targets for Fabric's 24 Minecraft releases. It contains 22 Forge and 23 NeoForge targets; Forge 1.20.5/1.21.2 and NeoForge 1.20 do not exist upstream. A listed target alone does not establish EVATEX compatibility. Compilation receipts, including failed attempts, are written to `build/native-loaders/matrix-build.json`; publication receipts include exact loader/version and the remote SHA-512. These local reports are build output, not shipped source documentation.

The Forge 47.4.27 / Minecraft 1.20.1 representative development client completed all ten menu/focus/native-configuration stages on Linux with the supplied NBSPLV 1080p60 clip, disabled audio at 10%, a hidden window on the secondary display and a CPU quota. Screens included title, EVATEX settings, options, world selection, multiplayer, Astral home and native Mods. This mapped development run is not a separate installed-SRG production launch; production mappings are checked in the packaged JAR. Initial asset preparation exceeded the first test deadline; the cached retry completed.

The early NeoGradle targets (1.20.2/1.20.3/1.20.5) do not honor binary-only preparation in the current toolchain. Their local decompiler processes were stopped; those targets are checked on GitHub runners, with bounded decompiler memory/thread settings. Other targets use binary preparation where supported. GitHub artifacts are accepted only from a pinned source commit and a successful job, with the complete archive SHA-256 matched to GitHub before packaging. Failed jobs are not treated as compatibility evidence.

Real Minecraft integration coverage remains the named 1.21.11 configurations above. Extended-version compilation is not an individual runtime test. Native packaging checks reject Fabric dependencies and QA helpers, verify exact Minecraft/loader requirements and Java bytecode targets, check production refmaps where SRG is used, and compare bundled engine hashes. Builds share a low CPU quota, Gradle has one worker, and binary development bundles avoid decompilation where supported.

All 24 Fabric, 22 Forge and 23 NeoForge 1.8.13 builds are published as stable releases on Modrinth. Each uploaded payload was checked against the remote SHA-512 and exact loader/Minecraft metadata. Native compilation completed across GitHub runs [38435699003](https://github.com/ZeninKaanX/evatex-background-video-1.8.2/actions/runs/38435699003), [38439160226](https://github.com/ZeninKaanX/evatex-background-video-1.8.2/actions/runs/38439160226) and [38442466984](https://github.com/ZeninKaanX/evatex-background-video-1.8.2/actions/runs/38442466984). Only successful target jobs were accepted; failed and timed-out attempts were retried before publication. Representative runtime coverage remains limited to the named configurations above.
