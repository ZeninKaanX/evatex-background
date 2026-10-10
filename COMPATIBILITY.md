# Minecraft compatibility — EVATEX 1.8.1

This release targets all 24 stable Java Edition releases from 1.20 through 26.3, using one exact-version Fabric jar per release. Install only the jar matching your game. Earlier versions, snapshots, other loaders and future releases are not supported by these files.

| Minecraft | Required Java | Release file |
| --- | --- | --- |
| 1.20 | 17+ | evatex-background-video-1.8.1-mc1.20.jar |
| 1.20.1 | 17+ | evatex-background-video-1.8.1-mc1.20.1.jar |
| 1.20.2 | 17+ | evatex-background-video-1.8.1-mc1.20.2.jar |
| 1.20.3 | 17+ | evatex-background-video-1.8.1-mc1.20.3.jar |
| 1.20.4 | 17+ | evatex-background-video-1.8.1-mc1.20.4.jar |
| 1.20.5 | 21+ | evatex-background-video-1.8.1-mc1.20.5.jar |
| 1.20.6 | 21+ | evatex-background-video-1.8.1-mc1.20.6.jar |
| 1.21 | 21+ | evatex-background-video-1.8.1-mc1.21.jar |
| 1.21.1 | 21+ | evatex-background-video-1.8.1-mc1.21.1.jar |
| 1.21.2 | 21+ | evatex-background-video-1.8.1-mc1.21.2.jar |
| 1.21.3 | 21+ | evatex-background-video-1.8.1-mc1.21.3.jar |
| 1.21.4 | 21+ | evatex-background-video-1.8.1-mc1.21.4.jar |
| 1.21.5 | 21+ | evatex-background-video-1.8.1-mc1.21.5.jar |
| 1.21.6 | 21+ | evatex-background-video-1.8.1-mc1.21.6.jar |
| 1.21.7 | 21+ | evatex-background-video-1.8.1-mc1.21.7.jar |
| 1.21.8 | 21+ | evatex-background-video-1.8.1-mc1.21.8.jar |
| 1.21.9 | 21+ | evatex-background-video-1.8.1-mc1.21.9.jar |
| 1.21.10 | 21+ | evatex-background-video-1.8.1-mc1.21.10.jar |
| 1.21.11 | 21+ | evatex-background-video-1.8.1-mc1.21.11.jar |
| 26.1 | 25+ | evatex-background-video-1.8.1-mc26.1.jar |
| 26.1.1 | 25+ | evatex-background-video-1.8.1-mc26.1.1.jar |
| 26.1.2 | 25+ | evatex-background-video-1.8.1-mc26.1.2.jar |
| 26.2 | 25+ | evatex-background-video-1.8.1-mc26.2.jar |
| 26.3 | 25+ | evatex-background-video-1.8.1-mc26.3.jar |

## API adapters

`scripts/ports.py` generates version-specific sources from the shared legacy and modern source trees. Adapters cover screen background/render callbacks, panorama signatures, server-list background flags, identifier factories, button rendering, render layers/pipelines and texture filtering. Minecraft 26.x uses native named APIs and Java 25; 26.2/26.3 also require the new GUI access and shutdown methods. Mapping reference inputs stay in the ignored build directory.

Resource loader v0 is embedded through 1.21.8. Both v0 and v1 are needed on 1.21.9/1.21.10; v1 is embedded from 1.21.11 onward. Fabric API base is also embedded, so full Fabric API is optional for EVATEX.

## Validation

Each listed version is compiled against its own official client and tested in a real isolated Linux x64 client. Checks exercise title video, Turkish Astral settings, animations disabled, multiplayer selection, return to the title, fallback with video disabled and decoder shutdown. Screenshots are inspected for visible video and interface layout. Logs and screenshots are produced under `build/smoke-<version>-standalone`; see `VALIDATION.md` for the test method and limits.

Atlas Client 3.0.3 integration is validated separately on Minecraft 1.21.11 only. Atlas and its dependencies remain separate installations. Windows x64 decoder binaries are bundled but Windows runtime has not been tested here. macOS Intel/Apple Silicon and Linux ARM64 engines are also bundled and checked against pinned SHA256 hashes. Their binaries have not been executed here. Native decoder/config regressions passed on Windows x64, Linux x64 and macOS Apple Silicon in GitHub Actions run 38072739304. Full Windows/macOS game UI and physical audio devices, macOS Intel and Linux ARM64 runtime remain untested.

`scripts/build.py --all` builds this matrix with JDK 25. `scripts/check_packages.py` verifies every bundled archive, exact Minecraft/Java metadata, class versions, translations, embedded modules, FFmpeg provenance and SHA256 checksum. GitHub Actions compiles and runs decoder/configuration regressions for the same matrix; desktop GUI checks remain local.
