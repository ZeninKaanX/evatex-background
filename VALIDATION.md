# Background media and frame transport 1.8.4

Real-FFmpeg regression now covers PNG/JPEG/BMP single-frame loading, literal percent and Unicode paths, successful still-image process exit without clearing the background, GIF changes/looping with paced timing, forced local TCP playback on every test host, bounded reusable pixel arrays, immutable public callbacks, GPU-failure fallback over TCP, and socket cleanup. Production callbacks borrow pooled arrays only during their synchronous call; VideoBackground copies into persistent staging storage while holding the same monitor used for texture uploads.

Windows uses a buffered loopback video connection; other systems retain stdout transport. Still photos leave no continuously running decoder. GPU selection retains the 1.8.3 D3D11 paths. The visible name and translated media labels change without migrating the mod ID, config filename or saved videoPath.

This validates frame ownership and media behavior, not a guaranteed Windows FPS or CPU reduction. Actual performance on the user's affected Windows machine remains unmeasured. No 4K/8K playback and no full per-version GUI matrix are performed for this patch.

# Windows GPU optimization 1.8.3

This patch addresses the missing hardware decoder request found in 1.8.2. Windows tries D3D11 decoding and scaling, then D3D11 decoding with CPU scaling, then software decoding. The hardware filter requires D3D11 frames and reports active only after a complete frame is read. A watchdog limits first-frame waits to 12 seconds per attempt. Configured FPS, size cap, source video and audio settings are unchanged.

Local real-FFmpeg regression covers fallback from unavailable D3D11 filters/devices to working software decoding, 15/60 FPS timelines, immutable pixel ownership, colour channel order, config preference copying and startup thread cleanup. Existing Linux/macOS decoder selection is preserved. The user’s 1080p clip is used for the targeted Linux game check; no 4K/8K playback or full per-version GUI matrix is run.

Physical Windows GPU throughput, CPU reduction and smoothness are not verified by Linux tests or GPU-less CI runners. These require a Windows machine with a supported driver and the same video/settings. The earlier releases' validation remains documented below.

# Astral 1.8.2 validation

## Menu shortcut update

The 1.8.2 change moves the native shortcut to the lower centre and the Atlas shortcut to the lower right. Native labels use Minecraft font height. Atlas explicitly overrides local button alignment, padding and rounding, restoring every style variable and colour after drawing. Placement uses window dimensions and GUI scale and does not inspect launcher paths.

The previous release’s playback validation is recorded below. The full 24-version game matrix applies to 1.8.1; 1.8.2 uses targeted menu checks plus compilation and package verification across all 24 adapters. Full compatibility with every launcher or unrelated custom-menu mod has not been established.

## Playback validation retained from 1.8.1

The local test host is Linux x64 with Java 21/25 and Fabric Loader 0.19.5. Clients run in isolated directories on the right, secondary display. Test helpers and personal media are excluded from distribution jars.

## Decoder and sound

The user's supplied video is 1920×1080 H.264 at 60 FPS with stereo AAC audio, 16.602 seconds long. It is used without changing the original file. Lightweight audio regression output is 480×270 at 30 FPS; menu tests use the normal 1080p output cap. Live volume changes, including zero gain and restore, do not restart the decoder. PCM gain tests check signed 16-bit positive/negative samples and exact zeroing; the UI harness applies and verifies the native slider. Live unmute/mute does not restart the FFmpeg process, video continues while muted, and the original clip loops past its duration. Shutdown stops frame publication and closes the audio device/process.

Sound uses stereo 48 kHz PCM from the same FFmpeg input as video. A 32 KiB bounded output buffer is primed before playback to absorb short scheduling delays. A continuous 12-second sound test records zero output-buffer underruns. An isolated PipeWire capture, routed using process-local `PIPEWIRE_NODE`, contains no unexpected silent gap of 50 ms or more in its approximately 11.6-second active interval; other desktop audio is excluded. Muting flushes and closes the device, while PCM is drained so video continues. Device initialization/write failures are handled without stopping video. Linux explicitly selects ALSA's default mixer so the user's PipeWire/PulseAudio output is respected.

A four-frame bounded queue separates native pipe reads from presentation. The presentation clock preserves deadlines across normal OS timer oversleep and resets after substantial interruptions, preventing cumulative slow playback. The heap-based frame budget includes this queue.

The 160×90 timing fixture separately validates 15/60 FPS pacing, looping, immutable frame ownership and cleanup. Tests also cover corrupt config values, invalid/Unicode paths, RGBA/ABGR channel order, portable folder commands, Turkish/English key parity and layout bounds across multiple dimensions.

No 4K or 8K video is decoded or played on this computer. High-resolution checks use width/height integers only, verifying that 3840×2160 and 7680×4320 sources select the safe 1920×1080 output under default settings. This validates allocation policy, not real high-resolution codec performance. Larger source decoding still requires sufficient hardware.

## In-game checks

The 24-version matrix from Minecraft 1.20 through 26.3 visits the title, both Astral settings pages, multiplayer selection, return to title and video-disabled idle state. It asserts video continuity, Turkish translations, live sound enable/disable and clean exit. The splash pool must preserve the vanilla/resource-pack messages and contain all 20 additions; a forced chemical-formula splash checks Unicode rendering. GLFW positions older clients on the secondary display; SDL positions Minecraft 26.3 there. Logs and screenshots are under `build/smoke-<version>-standalone`; the result manifest is `build/qa-1.8.1-matrix.json`. Run `scripts/qa_matrix.py` on this desktop to reproduce.

Atlas Client 3.0.3 is checked separately on Minecraft 1.21.11 with its own Fabric API and Player Animation Library dependencies. Atlas's custom ImGui and vanilla modes remain separate from EVATEX. Screenshots are inspected for actual visible video and control layout; a decoder reporting active is not sufficient evidence of rendering.

## Desktop platforms

Distributable jars contain Windows x64, Linux x64/ARM64 and macOS Intel/Apple Silicon engines. Compressed and extracted engine hashes are checked, and license/build information is included. Cached macOS/ARM binaries are checksum-verified before use; Linux permissions and incomplete extraction are repaired. Compatible system FFmpeg is a fallback if bundled execution fails.

Linux x64 full game UI and audio are exercised on this host. Native decoder/config regressions passed on Ubuntu x64, Windows x64 and macOS Apple Silicon in [GitHub Actions run 38072739304](https://github.com/ZeninKaanX/evatex-background-video-1.6.0/actions/runs/38072739304). These jobs use each host’s pinned engine and validate real 15/60 FPS decoding, looping and cleanup. Full game UI, physical audio devices and folder managers on Windows/macOS, plus Linux ARM64 and macOS Intel runtime, remain untested. Engine packaging and hashes alone do not prove those behaviors.

## Scope

Support is limited to the 24 exact stable Java Edition/Fabric versions in `COMPATIBILITY.md`. Older releases, snapshots, future releases and other loaders are outside this matrix. Atlas support is restricted to 3.0.3 on 1.21.11. No guarantee is made for every other mod, hardware configuration or codec.

Meteor counts are bounded, animations can be disabled, startup runs outside the render thread, texture uploads occur only for new frames, and leaving supported menus closes playback. The mod adds menu behavior rather than world/server features. Only Turkish and English translations are bundled; other game languages fall back to English.
