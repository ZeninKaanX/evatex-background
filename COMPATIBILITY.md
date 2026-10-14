# Minecraft compatibility — EVATEX 1.8.16

This release targets all 24 stable Java Edition releases from 1.20 through 26.3, using one exact-version Fabric jar per release. Install only the jar matching your game. Earlier versions, snapshots and future releases are not supported by these Fabric files.

| Minecraft | Required Java | Release file |
| --- | --- | --- |
| 1.20 | 17+ | evatex-background-video-1.8.16-mc1.20.jar |
| 1.20.1 | 17+ | evatex-background-video-1.8.16-mc1.20.1.jar |
| 1.20.2 | 17+ | evatex-background-video-1.8.16-mc1.20.2.jar |
| 1.20.3 | 17+ | evatex-background-video-1.8.16-mc1.20.3.jar |
| 1.20.4 | 17+ | evatex-background-video-1.8.16-mc1.20.4.jar |
| 1.20.5 | 21+ | evatex-background-video-1.8.16-mc1.20.5.jar |
| 1.20.6 | 21+ | evatex-background-video-1.8.16-mc1.20.6.jar |
| 1.21 | 21+ | evatex-background-video-1.8.16-mc1.21.jar |
| 1.21.1 | 21+ | evatex-background-video-1.8.16-mc1.21.1.jar |
| 1.21.2 | 21+ | evatex-background-video-1.8.16-mc1.21.2.jar |
| 1.21.3 | 21+ | evatex-background-video-1.8.16-mc1.21.3.jar |
| 1.21.4 | 21+ | evatex-background-video-1.8.16-mc1.21.4.jar |
| 1.21.5 | 21+ | evatex-background-video-1.8.16-mc1.21.5.jar |
| 1.21.6 | 21+ | evatex-background-video-1.8.16-mc1.21.6.jar |
| 1.21.7 | 21+ | evatex-background-video-1.8.16-mc1.21.7.jar |
| 1.21.8 | 21+ | evatex-background-video-1.8.16-mc1.21.8.jar |
| 1.21.9 | 21+ | evatex-background-video-1.8.16-mc1.21.9.jar |
| 1.21.10 | 21+ | evatex-background-video-1.8.16-mc1.21.10.jar |
| 1.21.11 | 21+ | evatex-background-video-1.8.16-mc1.21.11.jar |
| 26.1 | 25+ | evatex-background-video-1.8.16-mc26.1.jar |
| 26.1.1 | 25+ | evatex-background-video-1.8.16-mc26.1.1.jar |
| 26.1.2 | 25+ | evatex-background-video-1.8.16-mc26.1.2.jar |
| 26.2 | 25+ | evatex-background-video-1.8.16-mc26.2.jar |
| 26.3 | 25+ | evatex-background-video-1.8.16-mc26.3.jar |

## API adapters

`scripts/ports.py` generates version-specific sources from the shared legacy and modern source trees. Adapters cover screen background/render callbacks, panorama signatures, legacy list background flags and modern list background/separator callbacks, identifier factories, button rendering, render layers/pipelines and texture filtering. Minecraft 26.x uses native named APIs and Java 25; 26.2/26.3 also require the new GUI access and shutdown methods. Mapping reference inputs stay in the ignored build directory.

Resource loader v0 is embedded through 1.21.8. Both v0 and v1 are needed on 1.21.9/1.21.10; v1 is embedded from 1.21.11 onward. Fabric API base is also embedded, so full Fabric API is optional for EVATEX.

## Validation

The 1.8.16 candidate adds world selection/main-menu options and scoped list transparency, with independent saved switches. It includes the reviewed Windows decoder fixes; physical Windows game performance is still pending.

Each listed version is compiled against its own official client. The full real-client Linux x64 matrix was checked for 1.8.1; the 1.8.16 menu update receives targeted world/options/toggle checks on Linux Minecraft 1.20.1 and 1.21.11, plus Atlas 3.0.3 on 1.21.11. Every adapter receives package verification; the complete GUI matrix is not rerun. Checks exercise title video, Turkish Astral settings, animations disabled, multiplayer selection, return to the title, fallback with video disabled and decoder shutdown. Screenshots are inspected for visible video and interface layout. Logs and screenshots are produced under `build/smoke-<version>-standalone`; see `VALIDATION.md` for the test method and limits.

Atlas Client 3.0.3 integration is validated separately on Minecraft 1.21.11 only. Atlas and its dependencies remain separate installations. Windows x64 decoder binaries are bundled but Windows runtime has not been tested here. macOS Intel/Apple Silicon and Linux ARM64 engines are also bundled and checked against pinned SHA256 hashes. Their binaries have not been executed here. Native decoder/config regressions passed on Windows x64, Linux x64 and macOS Apple Silicon in GitHub Actions run 38098007354. Full Windows/macOS game UI and physical audio devices, macOS Intel and Linux ARM64 runtime remain untested.

`scripts/build.py --all` builds this matrix with JDK 25. `scripts/check_packages.py` verifies every bundled archive, exact Minecraft/Java metadata, class versions, translations, embedded modules, FFmpeg provenance and SHA256 checksum. GitHub Actions compiles and runs decoder/configuration regressions for the same matrix; desktop GUI checks remain local.

## Adaptive menu shortcut (1.8.6)

The native shortcut attaches via Screen initialization and is checked on the client tick. Visible native widgets, nested parent elements and custom elements exposing public `getX/getY/getWidth/getHeight` bounds participate in collision detection. Geometry changes trigger deterministic placement; an unchanged layout reuses its previous result. Native menus use a two-GUI-unit gap to retain the established lower-centre position. Atlas uses its existing separate bridge and pixel-space lower-right preference.

Title subclasses and main menu screens with names ending in MainMenu/MainMenuScreen/TitleScreen/TitleMenuScreen/HomeScreen are recognized while no world is loaded. Opaque custom renderers and unregistered controls cannot be measured through this mechanism. Lunar's [Vanilla Overlay](https://support.lunarclient.com/solutions/enabling-vanilla-overlay) restores native menu buttons for external mods. No Lunar runtime validation is claimed.

The optional Astral home remains disabled for new and existing installations. When enabled, title screens are replaced through the client lifecycle while no world is loaded. It delegates game operations to captured native title buttons, preserves their disabled state and retains extra title actions. It uses only EVATEX widgets and native rendering, with independent legacy/modern matrix adapters.

## Astral 1.8.16 UI validation

All 24 exact-version targets compile. Native-menu actions, return paths, opt-out, compact fallback and restoration after a late foreign control were checked on 1.21.11 and 1.20.1. Home ownership and focus pause/resume were checked on 1.21.11; GUI scales 2 and 3 have 1920×1080 renderer captures. Atlas 3.0.3, Sodium and Mod Menu were checked together on 1.21.11. No physical Windows performance claim is made by these Linux UI checks.

### Lunar Client and restricted playback engines

Use Lunar's Fabric add-on. Its custom themes can hide ordinary Minecraft mod buttons; enable the theme setting **Main Menu Style → Vanilla (Overlay)** to expose them. EVATEX also supplies an optional Mod Menu configuration entrypoint, so its settings can be opened through the mod list. Public widget bounds are used for collision placement; private/obfuscated client controls are not assumed to be discoverable. Full Lunar Client runtime validation is still required. Official instructions: https://support.lunarclient.com/solutions/enabling-vanilla-overlay

On Windows, `CreateProcess error=5` means the bundled FFmpeg executable was denied execution. A missing system FFmpeg then produces error=2. EVATEX displays the engine failure rather than calling the video invalid. Java-supported photos remain available without FFmpeg. Operating-system execution policies are not changed by the mod.


## Native Forge and NeoForge

| Minecraft | Loader | Minimum loader | Java | Artifact suffix |
| --- | --- | --- | --- | --- |
| 1.21.11 | Forge | 61.2.3 | 21+ | `-mc1.21.11-forge.jar` |
| 1.21.11 | NeoForge | 21.11.45 | 21+ | `-mc1.21.11-neoforge.jar` |

Install the file for your actual loader. Native builds do not load Fabric modules or Mod Menu and do not require a loader bridge. The Mods configuration action opens EVATEX settings. Forge and NeoForge Mods screens are recognized by the options-menu wallpaper switch. Native builds share the player, configuration, resource assets and Astral controls with Fabric. The bundled engines cover the same target operating systems; loader QA recorded here is Linux only, not a new Windows/macOS performance certification. Additional native files are published after exact-version compilation and package verification; check the release list for available files. `loaders/targets.json` is the planned upstream matrix, not a claim that every listed EVATEX build has passed runtime testing. See `VALIDATION.md` for the named game-test configurations.
