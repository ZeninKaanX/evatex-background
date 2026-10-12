# EVATEX 1.8.9 validation

Status: release candidate pending the requested visual review. No 1.8.9 Modrinth publication has been performed.

Minecraft options use a centred, bounded two-column layout with Done in a separate footer. EVATEX settings have direct Playback, Appearance and Menus tabs; small windows retain paged categories. Existing settings and automatic saving remain available.

The background library no longer adds a full-screen darkening overlay. Its translucent panels still provide text contrast, and user-selected wallpaper dimming/vignette remain respected. Astral primary menus avoid additional full-screen dimming while video is playing.

Linear filtering affects EVATEX font/icon/meteor atlases only. Minecraft 1.21.11 and 26.x require an explicit glyph sampler override; earlier renderers use the texture filter. Other clients' UI and source-video detail are unchanged.

Minecraft 1.21.11 with Mod Menu and Sodium: native menu geometry/actions, dialog return paths, direct settings tabs, opt-out, small-window fallback and late foreign-control recovery passed. Mod Menu's Mods screen continued wallpaper playback. Minecraft 1.20.1 passed the corresponding native-menu test. The 26.1 shortcut/startup smoke passed after narrowing named text hooks to exact overload signatures; the local 26.1 asset index was unavailable, so this is not a full vanilla-asset visual verification.

Captures are actual 1920 × 1080 game framebuffers from a hidden OpenGL window positioned on the secondary eDP-1 display. The locked desktop prevents mapped OS-fullscreen captures. Tests use the supplied 1080p60 video, audio muted at 10%, and zero dimming without vignette. No 4K/8K stress or physical Windows GPU test was performed.

Decoder, image/GIF, audio, responsive shortcut layout, cooperative menu ownership and English/Turkish translation parity regressions passed. Final package verification is recorded in the release-candidate folder.
