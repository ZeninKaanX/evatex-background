# EVATEX 1.8.11 validation

All 24 exact-version builds were compiled and their metadata, Java bytecode targets, embedded libraries, bundled engines, archive CRC and SHA-256 sidecars checked. Decoder/audio regression tests include real MP4/GIF playback, native PNG/JPEG/BMP decoding, error classification, bounded buffers, focus behavior, custom-widget bounds and menu ownership.

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
