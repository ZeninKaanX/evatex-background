## 1.8.21 validation scope

- Exact-API Fabric 1.21.11 compilation and shared decoder, audio, authenticated local media pipe, focus pause/resume, opaque menu pause/resume and recovery regression checks run locally on Linux.
- Both config implementations are tested with mixed malformed/duplicate favorites, saved preference round-trips and malformed JSON fallback in temporary directories.
- Options shortcut UI is checked in an isolated, muted hidden client; see docs/validation/settings-1.8.21.json for the recorded result. No user instance is modified.
- Build/package matrices and platform upload receipts are recorded separately from runtime checks. New physical Windows/macOS/Lunar or every-version in-world runtime coverage is not claimed.

## Calm recovery (1.8.20)

Linux x64 hidden clients on the secondary display, audio muted at a saved 10% volume, supplied original 1080p60 Reze input. Injecting an EVATEX button exception on Minecraft 1.21.11 / Loader 0.17.3 and 1.20.1 / Loader 0.16.14 showed the calm recovery screen, stopped video/audio resources, and continued to a native title without reopening the warning. A separate 1.21.11 rendering fault injected into the EVATEX settings screen also recovered through the same notice and continued native title. The pure Java circuit test verifies one-shot handling, preservation of the original cause, and exclusion of JVM-fatal errors.

Recovery catches RuntimeException and LinkageError only at protected EVATEX boundaries. It leaves pre-launch Loader/Mixin failures, third-party crash handlers and fatal JVM errors alone. In-world continuation selects the already-loaded world instead of reconnecting/reloading; that branch has not yet received a fresh world-loaded GUI test. Native text and a static opaque surface avoid relying on a failed wallpaper or custom font. No 4K/8K playback or physical Windows recovery GUI was tested.

The first generic GUI sequence remains stale at its redesigned volume-slider lookup, as recorded below. It is not counted as a passing recovery test.

## Fabric Loader compatibility (1.8.19)

The Fabric build no longer imposes the 26.x Loader floor on older games. Packaging checks the declared minimum against every embedded module. The isolated `--loader` test now uses the release JAR unchanged instead of rewriting its requirement.

Real Linux clients, muted audio at a saved 10% volume, hidden windows confined to the secondary display, original supplied 1080p60 Reze input:

- Minecraft 1.21.11 / Loader 0.17.3: startup, video playback, Settings pause and return/resume passed.
- Minecraft 1.20.1 / Loader 0.16.14: the same checks passed.
- Minecraft 1.21.11 / Loader 0.19.3: the same checks passed with bundled MixinExtras 0.5.5.

All 24 embedded-module combinations satisfy the new advertised floors. Compile/package checks cover the full Fabric matrix; these are not 24 new GUI tests or a Windows hardware validation. Other installed mods may impose stricter requirements. The initial broad GUI test stopped at a stale volume-slider lookup after the settings redesign; the targeted Loader test does not claim that unrelated regression sequence passed.

Machine-readable release checks: [Loader validation](docs/validation/loader-1.8.19.json).

## 1.8.18 validation

- Minecraft 1.21.11 Fabric development API compilation: passed.
- Live catalogue regression: concurrent checks share one fetch; automatic checks throttle after success; remote metadata persists; malformed updates preserve the previous list. Passed with an isolated HTTPS transport fixture and temporary game directory.
- Catalogue compiler: deterministic output; adding/removing an entry changes/restores the published catalogue; duplicate identifiers, insecure URLs, invalid checksums and excessive sizes are rejected. Passed.
- Existing library checksum, personal Git blob, cancellation, offline originals and 1,000-page thumbnail queue regressions: passed.
- Minecraft 1.20.1 legacy API compilation: passed. The contour-only secondary-display review passed at 1920×1080, with a bright 1080p video and fully loaded mixed-category Store covers. User review then requested softer lettering: small labels now use two low-opacity offset shadows instead of four opaque contour passes; the wordmark shadow is Gaussian-filtered once during asset generation. Final soft-shadow capture passed on the secondary display at 1920×1080 with the bright Reze video, muted host audio and no new dark panels. Both current (1.21.11) and legacy (1.20.1) source API builds passed; the final wordmark-only revision changes the generated PNG, not the legacy Java API. Main screen was not touched; host sound stayed muted. Navigation retained the same decoder/native process and paused/resumed its timeline. The rejected dark-panel prototype is excluded.
- Real Windows GPU/macOS/Lunar runtime coverage is unchanged from the recorded earlier validation; these metadata/UI changes do not imply new device validation.

## 1.8.17 local validation

The user superseded the earlier continuous-audio behavior: all Backgrounds screens must pause both picture and PCM, then resume the same player. The 1.8.16 expansion service was stopped; published 1.8.16 files remain immutable. A new presentation regression checks both counters stop and resume without changing decoder/native process/audio sink, over pipe and TCP with no host sound.

Modern list bytecode confirms that `method_57714(width,height,y)` lays out cached entries at x=0; moving x afterward does not reflow entries. The new atomic `method_73369(width,height,x,y)` path sets the final geometry before cached entry layout. Older APIs keep their existing fallback.

Local builds passed against Minecraft 1.20.1, 1.21.4, 1.21.5 and 1.21.11 APIs. The full 1.21.11 decoder/audio/ownership/transport suite passed, including complete picture+PCM pause and same-process resume over both pipe and TCP. The catalogue/download lifecycle and 1,000 rapid-page-change checks passed. Actual 1.21.11 GUI QA verified ten server entries keep their cached x coordinate aligned with the resized list, and captured MP4 Store, settings, library, wordmark and repeated pause/resume transitions. All GUI work was hidden on the secondary display with sound muted, capped to 50% of one CPU core; no extra game was opened on the primary display. Logs/screenshots are in the local `store-review/hotfix17-*` review artifacts. No physical Windows GPU, macOS or every-client runtime validation is claimed.

Four official NASA/Goddard MP4 downloads were fetched and checked for exact SHA-256, byte size and <=1080p video dimensions with ffprobe. No 4K/8K video was played. Source and credit: https://svs.gsfc.nasa.gov/13326/ ; usage: https://www.nasa.gov/nasa-brand-center/images-and-media/ . No NASA endorsement is implied. Downloads remain outside the JAR. A real catalogue MP4 also passed the actual LibraryDownloads HTTP path, SHA-256/size verification, MP4 file extension and installed-state checks in an isolated temporary game directory (`hotfix17-real-download.log`).

## Historical 1.8.16 behavior (superseded in 1.8.17)

Settings now uses the same Astral navigation as My Library and Store, with only Playback, Appearance and Menus preferences. The opaque menu suppresses Java pixel packing, frame publishing and texture uploads while enabled PCM continues; native FFmpeg still drains its shared audio/video pipeline. Muted/no-audio opaque playback and unfocused Minecraft pause completely. No zero-CPU or zero-FPS-cost claim is made.

A real 160×90 audio/video fixture verifies both pipe and authenticated loopback TCP paths: held video publishes no frames, muted PCM continues without a new process/audio object, visual frames resume on that same timeline, and full pause stops both counters. Existing decoder/audio, playback policy, ownership, locale and transport regressions passed locally with host sound disabled. Actual Settings-only, library and Store navigation and widget bounds passed at 1920×1080, 960×540 and 640×480. Final opaque-only Minecraft 1.21.11 GUI QA passed repeated title → library → Store → Settings → title → library → title transitions, with the same decoder and native FFmpeg process throughout. Muted decoded-frame counters stayed frozen in Backgrounds and advanced again on exit. Screenshots and logs: `store-review/opaque-*.png`, `opaque-preview.log`, `opaque-playback-tests.log` in the local review workspace. Tests were hidden on the secondary monitor, capped to 55% of one CPU core, with host sound disabled and a 1080p source; no 4K/8K video runtime test was performed. These results do not substitute for physical Windows/macOS GPU runtime testing.

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


## 1.8.23 playback and Loader review

See [the review](docs/verification/playback-1.8.23/README.md) and [coverage record](docs/verification/playback-1.8.23/validation.json). All 24 Fabric adapters compile against their declared Loader floor. Real hidden-window Linux tests cover 1.20.1 with Loader 0.15.11 / Java 17 and 1.21.11 with Loader 0.17.3 / Java 21 and the supplied 60 FPS clip. Native Forge/NeoForge 1.21.11 compile and package checks are separate from runtime coverage. Physical Windows GPU performance remains unmeasured.

## 1.8.24 Windows Discord transport

The native Windows named-pipe regression passed in [GitHub run 40427109564](https://github.com/ZeninKaanX/evatex-background-video-1.8.2/actions/runs/40427109564) for source commit `12c6668`. It exercises the production Java transport against a unique Win32 byte-mode pipe using fragmented READY messages, activity acknowledgements, ping/pong, disable/clear and reconnect. Linux IPC ownership/handoff and the full playback regression suite also pass locally. This is transport validation, not a screenshot of the Discord UI or a physical Windows GPU benchmark.
