# Astral 1.8.1 validation

The local test host is Linux x64 with Java 21/25 and Fabric Loader 0.19.5. Clients run in isolated directories on the right, secondary display. Test helpers and personal media are excluded from distribution jars.

## Decoder and sound

The user's supplied video is 1920×1080 H.264 at 60 FPS with stereo AAC audio, 16.602 seconds long. It is used without changing the original file. Lightweight audio regression output is 480×270 at 30 FPS; menu tests use the normal 1080p output cap. Live volume changes, including zero gain and restore, do not restart the decoder. PCM gain tests check signed 16-bit positive/negative samples and exact zeroing; the UI harness applies and verifies the native slider. Live unmute/mute does not restart the FFmpeg process, video continues while muted, and the original clip loops past its duration. Shutdown stops frame publication and closes the audio device/process.

Sound uses stereo 48 kHz PCM from the same FFmpeg input as video. A 32 KiB bounded output buffer is primed before playback to absorb short scheduling delays. A continuous 12-second sound test records zero output-buffer underruns. An isolated PipeWire capture, routed using process-local `PIPEWIRE_NODE`, contains no unexpected silent gap of 50 ms or more in its approximately 11.6-second active interval; other desktop audio is excluded. Muting flushes and closes the device, while PCM is drained so video continues. Device initialization/write failures are handled without stopping video. Linux explicitly selects ALSA's default mixer so the user's PipeWire/PulseAudio output is respected.

The 160×90 timing fixture separately validates 15/60 FPS pacing, looping, immutable frame ownership and cleanup. Tests also cover corrupt config values, invalid/Unicode paths, RGBA/ABGR channel order, portable folder commands, Turkish/English key parity and layout bounds across multiple dimensions.

No 4K or 8K video is decoded or played on this computer. High-resolution checks use width/height integers only, verifying that 3840×2160 and 7680×4320 sources select the safe 1920×1080 output under default settings. This validates allocation policy, not real high-resolution codec performance. Larger source decoding still requires sufficient hardware.

## In-game checks

The 24-version matrix from Minecraft 1.20 through 26.3 visits the title, both Astral settings pages, multiplayer selection, return to title and video-disabled idle state. It asserts video continuity, Turkish translations, live sound enable/disable and clean exit. The splash pool must preserve the vanilla/resource-pack messages and contain all 20 additions; a forced chemical-formula splash checks Unicode rendering. GLFW positions older clients on the secondary display; SDL positions Minecraft 26.3 there. Logs and screenshots are under `build/smoke-<version>-standalone`; the result manifest is `build/qa-1.8.1-matrix.json`. Run `scripts/qa_matrix.py` on this desktop to reproduce.

Atlas Client 3.0.3 is checked separately on Minecraft 1.21.11 with its own Fabric API and Player Animation Library dependencies. Atlas's custom ImGui and vanilla modes remain separate from EVATEX. Screenshots are inspected for actual visible video and control layout; a decoder reporting active is not sufficient evidence of rendering.

## Desktop platforms

Distributable jars contain Windows x64, Linux x64/ARM64 and macOS Intel/Apple Silicon engines. Compressed and extracted engine hashes are checked, and license/build information is included. Cached macOS/ARM binaries are checksum-verified before use; Linux permissions and incomplete extraction are repaired. Compatible system FFmpeg is a fallback if bundled execution fails.

Only Linux x64 runtime is directly exercised on this host. Packaging does not prove Windows, macOS or ARM runtime behavior. GitHub Actions includes native decoder/config regressions on Ubuntu, Windows and macOS in addition to compilation/decoder checks for all Minecraft versions; consult actual workflow results before claiming those jobs passed. Desktop folder-manager behavior and full game UI on Windows/macOS still require those systems.

## Scope

Support is limited to the 24 exact stable Java Edition/Fabric versions in `COMPATIBILITY.md`. Older releases, snapshots, future releases and other loaders are outside this matrix. Atlas support is restricted to 3.0.3 on 1.21.11. No guarantee is made for every other mod, hardware configuration or codec.

Meteor counts are bounded, animations can be disabled, startup runs outside the render thread, texture uploads occur only for new frames, and leaving supported menus closes playback. The mod adds menu behavior rather than world/server features. Only Turkish and English translations are bundled; other game languages fall back to English.
