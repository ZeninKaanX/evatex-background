# EVATEX 1.8.11 validation

All 24 exact-version builds were compiled and their metadata, Java bytecode targets, embedded libraries, bundled engines, archive CRC and SHA-256 sidecars checked. Decoder/audio regression tests include real MP4/GIF playback, native PNG/JPEG/BMP decoding, error classification, bounded buffers, focus behavior, custom-widget bounds and menu ownership.

Actual Linux Minecraft 1.21.11 with Fabric Loader 0.18.4 completed native home actions, settings return, compact layouts, foreign-menu ownership and focus pause/resume using the supplied Lost Soul 1080p60 video. Loader 0.19.3's original bundled Mixin Extras 0.5.4 reproduced a startup failure; the corrected 0.5.5 library allowed the real GUI to run.

Minecraft 26.1.2 native Options was opened successfully in a clean profile. A rendering-mod combination based on the supplied Windows log is tested separately. Runtime results and limits are recorded in docs/client-playback-1.8.11-validation.md.

The desktop is locked. GUI tests render a real hidden window assigned to the secondary display; captures are game framebuffers, not screenshots of a mapped desktop window. Sound is muted at 10% volume. No 4K/8K video playback stress was performed.

The supplied Windows game log proves bundled FFmpeg execution was denied, followed by missing system FFmpeg. No fatal settings-crash stack appears in the supplied logs. No claim of real Windows or Lunar runtime testing is made for this Linux test session.
