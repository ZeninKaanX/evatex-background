# 1.8.2 — Clean menu shortcut

- Move Video Menu to the lower centre in Minecraft and Atlas Client 3.0.3.
- Use a smaller 100×14 native shortcut and a compact Atlas button, clear of the footer; extremely short native menus fall back to the top.
- Centre the native label using Minecraft font height.
- Give the Atlas shortcut explicit local padding, alignment and rounding; restore every style override after drawing.
- Scale shortcut placement with the game window and GUI scale, independently of launcher installation paths.

# 1.8.1 — Playback and desktop compatibility

- Own Astral volume slider with live 0–100% software gain, portable across audio devices; global system/game volume remains independent.
- Live sound on/off control with default mute, using the same FFmpeg input as video.
- Audio-device errors no longer prevent video playback.
- Safe 1080p default output, adjustable decode-height limits and memory-bounded frame storage.
- Limit FFmpeg decode/filter/output threads to reduce resource pressure on large inputs.
- Paginate playback settings and use tabs on narrow screens to prevent overlapping controls.
- Open video folders asynchronously using Windows, Linux and macOS commands; clear inherited native-library overrides for desktop helpers.
- Bundle pinned, checksum-verified FFmpeg engines for macOS Intel/Apple Silicon and Linux ARM64 alongside Windows/Linux x64.
- Repair cached executable permissions and incomplete extracted engines; fall back to system FFmpeg when execution fails.
- Add 20 requested motivational, creator and anime splash texts to Minecraft’s existing random pool; preserve vanilla/resource-pack messages and holiday behavior.
- Retain all 24 exact-version Fabric builds and separate Atlas Client 3.0.3 integration.

# 1.8.0 — Astral

- An exclusive black-and-white Astral interface, with optional screen and hover transitions and a bounded meteor animation.
- Continuous video on multiplayer server selection as well as the title screen, including removal of the legacy opaque server-list background while video is active.
- Optional Atlas Client 3.0.3 integration for both custom ImGui and vanilla menus, without modifying or bundling Atlas.
- Astral interface with dark panels, black/white accents, keyboard focus and live preview.
- Minecraft language integration: Turkish and English, with English fallback for other languages.
- Responsive video library, refresh, playback status and an on/off switch.
- Full resolution by default for new configurations; existing quality settings are preserved.
- Lanczos scaling, fill/fit framing and a bounded 4K decode size.
- FPS conversion now preserves playback speed; changing darkening, framing or vignette does not restart the decoder.
- Immutable frame handoff and session checks prevent torn frames and stale frames after changing videos.
- Asynchronous video probing, timeout and process cleanup keep the menu responsive.
- Validate corrupted config values and save configuration atomically.
- Both `videos` and existing `videolar` folders are scanned; invalid paths and directories disguised as videos are ignored.
- 24 separate Fabric builds cover every stable Java Edition release from 1.20 through 26.3, each with exact-version metadata.
- Version-specific GUI, texture, panorama and resource-loading adapters, including native named APIs and Java 25 for Minecraft 26.x.
- Fix Turkish/English resource loading on 1.21.9 and 1.21.10 by including both required loader modules.
- Automated compatibility build and decoder regression matrix for all supported releases.
